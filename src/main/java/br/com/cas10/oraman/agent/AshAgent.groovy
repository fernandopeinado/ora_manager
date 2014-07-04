package br.com.cas10.oraman.agent

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.oracle.WaitClass
import br.com.cas10.oraman.service.OracleService

@Component
class AshAgent extends Agent {

	private static final int SNAPSHOT_SAMPLES = 15
	private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(1)
	private static final int STORAGE_SIZE = TimeUnit.HOURS.toMillis(1) / (SAMPLING_INTERVAL * SNAPSHOT_SAMPLES)

	private final AtomicReference<SnapshotBuilder> builderRef = new AtomicReference()

	@Autowired
	private OracleService service

	public AshAgent() {
		super(SAMPLING_INTERVAL, STORAGE_SIZE)
		builderRef.set(new SnapshotBuilder())
	}

	@Override
	public void run() {
		long timestamp = System.currentTimeMillis()
		def activeSessions = service.getActiveSessions()

		SnapshotBuilder builder = builderRef.get()
		synchronized(builder) {
			builder.add(activeSessions)
			if (builder.isFull()) {
				snapshots.add(builder.build(timestamp))
				builderRef.set(new SnapshotBuilder())
			}
		}
	}

	private static class SnapshotBuilder {

		private List<List<Map<String, Object>>> data = []

		boolean isFull() {
			data.size() == SNAPSHOT_SAMPLES
		}

		void add(List<Map<String, Object>> activeSessions) {
			data.add(activeSessions)
		}

		Snapshot build(long timestamp) {
			Snapshot s = new Snapshot()
			s.timestamp = timestamp

			s.observations['CPU + CPU Wait'] = 0
			for (waitClass in WaitClass.VALUES) {
				s.observations[waitClass.waitClassName] = 0
			}

			for (sample in data) {
				for (activeSession in sample) {
					s.observations[activeSession.wait_class] += 1
				}
			}
			for (entry in s.observations.entrySet()) {
				entry.value = entry.value / data.size()
			}
			return s
		}
	}
}
