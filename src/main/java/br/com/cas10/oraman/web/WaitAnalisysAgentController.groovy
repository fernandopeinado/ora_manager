package br.com.cas10.oraman.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import br.com.cas10.oraman.agent.WaitAnalisysAgent

@Controller
@RequestMapping("/agent/waitAnalisys")
class WaitAnalisysAgentController {

	@Autowired
	private WaitAnalisysAgent agent

	@RequestMapping(method = RequestMethod.GET)
	public String open(Model model) {
		model.addAttribute("snapshots", agent.data)
		return "agent/waitAnalisys";
	}
}
