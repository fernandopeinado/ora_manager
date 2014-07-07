package br.com.cas10.oraman.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.analitics.SqlActivity

@Component
@Transactional
class AshService {

	@Autowired
	private OracleService service

	List<SqlActivity> topSql(List<AshSnapshot> snapshots) {
		int totalSamples = 0
		List<Map> activeSessions = []
		for (snapshot in snapshots) {
			totalSamples += snapshot.samples
			activeSessions.addAll(snapshot.activeSessions)
		}
		int totalActivity = activeSessions.size()

		Map<String, List<Map>> statements = activeSessions.groupBy { it.sql_id }
		List<Map.Entry> sortedStatements = statements.entrySet().sort { a, b ->
			b.value.size() <=> a.value.size()
		}
		List<Map.Entry> topStatements = sortedStatements
		if (topStatements.size() > 10) {
			topStatements = topStatements[0..9]
		}

		return topStatements.collect {
			String sqlText = service.getSqlText(it.key)
			new SqlActivity(it.key, sqlText, it.value, totalActivity, totalSamples)
		}
	}
}
