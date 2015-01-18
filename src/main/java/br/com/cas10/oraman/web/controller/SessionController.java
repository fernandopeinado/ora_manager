package br.com.cas10.oraman.web.controller;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import br.com.cas10.oraman.oracle.Sessions;
import br.com.cas10.oraman.oracle.data.Session;

import com.google.common.collect.ImmutableList;

@Controller
class SessionController {

  @Autowired
  private Sessions sessions;

  @ResponseBody
  @RequestMapping(value = "/session", method = RequestMethod.GET)
  Map<String, ?> session(@RequestParam("sid") Long sid, @RequestParam(value = "serialNumber",
      required = false) Long serialNumber) {

    Session session = null;
    List<Session> candidates = ImmutableList.of();
    if (serialNumber != null) {
      session = sessions.getSession(sid, serialNumber);
    } else {
      candidates = sessions.getSessions(sid);
      if (candidates.size() == 1) {
        session = candidates.get(0);
      }
    }

    Map<String, Object> response = new HashMap<>();
    response.put("sessionTerminationEnabled", sessions.sessionTerminationEnabled());

    String result;
    if (session != null) {
      result = "sessionFound";

      Map<String, Object> s = new HashMap<>();
      s.put("sid", session.sessionId);
      s.put("serialNumber", session.serialNumber);
      s.put("user", session.username);
      s.put("program", session.program);
      response.put("session", s);

    } else if (!candidates.isEmpty()) {
      result = "multipleSessionsFound";

      response.put("candidates", candidates.stream().map(c -> {
        Map<String, Object> s = new HashMap<>();
        s.put("sid", c.sessionId);
        s.put("serialNumber", c.serialNumber);
        s.put("user", c.username);
        s.put("program", c.program);
        return s;
      }).collect(toList()));

    } else {
      result = "sessionNotFound";
    }
    response.put("result", result);

    return response;
  }

  @ResponseStatus(HttpStatus.OK)
  @RequestMapping(value = "/session/kill", method = RequestMethod.POST)
  void killSession(@RequestParam("sid") Long sid, @RequestParam("serialNumber") Long serialNumber) {
    sessions.killSession(sid, serialNumber);
  }
}
