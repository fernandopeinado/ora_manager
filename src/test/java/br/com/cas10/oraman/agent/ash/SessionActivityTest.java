package br.com.cas10.oraman.agent.ash;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import br.com.cas10.oraman.oracle.data.ActiveSession;

public class SessionActivityTest {

  private static final String WAIT_CLASS_1 = "wait class 1";
  private static final String WAIT_CLASS_2 = "wait class 2";

  @Test
  public void testBuilder() {
    SessionActivity.Builder builder = new SessionActivity.Builder("1", "1", "oraman", "OraManager");

    builder.add(newActiveSession(WAIT_CLASS_1));
    builder.add(newActiveSession(WAIT_CLASS_1));
    builder.add(newActiveSession(WAIT_CLASS_1));
    builder.add(newActiveSession(WAIT_CLASS_2));

    assertEquals(4, builder.getActivity());

    final int totalActivity = 11;

    SessionActivity sqlActivity = builder.build(totalActivity);

    assertEquals(4, sqlActivity.activity);
    assertEquals(400d / totalActivity, sqlActivity.percentageTotalActivity, 0);

    assertEquals(4, sqlActivity.activityByWaitClass.size());
    assertEquals(3, sqlActivity.activityByWaitClass.count(WAIT_CLASS_1));
    assertEquals(1, sqlActivity.activityByWaitClass.count(WAIT_CLASS_2));
  }

  private static ActiveSession newActiveSession(String waitClass) {
    ActiveSession as = new ActiveSession();
    as.waitClass = waitClass;
    return as;
  }
}
