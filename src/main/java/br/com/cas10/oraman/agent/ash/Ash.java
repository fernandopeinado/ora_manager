package br.com.cas10.oraman.agent.ash;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.cas10.oraman.oracle.Cursors;
import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.util.Snapshot;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;

@Service
public class Ash {

  @Autowired
  private AshAgent agent;
  @Autowired
  private Cursors cursors;

  public List<String> getWaitClasses() {
    return agent.getWaitClasses();
  }

  /**
   * @return snapshots with the average active sessions by wait class, calculated from the ASH
   *         snapshots in memory.
   */
  @Transactional(readOnly = true)
  public List<Snapshot<Double>> getWaitClassesSnapshots() {
    List<AshSnapshot> snapshots = agent.getSnapshots();
    List<Snapshot<Double>> waitClassesSnapshots = new ArrayList<>(snapshots.size());
    snapshots.forEach(s -> waitClassesSnapshots.add(s.waitClassesSnapshot));
    return waitClassesSnapshots;
  }

  /**
   * @param start interval start.
   * @param end interval end.
   * @return snapshots with the average active sessions by wait class, calculated from the ASH
   *         snapshots currently in memory whose timestamp is in the interval {@code [start, end]};
   *         top 10 SQL statements and sessions of the selected snapshots.
   */
  @Transactional(readOnly = true)
  public IntervalActivity getIntervalActivity(long start, long end) {
    List<AshSnapshot> snapshots = agent.getSnapshots();
    return intervalActivity(snapshots.iterator(), start, end);
  }

  private IntervalActivity intervalActivity(Iterator<AshSnapshot> snapshots, long start, long end) {
    int totalSamples = 0;
    int totalActivity = 0;
    List<Snapshot<Double>> waitClassesSnapshots = new ArrayList<>();
    Map<String, SqlActivity.Builder> sqlMap = new HashMap<>();
    Table<String, String, SessionActivity.Builder> sessionsTable = HashBasedTable.create();

    while (snapshots.hasNext()) {
      AshSnapshot snapshot = snapshots.next();
      if (snapshot.timestamp < start || snapshot.timestamp > end) {
        continue;
      }
      totalSamples += snapshot.samples;
      totalActivity += snapshot.activeSessions.size();
      waitClassesSnapshots.add(snapshot.waitClassesSnapshot);
      for (ActiveSession s : snapshot.activeSessions) {
        sqlMap.computeIfAbsent(s.sqlId, SqlActivity.Builder::new).add(s);

        SessionActivity.Builder sessBuilder = sessionsTable.get(s.sid, s.serialNumber);
        if (sessBuilder == null) {
          sessBuilder = new SessionActivity.Builder(s.sid, s.serialNumber, s.username, s.program);
          sessionsTable.put(s.sid, s.serialNumber, sessBuilder);
        }
        sessBuilder.add(s);
      }
    }

    Ordering<SqlActivity.Builder> sqlOrdering =
        Ordering.from((a, b) -> Integer.compare(a.getActivity(), b.getActivity()));
    List<SqlActivity> topSql = new ArrayList<>();
    for (SqlActivity.Builder builder : sqlOrdering.greatestOf(sqlMap.values(), 10)) {
      String sqlText = builder.getSqlId() == null ? null : cursors.getSqlText(builder.getSqlId());
      topSql.add(builder.build(sqlText, totalActivity, totalSamples));
    }

    Ordering<SessionActivity.Builder> sessionsOrdering =
        Ordering.from((a, b) -> Integer.compare(a.getActivity(), b.getActivity()));
    List<SessionActivity> topSessions = new ArrayList<>();
    for (SessionActivity.Builder builder : sessionsOrdering.greatestOf(sessionsTable.values(), 10)) {
      topSessions.add(builder.build(totalActivity));
    }

    return new IntervalActivity(start, end, waitClassesSnapshots, topSql, topSessions);
  }

  /**
   * @param sqlId the identifier of a parent cursor.
   * @return the wait events of the specified cursor, calculated from the ASH snapshots currently in
   *         memory.
   */
  @Transactional(readOnly = true)
  public List<WaitEventActivity> getSqlWaitEvents(String sqlId) {
    checkNotNull(sqlId);

    List<AshSnapshot> snapshots = agent.getSnapshots();

    Map<String, List<ActiveSession>> sessionsByEvent =
        snapshots.stream().flatMap(s -> s.activeSessions.stream())
            .filter(s -> sqlId.equals(s.sqlId)).collect(groupingBy(s -> s.event));

    List<WaitEventActivity> result = new ArrayList<>();
    for (List<ActiveSession> sessions : sessionsByEvent.values()) {
      ActiveSession first = sessions.get(0);
      result.add(new WaitEventActivity(first.event, first.waitClass, sessions.size()));
    }
    Collections.sort(result, (a, b) -> -Integer.compare(a.activity, b.activity));

    return result;
  }
}
