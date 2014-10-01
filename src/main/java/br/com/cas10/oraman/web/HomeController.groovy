package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.agent.WaitsAgent
import br.com.cas10.oraman.oracle.DatabaseSystem
import br.com.cas10.oraman.util.Snapshot

@Controller
class HomeController {

  @Autowired
  private DatabaseSystem databaseSystem
  @Autowired
  private WaitsAgent waitsAgent

  @RequestMapping(value = '/home/average-active-sessions', method = RequestMethod.GET)
  @ResponseBody String averageActiveSessions() {
    List<Snapshot<Double>> agentData = waitsAgent.getSnapshots()
    List<String> waitClasses = waitsAgent.getWaitClasses()
    def map = [
      'cpuCores' : databaseSystem.cpuCores,
      'cpuThreads' : databaseSystem.cpuThreads,
      'keys' : waitClasses,
      'data' : agentData.collect { snapshot ->
        [snapshot.timestamp]<< (waitClasses.collect() { key -> snapshot.values[key] ?: 0 })
      }
    ]
    return new JsonBuilder(map).toString()
  }
}
