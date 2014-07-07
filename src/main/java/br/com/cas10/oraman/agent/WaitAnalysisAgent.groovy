package br.com.cas10.oraman.agent

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.oracle.WaitClass
import br.com.cas10.oraman.service.OracleService

@Component
class WaitAnalysisAgent extends Agent {

	private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(15)
	private static final int STORAGE_SIZE = TimeUnit.HOURS.toMillis(1) / SAMPLING_INTERVAL

	@Autowired
	private OracleService service

	WaitAnalysisAgent() {
		super(SAMPLING_INTERVAL, STORAGE_SIZE)
	}

	@Override
	void run() {
		Snapshot s = new WaitAnalysisSnapshot()
		s.timestamp = System.currentTimeMillis()
		for (row in service.getWaits()) {
			s.observations[row.eventclass] = (Long) row.eventtime
		}
		for (waitClass in WaitClass.VALUES) {
			if (s.observations[waitClass.waitClassName] == null) {
				s.observations[waitClass.waitClassName] = 0L
			}
		}
		snapshots.add(s)
	}

	private static class WaitAnalysisSnapshot extends Snapshot {

		@Override
		protected Object delta(Object prev, Object curr) {
			Long difference = prev ? curr - prev : 0
			return difference / TimeUnit.MILLISECONDS.toMicros(SAMPLING_INTERVAL)
		}
	}
}
