package br.com.cas10.oraman.agent.ash;

import br.com.cas10.oraman.util.Snapshot;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class IntervalActivity {

  public final long intervalStart;
  public final long intervalEnd;
  public final List<Snapshot<Double>> eventsSnapshots;
  public final List<Snapshot<Double>> waitClassesSnapshots;
  public final List<SqlActivity> topSql;
  public final List<SessionActivity> topSessions;

  IntervalActivity(long intervalStart, long intervalEnd, List<Snapshot<Double>> eventsSnapshots,
      List<Snapshot<Double>> waitClassesSnapshots, List<SqlActivity> topSql,
      List<SessionActivity> topSessions) {
    this.intervalStart = intervalStart;
    this.intervalEnd = intervalEnd;
    this.eventsSnapshots = ImmutableList.copyOf(eventsSnapshots);
    this.waitClassesSnapshots = ImmutableList.copyOf(waitClassesSnapshots);
    this.topSql = ImmutableList.copyOf(topSql);
    this.topSessions = ImmutableList.copyOf(topSessions);
  }
}
