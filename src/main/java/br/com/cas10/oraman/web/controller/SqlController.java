package br.com.cas10.oraman.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.cas10.oraman.oracle.Cursors;

@Controller
class SqlController {

  @Autowired
  private Cursors cursors;

  @ResponseBody
  @RequestMapping(value = "/sql/{sqlId}", method = RequestMethod.GET)
  Map<String, ?> sql(@PathVariable("sqlId") String sqlId) {
    Map<String, Object> response = new HashMap<>();
    response.put("fullText", cursors.getSqlFullText(sqlId));
    response.put("executionPlans", cursors.getExecutionPlans(sqlId));
    return response;
  }
}
