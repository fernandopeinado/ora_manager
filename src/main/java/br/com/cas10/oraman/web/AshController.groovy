package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.util.HtmlUtils

import br.com.cas10.oraman.agent.AshAgent
import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.service.AshService
import br.com.cas10.oraman.service.OracleService

@Controller
class AshController {

	@Autowired
	private AshAgent agent
	@Autowired
	private AshService ashService
	@Autowired
	private OracleService oracleService

	@RequestMapping(value = '/ash/ash', method = RequestMethod.GET)
	@ResponseBody String ash() {
		long currentTime = System.currentTimeMillis()
		long fiveMinutes = TimeUnit.MINUTES.toMillis(5)

		List<Snapshot> agentData = agent.data
		List<Snapshot> lastFiveMinutes = agentData.findAll {
			(currentTime - it.timestamp) < fiveMinutes
		}
		Map map = [:]

		// Average Active Sessions
		List<String> aasKeys = agentData ? agentData[0].observations.keySet().toList() : []
		Map aasMap = [
			'cpuCores' : oracleService.cpuCores,
			'cpuThreads' : oracleService.cpuThreads,
			'keys' : aasKeys,
			'data' : agentData.collect { snapshot ->
				[snapshot.timestamp]<< (aasKeys.collect() { key -> snapshot.observations[key] })
			}
		]
		map['averageActiveSessions'] = aasMap

		// Top SQL
		List<Map> topSql = ashService.topSql(lastFiveMinutes).collect {
			[
				'sqlId' : it.sqlId ?: 'Unknown',
				'sqlText' : it.sqlText ? HtmlUtils.htmlEscape(it.sqlText) : 'Unavailable',
				'activity' : it.activity,
				'averageActiveSessions' : it.averageActiveSessions,
				'percentageTotalActivity' : it.percentageTotalActivity,
				'activityByWaitClass' : it.activityByWaitClass
			]
		}
		map['topSql'] = topSql

		return new JsonBuilder(map).toString()
	}
}
