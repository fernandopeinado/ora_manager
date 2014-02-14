package br.com.cas10.oraman.agent

import java.util.Map.Entry

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.analitics.Snapshots
import br.com.cas10.oraman.service.OracleService;

@Component
class WaitAnalisysAgent extends Agent {

	@Autowired
	private OracleService service;
	
	WaitAnalisysAgent() {
		super("wait", 15000L, 240)
	}
	
	@Override
	public void run() {
		Snapshot s = new Snapshot()
		s.type = this.type
		s.timestamp = System.currentTimeMillis()
		List<Map<String,Object>> list = service.getWaits()
		list.each { row ->
			row.each { Entry<String, Object> entry ->
				s.observations[row.waitclass] = (Long) row.time
			}
		}
		snapshots.add(s)
	}
		
}
