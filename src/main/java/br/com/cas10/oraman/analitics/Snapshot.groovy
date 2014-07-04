package br.com.cas10.oraman.analitics

class Snapshot {

	long timestamp
	Map<String, Object> observations = [:]
	Map<String, Object> deltas = [:]

	void calculateDeltas(Snapshot prev) {
		for (obs in observations.entrySet()) {
			Object prevValue = prev ? prev.observations[obs.key] : null
			deltas[obs.key] = delta(prevValue, obs.value)
		}
	}

	protected Object delta(Object prev, Object curr) {
		return prev ? curr - prev : null
	}
}
