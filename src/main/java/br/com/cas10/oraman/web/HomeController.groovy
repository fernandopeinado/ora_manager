package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.agent.WaitAnalysisAgent
import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.service.OracleService

@Controller
class HomeController {

	@Autowired
	private OracleService service
	@Autowired
	private WaitAnalysisAgent agent

	@RequestMapping(value = '/home/average-active-sessions', method = RequestMethod.GET)
	@ResponseBody String averageActiveSessions() {
		List<Snapshot> agentData = agent.data
		def keys = agentData ? agentData[0].deltas.keySet().toList() : []
		def map = [
			'cpuCores' : service.cpuCores,
			'cpuThreads' : service.cpuThreads,
			'keys' : keys,
			'data' : agentData.collect { snapshot ->
				[snapshot.timestamp]<< (keys.collect() { key -> snapshot.deltas[key] })
			}
		]
		return new JsonBuilder(map).toString()
	}
}
