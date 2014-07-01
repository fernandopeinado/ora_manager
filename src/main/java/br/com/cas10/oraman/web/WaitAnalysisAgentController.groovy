package br.com.cas10.oraman.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import br.com.cas10.oraman.agent.WaitAnalysisAgent
import br.com.cas10.oraman.service.OracleService

@Controller
@RequestMapping("/agent/waitAnalysis")
class WaitAnalysisAgentController {

	@Autowired
	private OracleService service
	@Autowired
	private WaitAnalysisAgent agent

	@RequestMapping(method = RequestMethod.GET)
	public String open(Model model) {
		model.addAttribute("cpuCores", service.cpuCores)
		model.addAttribute("cpuThreads", service.cpuThreads)
		model.addAttribute("snapshots", agent.data)
		return "agent/waitAnalysis";
	}
}
