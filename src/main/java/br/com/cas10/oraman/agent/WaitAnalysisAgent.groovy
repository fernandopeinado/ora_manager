package br.com.cas10.oraman.agent

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.service.OracleService

@Component
class WaitAnalysisAgent extends Agent {

	private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(15)

	@Autowired
	private OracleService service;

	WaitAnalysisAgent() {
		super("wait", SAMPLING_INTERVAL, 240)
	}

	@Override
	public void run() {
		Snapshot s = new WaitAnalysisSnapshot()
		s.type = this.type
		s.timestamp = System.currentTimeMillis()
		List<Map<String,Object>> list = service.getWaits()
		list.each { row ->
			s.observations[row.eventclass] = (Long) row.eventtime
		}
		snapshots.add(s)
	}

	private static class WaitAnalysisSnapshot extends Snapshot {

		@Override
		public void calculateDelta(Snapshot prev) {
			for (obs in observations.entrySet()) {
				Long delta = delta(prev.observations[obs.key], obs.value)
				if (delta != null) {
					deltaObs[obs.key] = (double) delta / TimeUnit.MILLISECONDS.toMicros(SAMPLING_INTERVAL)
				}
			}
		}
	}
}
