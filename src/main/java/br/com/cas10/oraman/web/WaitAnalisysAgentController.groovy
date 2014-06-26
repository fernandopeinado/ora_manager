package br.com.cas10.oraman.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import br.com.cas10.oraman.agent.TopSQLAgent;
import br.com.cas10.oraman.agent.WaitAnalisysAgent;
import br.com.cas10.oraman.analitics.Snapshot;
import br.com.cas10.oraman.domain.SQLCpuTime;

@Controller
@RequestMapping("/agent/waitAnalisys")
class WaitAnalisysAgentController {

	@Autowired
	private WaitAnalisysAgent agent

	@Autowired
	private TopSQLAgent topSQLagent

	@RequestMapping(method = RequestMethod.GET)
	public String open(Model model) {
		model.addAttribute("snapshots", agent.data)
		return "agent/waitAnalisys";
	}

	@RequestMapping(value = "/topSql", method = RequestMethod.GET)
	public String topSql(Model model) {
		SortedSet<SQLCpuTime> topSqls = new TreeSet<SQLCpuTime>()
		Snapshot snap = topSQLagent.lastData;
		long total = 0;
		if (snap != null) {
			snap.deltaObs.each { Map.Entry<String, Object> entry ->
				if (entry.value > 0) {
					total += (long) entry.value
					topSqls.add(new SQLCpuTime(entry.key, topSQLagent.getSql(entry.key), (Long) entry.value))
				}
			}
			model.addAttribute("timestamp", snap.getDateTime())
		}
		topSqls.each { SQLCpuTime cpu ->
			cpu.totalCpuTime = total;
		}
		model.addAttribute("topSqls", topSqls.size() > 10 ? topSqls.asList().subList(0, 10) : topSqls.asList())
		return "agent/topSqls";
	}
}
