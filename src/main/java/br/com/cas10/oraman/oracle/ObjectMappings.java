package br.com.cas10.oraman.oracle;

import static com.google.common.base.MoreObjects.firstNonNull;

import br.com.cas10.oraman.OramanProperties;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ObjectMappings {

  private final Map<String, String> mappings;

  @Autowired
  ObjectMappings(OramanProperties properties) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    properties.getObjectMappings().forEach(m -> builder.put(m.getFrom().toLowerCase(), m.getTo()));
    mappings = builder.build();
  }

  String lookup(String name) {
    return firstNonNull(mappings.get(name.toLowerCase()), name);
  }
}
