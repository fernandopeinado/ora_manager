package br.com.cas10.oraman.agent

import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.ActiveSession
import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.service.AshArchive
import br.com.cas10.oraman.service.OracleService

@Component
@CompileStatic
class AshAgent extends Agent {

	private static final int SNAPSHOT_SAMPLES = 15
	private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(1)

	private static int storageSize() {
		(int) (TimeUnit.HOURS.toMillis(1) / (SAMPLING_INTERVAL * SNAPSHOT_SAMPLES))
	}

	private List<List<ActiveSession>> samples = []

	@Autowired
	private AshArchive ashArchive
	@Autowired
	private OracleService service

	AshAgent() {
		super(SAMPLING_INTERVAL, storageSize())
	}

	@Override
	void run() {
		long timestamp = System.currentTimeMillis()
		List<ActiveSession> sample = service.getActiveSessions()

		AshSnapshot snapshot = null
		synchronized(samples) {
			samples.add(sample)
			if (samples.size() == SNAPSHOT_SAMPLES) {
				snapshot = new AshSnapshot(samples, timestamp)
				samples = []
			}
		}
		if (snapshot) {
			snapshots.add(snapshot)
			ashArchive.archive(snapshot)
		}
	}
}
