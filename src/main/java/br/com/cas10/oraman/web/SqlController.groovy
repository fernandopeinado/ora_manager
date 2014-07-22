package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.service.AshService
import br.com.cas10.oraman.service.SqlService

@Controller
class SqlController {

	@Autowired
	private AshService ashService
	@Autowired
	private SqlService sqlService

	@RequestMapping(value = '/sql/{sqlId}', method = RequestMethod.GET)
	@ResponseBody String sql(@PathVariable('sqlId') String sqlId) {
		Map data = sqlService.getSqlData(sqlId)
		Map response = [
			'fullText' : data.fullText,
			'activity' : ashService.getSqlData(sqlId),
			'executionPlans' : data.executionPlans.collect {
				[
					'planHashValue' : it.plan_hash_value,
					'planText' : it.plan_text,

					'cursors' : it.cursors,
					'parseCalls' : it.parse_calls,
					'invalidations' : it.invalidations,
					'firstLoadTime' : it.first_load_time,
					'lastLoadTime' : it.last_load_time,

					'executions' : it.executions,
					'rowsProcessed' : it.rows_processed,
					'diskReads' : it.disk_reads,
					'directWrites' : it.direct_writes,
					'bufferGets' : it.buffer_gets,
					'fetches' : it.fetches,

					'elapsedTime' : it.elapsed_time,
					'cpuTime' : it.cpu_time,
					'applicationWaitTime' : it.application_wait_time,
					'concurrencyWaitTime' : it.concurrency_wait_time,
					'clusterWaitTime' : it.cluster_wait_time,
					'userIoWaitTime' : it.user_io_wait_time
				]
			}
		]
		return new JsonBuilder(response).toString()
	}
}
