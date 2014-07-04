package br.com.cas10.oraman.web

import groovy.json.JsonBuilder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.service.SystemService

@Controller
class SystemController {

	@Autowired
	private SystemService system

	@RequestMapping(value = '/system/info', method = RequestMethod.GET)
	@ResponseBody String info() {
		def info = [
			'basic' : system.basicInfo,
			'processor' : system.processorInfo,
			'memory' : system.memoryInfo,
			'network' : system.networkInfo,
			'storage' : system.storageInfo,
			'sysctl' : system.sysctl
		]
		return new JsonBuilder(info).toString()
	}
}
