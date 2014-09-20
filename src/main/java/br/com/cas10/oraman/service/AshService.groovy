package br.com.cas10.oraman.service

import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import br.com.cas10.oraman.agent.AshAgent
import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.analitics.SessionActivity
import br.com.cas10.oraman.analitics.SqlActivity
import br.com.cas10.oraman.oracle.Cursors
import br.com.cas10.oraman.oracle.data.ActiveSession

@Component
@Transactional(readOnly = true)
@CompileStatic
class AshService {

  private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5)

  @Autowired
  private AshAgent agent
  @Autowired
  private Cursors cursors

  Map getData() {
    List<AshSnapshot> agentData = agent.data
    long lastTimestamp = agentData ? agentData[-1].timestamp : 0

    Map<String, Object> result = (Map<String, Object>) ['snapshots' : agentData]
    result << intervalData(agentData.iterator(), lastTimestamp - FIVE_MINUTES, lastTimestamp)
    return result
  }

  Map getIntervalData(long start, long end) {
    List<AshSnapshot> agentData = agent.data
    return intervalData(agentData.iterator(), start, end)
  }

  List<Map> getSqlData(String sqlId) {
    List<AshSnapshot> agentData = agent.data

    List<ActiveSession> activeSessions = []
    for (snapshot in agentData) {
      for (activeSession in snapshot.activeSessions) {
        if (activeSession.sqlId == sqlId) {
          activeSessions.add(activeSession)
        }
      }
    }

    Map<String, List<ActiveSession>> groups = activeSessions.groupBy { ActiveSession it -> it.event }
    List<List<ActiveSession>> sortedGroups = groups.values().sort { List a, List b -> b.size() <=> a.size() }
    return sortedGroups.collect { List<ActiveSession> it ->
      ActiveSession first = it.first()
      return ['event' : first.event, 'waitClass': first.waitClass, 'activity' : it.size()]
    }
  }

  Map intervalData(Iterator<AshSnapshot> snapshots, long start, long end) {
    int totalSamples = 0
    int totalActivity = 0
    Map<String, SqlActivity.Builder> sqlMap = new HashMap()
    Map<String, SessionActivity.Builder> sessionsMap = new HashMap()

    while (snapshots.hasNext()) {
      AshSnapshot snapshot = snapshots.next()

      if (snapshot.timestamp < start || snapshot.timestamp > end) {
        continue
      }

      totalSamples += snapshot.samples
      for (s in snapshot.activeSessions) {
        totalActivity++

        SqlActivity.Builder sqlBuilder = sqlMap[s.sqlId]
        if (!sqlBuilder) {
          sqlBuilder = new SqlActivity.Builder(s.sqlId)
          sqlMap[s.sqlId] = sqlBuilder
        }
        sqlBuilder.add(s)

        String sessionKey = s.sid + '-' + s.serialNumber
        SessionActivity.Builder sessionBuilder = sessionsMap[sessionKey]
        if (!sessionBuilder) {
          sessionBuilder = new SessionActivity.Builder(s.sid, s.serialNumber, s.username, s.program)
          sessionsMap[sessionKey] = sessionBuilder
        }
        sessionBuilder.add(s)
      }
    }

    List<SqlActivity> topSql = sqlMap.values()
        .sort { SqlActivity.Builder a, SqlActivity.Builder b ->
          b.activity <=> a.activity
        }
        .take(10)
        .collect { SqlActivity.Builder builder ->
          String sqlText = builder.sqlId ? cursors.getSqlText(builder.sqlId) : null
          return builder.build(sqlText, totalActivity, totalSamples)
        }
    List<SessionActivity> topSessions = sessionsMap.values()
        .sort { SessionActivity.Builder a, SessionActivity.Builder b ->
          b.activity <=> a.activity
        }
        .take(10)
        .collect { SessionActivity.Builder builder ->
          return builder.build(totalActivity)
        }

    return [
      'topSql' : topSql,
      'topSessions' : topSessions,
      'intervalStart' : start,
      'intervalEnd' : end
    ]
  }
}
