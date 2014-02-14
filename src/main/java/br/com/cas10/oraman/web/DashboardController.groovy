package br.com.cas10.oraman.web

import groovy.transform.CompileStatic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.cas10.oraman.service.OracleService;

@CompileStatic
@Controller
@RequestMapping("/dashboard")
class DashboardController {
	
	@Autowired
	private OracleService service

	@RequestMapping(method = RequestMethod.GET)
	public String open(Model model) {
		return "dashboard";
	}

}
