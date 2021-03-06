package br.com.cas10.oraman.web.controller;

import br.com.cas10.oraman.oracle.Tables;
import br.com.cas10.oraman.oracle.data.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@OramanController
class TablesController {

  @Autowired
  private Tables tables;

  @RequestMapping(value = "/schemas", method = RequestMethod.GET)
  Map<String, ?> schemas() {
    List<String> schemas = this.tables.getSchemas();
    Map<String, Object> response = new HashMap<>();
    response.put("schemas", schemas);
    return response;
  }

  @RequestMapping(value = "/tables/{schema}", method = RequestMethod.GET)
  Map<String, ?> tables(@PathVariable("schema") String schema) {
    List<Table> tbls = this.tables.getTables(schema);
    Map<String, Object> response = new HashMap<>();
    response.put("tables", tbls);
    return response;
  }

  @RequestMapping(value = "/tables/{schema}/{table}", method = RequestMethod.GET)
  Map<String, ?> fullTable(@PathVariable("schema") String schema,
      @PathVariable("table") String table) {
    Table tbl = this.tables.getFullTable(schema, table);
    Map<String, Object> response = new HashMap<>();
    response.put("table", tbl);
    return response;
  }
}
