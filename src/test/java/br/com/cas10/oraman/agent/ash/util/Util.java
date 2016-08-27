package br.com.cas10.oraman.agent.ash.util;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Multiset;

import br.com.cas10.oraman.util.Snapshot;

public class Util {

  public static void verifySnapshot(Snapshot<Double> snapshot, long timestamp, int samples,
      Object... counts) {
    assertEquals(timestamp, snapshot.getTimestamp());
    for (int i = 0; i < counts.length; i += 2) {
      String key = (String) counts[i];
      Integer count = (Integer) counts[i + 1];
      assertEquals((double) count / samples, snapshot.getValues().get(key), 0);
    }
    assertEquals(counts.length / 2, snapshot.getValues().size());
  }

  static void verifyMultiset(Object[] expected, Multiset<String> actual) {
    int expectedSize = 0;
    for (int i = 0; i < expected.length; i += 2) {
      String expectedKey = (String) expected[i];
      Integer expectedValue = (Integer) expected[i + 1];
      expectedSize += expectedValue;
      assertEquals(expectedValue.intValue(), actual.count(expectedKey));
    }
    assertEquals(expectedSize, actual.size());
  }
}
