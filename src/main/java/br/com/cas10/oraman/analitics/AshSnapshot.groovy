package br.com.cas10.oraman.analitics

import groovy.transform.CompileStatic
import br.com.cas10.oraman.oracle.data.ActiveSession

@CompileStatic
class AshSnapshot extends Snapshot implements Serializable {

  final int samples
  final List<ActiveSession> activeSessions = []

  AshSnapshot(List<List<ActiveSession>> samples, long timestamp, List<String> waitClasses) {
    this.timestamp = timestamp
    this.samples = samples.size()

    observations['CPU + CPU Wait'] = 0
    for (waitClass in waitClasses) {
      observations[waitClass] = 0
    }

    for (sample in samples) {
      for (activeSession in sample) {
        int value = (int) observations[activeSession.waitClass]
        observations[activeSession.waitClass] = value + 1
      }
      activeSessions.addAll(sample)
    }
    for (entry in observations.entrySet()) {
      entry.value = ((int) entry.value) / this.samples
    }
  }
}
