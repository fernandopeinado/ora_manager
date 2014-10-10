package br.com.cas10.oraman.web

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.agent.ash.Ash
import br.com.cas10.oraman.agent.ash.IntervalActivity
import br.com.cas10.oraman.analitics.SessionActivity
import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.analitics.SqlActivity
import br.com.cas10.oraman.oracle.DatabaseSystem
import br.com.cas10.oraman.service.AshArchive

@Controller
@CompileStatic
class AshController {

  private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5)

  @Autowired
  private Ash ash
  @Autowired
  private AshArchive ashArchive
  @Autowired
  private DatabaseSystem databaseSystem

  @RequestMapping(value = '/ash/ash', method = RequestMethod.GET)
  @ResponseBody String ash() {
    long currentTimeMillis = System.currentTimeMillis()

    List<br.com.cas10.oraman.util.Snapshot<Double>> snapshots = ash.waitClassesSnapshots
    IntervalActivity intervalActivity = ash.getIntervalActivity(currentTimeMillis - FIVE_MINUTES, currentTimeMillis)

    Map response = [:]
    putAasData(snapshots, response)
    putIntervalData(intervalActivity, response)
    return new JsonBuilder(response).toString()
  }

  @RequestMapping(value = '/ash/ash-archive/{year}/{month}/{dayOfMonth}/{hourOfDay}', method = RequestMethod.GET)
  @ResponseBody String ashArchive(@PathVariable('year') Integer year, @PathVariable('month') Integer month,
      @PathVariable('dayOfMonth') Integer dayOfMonth, @PathVariable('hourOfDay') Integer hourOfDay) {
    Map data = ashArchive.getArchivedData(year, month, dayOfMonth, hourOfDay)
    Map response = [:]
    putAasData(data, response)
    putIntervalData(data, response)
    return new JsonBuilder(response).toString()
  }

  @RequestMapping(value = '/ash/ash-interval', method = RequestMethod.GET)
  @ResponseBody String ashInterval(@RequestParam("start") Long start, @RequestParam("end") Long end) {
    IntervalActivity intervalActivity = ash.getIntervalActivity(start, end)
    Map response = [:]
    putIntervalData(intervalActivity, response)
    return new JsonBuilder(response).toString()
  }

  private void putAasData(Map data, Map response) {
    List<Snapshot> snapshots = (List<Snapshot>) data.snapshots
    List<String> aasKeys = Collections.emptyList()
    if (snapshots) {
      aasKeys =  snapshots.first().observations.keySet().toList()
    }
    Map averageActiveSessions = [
      'cpuCores' : databaseSystem.cpuCores,
      'cpuThreads' : databaseSystem.cpuThreads,
      'keys' : aasKeys,
      'data' : snapshots.collect { Snapshot snapshot ->
        ((List<Object>) [snapshot.timestamp]) << (aasKeys.collect() { String key -> snapshot.observations[key] })
      }
    ]
    response.averageActiveSessions = averageActiveSessions
  }

  private void putIntervalData(Map data, Map response) {
    response.topSql = data.topSql.collect { SqlActivity it ->
      [
        'sqlId' : it.sqlId ?: 'Unknown',
        'sqlText' : it.sqlText ?: 'Unavailable',
        'activity' : it.activity,
        'averageActiveSessions' : it.averageActiveSessions,
        'percentageTotalActivity' : it.percentageTotalActivity,
        'activityByWaitClass' : it.activityByWaitClass
      ]
    }
    response.topSessions = data.topSessions.collect { SessionActivity it ->
      [
        'sessionId' : it.sessionId,
        'serialNumber' : it.serialNumber,
        'username' : it.username,
        'program' : it.program,
        'activity' : it.activity,
        'percentageTotalActivity': it.percentageTotalActivity,
        'activityByWaitClass' : it.activityByWaitClass
      ]
    }
    response.intervalStart = data.intervalStart
    response.intervalEnd = data.intervalEnd
  }

  private void putAasData(List<br.com.cas10.oraman.util.Snapshot<Double>> snapshots, Map response) {
    List<String> waitClasses = ash.waitClasses
    Map averageActiveSessions = [
      'cpuCores' : databaseSystem.cpuCores,
      'cpuThreads' : databaseSystem.cpuThreads,
      'keys' : waitClasses,
      'data' : snapshots.collect { snapshot ->
        ((List<Object>) [snapshot.timestamp]) << (waitClasses.collect() { String key -> snapshot.values[key] ?: 0 })
      }
    ]
    response.averageActiveSessions = averageActiveSessions
  }

  private void putIntervalData(IntervalActivity intervalActivity, Map response) {
    response.topSql = intervalActivity.topSql.collect { it ->
      [
        'sqlId' : it.sqlId ?: 'Unknown',
        'sqlText' : it.sqlText ?: 'Unavailable',
        'activity' : it.activity,
        'averageActiveSessions' : it.averageActiveSessions,
        'percentageTotalActivity' : it.percentageTotalActivity,
        'activityByWaitClass' : it.activityByWaitClass.elementSet().collectEntries { key ->
          [
            key,
            it.activityByWaitClass.count(key)
          ]
        }
      ]
    }
    response.topSessions = intervalActivity.topSessions.collect { it ->
      [
        'sessionId' : it.sessionId,
        'serialNumber' : it.serialNumber,
        'username' : it.username,
        'program' : it.program,
        'activity' : it.activity,
        'percentageTotalActivity': it.percentageTotalActivity,
        'activityByWaitClass' : it.activityByWaitClass.elementSet().collectEntries { key ->
          [
            key,
            it.activityByWaitClass.count(key)
          ]
        }
      ]
    }
    response.intervalStart = intervalActivity.intervalStart
    response.intervalEnd = intervalActivity.intervalEnd
  }
}