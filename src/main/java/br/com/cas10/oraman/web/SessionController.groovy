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

import br.com.cas10.oraman.service.AdminService
import br.com.cas10.oraman.service.OracleService

@Controller
class SessionController {

	@Autowired
	private AdminService adminService
	@Autowired
	private OracleService oracleService

	@RequestMapping(value = '/session', method = RequestMethod.GET)
	@ResponseBody String session(@RequestParam('sid') Long sid, @RequestParam(value = 'serialNumber', required = false) Long serialNumber) {
		Map session = null
		List<Map> sessions = []

		if (serialNumber == null) {
			sessions = oracleService.getSessions(sid)
			if (sessions.size() == 1) {
				session = sessions.first()
			}
		} else {
			session = oracleService.getSession(sid, serialNumber)
		}

		String status
		if (session) {
			status = 'sessionFound'
		} else if (sessions) {
			status = 'multipleSessionsFound'
		} else {
			status = 'sessionNotFound'
		}

		Map response = [
			'status' : status,
			'sessionTerminationEnabled' : adminService.sessionTerminationEnabled()
		]
		if (session) {
			response.session = [
				'sid' : session.sid,
				'serialNumber' : session['serial#'],
				'user' : session.username,
				'program' : session.program,
			]
		} else if (sessions) {
			response.sessions = sessions.collect {
				[
					'sid' : it.sid,
					'serialNumber' : it['serial#'],
					'user' : it.username,
					'program': it.program
				]
			}
		}

		return new JsonBuilder(response).toString()
	}

	@RequestMapping(value = '/session/kill', method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void killSession(@RequestParam('sid') Long sid, @RequestParam('serialNumber') Long serialNumber) {
		adminService.killSession(sid, serialNumber)
	}
}
