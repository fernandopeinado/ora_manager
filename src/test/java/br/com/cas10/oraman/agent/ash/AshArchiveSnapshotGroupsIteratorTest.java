package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.AshArchiveTestUtils.ARCHIVE_PATH;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.com.cas10.oraman.agent.ash.AshArchive.SnapshotGroupsIterator;
import br.com.cas10.oraman.oracle.data.ActiveSession;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AshArchiveSnapshotGroupsIteratorTest {

  private static final LocalDate DATE = LocalDate.of(2018, 9, 7);
  private static final Path DATA_FILE_PATH = ARCHIVE_PATH.resolve("2018-09-07-15");
  private static final int SNAPSHOT_SAMPLES = 10;

  private static final String WC_1 = "wait class 1";
  private static final String WC_2 = "wait class 2";

  @BeforeClass
  public static void setUp() throws IOException {
    AshArchiveTestUtils.createArchiveDir();

    List<AshSnapshot> snapshots = new ArrayList<>();

    snapshots.add(newSnapshot(LocalTime.of(15, 9, 45), WC_1));

    snapshots.add(newSnapshot(LocalTime.of(15, 10, 15), WC_1));
    snapshots.add(newSnapshot(LocalTime.of(15, 10, 45), WC_1, WC_2));

    snapshots.add(newSnapshot(LocalTime.of(15, 11, 15), WC_1, WC_2));
    snapshots.add(newSnapshot(LocalTime.of(15, 11, 30), WC_2));
    snapshots.add(newSnapshot(LocalTime.of(15, 11, 45), WC_2));

    snapshots.add(newSnapshot(LocalTime.of(15, 13, 15), WC_1, WC_2));

    snapshots.add(newSnapshot(LocalTime.of(15, 15, 15), WC_2));

    checkState(Files.notExists(DATA_FILE_PATH));

    try (OutputStream fos = Files.newOutputStream(DATA_FILE_PATH);
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
      oos.writeInt(snapshots.size());
      for (AshSnapshot snapshot : snapshots) {
        oos.writeObject(snapshot);
      }
    }
  }

  @AfterClass
  public static void tearDown() throws IOException {
    Files.deleteIfExists(DATA_FILE_PATH);
    AshArchiveTestUtils.deleteArchiveDir();
  }

  @Test
  public void test() throws IOException {
    long start = toTimestamp(LocalTime.of(15, 10));
    long end = toTimestamp(LocalTime.of(15, 15));
    long groupInterval = MINUTES.toMillis(1);

    try (SnapshotGroupsIterator iterator =
        new SnapshotGroupsIterator(ARCHIVE_PATH, start, end, groupInterval)) {

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot1 = iterator.next();
      verifySnapshot(snapshot1, LocalTime.of(15, 10, 45), 2, 2, 1);

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot2 = iterator.next();
      verifySnapshot(snapshot2, LocalTime.of(15, 11, 45), 3, 1, 3);

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot3 = iterator.next();
      verifySnapshot(snapshot3, LocalTime.of(15, 13, 0), 0, 0, 0);

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot4 = iterator.next();
      verifySnapshot(snapshot4, LocalTime.of(15, 13, 15), 1, 1, 1);

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot5 = iterator.next();
      verifySnapshot(snapshot5, LocalTime.of(15, 15, 0), 0, 0, 0);

      assertFalse(iterator.hasNext());
    }
  }

  @Test
  public void testGroupsBeyondIteratorEnd() throws IOException {
    long start = toTimestamp(LocalTime.of(15, 15));
    long end = toTimestamp(LocalTime.of(15, 17));
    long groupInterval = MINUTES.toMillis(1);

    try (SnapshotGroupsIterator iterator =
        new SnapshotGroupsIterator(ARCHIVE_PATH, start, end, groupInterval)) {

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot1 = iterator.next();
      verifySnapshot(snapshot1, LocalTime.of(15, 15, 15), 1, 0, 1);

      assertTrue(iterator.hasNext());
      AshSnapshot snapshot2 = iterator.next();
      verifySnapshot(snapshot2, LocalTime.of(15, 17, 0), 0, 0, 0);

      assertFalse(iterator.hasNext());
    }
  }

  @Test
  public void testStartEqualsEnd() throws IOException {
    long start = toTimestamp(LocalTime.of(15, 11, 15));
    long end = start;
    long groupInterval = MINUTES.toMillis(1);

    try (SnapshotGroupsIterator iterator =
        new SnapshotGroupsIterator(ARCHIVE_PATH, start, end, groupInterval)) {
      assertFalse(iterator.hasNext());
    }
  }

  @Test
  public void testStartGreaterThanEnd() throws IOException {
    long start = toTimestamp(LocalTime.of(15, 11, 15));
    long end = toTimestamp(LocalTime.of(15, 10, 15));
    long groupInterval = MINUTES.toMillis(1);

    try (SnapshotGroupsIterator iterator =
        new SnapshotGroupsIterator(ARCHIVE_PATH, start, end, groupInterval)) {
      assertFalse(iterator.hasNext());
    }
  }

  static AshSnapshot newSnapshot(LocalTime time, String... waitClasses) {
    List<ActiveSession> activeSessions = stream(waitClasses).map(wc -> {
      ActiveSession as = new ActiveSession();
      as.waitClass = wc;
      return as;
    }).collect(toList());
    return new AshSnapshot(toTimestamp(time), activeSessions, SNAPSHOT_SAMPLES);
  }

  static long toTimestamp(LocalTime time) {
    return LocalDateTime.of(DATE, time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  static void verifySnapshot(AshSnapshot snapshot, LocalTime expectedTime,
      int expectedSnapshotCount, int expectedWC1, int expectedWC2) {
    assertEquals(toTimestamp(expectedTime), snapshot.timestamp);
    assertEquals(expectedSnapshotCount * SNAPSHOT_SAMPLES, snapshot.samples);

    Multiset<String> waitClassActivity = HashMultiset.create();
    snapshot.activeSessions.stream().map(as -> as.waitClass).forEach(waitClassActivity::add);

    assertEquals(expectedWC1, waitClassActivity.count(WC_1));
    assertEquals(expectedWC2, waitClassActivity.count(WC_2));
  }
}
