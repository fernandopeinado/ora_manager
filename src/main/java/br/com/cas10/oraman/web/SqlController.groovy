package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.service.OracleService

@Controller
class SqlController {

	@Autowired
	private OracleService service

	@RequestMapping(value = '/sql/{sqlId}', method = RequestMethod.GET)
	@ResponseBody String sql(@PathVariable('sqlId') String sqlId) {
		Map response = [:]
		response.executionPlans = service.getExecutionPlans(sqlId)
		return new JsonBuilder(response).toString()
	}
}
