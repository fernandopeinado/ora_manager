package br.com.cas10.oraman.service

import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import br.com.cas10.oraman.agent.AshAgent
import br.com.cas10.oraman.analitics.ActiveSession
import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.analitics.SessionActivity
import br.com.cas10.oraman.analitics.SqlActivity

@Component
@Transactional(readOnly = true)
@CompileStatic
class AshService {

	private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5)

	@Autowired
	private AshAgent agent
	@Autowired
	private OracleService service

	Map getData() {
		List<AshSnapshot> agentData = agent.data
		long lastTimestamp = agentData ? agentData[-1].timestamp : 0
		return ((Map<String, Object>) ['snapshots' : agentData]) << intervalData(agentData, lastTimestamp - FIVE_MINUTES, lastTimestamp)
	}

	Map getIntervalData(long start, long end) {
		List<AshSnapshot> agentData = agent.data
		return intervalData(agentData, start, end)
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

	private Map intervalData(List<AshSnapshot> snapshots, long start, long end) {
		Collection<AshSnapshot> intervalSnapshots = snapshots.findAll { AshSnapshot it ->
			it.timestamp >= start && it.timestamp <= end
		}

		int totalSamples = 0
		List<ActiveSession> activeSessions = []
		for (snapshot in intervalSnapshots) {
			totalSamples += snapshot.samples
			activeSessions.addAll(snapshot.activeSessions)
		}
		int totalActivity = activeSessions.size()

		List topSql = topActivity(activeSessions, 'sqlId').collect { List<ActiveSession> it ->
			ActiveSession activeSession = it.first()
			String sqlText = service.getSqlText(activeSession.sqlId)
			new SqlActivity(activeSession.sqlId, sqlText, it, totalActivity, totalSamples)
		}
		List topSessions = topActivity(activeSessions, 'serialNumber').collect { List<ActiveSession> it ->
			ActiveSession activeSession = it.first()
			new SessionActivity(activeSession.sid, activeSession.serialNumber, activeSession.username,
					activeSession.program, it, totalActivity, totalSamples)
		}

		return [
			'topSql' : topSql,
			'topSessions' : topSessions,
			'intervalStart' : start,
			'intervalEnd' : end
		]
	}

	private List<List<ActiveSession>> topActivity(List<ActiveSession> activeSessions, String groupKey) {
		Map<Object, List<ActiveSession>> groups = activeSessions.groupBy { it[groupKey] }
		List<List<ActiveSession>> sortedGroups = groups.values().sort { List a, List b ->
			b.size() <=> a.size()
		}
		return (sortedGroups.size() > 10) ? sortedGroups[0..9] : sortedGroups
	}
}
