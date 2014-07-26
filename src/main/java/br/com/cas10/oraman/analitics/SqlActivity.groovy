package br.com.cas10.oraman.analitics

import groovy.transform.CompileStatic

@CompileStatic
class SqlActivity {

	final String sqlId
	final String sqlText
	final int activity
	final BigDecimal averageActiveSessions
	final BigDecimal percentageTotalActivity
	final Map<String, Integer> activityByWaitClass

	SqlActivity(String sqlId, String sqlText, List<ActiveSession> activeSessions, int totalActivity,
	int totalSamples) {

		this.sqlId = sqlId
		this.sqlText = sqlText
		this.activity = activeSessions.size()
		this.averageActiveSessions = activity / totalSamples
		this.percentageTotalActivity = (activity * 100) / totalActivity

		Map<String, List<ActiveSession>> waitClassGroups = activeSessions.groupBy { ActiveSession it -> it.waitClass }
		activityByWaitClass = ((Map<String, Integer>) waitClassGroups.collectEntries { String key, List value ->
			[key, value.size()]
		}).asImmutable()
	}
}
