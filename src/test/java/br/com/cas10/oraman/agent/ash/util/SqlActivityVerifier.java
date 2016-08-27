package br.com.cas10.oraman.agent.ash.util;

import static br.com.cas10.oraman.agent.ash.util.Util.verifyMultiset;
import static org.junit.Assert.assertEquals;

import br.com.cas10.oraman.agent.ash.SqlActivity;

public class SqlActivityVerifier {

  private final int totalActivity;
  private final int totalSamples;

  public SqlActivityVerifier(int totalActivity, int totalSamples) {
    this.totalActivity = totalActivity;
    this.totalSamples = totalSamples;
  }

  public Verification verify(SqlActivity sqlActivity) {
    return new Verification(sqlActivity);
  }

  public class Verification {

    private final SqlActivity sqlActivity;

    private Verification(SqlActivity sqlActivity) {
      this.sqlActivity = sqlActivity;
    }

    public Verification sqlId(String sqlId) {
      assertEquals(sqlId, sqlActivity.sqlId);
      return this;
    }

    public Verification activity(int activity) {
      assertEquals(activity, sqlActivity.activity);
      assertEquals((double) activity / totalSamples, sqlActivity.averageActiveSessions, 0);
      assertEquals((100d * activity) / totalActivity, sqlActivity.percentageTotalActivity, 0);
      return this;
    }

    public Verification events(Object... events) {
      verifyMultiset(events, sqlActivity.activityByEvent);
      return this;
    }

    public Verification waitClasses(Object... waitClasses) {
      verifyMultiset(waitClasses, sqlActivity.activityByWaitClass);
      return this;
    }
  }
}
