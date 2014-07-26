package br.com.cas10.oraman.analitics

import groovy.transform.CompileStatic

@CompileStatic
class SessionActivity {

	final String sessionId
	final String serialNumber
	final String username
	final String program
	final int activity
	final BigDecimal percentageTotalActivity
	final Map<String, Integer> activityByWaitClass

	SessionActivity(String sessionId, String serialNumber, String username, String program,
	List<ActiveSession> activeSessions, int totalActivity, int totalSamples) {

		this.sessionId = sessionId
		this.serialNumber = serialNumber
		this.username = username
		this.program = program
		this.activity = activeSessions.size()
		this.percentageTotalActivity = (activity * 100) / totalActivity

		Map<String, List<ActiveSession>> waitClassGroups = activeSessions.groupBy { ActiveSession it -> it.waitClass }
		activityByWaitClass = ((Map<String, Integer>) waitClassGroups.collectEntries { String key, List value ->
			[key, value.size()]
		}).asImmutable()
	}
}
