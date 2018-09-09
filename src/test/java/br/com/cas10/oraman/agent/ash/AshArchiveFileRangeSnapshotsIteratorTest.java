package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.AshArchiveTestUtils.ARCHIVE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.com.cas10.oraman.agent.ash.AshArchive.FileRangeSnapshotsIterator;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AshArchiveFileRangeSnapshotsIteratorTest {

  private static final Map<String, long[]> DATA_FILES_TO_TIMESTAMPS;

  static {
    String[] dataFiles =
        {"2014-10-04-22", "2014-10-04-23", "2014-10-05-00", "2014-10-05-01", "2014-10-05-04"};

    long t = 0;
    Map<String, long[]> dataFilesToTimestamps = new HashMap<>();
    for (String dataFile : dataFiles) {
      dataFilesToTimestamps.put(dataFile, new long[] {t++, t++});
    }
    DATA_FILES_TO_TIMESTAMPS = dataFilesToTimestamps;
  }

  @BeforeClass
  public static void setUp() throws IOException {
    AshArchiveTestUtils.createArchiveDir();
    for (Map.Entry<String, long[]> e : DATA_FILES_TO_TIMESTAMPS.entrySet()) {
      AshArchiveTestUtils.writeSnapshots(e.getKey(), e.getValue());
    }
  }

  @AfterClass
  public static void tearDown() throws IOException {
    for (String dataFile : DATA_FILES_TO_TIMESTAMPS.keySet()) {
      Files.deleteIfExists(ARCHIVE_PATH.resolve(dataFile));
    }
    AshArchiveTestUtils.deleteArchiveDir();
  }

  @Test
  public void test() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 4, 22, 0);
    LocalDateTime end = LocalDateTime.of(2014, 10, 5, 1, 0);

    String[] expectedFiles = {"2014-10-04-22", "2014-10-04-23", "2014-10-05-00"};

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      verifySnapshots(iterator, expectedFiles);
    }
  }

  @Test
  public void testFirstFileDoesNotExist() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 4, 21, 0);
    LocalDateTime end = LocalDateTime.of(2014, 10, 4, 23, 0);

    String[] expectedFiles = {"2014-10-04-22"};

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      verifySnapshots(iterator, expectedFiles);
    }
  }

  @Test
  public void testLastFileDoesNotExist() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 5, 1, 0);
    LocalDateTime end = LocalDateTime.of(2014, 10, 5, 3, 0);

    String[] expectedFiles = {"2014-10-05-01"};

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      verifySnapshots(iterator, expectedFiles);
    }
  }

  @Test
  public void testFilesMissingInTheMiddle() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 5, 1, 0);
    LocalDateTime end = LocalDateTime.of(2014, 10, 5, 5, 0);

    String[] expectedFiles = {"2014-10-05-01", "2014-10-05-04"};

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      verifySnapshots(iterator, expectedFiles);
    }
  }

  @Test
  public void testStartEqualsEnd() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 5, 1, 0);
    LocalDateTime end = start;

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      assertFalse(iterator.hasNext());
    }
  }

  @Test
  public void testStartGreaterThanEnd() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 5, 1, 0);
    LocalDateTime end = LocalDateTime.of(2014, 10, 4, 23, 0);

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      assertFalse(iterator.hasNext());
    }
  }

  @Test
  public void testRangeWithoutFiles() throws IOException {
    LocalDateTime start = LocalDateTime.of(2014, 10, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2014, 10, 1, 13, 0);

    try (FileRangeSnapshotsIterator iterator =
        new FileRangeSnapshotsIterator(ARCHIVE_PATH, start, end)) {
      assertFalse(iterator.hasNext());
    }
  }

  static void verifySnapshots(FileRangeSnapshotsIterator iterator, String[] expectedFiles) {
    for (String expectedFile : expectedFiles) {
      for (long expectedTimestamp : DATA_FILES_TO_TIMESTAMPS.get(expectedFile)) {
        assertTrue(iterator.hasNext());
        assertEquals(expectedTimestamp, iterator.next().timestamp);
      }
    }
    assertFalse(iterator.hasNext());
  }
}
