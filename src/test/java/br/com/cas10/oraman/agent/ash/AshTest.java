package br.com.cas10.oraman.agent.ash;

import static java.util.stream.Collectors.summingInt;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import br.com.cas10.oraman.oracle.Cursors;
import br.com.cas10.oraman.oracle.data.ActiveSession;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;

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
  public void testGetIntervalActivity() {
    final int samples = 10;

    List<ActiveSession> s1Sessions = new ArrayList<>();
    s1Sessions.add(newActiveSession(SESSION_1, SQL_ID_1, WAIT_CLASS_1));
    AshSnapshot s1 = newSnapshot(1, samples, s1Sessions);

    List<ActiveSession> s2Sessions = new ArrayList<>();
    s2Sessions.add(newActiveSession(SESSION_1, SQL_ID_1, WAIT_CLASS_1));
    s2Sessions.add(newActiveSession(SESSION_1, SQL_ID_1, WAIT_CLASS_1));
    s2Sessions.add(newActiveSession(SESSION_2, SQL_ID_2, WAIT_CLASS_1));
    s2Sessions.add(newActiveSession(SESSION_2, SQL_ID_2, WAIT_CLASS_2));
    AshSnapshot s2 = newSnapshot(2, samples, s2Sessions);

    List<ActiveSession> s3Sessions = new ArrayList<>();
    s3Sessions.add(newActiveSession(SESSION_2, SQL_ID_2, WAIT_CLASS_2));
    s3Sessions.add(newActiveSession(SESSION_2, SQL_ID_2, WAIT_CLASS_2));
    s3Sessions.add(newActiveSession(SESSION_2, SQL_ID_1, WAIT_CLASS_1));
    s3Sessions.add(newActiveSession(SESSION_2, SQL_ID_1, WAIT_CLASS_1));
    AshSnapshot s3 = newSnapshot(3, samples, s3Sessions);

    List<ActiveSession> s4Sessions = new ArrayList<>();
    s4Sessions.add(newActiveSession(SESSION_2, SQL_ID_1, WAIT_CLASS_1));
    s4Sessions.add(newActiveSession(SESSION_3, SQL_ID_1, WAIT_CLASS_2));
    AshSnapshot s4 = newSnapshot(4, samples, s4Sessions);

    List<ActiveSession> s5Sessions = new ArrayList<>();
    s5Sessions.add(newActiveSession(SESSION_3, SQL_ID_1, WAIT_CLASS_2));
    AshSnapshot s5 = newSnapshot(5, samples, s5Sessions);

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
    assertEquals(3, activity.waitClassesSnapshots.size());

    List<SqlActivity> topSql = activity.topSql;

    assertEquals(2, topSql.size());
    verifySqlActivity(SQL_ID_1, 6, ImmutableMap.of(WAIT_CLASS_1, 5, WAIT_CLASS_2, 1),
        totalActivity, totalSamples, topSql.get(0));
    verifySqlActivity(SQL_ID_2, 4, ImmutableMap.of(WAIT_CLASS_1, 1, WAIT_CLASS_2, 3),
        totalActivity, totalSamples, topSql.get(1));

    List<SessionActivity> topSessions = activity.topSessions;

    assertEquals(3, topSessions.size());
    verifySessionActivity(SESSION_2, 7, ImmutableMap.of(WAIT_CLASS_1, 4, WAIT_CLASS_2, 3),
        totalActivity, topSessions.get(0));
    verifySessionActivity(SESSION_1, 2, ImmutableMap.of(WAIT_CLASS_1, 2), totalActivity,
        topSessions.get(1));
    verifySessionActivity(SESSION_3, 1, ImmutableMap.of(WAIT_CLASS_2, 1), totalActivity,
        topSessions.get(2));
  }

  @Test
  public void testGetSqlWaitEvents() {
    final int samples = 5;

    List<ActiveSession> s1Sessions = new ArrayList<>();
    s1Sessions.add(newActiveSession(SQL_ID_1, EVENT_1));
    s1Sessions.add(newActiveSession(SQL_ID_2, EVENT_2));
    AshSnapshot s1 = newSnapshot(1, samples, s1Sessions);

    List<ActiveSession> s2Sessions = new ArrayList<>();
    s2Sessions.add(newActiveSession(SQL_ID_1, EVENT_1));
    s2Sessions.add(newActiveSession(SQL_ID_1, EVENT_3));
    s2Sessions.add(newActiveSession(null, EVENT_3));
    AshSnapshot s2 = newSnapshot(2, samples, s2Sessions);

    AshAgent agent = mock(AshAgent.class);
    when(agent.getSnapshots()).thenReturn(ImmutableList.of(s1, s2));

    Ash ash = new Ash();
    setField(ash, "agent", agent);

    List<WaitEventActivity> waitEvents = ash.getSqlWaitEvents(SQL_ID_1);

    assertEquals(2, waitEvents.size());

    WaitEventActivity event1 = waitEvents.get(0);

    assertEquals(EVENT_1[0], event1.event);
    assertEquals(EVENT_1[1], event1.waitClass);
    assertEquals(2, event1.activity);

    WaitEventActivity event2 = waitEvents.get(1);

    assertEquals(EVENT_3[0], event2.event);
    assertEquals(EVENT_3[1], event2.waitClass);
    assertEquals(1, event2.activity);
  }

  private static void verifySqlActivity(String expectedSqlId, int expectedActivity,
      Map<String, Integer> expectedWaitClasses, int totalActivity, int totalSamples,
      SqlActivity sqlActivity) {

    assertEquals(expectedSqlId, sqlActivity.sqlId);
    assertEquals(expectedActivity, sqlActivity.activity);
    assertEquals((double) expectedActivity / totalSamples, sqlActivity.averageActiveSessions, 0);
    assertEquals((100d * expectedActivity) / totalActivity, sqlActivity.percentageTotalActivity, 0);

    verifyMultiset(expectedWaitClasses, sqlActivity.activityByWaitClass);
  }

  private static void verifySessionActivity(String[] expectedSession, int expectedActivity,
      Map<String, Integer> expectedWaitClasses, int totalActivity, SessionActivity sessionActivity) {

    assertEquals(expectedSession[0], sessionActivity.sessionId);
    assertEquals(expectedSession[1], sessionActivity.serialNumber);
    assertEquals(expectedActivity, sessionActivity.activity);
    assertEquals((100d * expectedActivity) / totalActivity,
        sessionActivity.percentageTotalActivity, 0);

    verifyMultiset(expectedWaitClasses, sessionActivity.activityByWaitClass);
  }

  private static void verifyMultiset(Map<String, Integer> expected, Multiset<String> actual) {
    int size = 0;
    for (Map.Entry<String, Integer> entry : expected.entrySet()) {
      size += entry.getValue();
      assertEquals(entry.getValue().intValue(), actual.count(entry.getKey()));
    }
    assertEquals(size, actual.size());
  }

  private static ActiveSession newActiveSession(String[] session, String sqlId, String waitClass) {
    return newActiveSession(session, sqlId, new String[] {null, waitClass});
  }

  private static ActiveSession newActiveSession(String sqlId, String[] event) {
    return newActiveSession(SESSION_1, sqlId, event);
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

  private static AshSnapshot newSnapshot(long timestamp, int samples,
      List<ActiveSession> activeSessions) {
    return new AshSnapshot(timestamp, activeSessions, samples);
  }
}
