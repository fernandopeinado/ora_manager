package br.com.cas10.oraman.agent.ash;

import static br.com.cas10.oraman.agent.ash.AshAgent.SNAPSHOT_SAMPLES;
import static java.util.Collections.unmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import br.com.cas10.oraman.oracle.Sessions;
import br.com.cas10.oraman.oracle.data.ActiveSession;

public class AshAgentTest {

  private static final String WAIT_CLASS_1 = "wait class 1";
  private static final String WAIT_CLASS_2 = "wait class 2";

  @Test
  public void testRun() {
    List<ActiveSession> activeSessionsList = activeSessionsList();

    Sessions sessions = mock(Sessions.class);
    when(sessions.getActiveSessions()).thenReturn(activeSessionsList);

    AshAgent agent = new AshAgent();
    setField(agent, "archive", mock(AshArchive.class));
    setField(agent, "sessions", sessions);

    for (int i = 0; i < SNAPSHOT_SAMPLES; i++) {
      assertTrue(agent.getSnapshots().isEmpty());
      agent.run();
    }
    for (int i = 0; i < SNAPSHOT_SAMPLES; i++) {
      assertEquals(1, agent.getSnapshots().size());
      agent.run();
    }
    assertEquals(2, agent.getSnapshots().size());

    AshSnapshot snapshot = agent.getSnapshots().get(1);

    assertEquals(SNAPSHOT_SAMPLES, snapshot.samples);
    assertEquals(SNAPSHOT_SAMPLES * activeSessionsList.size(), snapshot.activeSessions.size());

    Map<String, Double> activityByWaitClass = snapshot.waitClassesSnapshot.getValues();

    assertEquals(2, activityByWaitClass.size());
    assertTrue(activityByWaitClass.containsKey(WAIT_CLASS_1));
    assertTrue(activityByWaitClass.containsKey(WAIT_CLASS_2));
    assertEquals(3d, activityByWaitClass.get(WAIT_CLASS_1), 0);
    assertEquals(1d, activityByWaitClass.get(WAIT_CLASS_2), 0);
  }

  private static List<ActiveSession> activeSessionsList() {
    List<ActiveSession> list = new ArrayList<>();
    list.add(newActiveSession(WAIT_CLASS_1));
    list.add(newActiveSession(WAIT_CLASS_1));
    list.add(newActiveSession(WAIT_CLASS_1));
    list.add(newActiveSession(WAIT_CLASS_2));
    return unmodifiableList(list);
  }

  private static ActiveSession newActiveSession(String waitClass) {
    ActiveSession as = new ActiveSession();
    as.waitClass = waitClass;
    return as;
  }
}
