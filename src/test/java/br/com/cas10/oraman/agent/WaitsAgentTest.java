package br.com.cas10.oraman.agent;

import static br.com.cas10.oraman.agent.WaitsAgent.SAMPLING_INTERVAL_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import br.com.cas10.oraman.oracle.Waits;
import br.com.cas10.oraman.oracle.data.Wait;
import br.com.cas10.oraman.util.Snapshot;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class WaitsAgentTest {

  private static final String WAIT_CLASS_1 = "wait class 1";
  private static final String WAIT_CLASS_2 = "wait class 2";

  @Test
  public void testRun() {
    List<Wait> waits1 = waitsList(ImmutableMap.of(WAIT_CLASS_1, 5));
    List<Wait> waits2 = waitsList(ImmutableMap.of(WAIT_CLASS_1, 10, WAIT_CLASS_2, 3));
    List<Wait> waits3 = waitsList(ImmutableMap.of(WAIT_CLASS_1, 13, WAIT_CLASS_2, 10));

    Waits waits = mock(Waits.class);
    when(waits.getWaits()).thenReturn(waits1).thenReturn(waits2).thenReturn(waits3);

    WaitsAgent agent = new WaitsAgent();
    setField(agent, "waits", waits);

    assertEquals(0, agent.getSnapshots().size());
    agent.run();
    assertEquals(0, agent.getSnapshots().size());
    agent.run();
    assertEquals(1, agent.getSnapshots().size());
    agent.run();
    assertEquals(2, agent.getSnapshots().size());

    final double samplingIntervalSeconds = MILLISECONDS.toSeconds(SAMPLING_INTERVAL_MILLIS);

    Snapshot<Double> snapshot1 = agent.getSnapshots().get(0);
    assertEquals(2, snapshot1.getValues().size());
    assertEquals(5 / samplingIntervalSeconds, snapshot1.getValues().get(WAIT_CLASS_1), 0);
    assertEquals(3 / samplingIntervalSeconds, snapshot1.getValues().get(WAIT_CLASS_2), 0);

    Snapshot<Double> snapshot2 = agent.getSnapshots().get(1);
    assertEquals(2, snapshot2.getValues().size());
    assertEquals(3 / samplingIntervalSeconds, snapshot2.getValues().get(WAIT_CLASS_1), 0);
    assertEquals(7 / samplingIntervalSeconds, snapshot2.getValues().get(WAIT_CLASS_2), 0);
  }

  private static List<Wait> waitsList(Map<String, Integer> waits) {
    return waits.entrySet().stream().map(entry -> {
      Wait wait = new Wait();
      wait.waitClass = entry.getKey();
      wait.timeWaitedMicros = SECONDS.toMicros(entry.getValue());
      return wait;
    }).collect(toList());
  }
}
