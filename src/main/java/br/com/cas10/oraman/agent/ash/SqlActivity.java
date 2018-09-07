package br.com.cas10.oraman.agent.ash;

import static com.google.common.collect.Multisets.unmodifiableMultiset;

import br.com.cas10.oraman.oracle.data.ActiveSession;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class SqlActivity {

  public final String sqlId;
  public final String sqlText;
  public final String command;
  public final int activity;
  public final double averageActiveSessions;
  public final double percentageTotalActivity;
  public final Multiset<String> activityByEvent;
  public final Multiset<String> activityByWaitClass;

  private SqlActivity(String sqlId, String sqlText, String command,
      Multiset<String> activityByEvent, Multiset<String> activityByWaitClass, int totalActivity,
      int totalSamples) {
    this.sqlId = sqlId;
    this.sqlText = sqlText;
    this.command = command;
    this.activity = activityByWaitClass.size();
    this.averageActiveSessions = (double) activity / totalSamples;
    this.percentageTotalActivity = (activity * 100d) / totalActivity;
    this.activityByEvent = unmodifiableMultiset(activityByEvent);
    this.activityByWaitClass = unmodifiableMultiset(activityByWaitClass);
  }

  public static class Builder {

    private final String sqlId;
    private final Multiset<String> activityByEvent = HashMultiset.create();
    private final Multiset<String> activityByWaitClass = HashMultiset.create();

    public Builder(String sqlId) {
      this.sqlId = sqlId;
    }

    public String getSqlId() {
      return sqlId;
    }

    public int getActivity() {
      return activityByWaitClass.size();
    }

    public void add(ActiveSession activeSession) {
      activityByEvent.add(activeSession.event);
      activityByWaitClass.add(activeSession.waitClass);
    }

    public SqlActivity build(String sqlText, String command, int totalActivity, int totalSamples) {
      return new SqlActivity(sqlId, sqlText, command, activityByEvent, activityByWaitClass,
          totalActivity, totalSamples);
    }
  }
}
