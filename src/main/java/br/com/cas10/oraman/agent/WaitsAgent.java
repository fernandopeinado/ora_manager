package br.com.cas10.oraman.agent;

import static br.com.cas10.oraman.oracle.OracleObject.V_SYSTEM_EVENT;
import static br.com.cas10.oraman.oracle.OracleObject.V_SYS_TIME_MODEL;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import br.com.cas10.oraman.oracle.AccessChecker;
import br.com.cas10.oraman.oracle.Waits;
import br.com.cas10.oraman.util.DeltaBuffer;
import br.com.cas10.oraman.util.Snapshot;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class WaitsAgent {

  private static final Logger logger = LoggerFactory.getLogger(WaitsAgent.class);

  @VisibleForTesting
  static final long SAMPLING_INTERVAL_MILLIS = SECONDS.toMillis(15);

  private static final String CPU_CLASS = "CPU";

  @Autowired
  private AccessChecker accessChecker;
  @Autowired
  private TaskScheduler scheduler;
  @Autowired
  private Waits waits;

  private List<String> waitClasses;

  @PostConstruct
  private void init() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    waitClasses = builder.add(CPU_CLASS).addAll(waits.getWaitClasses()).build();

    List<String> notAccessible = new ArrayList<>();
    if (!accessChecker.isQueryable(V_SYS_TIME_MODEL)) {
      notAccessible.add(V_SYS_TIME_MODEL.name);
    }
    if (!accessChecker.isQueryable(V_SYSTEM_EVENT)) {
      notAccessible.add(V_SYSTEM_EVENT.name);
    }

    if (notAccessible.isEmpty()) {
      scheduler.scheduleAtFixedRate(this::run, SAMPLING_INTERVAL_MILLIS);
    } else {
      logger.warn("Not accessible: {}. WaitsAgent will not be started.",
          Joiner.on(", ").join(notAccessible));
    }
  }

  private final DeltaBuffer<Snapshot<Long>, Snapshot<Double>> snapshots;

  @VisibleForTesting
  WaitsAgent() {
    int storageSize = (int) (HOURS.toMillis(1) / SAMPLING_INTERVAL_MILLIS);
    long samplingIntervalMicros = MILLISECONDS.toMicros(SAMPLING_INTERVAL_MILLIS);

    snapshots = new DeltaBuffer<>(storageSize, (prev, curr) -> {

      ImmutableMap.Builder<String, Double> values = ImmutableMap.builder();
      for (Map.Entry<String, Long> entry : curr.getValues().entrySet()) {
        double delta = entry.getValue() - prev.getValues().getOrDefault(entry.getKey(), 0L);
        values.put(entry.getKey(), delta / samplingIntervalMicros);
      }
      return new Snapshot<>(curr.getTimestamp(), values.build());
    });
  }

  @VisibleForTesting
  void run() {
    long timestamp = System.currentTimeMillis();
    Map<String, Long> values = new HashMap<>();
    waits.getWaits().forEach(wait -> values.put(wait.waitClass, wait.timeWaitedMicros));
    snapshots.add(new Snapshot<>(timestamp, values));
  }

  public List<String> getWaitClasses() {
    return waitClasses;
  }

  public List<Snapshot<Double>> getSnapshots() {
    return snapshots.toList();
  }
}
