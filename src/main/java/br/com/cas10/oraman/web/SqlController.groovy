package br.com.cas10.oraman.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import br.com.cas10.oraman.oracle.Cursors
import br.com.cas10.oraman.service.AshService

@Controller
class SqlController {

  @Autowired
  private AshService ashService
  @Autowired
  private Cursors cursors

  @RequestMapping(value = '/sql/{sqlId}', method = RequestMethod.GET)
  @ResponseBody Map<String, ?> sql(@PathVariable('sqlId') String sqlId) {
    if (sqlId == null) {
      return null
    }
    return [
      'fullText' : cursors.getSqlFullText(sqlId),
      'activity' : ashService.getSqlData(sqlId),
      'executionPlans' : cursors.getExecutionPlans(sqlId)
    ]
  }
}
