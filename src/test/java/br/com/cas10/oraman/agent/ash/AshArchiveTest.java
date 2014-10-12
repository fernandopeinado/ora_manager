package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.AshArchive.ARCHIVE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;

import org.junit.After;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class AshArchiveTest {

  @After
  public void tearDown() throws IOException {
    Files.deleteIfExists(ARCHIVE_PATH);
  }

  @Test
  public void testArchiveSnapshot() throws ClassNotFoundException, IOException {
    Path expectedFile = ARCHIVE_PATH.resolve("2014-10-05-10");
    assertFalse(Files.exists(expectedFile));

    try {
      AshArchive archive = new AshArchive();

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
  public void testGetArchivedSnapshots() throws IOException {
    Path dataFile = ARCHIVE_PATH.resolve("2014-10-05-20");
    assertFalse(Files.exists(dataFile));

    try {
      Files.createDirectories(ARCHIVE_PATH);
      try (OutputStream fos = Files.newOutputStream(dataFile);
          ObjectOutputStream oos = new ObjectOutputStream(fos)) {
        oos.writeInt(3);
        oos.writeObject(newSnapshot(0));
        oos.writeObject(newSnapshot(1));
        oos.writeObject(newSnapshot(2));
      }

      AshArchive archive = new AshArchive();

      Iterator<AshSnapshot> iterator = archive.getArchivedSnapshots(2014, 10, 5, 20);
      assertTrue(iterator.hasNext());
      assertEquals(0, iterator.next().timestamp);
      assertTrue(iterator.hasNext());
      assertEquals(1, iterator.next().timestamp);
      assertTrue(iterator.hasNext());
      assertEquals(2, iterator.next().timestamp);
      assertFalse(iterator.hasNext());
    } finally {
      Files.deleteIfExists(dataFile);
    }
  }

  @Test
  public void testCleanUpArchive() throws IOException {
    Path file1 = ARCHIVE_PATH.resolve("2000-01-01-00");
    Path file2 = ARCHIVE_PATH.resolve("2100-01-01-00");

    assertFalse(Files.exists(file1));
    assertFalse(Files.exists(file2));

    try {
      Files.createDirectories(ARCHIVE_PATH);
      Files.createFile(file1);
      Files.createFile(file2);

      AshArchive archive = new AshArchive();
      archive.cleanUpArchive();

      assertFalse(Files.exists(file1));
      assertTrue(Files.exists(file2));
    } finally {
      Files.deleteIfExists(file1);
      Files.deleteIfExists(file2);
    }
  }

  private static AshSnapshot newSnapshot(long timestamp) {
    return new AshSnapshot(timestamp, ImmutableList.of(), 10);
  }

  private static long toTimestamp(int year, int month, int dayOfMonth, int hour) {
    return LocalDateTime.of(year, month, dayOfMonth, hour, 0).atZone(ZoneId.systemDefault())
        .toInstant().toEpochMilli();
  }
}
