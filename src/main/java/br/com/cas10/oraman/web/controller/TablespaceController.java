package br.com.cas10.oraman.web.controller;

import br.com.cas10.oraman.oracle.Tablespaces;
import br.com.cas10.oraman.oracle.data.TablespaceUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class TablespaceController {

  @Autowired
  private Tablespaces tablespaces;

  @ResponseBody
  @RequestMapping(value = "/tablespaces", method = RequestMethod.GET)
  Map<String, ?> tablespaces() {
    List<TablespaceUsage> usages = this.tablespaces.getUsage();
    Map<String, Object> response = new HashMap<>();
    response.put("usages", usages);
    return response;
  }

}
