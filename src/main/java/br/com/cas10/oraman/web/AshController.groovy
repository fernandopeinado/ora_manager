package br.com.cas10.oraman.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import br.com.cas10.oraman.agent.AshAgent
import br.com.cas10.oraman.service.OracleService

@Controller
class AshController {

	@Autowired
	private OracleService service;
	@Autowired
	private AshAgent agent

	@RequestMapping(value = "/ash", method = RequestMethod.GET)
	public String open(Model model) {
		return "ash";
	}

	@RequestMapping(value = "/agent/ash", method = RequestMethod.GET)
	public String averageActiveSessions(Model model) {
		model.addAttribute("cpuCores", service.cpuCores)
		model.addAttribute("cpuThreads", service.cpuThreads)
		model.addAttribute("snapshots", agent.data)
		return "agent/averageActiveSessions";
	}
}
