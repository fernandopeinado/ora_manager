package br.com.cas10.oraman.web.controller;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.cas10.oraman.agent.ash.Ash;
import br.com.cas10.oraman.agent.ash.IntervalActivity;
import br.com.cas10.oraman.agent.ash.SessionActivity;
import br.com.cas10.oraman.agent.ash.SqlActivity;
import br.com.cas10.oraman.oracle.DatabaseSystem;
import br.com.cas10.oraman.util.Snapshot;

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

@Controller
class AshController {

  private static final long FIVE_MINUTES = MINUTES.toMillis(5);
  private static final Double ZERO = Double.valueOf(0);

  @Autowired
  private Ash ash;
  @Autowired
  private DatabaseSystem databaseSystem;

  @ResponseBody
  @RequestMapping(value = "/ash/ash", method = GET)
  Map<String, ?> ash() {
    long currentTimeMillis = System.currentTimeMillis();
    List<Snapshot<Double>> snapshots = ash.getWaitClassesSnapshots();
    IntervalActivity intervalActivity =
        ash.getIntervalActivity(currentTimeMillis - FIVE_MINUTES, currentTimeMillis);

    Map<String, Object> response = new LinkedHashMap<>();
    putAasData(snapshots, response);
    putIntervalData(intervalActivity, response);
    return response;
  }

  @ResponseBody
  @RequestMapping(value = "/ash/ash-archive/{year}/{month}/{dayOfMonth}/{hour}", method = GET)
  Map<String, ?> ashArchive(@PathVariable("year") Integer year,
      @PathVariable("month") Integer month, @PathVariable("dayOfMonth") Integer dayOfMonth,
      @PathVariable("hour") Integer hour) {
    IntervalActivity intervalActivity = ash.getIntervalActivity(year, month, dayOfMonth, hour);

    Map<String, Object> response = new LinkedHashMap<>();
    putAasData(intervalActivity.waitClassesSnapshots, response);
    putIntervalData(intervalActivity, response);
    return response;
  }

  @ResponseBody
  @RequestMapping(value = "/ash/ash-interval", method = GET)
  Map<String, ?> ashInterval(@RequestParam("start") Long start, @RequestParam("end") Long end) {
    IntervalActivity intervalActivity = ash.getIntervalActivity(start, end);

    Map<String, Object> response = new LinkedHashMap<>();
    putIntervalData(intervalActivity, response);
    return response;
  }

  private void putAasData(List<Snapshot<Double>> snapshots, Map<String, Object> response) {
    List<String> waitClasses = ash.getWaitClasses();

    List<List<Object>> data = new ArrayList<>(snapshots.size());
    for (Snapshot<Double> snapshot : snapshots) {
      List<Object> snapshotData = new ArrayList<>(1 + waitClasses.size());
      snapshotData.add(snapshot.getTimestamp());
      snapshotData.add(waitClasses.stream().map(wc -> snapshot.getValues().getOrDefault(wc, ZERO))
          .collect(toList()));
      data.add(snapshotData);
    }

    Map<String, Object> averageActiveSessions = new LinkedHashMap<>();
    averageActiveSessions.put("cpuCores", databaseSystem.getCpuCores());
    averageActiveSessions.put("cpuThreads", databaseSystem.getCpuThreads());
    averageActiveSessions.put("keys", waitClasses);
    averageActiveSessions.put("data", data);

    response.put("averageActiveSessions", averageActiveSessions);
  }

  private void putIntervalData(IntervalActivity intervalActivity, Map<String, Object> response) {
    List<Map<String, Object>> topSql = new ArrayList<>();
    for (SqlActivity sql : intervalActivity.topSql) {
      Map<String, Object> sqlMap = new LinkedHashMap<>();
      sqlMap.put("sqlId", firstNonNull(sql.sqlId, "Unknown"));
      sqlMap.put("sqlText", firstNonNull(sql.sqlText, "Unavailable"));
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