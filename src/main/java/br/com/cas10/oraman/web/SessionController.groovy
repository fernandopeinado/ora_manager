package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

import br.com.cas10.oraman.oracle.Sessions
import br.com.cas10.oraman.oracle.data.Session

@Controller
class SessionController {

  @Autowired
  private Sessions sessions

  @RequestMapping(value = '/session', method = RequestMethod.GET)
  @ResponseBody String session(@RequestParam('sid') Long sid, @RequestParam('serialNumber') Long serialNumber) {
    Session session = sessions.getSession(sid, serialNumber)
    Map response = (session == null) ? null : [
      'user' : session.username,
      'program' : session.program,
      'sessionTerminationEnabled' : sessions.sessionTerminationEnabled()
    ]
    return new JsonBuilder(response).toString()
  }

  @RequestMapping(value = '/session/kill', method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  void killSession(@RequestParam('sid') Long sid, @RequestParam('serialNumber') Long serialNumber) {
    sessions.killSession(sid, serialNumber)
  }
}
