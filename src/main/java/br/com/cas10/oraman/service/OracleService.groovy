package br.com.cas10.oraman.service

import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OracleService {

	private NamedParameterJdbcTemplate jdbc

	@Autowired
	public void setDataSource(DataSource dataSource) {
		jdbc = new NamedParameterJdbcTemplate(dataSource);
	}

	public List<Map<String,Object>> getWaits() {
		NamedParameterJdbcTemplate tmpl = jdbc
		Map params = new HashMap<String, Object>();
		String query = '''
			SELECT COL || ';' || wait_class waitclass, ROUND (time_secs) time
			FROM (
			SELECT 'CLASS' AS COL, n.wait_class, sum(e.time_waited) / 1 time_secs
			FROM v$system_event e, v$event_name n
			WHERE n.NAME = e.event AND n.wait_class <> 'Idle' AND e.time_waited > 0 group by 'CLASS', n.wait_class
			UNION
			SELECT 'CLASS', 'CPU', SUM (VALUE / 10000)
			FROM v$sys_time_model
			WHERE stat_name IN ('background cpu time', 'DB CPU'))
			'''
		List<Map<String,Object>> result = tmpl.queryForList(query, params);
		return result;
	}
}
