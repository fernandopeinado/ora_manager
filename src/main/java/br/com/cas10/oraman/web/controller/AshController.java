package br.com.cas10.oraman.web.controller;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import br.com.cas10.oraman.agent.ash.Ash;
import br.com.cas10.oraman.agent.ash.IntervalActivity;
import br.com.cas10.oraman.agent.ash.SessionActivity;
import br.com.cas10.oraman.agent.ash.SqlActivity;
import br.com.cas10.oraman.oracle.DatabaseSystem;
import br.com.cas10.oraman.util.Snapshot;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@OramanController
class AshController {

  private static final long FIVE_MINUTES = MINUTES.toMillis(5);
  private static final Double ZERO = Double.valueOf(0);

  @Autowired
  private Ash ash;
  @Autowired
  private DatabaseSystem databaseSystem;

  @RequestMapping(value = "/ash/ash", method = GET)
  Map<String, ?> ash() {
    List<Snapshot<Double>> snapshots = ash.getWaitClassesSnapshots();

    long intervalStart = 0;
    long intervalEnd = 0;
    if (!snapshots.isEmpty()) {
      intervalEnd = Iterables.getLast(snapshots).getTimestamp();
      intervalStart = Math.max(snapshots.get(0).getTimestamp(), intervalEnd - FIVE_MINUTES);
    }
    IntervalActivity intervalActivity = ash.getIntervalActivity(intervalStart, intervalEnd, 10);

    Map<String, Object> response = new LinkedHashMap<>();
    putAasData(snapshots, response);
    putIntervalData(intervalActivity, response);
    return response;
  }

  @RequestMapping(value = "/ash/ash-archive", method = GET)
  Map<String, ?> ashArchiveInterval(@RequestParam("start") Long start,
      @RequestParam("end") Long end,
      @RequestParam(value = "topQueriesCount", required = false, defaultValue = "10")
      Integer topQueriesCount) {
    long groupInterval = Math.max((end - start) / 240, 15_000);
    IntervalActivity intervalActivity = ash.getArchivedIntervalActivity(
            start, end, groupInterval,topQueriesCount);

    Map<String, Object> response = new LinkedHashMap<>();
    putAasData(intervalActivity.waitClassesSnapshots, response);
    putIntervalData(intervalActivity, response);
    response.put("topQueriesCount", topQueriesCount);
    return response;
  }

  @RequestMapping(value = "/ash/ash-interval", method = GET)
  Map<String, ?> ashInterval(@RequestParam("start") Long start,
      @RequestParam("end") Long end,
      @RequestParam(value = "topQueriesCount", required = false, defaultValue = "10")
      Integer topQueriesCount) {
    IntervalActivity intervalActivity = ash.getIntervalActivity(start, end, topQueriesCount);

    Map<String, Object> response = new LinkedHashMap<>();
    putIntervalData(intervalActivity, response);
    response.put("topQueriesCount", topQueriesCount);
    return response;
  }

  @RequestMapping(value = "/ash/ash-sql/{sqlId}", method = GET)
  Map<String, ?> ashSql(@PathVariable("sqlId") String sqlId) {
    IntervalActivity activity = ash.getActivity(s -> sqlId.equals(s.sqlId), 10);

    Map<String, Object> response = new LinkedHashMap<>();
    putEventsAasData(activity.eventsSnapshots, response);

    List<Map<String, Object>> topSessions = new ArrayList<>();
    for (SessionActivity session : activity.topSessions) {
      Map<String, Object> sessionMap = new LinkedHashMap<>();
      sessionMap.put("sessionId", session.sessionId);
      sessionMap.put("serialNumber", session.serialNumber);
      sessionMap.put("username", session.username);
      sessionMap.put("program", session.program);
      sessionMap.put("activity", session.activity);
      sessionMap.put("percentageTotalActivity", session.percentageTotalActivity);
      sessionMap.put("activityByEvent", asMap(session.activityByEvent));
      topSessions.add(sessionMap);
    }
    response.put("topSessions", topSessions);

    return response;
  }

