package br.com.cas10.oraman.analitics

import groovy.transform.CompileStatic
import br.com.cas10.oraman.oracle.data.ActiveSession

@CompileStatic
class SessionActivity {

  final String sessionId
  final String serialNumber
  final String username
  final String program
  final int activity
  final BigDecimal percentageTotalActivity
  final Map<String, Integer> activityByWaitClass

  SessionActivity(String sessionId, String serialNumber, String username, String program,
  int activity, Map<String, Integer> activityByWaitClass, int totalActivity) {

    this.sessionId = sessionId
    this.serialNumber = serialNumber
    this.username = username
    this.program = program
    this.activity = activity
    this.percentageTotalActivity = (activity * 100) / totalActivity
    this.activityByWaitClass = activityByWaitClass
  }

  static class Builder {

    private final String sessionId
    private final String serialNumber
    private final String username
    private final String program

    private int activity = 0
    private Map<String, Integer> activityByWaitClass = new HashMap()

    Builder(String sessionId, String serialNumber, String username, String program) {
      this.sessionId = sessionId
      this.serialNumber = serialNumber
      this.username = username
      this.program = program
    }

    int getActivity() {
      activity
    }

    void add(ActiveSession activeSession) {
      activity++
      int waitClassActivity = activityByWaitClass[activeSession.waitClass] ?: 0
      activityByWaitClass[activeSession.waitClass] = waitClassActivity + 1
    }

    SessionActivity build(int totalActivity) {
      new SessionActivity(sessionId, serialNumber, username, program, activity,
          activityByWaitClass.asImmutable(), totalActivity)
    }
  }
}
