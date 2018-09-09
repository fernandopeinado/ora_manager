package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.AshArchiveTestUtils.newOramanProperties;
import static br.com.cas10.oraman.agent.ash.AshArchiveTestUtils.newSnapshot;
import static com.google.common.base.StandardSystemProperty.JAVA_IO_TMPDIR;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.cas10.oraman.OramanProperties;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.After;
import org.junit.Test;

public class AshArchiveTest {

  private static final Path ARCHIVE_PATH = Paths.get(JAVA_IO_TMPDIR.value(), "oraman");

  @After
  public void tearDown() throws IOException {
    AshArchiveTestUtils.deleteArchiveDir();
  }

  @Test
  public void testArchiveSnapshot() throws ClassNotFoundException, IOException {
    Path expectedFile = ARCHIVE_PATH.resolve("2014-10-05-10");
    assertFalse(Files.exists(expectedFile));

    try {
      AshArchive archive = new AshArchive(newOramanProperties());

      final long timestamp1 = toTimestamp(2014, 10, 5, 10);
      final long timestamp2 = toTimestamp(2014, 10, 5, 11);

      archive.archiveSnapshot(newSnapshot(timestamp1));
      archive.archiveSnapshot(newSnapshot(timestamp1));
      archive.archiveSnapshot(newSnapshot(timestamp2));

      assertFalse(Files.exists(ARCHIVE_PATH.resolve("2014-10-05-11")));

      try (InputStream fis = Files.newInputStream(expectedFile);
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        assertEquals(2, ois.readInt());
        assertNotNull(ois.readObject());
        assertNotNull(ois.readObject());
      }
    } finally {
      Files.deleteIfExists(expectedFile);
    }
  }

  @Test
  public void testCleanUpArchive() throws IOException {
    int archiveMaxDays = 5;

    Instant now =
        LocalDateTime.parse("2018-03-10T15:30:00").atZone(ZoneId.systemDefault()).toInstant();
    Clock clock = mock(Clock.class);
    when(clock.instant()).thenReturn(now);

    List<Path> shouldRemove = asList("2000-01-01-00", "2018-03-04-23").stream()
        .map(ARCHIVE_PATH::resolve).collect(toList());
    List<Path> shouldNotRemove = asList("2018-03-05-00", "2100-01-01-00").stream()
        .map(ARCHIVE_PATH::resolve).collect(toList());

    for (Path path : Iterables.concat(shouldRemove, shouldNotRemove)) {
      assertTrue(Files.notExists(path));
    }

    try {
      AshArchiveTestUtils.createArchiveDir();
      for (Path path : Iterables.concat(shouldRemove, shouldNotRemove)) {
        Files.createFile(path);
      }

      OramanProperties properties = newOramanProperties();
      properties.getArchive().setMaxDays(archiveMaxDays);
      AshArchive archive = new AshArchive(properties);
      archive.clock = clock;

      archive.cleanUpArchive();

      for (Path path : shouldRemove) {
        assertTrue(Files.notExists(path));
      }
      for (Path path : shouldNotRemove) {
        assertTrue(Files.exists(path));
      }
    } finally {
      for (Path path : Iterables.concat(shouldRemove, shouldNotRemove)) {
        Files.deleteIfExists(path);
      }
    }
  }

  private static long toTimestamp(int year, int month, int dayOfMonth, int hour) {
    return LocalDateTime.of(year, month, dayOfMonth, hour, 0).atZone(ZoneId.systemDefault())
        .toInstant().toEpochMilli();
  }
}
