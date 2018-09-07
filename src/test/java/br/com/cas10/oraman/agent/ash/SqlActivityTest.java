package br.com.cas10.oraman.agent.ash;

import static org.junit.Assert.assertEquals;

import br.com.cas10.oraman.oracle.data.ActiveSession;
import org.junit.Test;

public class SqlActivityTest {

  private static final String WAIT_CLASS_1 = "wait class 1";
  private static final String WAIT_CLASS_2 = "wait class 2";

  @Test
  public void testBuilder() {
    SqlActivity.Builder builder = new SqlActivity.Builder("x");

    builder.add(newActiveSession(WAIT_CLASS_1));
    builder.add(newActiveSession(WAIT_CLASS_1));
    builder.add(newActiveSession(WAIT_CLASS_1));
    builder.add(newActiveSession(WAIT_CLASS_2));

    assertEquals(4, builder.getActivity());

    final int totalActivity = 11;
    final int totalSamples = 17;

    SqlActivity sqlActivity = builder.build(null, null, totalActivity, totalSamples);

    assertEquals(4, sqlActivity.activity);
    assertEquals(4d / totalSamples, sqlActivity.averageActiveSessions, 0);
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
