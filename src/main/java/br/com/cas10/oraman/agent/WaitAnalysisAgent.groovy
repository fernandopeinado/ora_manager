package br.com.cas10.oraman.agent

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.oracle.Waits

@Component
class WaitAnalysisAgent extends Agent {

  private static final long SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(15)
  private static final int STORAGE_SIZE = TimeUnit.HOURS.toMillis(1) / SAMPLING_INTERVAL

  @Autowired
  private Waits waits

  WaitAnalysisAgent() {
    super(SAMPLING_INTERVAL, STORAGE_SIZE)
  }

  @Override
  void run() {
    Snapshot s = new WaitAnalysisSnapshot()
    s.timestamp = System.currentTimeMillis()
    for (row in waits.getWaits()) {
      s.observations[row.waitClass] = row.timeWaitedMicros
    }
    for (waitClass in waits.getWaitClasses()) {
      if (s.observations[waitClass] == null) {
        s.observations[waitClass] = 0L
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
