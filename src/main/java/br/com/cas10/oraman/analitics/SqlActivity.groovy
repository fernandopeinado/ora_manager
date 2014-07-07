package br.com.cas10.oraman.analitics

class SqlActivity {

	final String sqlId
	final String sqlText
	final int activity
	final BigDecimal averageActiveSessions
	final BigDecimal percentageTotalActivity
	final Map<String, BigDecimal> activityByWaitClass

	SqlActivity(String sqlId, String sqlText, List<Map> activeSessions, int totalActivity, int totalSamples) {
		this.sqlId = sqlId
		this.sqlText = sqlText
		this.activity = activeSessions.size()
		this.averageActiveSessions = activity / totalSamples
		this.percentageTotalActivity = (activity * 100) / totalActivity

		Map<String, List<Map>> waitClassGroups = activeSessions.groupBy { it.wait_class }
		activityByWaitClass = waitClassGroups.collectEntries { key, value ->
			[key, value.size()]
		}.asImmutable()
	}
}
