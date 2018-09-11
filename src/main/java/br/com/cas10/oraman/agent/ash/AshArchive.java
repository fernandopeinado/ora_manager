package br.com.cas10.oraman.agent.ash;

import br.com.cas10.oraman.OramanProperties;
import br.com.cas10.oraman.oracle.data.ActiveSession;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Closer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
class AshArchive {

  private static final Logger logger = LoggerFactory.getLogger(AshArchive.class);

  private static final DateTimeFormatter FILENAME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
  private static final Pattern FILENAME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d{2}");
  private static final int SNAPSHOTS_BATCH_SIZE = 100;

  @Autowired
  private TaskScheduler scheduler;

  @VisibleForTesting
  Clock clock = Clock.systemDefaultZone();

  private final Path archivePath;
  private final int archiveMaxDays;

  @Autowired
  AshArchive(OramanProperties properties) {
    this.archivePath = Paths.get(properties.getArchive().getDir());
    this.archiveMaxDays = properties.getArchive().getMaxDays();
  }

  @PostConstruct
  private void init() throws IOException {
    Files.createDirectories(archivePath);
    scheduler.schedule(this::cleanUpArchive, new CronTrigger("0 30 * * * *"));
  }

  private final List<AshSnapshot> buffer = new ArrayList<>();
  private String currentFile = null;

  synchronized void archiveSnapshot(AshSnapshot snapshot) {
    ZonedDateTime snapshotDateTime =
        Instant.ofEpochMilli(snapshot.timestamp).atZone(ZoneId.systemDefault());
    String snapshotFile = FILENAME_FORMATTER.format(snapshotDateTime);
    if (!snapshotFile.equals(currentFile)) {
      if (currentFile != null) {
        try {
          flushBuffer();
        } catch (IOException e) {
          logger.error("Error while archiving snapshots", e);
        }
        buffer.clear();
      }
      currentFile = snapshotFile;
    }
    buffer.add(snapshot);
  }

  private void flushBuffer() throws IOException {
    Files.createDirectories(archivePath);
    Path outPath = archivePath.resolve(currentFile);
    try (OutputStream fos = Files.newOutputStream(outPath);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeInt(buffer.size());
      for (int i = 0; i < buffer.size(); i++) {
        if (i % SNAPSHOTS_BATCH_SIZE == 0) {
          oos.reset();
        }
        oos.writeObject(buffer.get(i));
      }
    }
  }

  ArchivedSnapshotsIterator getArchivedSnapshots(long start, long end, long groupInterval) {
    return new SnapshotGroupsIterator(archivePath, start, end, groupInterval);
  }

