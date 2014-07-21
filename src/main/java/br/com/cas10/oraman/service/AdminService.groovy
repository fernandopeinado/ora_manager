package br.com.cas10.oraman.service

import groovy.util.logging.Log4j

import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
@Log4j
class AdminService {

	private JdbcTemplate jdbc

	@Autowired(required = false)
	@Qualifier('admin')
	void setDataSource(DataSource dataSource) {
		jdbc = new JdbcTemplate(dataSource)
	}

	void killSession(Long sid, Long serialNumber) {
		if (!jdbc) {
			return
		}
		jdbc.execute("alter system kill session '${sid},${serialNumber}' immediate")
		log.info("Session killed: ${sid} (SID), ${serialNumber} (Serial#)")
	}

	boolean sessionTerminationEnabled() {
		jdbc != null
	}
}
