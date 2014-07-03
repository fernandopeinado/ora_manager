package br.com.cas10.oraman.agent;

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.service.OracleService

@Component
class AshAgent extends Agent {

	private static final int SNAPSHOT_SAMPLES = 15
	private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(1)
	private static final long STORAGE_SIZE = TimeUnit.HOURS.toMillis(1) / (SAMPLING_INTERVAL * SNAPSHOT_SAMPLES)

	private final AtomicReference<SnapshotBuilder> builderRef = new AtomicReference()

	@Autowired
	private OracleService service;

	public AshAgent() {
		super("ash", SAMPLING_INTERVAL, STORAGE_SIZE);
		builderRef.set(new SnapshotBuilder())
	}

	@Override
	public void run() {
		long timestamp = System.currentTimeMillis()
		List<List<Map<String, Object>>> activeSessions = service.getActiveSessions()

		SnapshotBuilder builder = builderRef.get()
		synchronized(builder) {
			builder.add(activeSessions)
			if (builder.isFull()) {
				snapshots.add(builder.build(type, timestamp))
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

		Snapshot build(String type, long timestamp) {
			Snapshot s = new Snapshot()
			s.observations = [:].withDefault { 0 }
			s.type = type
			s.timestamp = timestamp
			data.each { sample ->
				sample.each { activeSession ->
					s.observations[activeSession.wait_class] += 1
				}
			}
			s.observations.each { entry ->
				entry.value = (double) entry.value / data.size()
			}
			return s
		}
	}
}
