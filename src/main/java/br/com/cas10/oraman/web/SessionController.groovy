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
	@ResponseBody String session(@RequestParam('sid') Long sid, @RequestParam('serialNumber') Long serialNumber) {
		Map data = oracleService.getSession(sid, serialNumber)
		Map response = (data == null) ? null : [
			'user' : data.username,
			'program' : data.program,
			'sessionTerminationEnabled' : adminService.sessionTerminationEnabled()
		]
		return new JsonBuilder(response).toString()
	}

	@RequestMapping(value = '/session/kill', method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void killSession(@RequestParam('sid') Long sid, @RequestParam('serialNumber') Long serialNumber) {
		adminService.killSession(sid, serialNumber)
	}
}
