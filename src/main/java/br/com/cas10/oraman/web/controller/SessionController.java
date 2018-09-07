package br.com.cas10.oraman.web.controller;

import br.com.cas10.oraman.oracle.DatabaseSystem;
import br.com.cas10.oraman.oracle.Sessions;
import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.oracle.data.Session;
import com.google.common.collect.ImmutableList;
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

@Controller
class SessionController {

  @Autowired
  private DatabaseSystem system;
  @Autowired
  private Sessions sessions;

  @ResponseBody
  @RequestMapping(value = "/session", method = RequestMethod.GET)
  Map<String, ?> session(@RequestParam("sid") Long sid,
      @RequestParam(value = "serialNumber", required = false) Long serialNumber) {

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
    response.put("instanceNumber", system.getInstanceNumber());
    response.put("sessionTerminationEnabled", sessions.sessionTerminationEnabled());

    String result;
    if (session != null) {
      result = "sessionFound";
      response.put("session", session);
    } else if (!candidates.isEmpty()) {
      result = "multipleSessionsFound";
      response.put("candidates", candidates);
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

  @ResponseBody
  @RequestMapping(value = "/sessions", method = RequestMethod.GET)
  Map<String, ?> sessions() {
    List<ActiveSession> sessions = this.sessions.getAllSessions();
    Map<String, Object> response = new HashMap<>();
    response.put("instanceNumber", system.getInstanceNumber());
    response.put("sessionTerminationEnabled", this.sessions.sessionTerminationEnabled());
    response.put("sessions", sessions);
    return response;
  }

}
