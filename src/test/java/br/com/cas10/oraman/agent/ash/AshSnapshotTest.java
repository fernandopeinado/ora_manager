package br.com.cas10.oraman.agent.ash;

import static org.junit.Assert.assertEquals;

import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.util.Snapshot;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AshSnapshotTest {

  private static final String WAIT_CLASS_1 = "wait class 1";
  private static final String WAIT_CLASS_2 = "wait class 2";
  private static final String WAIT_CLASS_3 = "wait class 3";

  @Test
  public void testConstructor() {
    List<ActiveSession> activeSessions = new ArrayList<>();

    activeSessions.add(newActiveSession(WAIT_CLASS_1));
    activeSessions.add(newActiveSession(WAIT_CLASS_1));

    activeSessions.add(newActiveSession(WAIT_CLASS_2));
    activeSessions.add(newActiveSession(WAIT_CLASS_2));
    activeSessions.add(newActiveSession(WAIT_CLASS_2));
    activeSessions.add(newActiveSession(WAIT_CLASS_2));
    activeSessions.add(newActiveSession(WAIT_CLASS_2));

    activeSessions.add(newActiveSession(WAIT_CLASS_3));

    final int samples = 3;

    AshSnapshot snapshot = new AshSnapshot(1, activeSessions, samples);
    Snapshot<Double> waitClassesSnapshot = snapshot.waitClassesSnapshot;

    assertEquals(3, waitClassesSnapshot.getValues().size());
    assertEquals(2d / samples, waitClassesSnapshot.getValues().get(WAIT_CLASS_1), 0);
    assertEquals(5d / samples, waitClassesSnapshot.getValues().get(WAIT_CLASS_2), 0);
    assertEquals(1d / samples, waitClassesSnapshot.getValues().get(WAIT_CLASS_3), 0);
  }

  private static ActiveSession newActiveSession(String waitClass) {
    ActiveSession as = new ActiveSession();
    as.waitClass = waitClass;
    return as;
  }
}
