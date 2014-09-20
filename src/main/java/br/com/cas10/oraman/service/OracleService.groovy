package br.com.cas10.oraman.service

import groovy.transform.CompileStatic

import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
@CompileStatic
class OracleService {

  private NamedParameterJdbcTemplate jdbc

  @Autowired
  @Qualifier('monitoring')
  void setDataSource(DataSource dataSource) {
    jdbc = new NamedParameterJdbcTemplate(dataSource)
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
}
