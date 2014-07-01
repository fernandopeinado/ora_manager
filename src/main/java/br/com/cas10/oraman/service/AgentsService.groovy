package br.com.cas10.oraman.service

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service

import br.com.cas10.oraman.agent.Agent

@Service
class AgentsService {

	@Autowired
	private Agent[] agents

	@Autowired
	private TaskScheduler taskScheduler

	@PostConstruct
	void init() {
		for (Agent agent in agents) {
			println "Agendando: ${agent.class}"
			taskScheduler.scheduleAtFixedRate(agent, agent.interval);
		}
	}
}
