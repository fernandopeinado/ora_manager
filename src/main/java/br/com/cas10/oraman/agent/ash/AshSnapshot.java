package br.com.cas10.oraman.agent.ash;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.util.Snapshot;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

class AshSnapshot implements Serializable {

  private static final long serialVersionUID = 5152182232054887467L;

  final long timestamp;
  final List<ActiveSession> activeSessions;
  final int samples;
  final Snapshot<Double> waitClassesSnapshot;

  AshSnapshot(long timestamp, List<ActiveSession> activeSessions, int samples) {
    this.timestamp = timestamp;
    this.activeSessions = ImmutableList.copyOf(activeSessions);
    this.samples = samples;

    Multiset<String> activityByWaitClass = HashMultiset.create();
    activeSessions.forEach(as -> activityByWaitClass.add(as.waitClass));

    Map<String, Double> values = new HashMap<>();
    for (Multiset.Entry<String> entry : activityByWaitClass.entrySet()) {
      values.put(entry.getElement(), (double) entry.getCount() / samples);
    }

    this.waitClassesSnapshot = new Snapshot<>(timestamp, values);
  }
}
