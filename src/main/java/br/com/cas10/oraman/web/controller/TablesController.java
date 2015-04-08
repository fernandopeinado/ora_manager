package br.com.cas10.oraman.web.controller;

import br.com.cas10.oraman.oracle.Tables;
import br.com.cas10.oraman.oracle.data.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
class TablesController {

  @Autowired
  private Tables tables;

  @ResponseBody
  @RequestMapping(value = "/schemas", method = RequestMethod.GET)
  Map<String, ?> schemas() {
    List<String> schemas = this.tables.getSchemas();
    Map<String, Object> response = new HashMap<>();
    response.put("schemas", schemas);
    return response;
  }
  
  @ResponseBody
  @RequestMapping(value = "/tables/{schema}", method = RequestMethod.GET)
  Map<String, ?> tables(@PathVariable("schema") String schema) {
    List<Table> tbls = this.tables.getTables(schema);
    Map<String, Object> response = new HashMap<>();
    response.put("tables", tbls);
    return response;
  }

}
