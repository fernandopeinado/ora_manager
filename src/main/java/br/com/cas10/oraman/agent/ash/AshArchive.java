package br.com.cas10.oraman.agent.ash;

import static java.util.concurrent.TimeUnit.DAYS;

import br.com.cas10.oraman.OramanProperties;
import com.google.common.annotations.VisibleForTesting;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(AshArchive.class);

  private static final long ARCHIVE_SIZE_HOURS = DAYS.toHours(7);
  private static final DateTimeFormatter FILENAME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
  private static final Pattern FILENAME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d{2}");
  private static final int SNAPSHOTS_BATCH_SIZE = 100;

  @Autowired
  private TaskScheduler scheduler;

  private final Path archivePath;

  @Autowired
  AshArchive(OramanProperties properties) {
    this.archivePath = Paths.get(properties.getArchive().getDir());
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
          LOGGER.error("Error while archiving snapshots", e);
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

  ArchivedSnapshotsIterator getArchivedSnapshots(int year, int month, int dayOfMonth, int hour) {
    String fileName = FILENAME_FORMATTER.format(LocalDateTime.of(year, month, dayOfMonth, hour, 0));
    Path filePath = archivePath.resolve(fileName);
    return Files.exists(filePath) ? new FileSnapshotsIterator(filePath) : EmptyIterator.INSTANCE;
  }

  @VisibleForTesting
  void cleanUpArchive() {
    ZonedDateTime base = ZonedDateTime.now().minusHours(ARCHIVE_SIZE_HOURS);
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(archivePath)) {
      for (Path path : stream) {
        String fileName = path.getFileName().toString();
        if (!Files.isRegularFile(path) || !FILENAME_PATTERN.matcher(fileName).matches()) {
          continue;
        }
        ZonedDateTime fileDateTime =
            LocalDateTime.parse(fileName, FILENAME_FORMATTER).atZone(ZoneId.systemDefault());
        if (fileDateTime.isBefore(base)) {
          LOGGER.info(
              String.format("Removing file: %s", path.normalize().toAbsolutePath().toString()));
          Files.deleteIfExists(path);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error while cleaning up the archive", e);
    }
  }

  interface ArchivedSnapshotsIterator extends Closeable, Iterator<AshSnapshot> {
  }

  private static class FileSnapshotsIterator extends UnmodifiableIterator<AshSnapshot>
      implements ArchivedSnapshotsIterator {

    private final Path path;
    private final Closer closer = Closer.create();
    private ObjectInputStream ois;
    private int counter;

    private FileSnapshotsIterator(Path path) {
      this.path = path;
    }

    @Override
    public boolean hasNext() {
      if (ois == null) {
        try {
          InputStream fis = closer.register(Files.newInputStream(path));
          BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
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
