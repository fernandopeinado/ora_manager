package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.analitics.SessionActivity
import br.com.cas10.oraman.analitics.SqlActivity
import br.com.cas10.oraman.service.AshService
import br.com.cas10.oraman.service.OracleService

@Controller
class AshController {

	@Autowired
	private AshService ashService
	@Autowired
	private OracleService oracleService

	@RequestMapping(value = '/ash/ash', method = RequestMethod.GET)
	@ResponseBody String ash() {
		Map data = ashService.getData()
		Map response = [:]

		List<AshSnapshot> snapshots = data.snapshots
		List<String> aasKeys = snapshots ? snapshots[0].observations.keySet().toList() : []
		response.averageActiveSessions = [
			'cpuCores' : oracleService.cpuCores,
			'cpuThreads' : oracleService.cpuThreads,
			'keys' : aasKeys,
			'data' : snapshots.collect { snapshot ->
				[snapshot.timestamp]<< (aasKeys.collect() { key -> snapshot.observations[key] })
			}
		]

		response.topSql = convertSql(data.topSql)
		response.topSessions = convertSessions(data.topSessions)
		response.intervalStart = data.intervalStart
		response.intervalEnd = data.intervalEnd

		return new JsonBuilder(response).toString()
	}

	@RequestMapping(value = '/ash/ash-interval', method = RequestMethod.GET)
	@ResponseBody String ashInterval(@RequestParam("start") Long start, @RequestParam("end") Long end) {
		Map data = ashService.getIntervalData(start, end)

		Map response = [:]
		response.topSql = convertSql(data.topSql)
		response.topSessions = convertSessions(data.topSessions)
		response.intervalStart = data.intervalStart
		response.intervalEnd = data.intervalEnd

		return new JsonBuilder(response).toString()
	}

	private List<Map> convertSql(List<SqlActivity> topSql) {
		return topSql.collect {
			[
				'sqlId' : it.sqlId ?: 'Unknown',
				'sqlText' : it.sqlText ?: 'Unavailable',
				'activity' : it.activity,
				'averageActiveSessions' : it.averageActiveSessions,
				'percentageTotalActivity' : it.percentageTotalActivity,
				'activityByWaitClass' : it.activityByWaitClass
			]
		}
	}

	private List<Map> convertSessions(List<SessionActivity> topSessions) {
		return topSessions.collect {
			[
				'sessionId' : it.sessionId,
				'serialNumber' : it.serialNumber,
				'username' : it.username,
				'program' : it.program,
				'activity' : it.activity,
				'percentageTotalActivity': it.percentageTotalActivity,
				'activityByWaitClass' : it.activityByWaitClass
			]
		}
	}
}