package br.com.cas10.oraman.agent

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.service.OracleService

@Component
class AshAgent extends Agent {

	private static final int SNAPSHOT_SAMPLES = 15
	private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(1)
	private static final int STORAGE_SIZE = TimeUnit.HOURS.toMillis(1) / (SAMPLING_INTERVAL * SNAPSHOT_SAMPLES)

	private List<List<Map>> samples = []

	@Autowired
	private OracleService service

	AshAgent() {
		super(SAMPLING_INTERVAL, STORAGE_SIZE)
	}

	@Override
	void run() {
		long timestamp = System.currentTimeMillis()
		List<Map> sample = service.getActiveSessions()

		synchronized(samples) {
			samples.add(sample)
			if (samples.size() == SNAPSHOT_SAMPLES) {
				snapshots.add(new AshSnapshot(samples, timestamp))
				samples = []
			}
		}
	}
}
