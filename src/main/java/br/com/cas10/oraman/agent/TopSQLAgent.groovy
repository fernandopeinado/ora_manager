package br.com.cas10.oraman.agent

import java.util.Map.Entry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.analitics.Snapshots
import br.com.cas10.oraman.service.OracleService;

@Component
class TopSQLAgent extends Agent {

	private Map<String, String> sqls = new HashMap<String, String>();
	
	@Autowired
	private OracleService service;
	
	TopSQLAgent() {
		super("topSql", 60000L, 60)
	}
	
	String getSql(String sqlId) {
		return sqls[sqlId]
	}
	
	@Override
	public void run() {
		Snapshot s = new Snapshot()
		s.type = this.type
		s.timestamp = System.currentTimeMillis()
		List<Map<String,Object>> list = service.getSqlCpu()
		list.each { row ->
			row.each { Entry<String, Object> entry ->
				s.observations[row.SQL_ID] = (Long) row.TOTAL_TIME
				this.sqls[row.SQL_ID] = row.SQL
			}
		}
		snapshots.add(s)
	}
}
