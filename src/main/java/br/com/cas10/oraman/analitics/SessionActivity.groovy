package br.com.cas10.oraman.analitics

class SessionActivity {

	final long sessionId
	final long serialNumber
	final String username
	final String program
	final int activity
	final BigDecimal percentageTotalActivity
	final Map<String, BigDecimal> activityByWaitClass

	SessionActivity(long sessionId, long serialNumber, String username, String program, List<Map> activeSessions,
	int totalActivity, int totalSamples) {

		this.sessionId = sessionId
		this.serialNumber = serialNumber
		this.username = username
		this.program = program
		this.activity = activeSessions.size()
		this.percentageTotalActivity = (activity * 100) / totalActivity

		Map<String, List<Map>> waitClassGroups = activeSessions.groupBy { it.wait_class }
		activityByWaitClass = waitClassGroups.collectEntries { key, value ->
			[key, value.size()]
		}.asImmutable()
	}
}
