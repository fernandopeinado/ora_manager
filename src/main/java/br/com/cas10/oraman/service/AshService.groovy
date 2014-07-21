package br.com.cas10.oraman.service

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import br.com.cas10.oraman.agent.AshAgent
import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.analitics.SessionActivity
import br.com.cas10.oraman.analitics.SqlActivity

@Component
@Transactional(readOnly = true)
class AshService {

	private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5)

	@Autowired
	private AshAgent agent
	@Autowired
	private OracleService service

	Map getData() {
		List<AshSnapshot> agentData = agent.data
		long lastTimestamp = agentData ? agentData[-1].timestamp : 0
		return ['snapshots' : agentData] << intervalData(agentData, lastTimestamp - FIVE_MINUTES, lastTimestamp)
	}

	Map getIntervalData(long start, long end) {
		List<AshSnapshot> agentData = agent.data
		return intervalData(agentData, start, end)
	}

	private Map intervalData(List<AshSnapshot> snapshots, long start, long end) {
		List<AshSnapshot> intervalSnapshots = snapshots.findAll {
			it.timestamp >= start && it.timestamp <= end
		}

		int totalSamples = 0
		List<Map> activeSessions = []
		for (snapshot in intervalSnapshots) {
			totalSamples += snapshot.samples
			activeSessions.addAll(snapshot.activeSessions)
		}
		int totalActivity = activeSessions.size()

		List topSql = topActivity(activeSessions, 'sql_id').collect {
			Map activeSession = it.first()
			String sqlText = service.getSqlText(activeSession.sql_id)
			new SqlActivity(activeSession.sql_id, sqlText, it, totalActivity, totalSamples)
		}
		List topSessions = topActivity(activeSessions, 'serial_number').collect {
			Map activeSession = it.first()
			new SessionActivity((long) activeSession.sid, (long) activeSession.serial_number, activeSession.username,
					activeSession.program, it, totalActivity, totalSamples)
		}

		return [
			'topSql' : topSql,
			'topSessions' : topSessions,
			'intervalStart' : start,
			'intervalEnd' : end
		]
	}

	private List<List<Map>> topActivity(List<Map> activeSessions, String groupKey) {
		Map<String, List<Map>> groups = activeSessions.groupBy { it[groupKey] }
		List<List<Map>> sortedGroups = groups.values().sort { a, b ->
			b.size() <=> a.size()
		}
		return (sortedGroups.size() > 10) ? sortedGroups[0..9] : sortedGroups
	}
}