  @VisibleForTesting
  void cleanUpArchive() {
    String firstAllowedName = clock.instant().atZone(ZoneId.systemDefault()).toLocalDate()
        .minusDays(archiveMaxDays).atStartOfDay().format(FILENAME_FORMATTER);
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(archivePath)) {
      for (Path path : stream) {
        String fileName = path.getFileName().toString();
        if (!Files.isRegularFile(path) || !FILENAME_PATTERN.matcher(fileName).matches()) {
          continue;
        }
        if (fileName.compareTo(firstAllowedName) < 0) {
          logger.info(
              String.format("Removing file: %s", path.normalize().toAbsolutePath().toString()));
          Files.deleteIfExists(path);
        }
      }
    } catch (IOException e) {
      logger.error("Error while cleaning up the archive", e);
    }
  }

  interface ArchivedSnapshotsIterator extends Closeable, Iterator<AshSnapshot> {
  }

  static class SnapshotGroupsIterator extends UnmodifiableIterator<AshSnapshot>
      implements ArchivedSnapshotsIterator {

    private final long end;
    private final long groupInterval;
    private final FileRangeSnapshotsIterator iterator;

    private AshSnapshot current;
    private long groupStart;
    private long groupEnd;
    private List<AshSnapshot> groupMembers = new ArrayList<>();

    SnapshotGroupsIterator(Path archivePath, long start, long end, long groupInterval) {
      this.end = end;
      this.groupInterval = groupInterval;
      this.iterator = new FileRangeSnapshotsIterator(archivePath, toLocalDateTime(start),
          toLocalDateTime(end).plusHours(1));
      this.groupStart = start;
      this.groupEnd = Math.min(end, start + groupInterval);
    }

    private static LocalDateTime toLocalDateTime(long timeMillis) {
      return Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public boolean hasNext() {
      return groupStart < groupEnd;
    }

    @Override
    public AshSnapshot next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      while (current != null || iterator.hasNext()) {
        if (current == null) {
          current = iterator.next();
        }
        if (current.timestamp < groupStart) {
          current = null;
        } else {
          if (current.timestamp < groupEnd) {
            groupMembers.add(current);
            current = null;
          } else {
            return buildGroup();
          }
        }
      }
      return buildGroup();
    }

    private AshSnapshot buildGroup() {
      AshSnapshot group;
      switch (groupMembers.size()) {
        case 0:
          group = new AshSnapshot(groupEnd, ImmutableList.of(), 0);
          break;
        case 1:
          group = groupMembers.get(0);
          break;
        default:
          long timestamp = groupMembers.get(groupMembers.size() - 1).timestamp;
          ImmutableList.Builder<ActiveSession> activeSessions = ImmutableList.builder();
          int samples = 0;
          for (AshSnapshot snapshot : groupMembers) {
            activeSessions.addAll(snapshot.activeSessions);
            samples += snapshot.samples;
          }
          group = new AshSnapshot(timestamp, activeSessions.build(), samples);
      }
      groupStart = groupEnd;
      groupEnd = Math.min(end, groupStart + groupInterval);
      groupMembers.clear();
      return group;
    }

    @Override
    public void close() throws IOException {
      this.iterator.close();
    }
  }

  @VisibleForTesting
  static class FileRangeSnapshotsIterator extends UnmodifiableIterator<AshSnapshot>
      implements ArchivedSnapshotsIterator {

    private final Path archivePath;
    private final LocalDateTime end;
    private ArchivedSnapshotsIterator iterator;
    private LocalDateTime next;

    FileRangeSnapshotsIterator(Path archivePath, LocalDateTime start, LocalDateTime end) {
      this.archivePath = archivePath;
      this.end = LocalDateTime.of(end.toLocalDate(), LocalTime.of(end.getHour(), 0));

      start = LocalDateTime.of(start.toLocalDate(), LocalTime.of(start.getHour(), 0));
      this.iterator = start.isBefore(end) ? newIterator(start) : EmptyIterator.INSTANCE;
      this.next = start.plusHours(1);
    }

    @Override
    public boolean hasNext() {
      while (!iterator.hasNext() && next.isBefore(end)) {
        try {
          this.iterator.close();
        } catch (IOException e) {
          throw new RuntimeException();
        }
        this.iterator = newIterator(next);
        next = next.plusHours(1);
      }
      return iterator.hasNext();
    }

    @Override
    public AshSnapshot next() {
      return iterator.next();
    }

    @Override
    public void close() throws IOException {
      iterator.close();
    }

    private ArchivedSnapshotsIterator newIterator(LocalDateTime dateTime) {
      return new FileSnapshotsIterator(archivePath.resolve(FILENAME_FORMATTER.format(dateTime)));
    }
  }

  @VisibleForTesting
  static class FileSnapshotsIterator extends UnmodifiableIterator<AshSnapshot>
      implements ArchivedSnapshotsIterator {

    private final Path path;
    private final Closer closer = Closer.create();
    private ObjectInputStream ois;
    private int counter;

    FileSnapshotsIterator(Path path) {
      this.path = path;
    }

    @Override
    public boolean hasNext() {
      if (ois == null) {
        if (!Files.exists(path)) {
          return false;
        }
        InputStream fis;
        try {
          fis = closer.register(Files.newInputStream(path));
        } catch (NoSuchFileException e) {
          return false;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
        try {
          ois = closer.register(new ObjectInputStream(bis));
          counter = ois.readInt();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return counter > 0;
    }

    @Override
    public AshSnapshot next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      counter--;
      try {
        return (AshSnapshot) ois.readObject();
      } catch (ClassNotFoundException | IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void close() throws IOException {
      closer.close();
    }
  }

  private static class EmptyIterator extends UnmodifiableIterator<AshSnapshot>
      implements ArchivedSnapshotsIterator {

    private static final EmptyIterator INSTANCE = new EmptyIterator();

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public AshSnapshot next() {
      throw new NoSuchElementException();
    }

    @Override
    public void close() throws IOException {}
  }
}
