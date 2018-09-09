package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.AshArchiveTestUtils.ARCHIVE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.com.cas10.oraman.agent.ash.AshArchive.FileSnapshotsIterator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AshArchiveFileSnapshotsIteratorTest {

  @Before
  public void setUp() throws IOException {
    AshArchiveTestUtils.createArchiveDir();
  }

  @After
  public void tearDown() throws IOException {
    AshArchiveTestUtils.deleteArchiveDir();
  }

  @Test
  public void test() throws IOException {
    String dataFileName = "2014-10-05-10";
    Path dataFile = ARCHIVE_PATH.resolve(dataFileName);
    assertTrue(Files.notExists(dataFile));

    try {
      AshArchiveTestUtils.writeSnapshots(dataFileName, 0, 1, 2);

      try (FileSnapshotsIterator iterator = new FileSnapshotsIterator(dataFile)) {
        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.next().timestamp);
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next().timestamp);
        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.next().timestamp);
        assertFalse(iterator.hasNext());
      }
    } finally {
      Files.deleteIfExists(dataFile);
    }
  }

  @Test
  public void testFileDoesNotExist() throws IOException {
    Path dataFile = ARCHIVE_PATH.resolve("2014-10-05-20");
    assertTrue(Files.notExists(dataFile));

    try (FileSnapshotsIterator iterator = new FileSnapshotsIterator(dataFile)) {
      assertFalse(iterator.hasNext());
    }
  }
}
