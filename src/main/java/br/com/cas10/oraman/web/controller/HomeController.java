package br.com.cas10.oraman.web.controller;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.cas10.oraman.agent.WaitsAgent;
import br.com.cas10.oraman.oracle.DatabaseSystem;
import br.com.cas10.oraman.util.Snapshot;

@Controller
class HomeController {

  private static final Double ZERO = Double.valueOf(0);

  @Autowired
  private DatabaseSystem databaseSystem;
  @Autowired
  private WaitsAgent waitsAgent;

  @ResponseBody
  @RequestMapping(value = "/home/average-active-sessions", method = RequestMethod.GET)
  Map<String, ?> averageActiveSessions() {
    List<Snapshot<Double>> snapshots = waitsAgent.getSnapshots();
    List<String> waitClasses = waitsAgent.getWaitClasses();

    List<List<Object>> data = new ArrayList<>(snapshots.size());
    for (Snapshot<Double> snapshot : snapshots) {
      List<Object> snapshotData = new ArrayList<>(1 + waitClasses.size());
      snapshotData.add(snapshot.getTimestamp());
      snapshotData.add(waitClasses.stream().map(wc -> snapshot.getValues().getOrDefault(wc, ZERO))
          .collect(toList()));
      data.add(snapshotData);
    }

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("cpuCores", databaseSystem.getCpuCores());
    response.put("cpuThreads", databaseSystem.getCpuThreads());
    response.put("keys", waitClasses);
    response.put("data", data);
    return response;
  }
}
