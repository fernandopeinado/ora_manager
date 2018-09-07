package br.com.cas10.oraman.web.controller;

import br.com.cas10.oraman.oracle.Tablespaces;
import br.com.cas10.oraman.oracle.data.TablespaceUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@OramanController
class TablespaceController {

  @Autowired
  private Tablespaces tablespaces;

  @RequestMapping(value = "/tablespaces", method = RequestMethod.GET)
  Map<String, ?> tablespaces() {
    List<TablespaceUsage> usages = this.tablespaces.getUsage();
    Map<String, Object> response = new HashMap<>();
    response.put("usages", usages);
    return response;
  }

}
