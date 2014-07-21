package br.com.cas10.oraman.service

import javax.annotation.PostConstruct
import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OracleService {

	private NamedParameterJdbcTemplate jdbc

	int cpuCores
	int cpuThreads

	@Autowired
	@Qualifier('monitoring')
	void setDataSource(DataSource dataSource) {
		jdbc = new NamedParameterJdbcTemplate(dataSource)
	}

	@PostConstruct
	void init() {
		String xeQuery = "select count(1) from v\$version where banner like 'Oracle Database%Express Edition%'"
		int isXe = jdbc.queryForObject(xeQuery, Collections.emptyMap(), Integer.class)
		if (isXe) {
			cpuCores = 1
			cpuThreads = 1
			return
		}
		String osstatQuery = "select stat_name, value from v\$osstat where stat_name in ('NUM_CPUS', 'NUM_CPU_CORES')"
		List<Map<String, Object>> result = jdbc.queryForList(osstatQuery, Collections.emptyMap())
		for (row in result) {
			if (row.stat_name == 'NUM_CPUS'){
				cpuThreads = row.value
				cpuCores = cpuCores ?: row.value
			} else if (row.stat_name == 'NUM_CPU_CORES') {
				cpuCores = row.value
			}
		}
	}

	List<Map<String,Object>> getWaits() {
		String query = '''
			select wait_class as eventclass, sum(time_waited_micro) as eventtime
			from v$system_event where wait_class <> 'Idle' group by wait_class

			union all

			select 'CPU', sum(value) from v$sys_time_model where stat_name in ('DB CPU', 'background cpu time')
			'''
		List<Map<String,Object>> result = jdbc.queryForList(query, Collections.emptyMap())
		return result
	}

	List<Map<String, Object>> getActiveSessions() {
		String query = '''
			select
				sid,
				serial# as serial_number,
				decode(type, 'BACKGROUND', substr(program, -5, 4), username) as username,
				program,
				sql_id,
				sql_child_number,
				decode(wait_time, 0, event, 'CPU + CPU Wait') as event,
				decode(wait_time, 0, wait_class, 'CPU + CPU Wait') as wait_class
			from
				v\$session
			where
				(program <> 'OraManager' or program is null)
				and ((wait_time <> 0 and status = 'ACTIVE') or wait_class <> 'Idle')
			'''
		List<Map<String, Object>> result = jdbc.queryForList(query, Collections.emptyMap())
		return result
	}

	String getSqlText(String sqlId) {
		String query = 'select sql_text	from v$sql where sql_id = :id and rownum < 2'
		List<String> result = jdbc.queryForList(query, ['id' : sqlId], String.class)
		return result ? result.first() : null
	}

	Map getSession(Long sid, Long serialNumber) {
		String query = 'select username, program from v$session where sid = :sid and serial# = :serialNumber'
		List<Map> result = jdbc.queryForList(query, ['sid' : sid, 'serialNumber' : serialNumber])
		return result ? result.first() : null
	}
}
