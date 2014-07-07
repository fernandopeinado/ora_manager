package br.com.cas10.oraman.analitics

import br.com.cas10.oraman.oracle.WaitClass

class AshSnapshot extends Snapshot {

	final int samples
	final List<Map> activeSessions = []

	AshSnapshot(List<List<Map>> samples, long timestamp) {
		this.timestamp = timestamp
		this.samples = samples.size()

		observations['CPU + CPU Wait'] = 0
		for (waitClass in WaitClass.VALUES) {
			observations[waitClass.waitClassName] = 0
		}

		for (sample in samples) {
			for (activeSession in sample) {
				observations[activeSession.wait_class] += 1
			}
			activeSessions.addAll(sample)
		}
		for (entry in observations.entrySet()) {
			entry.value = entry.value / this.samples
		}
	}
}
