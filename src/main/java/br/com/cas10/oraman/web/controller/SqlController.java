package br.com.cas10.oraman.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.cas10.oraman.agent.ash.Ash;
import br.com.cas10.oraman.agent.ash.WaitEventActivity;
import br.com.cas10.oraman.oracle.Cursors;

@Controller
class SqlController {

  @Autowired
  private Ash ash;
  @Autowired
  private Cursors cursors;

  @ResponseBody
  @RequestMapping(value = "/sql/{sqlId}", method = RequestMethod.GET)
  Map<String, ?> sql(@PathVariable("sqlId") String sqlId) {
    List<WaitEventActivity> sqlWaitEvents = ash.getSqlWaitEvents(sqlId);

    List<Map<String, Object>> activity = new ArrayList<>(sqlWaitEvents.size());
    for (WaitEventActivity waitEvent : sqlWaitEvents) {
      Map<String, Object> map = new HashMap<>();
      map.put("event", waitEvent.event);
      map.put("waitClass", waitEvent.waitClass);
      map.put("activity", waitEvent.activity);
      activity.add(map);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("fullText", cursors.getSqlFullText(sqlId));
    response.put("activity", activity);
    response.put("executionPlans", cursors.getExecutionPlans(sqlId));
    return response;
  }
}
