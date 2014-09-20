package br.com.cas10.oraman.analitics

import groovy.transform.CompileStatic
import br.com.cas10.oraman.oracle.data.ActiveSession

@CompileStatic
class SqlActivity {

  final String sqlId
  final String sqlText
  final int activity
  final BigDecimal averageActiveSessions
  final BigDecimal percentageTotalActivity
  final Map<String, Integer> activityByWaitClass

  SqlActivity(String sqlId, String sqlText, int activity, Map<String, Integer> activityByWaitClass,
  int totalActivity, int totalSamples) {

    this.sqlId = sqlId
    this.sqlText = sqlText
    this.activity = activity
    this.averageActiveSessions = activity / totalSamples
    this.percentageTotalActivity = (activity * 100) / totalActivity
    this.activityByWaitClass = activityByWaitClass
  }

  static class Builder {

    final String sqlId

    private int activity = 0
    private Map<String, Integer> activityByWaitClass = new HashMap()

    Builder(String sqlId) {
      this.sqlId = sqlId
    }

    int getActivity() {
      activity
    }

    void add(ActiveSession activeSession) {
      activity++
      int waitClassActivity = activityByWaitClass[activeSession.waitClass] ?: 0
      activityByWaitClass[activeSession.waitClass] = waitClassActivity + 1
    }

    SqlActivity build(String sqlText, int totalActivity, int totalSamples) {
      new SqlActivity(sqlId, sqlText, activity, activityByWaitClass.asImmutable(), totalActivity, totalSamples)
    }
  }
}
