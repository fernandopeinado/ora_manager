package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.util.Util.verifySnapshot;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.summingInt;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import br.com.cas10.oraman.agent.ash.util.SessionActivityVerifier;
import br.com.cas10.oraman.agent.ash.util.SqlActivityVerifier;
import br.com.cas10.oraman.oracle.Cursors;
import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.util.Snapshot;

public class AshTest {

  private static final String SQL_ID_1 = "sql id 1";
  private static final String SQL_ID_2 = "sql id 2";

  private static final String[] SESSION_1 = {"1", "100"};
  private static final String[] SESSION_2 = {"2", "200"};
  private static final String[] SESSION_3 = {"3", "300"};

  private static final String WAIT_CLASS_1 = "wait class 1";
  private static final String WAIT_CLASS_2 = "wait class 2";

  private static final String[] EVENT_1 = {"event 1", WAIT_CLASS_1};
  private static final String[] EVENT_2 = {"event 2", WAIT_CLASS_2};
  private static final String[] EVENT_3 = {"event 3", WAIT_CLASS_2};

  @Test
  public void testGetActivityFilterSqlId() {
    Predicate<ActiveSession> filter = s -> SQL_ID_1.equals(s.sqlId);

    final int samples = 5;

    List<ActiveSession> s1Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_2, SQL_ID_2, EVENT_2))
        .add(newActiveSession(SESSION_3, (String) null, EVENT_3)).build();
    AshSnapshot s1 = new AshSnapshot(1, s1Sessions, samples);

    List<ActiveSession> s2Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_3))
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_3))
        .add(newActiveSession(SESSION_2, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_2, SQL_ID_1, EVENT_3)).build();
    AshSnapshot s2 = new AshSnapshot(2, s2Sessions, samples);

    AshSnapshot s3 = new AshSnapshot(3, ImmutableList.of(), samples);

    List<AshSnapshot> snapshots = ImmutableList.of(s1, s2, s3);
    final int totalSamples = snapshots.size() * samples;
    final int totalActivity = snapshots.stream().flatMap(s -> s.activeSessions.stream())
        .filter(filter).collect(counting()).intValue();

    AshAgent agent = mock(AshAgent.class);
    when(agent.getSnapshots()).thenReturn(snapshots);

    Ash ash = new Ash();
    setField(ash, "agent", agent);
    setField(ash, "cursors", mock(Cursors.class));

    IntervalActivity activity = ash.getActivity(filter);

    assertEquals(1, activity.intervalStart);
    assertEquals(3, activity.intervalEnd);

    List<Snapshot<Double>> eSnapshots = activity.eventsSnapshots;
    assertEquals(3, eSnapshots.size());
    verifySnapshot(eSnapshots.get(0), 1, samples, EVENT_1[0], 1);
    verifySnapshot(eSnapshots.get(1), 2, samples, EVENT_1[0], 1, EVENT_3[0], 3);
    verifySnapshot(eSnapshots.get(2), 3, samples);

    List<Snapshot<Double>> wcSnapshots = activity.waitClassesSnapshots;
    assertEquals(3, wcSnapshots.size());
    verifySnapshot(wcSnapshots.get(0), 1, samples, WAIT_CLASS_1, 1);
    verifySnapshot(wcSnapshots.get(1), 2, samples, WAIT_CLASS_1, 1, WAIT_CLASS_2, 3);
    verifySnapshot(wcSnapshots.get(2), 3, samples);

    SqlActivityVerifier sqlVerifier = new SqlActivityVerifier(totalActivity, totalSamples);
    assertEquals(1, activity.topSql.size());

    sqlVerifier.verify(activity.topSql.get(0)).sqlId(SQL_ID_1).activity(5)
        .events(EVENT_1[0], 2, EVENT_3[0], 3).waitClasses(WAIT_CLASS_1, 2, WAIT_CLASS_2, 3);

    SessionActivityVerifier sessionVerifier = new SessionActivityVerifier(totalActivity);
    assertEquals(2, activity.topSessions.size());

    sessionVerifier.verify(activity.topSessions.get(0)).session(SESSION_1).activity(3)
        .events(EVENT_1[0], 1, EVENT_3[0], 2).waitClasses(WAIT_CLASS_1, 1, WAIT_CLASS_2, 2);

    sessionVerifier.verify(activity.topSessions.get(1)).session(SESSION_2).activity(2)
        .events(EVENT_1[0], 1, EVENT_3[0], 1).waitClasses(WAIT_CLASS_1, 1, WAIT_CLASS_2, 1);
  }

  @Test
  public void testGetActivityFilterSession() {
    Predicate<ActiveSession> filter =
        s -> SESSION_1[0].equals(s.sid) && SESSION_1[1].equals(s.serialNumber);

    final int samples = 5;

    List<ActiveSession> s1Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_2, SQL_ID_2, EVENT_2)).build();
    AshSnapshot s1 = new AshSnapshot(1, s1Sessions, samples);

    List<ActiveSession> s2Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_3))
        .add(newActiveSession(SESSION_1, SQL_ID_2, EVENT_3)).build();
    AshSnapshot s2 = new AshSnapshot(2, s2Sessions, samples);

    AshSnapshot s3 = new AshSnapshot(3, ImmutableList.of(), samples);

    List<AshSnapshot> snapshots = ImmutableList.of(s1, s2, s3);
    final int totalSamples = snapshots.size() * samples;
    final int totalActivity = snapshots.stream().flatMap(s -> s.activeSessions.stream())
        .filter(filter).collect(counting()).intValue();

    AshAgent agent = mock(AshAgent.class);
    when(agent.getSnapshots()).thenReturn(snapshots);

    Ash ash = new Ash();
    setField(ash, "agent", agent);
    setField(ash, "cursors", mock(Cursors.class));

    IntervalActivity activity = ash.getActivity(filter);

    assertEquals(1, activity.intervalStart);
    assertEquals(3, activity.intervalEnd);

    List<Snapshot<Double>> eSnapshots = activity.eventsSnapshots;
    assertEquals(3, eSnapshots.size());
    verifySnapshot(eSnapshots.get(0), 1, samples, EVENT_1[0], 1);
    verifySnapshot(eSnapshots.get(1), 2, samples, EVENT_1[0], 1, EVENT_3[0], 2);
    verifySnapshot(eSnapshots.get(2), 3, samples);

    List<Snapshot<Double>> wcSnapshots = activity.waitClassesSnapshots;
    assertEquals(3, wcSnapshots.size());
    verifySnapshot(wcSnapshots.get(0), 1, samples, WAIT_CLASS_1, 1);
    verifySnapshot(wcSnapshots.get(1), 2, samples, WAIT_CLASS_1, 1, WAIT_CLASS_2, 2);
    verifySnapshot(wcSnapshots.get(2), 3, samples);

    SqlActivityVerifier sqlVerifier = new SqlActivityVerifier(totalActivity, totalSamples);
    assertEquals(2, activity.topSql.size());

    sqlVerifier.verify(activity.topSql.get(0)).sqlId(SQL_ID_1).activity(3)
        .events(EVENT_1[0], 2, EVENT_3[0], 1).waitClasses(WAIT_CLASS_1, 2, WAIT_CLASS_2, 1);

    sqlVerifier.verify(activity.topSql.get(1)).sqlId(SQL_ID_2).activity(1).events(EVENT_3[0], 1)
        .waitClasses(WAIT_CLASS_2, 1);

    SessionActivityVerifier sessionVerifier = new SessionActivityVerifier(totalActivity);
    assertEquals(1, activity.topSessions.size());

    sessionVerifier.verify(activity.topSessions.get(0)).session(SESSION_1).activity(4)
        .events(EVENT_1[0], 2, EVENT_3[0], 2).waitClasses(WAIT_CLASS_1, 2, WAIT_CLASS_2, 2);
  }

  @Test
  public void testGetIntervalActivity() {
    final int samples = 10;

    List<ActiveSession> s1Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_1)).build();
    AshSnapshot s1 = new AshSnapshot(1, s1Sessions, samples);

    List<ActiveSession> s2Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_1, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_2, SQL_ID_2, EVENT_1))
        .add(newActiveSession(SESSION_2, SQL_ID_2, EVENT_2)).build();
    AshSnapshot s2 = new AshSnapshot(2, s2Sessions, samples);

    List<ActiveSession> s3Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_2, SQL_ID_2, EVENT_2))
        .add(newActiveSession(SESSION_2, SQL_ID_2, EVENT_2))
        .add(newActiveSession(SESSION_2, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_2, SQL_ID_1, EVENT_1)).build();
    AshSnapshot s3 = new AshSnapshot(3, s3Sessions, samples);

    List<ActiveSession> s4Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_2, SQL_ID_1, EVENT_1))
        .add(newActiveSession(SESSION_3, SQL_ID_1, EVENT_2)).build();
    AshSnapshot s4 = new AshSnapshot(4, s4Sessions, samples);

    List<ActiveSession> s5Sessions = ImmutableList.<ActiveSession>builder()//
        .add(newActiveSession(SESSION_3, SQL_ID_1, EVENT_2)).build();
    AshSnapshot s5 = new AshSnapshot(5, s5Sessions, samples);

    List<AshSnapshot> selectedSnapshots = ImmutableList.of(s2, s3, s4);
    final int totalSamples = selectedSnapshots.size() * samples;
    final int totalActivity =
        selectedSnapshots.stream().collect(summingInt(s -> s.activeSessions.size()));

    AshAgent agent = mock(AshAgent.class);
    when(agent.getSnapshots()).thenReturn(ImmutableList.of(s1, s2, s3, s4, s5));

    Ash ash = new Ash();
    setField(ash, "agent", agent);
    setField(ash, "cursors", mock(Cursors.class));

    IntervalActivity activity = ash.getIntervalActivity(2, 4);

    assertEquals(2, activity.intervalStart);
    assertEquals(4, activity.intervalEnd);

    List<Snapshot<Double>> eSnapshots = activity.eventsSnapshots;
    assertEquals(3, eSnapshots.size());
    verifySnapshot(eSnapshots.get(0), 2, samples, EVENT_1[0], 3, EVENT_2[0], 1);
    verifySnapshot(eSnapshots.get(1), 3, samples, EVENT_1[0], 2, EVENT_2[0], 2);
    verifySnapshot(eSnapshots.get(2), 4, samples, EVENT_1[0], 1, EVENT_2[0], 1);

    List<Snapshot<Double>> wcSnapshots = activity.waitClassesSnapshots;
    assertEquals(3, wcSnapshots.size());
    verifySnapshot(wcSnapshots.get(0), 2, samples, WAIT_CLASS_1, 3, WAIT_CLASS_2, 1);
    verifySnapshot(wcSnapshots.get(1), 3, samples, WAIT_CLASS_1, 2, WAIT_CLASS_2, 2);
    verifySnapshot(wcSnapshots.get(2), 4, samples, WAIT_CLASS_1, 1, WAIT_CLASS_2, 1);

    SqlActivityVerifier sqlVerifier = new SqlActivityVerifier(totalActivity, totalSamples);
    assertEquals(2, activity.topSql.size());

    sqlVerifier.verify(activity.topSql.get(0)).sqlId(SQL_ID_1).activity(6)
        .events(EVENT_1[0], 5, EVENT_2[0], 1).waitClasses(WAIT_CLASS_1, 5, WAIT_CLASS_2, 1);

    sqlVerifier.verify(activity.topSql.get(1)).sqlId(SQL_ID_2).activity(4)
        .events(EVENT_1[0], 1, EVENT_2[0], 3).waitClasses(WAIT_CLASS_1, 1, WAIT_CLASS_2, 3);

    SessionActivityVerifier sessionVerifier = new SessionActivityVerifier(totalActivity);
    assertEquals(3, activity.topSessions.size());

    sessionVerifier.verify(activity.topSessions.get(0)).session(SESSION_2).activity(7)
        .events(EVENT_1[0], 4, EVENT_2[0], 3).waitClasses(WAIT_CLASS_1, 4, WAIT_CLASS_2, 3);

    sessionVerifier.verify(activity.topSessions.get(1)).session(SESSION_1).activity(2)
        .events(EVENT_1[0], 2).waitClasses(WAIT_CLASS_1, 2);

    sessionVerifier.verify(activity.topSessions.get(2)).session(SESSION_3).activity(1)
        .events(EVENT_2[0], 1).waitClasses(WAIT_CLASS_2, 1);
  }

  private static ActiveSession newActiveSession(String[] session, String sqlId, String[] event) {
    ActiveSession as = new ActiveSession();
    as.sid = session[0];
    as.serialNumber = session[1];
    as.sqlId = sqlId;
    as.event = event[0];
    as.waitClass = event[1];
    return as;
  }
}
