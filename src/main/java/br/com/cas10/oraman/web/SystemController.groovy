package br.com.cas10.oraman.web

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import br.com.cas10.oraman.service.SystemService

@CompileStatic
@Controller
@RequestMapping("/system")
class SystemController {
	
	@Autowired
	private SystemService system;
	
	@RequestMapping(method = RequestMethod.GET)
	public String open(Model model) {
		try { model.addAttribute("basicInfo", system.basicInfo) } catch (Exception e) { e.printStackTrace(); }
		try { model.addAttribute("processorInfo", system.processorInfo)  } catch (Exception e) { e.printStackTrace(); }
		try { model.addAttribute("memoryInfo", system.memoryInfo) } catch (Exception e) { e.printStackTrace(); }
		try { model.addAttribute("networkInfo", system.networkInfo) } catch (Exception e) { e.printStackTrace(); }
		try { model.addAttribute("storageInfo", system.storageInfo) } catch (Exception e) { e.printStackTrace(); }
		try { model.addAttribute("sysctl", system.sysctl) } catch (Exception e) { e.printStackTrace(); }
		return "system"
	}
	
}
