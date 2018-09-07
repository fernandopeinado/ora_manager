package br.com.cas10.oraman.agent.ash;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

import br.com.cas10.oraman.oracle.Sessions;
import br.com.cas10.oraman.oracle.Waits;
import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.util.Buffer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
class AshAgent {

  private static final Logger LOGGER = Logger.getLogger(AshAgent.class);

  @VisibleForTesting
  static final int SNAPSHOT_SAMPLES = 15;

  private static final long SAMPLING_INTERVAL = SECONDS.toMillis(1);
  private static final String CPU_CLASS = "CPU + CPU Wait";

  @Autowired
  private AshArchive archive;
  @Autowired
  private Sessions sessions;
  @Autowired
  @Qualifier("ash")
  private ThreadPoolTaskScheduler scheduler;
  @Autowired
  private Waits waits;

  private List<String> waitClasses;

  @PostConstruct
  private void init() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    waitClasses = builder.add(CPU_CLASS).addAll(waits.getWaitClasses()).build();

    scheduler.scheduleAtFixedRate(this::run, SAMPLING_INTERVAL);
  }

  private final Buffer<AshSnapshot> snapshots;
  private List<ActiveSession> activeSessions = new ArrayList<>();
  private int samples = 0;

  @VisibleForTesting
  AshAgent() {
    int storageSize = (int) (HOURS.toMillis(1) / (SAMPLING_INTERVAL * SNAPSHOT_SAMPLES));
    snapshots = new Buffer<>(storageSize);
  }

  @VisibleForTesting
  void run() {
    long timestamp = System.currentTimeMillis();
    List<ActiveSession> sample = sessions.getActiveSessions();

    synchronized (activeSessions) {
      activeSessions.addAll(sample);
      samples++;
      if (samples == SNAPSHOT_SAMPLES) {
        AshSnapshot snapshot = new AshSnapshot(timestamp, activeSessions, samples);
        snapshots.add(snapshot);
        activeSessions = new ArrayList<>();
        samples = 0;

        archive.archiveSnapshot(snapshot);
      }
    }

    long elapsedTimeMillis = System.currentTimeMillis() - timestamp;
    if (elapsedTimeMillis > 750) {
      LOGGER.warn("Execution time: " + elapsedTimeMillis + "ms");
    }
  }

  List<AshSnapshot> getSnapshots() {
    return this.snapshots.toList();
  }

  List<String> getWaitClasses() {
    return waitClasses;
  }
}
