package br.com.cas10.oraman.service

import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SqlService {

	private NamedParameterJdbcTemplate jdbc

	@Autowired
	@Qualifier('monitoring')
	void setDataSource(DataSource dataSource) {
		jdbc = new NamedParameterJdbcTemplate(dataSource)
	}

	Map getSqlData(String sqlId) {
		['fullText' : getFullText(sqlId), 'executionPlans' : getExecutionPlans(sqlId)]
	}

	private String getFullText(String sqlId) {
		String query = 'select sql_fulltext	from v$sql where sql_id = :id and rownum < 2'
		List<String> result = jdbc.queryForList(query, ['id' : sqlId], String)
		return result ? result.first() : null
	}

	private List<Map> getExecutionPlans(String sqlId) {
		String query = '''
			select
				plan_hash_value,

				count(1) as cursors,
				sum(parse_calls) as parse_calls,
				sum(invalidations) as invalidations,
				min(first_load_time) as first_load_time,
				max(last_load_time) as last_load_time,

				sum(executions) as executions,
				sum(rows_processed) as rows_processed,
				sum(disk_reads) as disk_reads,
				sum(direct_writes) as direct_writes,
				sum(buffer_gets) as buffer_gets,
				sum(fetches) as fetches,

				sum(elapsed_time) as elapsed_time,
				sum(cpu_time) as cpu_time,
				sum(application_wait_time) as application_wait_time,
				sum(concurrency_wait_time) as concurrency_wait_time,
				sum(cluster_wait_time) as cluster_wait_time,
				sum(user_io_wait_time) as user_io_wait_time
			from
				v\$sql
			where
				sql_id = :id
			group by
				plan_hash_value
			order by
				plan_hash_value
		'''
		List<Map> executionPlans = jdbc.queryForList(query, ['id' : sqlId])
		for (plan in executionPlans) {
			plan.plan_text = getPlanText(sqlId, plan.plan_hash_value)
		}
		return executionPlans
	}

	private String getPlanText(String sqlId, BigDecimal planHashValue) {
		BigDecimal childNumber = getChildNumber(sqlId, planHashValue)
		if (childNumber == null) {
			return null
		}
		String query = 'select plan_table_output from table(dbms_xplan.display_cursor(:id, :child))'
		List<String> lines = jdbc.queryForList(query, ['id' : sqlId, 'child' : childNumber], String)
		return lines.join('\n')
	}

	private BigDecimal getChildNumber(String sqlId, BigDecimal planHashValue) {
		String query = '''select min(child_number) as child_number
			from v\$sql_plan where sql_id = :id and plan_hash_value = :hash'''
		List<BigDecimal> result = jdbc.queryForList(query, ['id' : sqlId, 'hash' : planHashValue], BigDecimal)
		return result ? result.first() : null
	}
}
