package br.com.cas10.oraman.web.controller;

import java.util.HashMap;
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

@Controller
class SessionController {

  @Autowired
  private Sessions sessions;

  @ResponseBody
  @RequestMapping(value = "/session", method = RequestMethod.GET)
  Map<String, ?> session(@RequestParam("sid") Long sid,
      @RequestParam("serialNumber") Long serialNumber) {
    Session session = sessions.getSession(sid, serialNumber);

    if (session == null) {
      return null;
    }

    Map<String, Object> response = new HashMap<>();
    response.put("user", session.username);
    response.put("program", session.program);
    response.put("sessionTerminationEnabled", sessions.sessionTerminationEnabled());
    return response;
  }

  @ResponseStatus(HttpStatus.OK)
  @RequestMapping(value = "/session/kill", method = RequestMethod.POST)
  void killSession(@RequestParam("sid") Long sid, @RequestParam("serialNumber") Long serialNumber) {
    sessions.killSession(sid, serialNumber);
  }
}
