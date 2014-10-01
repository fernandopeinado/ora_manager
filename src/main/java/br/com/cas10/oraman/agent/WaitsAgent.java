package br.com.cas10.oraman.agent;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import br.com.cas10.oraman.oracle.Waits;
import br.com.cas10.oraman.util.DeltaBuffer;
import br.com.cas10.oraman.util.Snapshot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;



@Component
public class WaitsAgent {

  @VisibleForTesting
  static final long SAMPLING_INTERVAL_MILLIS = SECONDS.toMillis(15);

  private static final String CPU_CLASS = "CPU";

  @Autowired
  private TaskScheduler scheduler;
  @Autowired
  private Waits waits;

  private List<String> waitClasses;

  @PostConstruct
  private void init() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    waitClasses = builder.add(CPU_CLASS).addAll(waits.getWaitClasses()).build();

    scheduler.scheduleAtFixedRate(this::run, SAMPLING_INTERVAL_MILLIS);
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