  @RequestMapping(value = "/ash/ash-session", method = GET)
  Map<String, ?> ashSession(@RequestParam("sid") Long sid,
      @RequestParam("serialNumber") Long serialNumber) {
    String sidStr = sid.toString();
    String serialNumberStr = serialNumber.toString();

    IntervalActivity activity =
        ash.getActivity(s -> sidStr.equals(s.sid) && serialNumberStr.equals(s.serialNumber), 10);

    Map<String, Object> response = new LinkedHashMap<>();
    putEventsAasData(activity.eventsSnapshots, response);

    List<Map<String, Object>> topSql = new ArrayList<>();
    for (SqlActivity sql : activity.topSql) {
      Map<String, Object> sqlMap = new LinkedHashMap<>();
      sqlMap.put("sqlId", firstNonNull(sql.sqlId, "Unknown"));
      sqlMap.put("sqlText", firstNonNull(sql.sqlText, "Unavailable"));
      sqlMap.put("command", sql.command);
      sqlMap.put("activity", sql.activity);
      sqlMap.put("averageActiveSessions", sql.averageActiveSessions);
      sqlMap.put("percentageTotalActivity", sql.percentageTotalActivity);
      sqlMap.put("activityByEvent", asMap(sql.activityByEvent));
      topSql.add(sqlMap);
    }
    response.put("topSql", topSql);

    return response;
  }

  private void putAasData(List<Snapshot<Double>> snapshots, Map<String, Object> response) {
    List<String> waitClasses = ash.getWaitClasses();

    List<List<Object>> data = new ArrayList<>(snapshots.size());
    for (Snapshot<Double> snapshot : snapshots) {
      List<Double> values = waitClasses.stream()
          .map(wc -> snapshot.getValues().getOrDefault(wc, ZERO)).collect(toList());
      data.add(asList(snapshot.getTimestamp(), values));
    }

    Map<String, Object> averageActiveSessions = new LinkedHashMap<>();
    averageActiveSessions.put("cpuCores", databaseSystem.getCpuCores());
    averageActiveSessions.put("cpuThreads", databaseSystem.getCpuThreads());
    averageActiveSessions.put("keys", waitClasses);
    averageActiveSessions.put("data", data);

    response.put("averageActiveSessions", averageActiveSessions);
  }

  private void putEventsAasData(List<Snapshot<Double>> snapshots, Map<String, Object> response) {
    Multiset<String> events = snapshots.stream().flatMap(s -> s.getValues().keySet().stream())
        .collect(toCollection(HashMultiset::create));
    List<String> sortedEvents =
        events.entrySet().stream().sorted((a, b) -> Integer.compare(b.getCount(), a.getCount()))
            .map(e -> e.getElement()).collect(toList());

    List<List<Object>> data = new ArrayList<>(snapshots.size());
    for (Snapshot<Double> snapshot : snapshots) {
      List<Double> values = sortedEvents.stream()
          .map(e -> snapshot.getValues().getOrDefault(e, ZERO)).collect(toList());
      data.add(asList(snapshot.getTimestamp(), values));
    }

    response.put("keys", sortedEvents);
    response.put("data", data);
  }

  private void putIntervalData(IntervalActivity intervalActivity, Map<String, Object> response) {
    List<Map<String, Object>> topSql = new ArrayList<>();
    for (SqlActivity sql : intervalActivity.topSql) {
      Map<String, Object> sqlMap = new LinkedHashMap<>();
      sqlMap.put("sqlId", firstNonNull(sql.sqlId, "Unknown"));
      sqlMap.put("sqlText", firstNonNull(sql.sqlText, "Unavailable"));
      sqlMap.put("command", sql.command);
      sqlMap.put("activity", sql.activity);
      sqlMap.put("averageActiveSessions", sql.averageActiveSessions);
      sqlMap.put("percentageTotalActivity", sql.percentageTotalActivity);
      sqlMap.put("activityByWaitClass", asMap(sql.activityByWaitClass));
      topSql.add(sqlMap);
    }
    List<Map<String, Object>> topSessions = new ArrayList<>();
    for (SessionActivity session : intervalActivity.topSessions) {
      Map<String, Object> sessionMap = new LinkedHashMap<>();
      sessionMap.put("sessionId", session.sessionId);
      sessionMap.put("serialNumber", session.serialNumber);
      sessionMap.put("username", session.username);
      sessionMap.put("program", session.program);
      sessionMap.put("activity", session.activity);
      sessionMap.put("percentageTotalActivity", session.percentageTotalActivity);
      sessionMap.put("activityByWaitClass", asMap(session.activityByWaitClass));
      topSessions.add(sessionMap);
    }
    response.put("intervalStart", intervalActivity.intervalStart);
    response.put("intervalEnd", intervalActivity.intervalEnd);
    response.put("topSql", topSql);
    response.put("topSessions", topSessions);
  }

  private static Map<String, Integer> asMap(Multiset<String> multiset) {
    return Maps.asMap(multiset.elementSet(), key -> multiset.count(key));
  }
}
