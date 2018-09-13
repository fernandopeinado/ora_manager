package br.com.cas10.oraman;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

import java.util.Optional;
import org.apache.catalina.Valve;
import org.apache.catalina.valves.AccessLogValve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The {@code maxDays} property of Tomcat's AccessLogValve is not currently available in the
 * {@link ServerProperties.Tomcat} class.
 */
@Component
@Order(LOWEST_PRECEDENCE)
class TomcatAccessLogMaxDaysCustomizer
    implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Autowired
  private OramanProperties properties;

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    Optional<Valve> valve =
        factory.getEngineValves().stream().filter(v -> v instanceof AccessLogValve).findFirst();
    if (!valve.isPresent()) {
      return;
    }
    AccessLogValve accessLogValve = (AccessLogValve) valve.get();
    accessLogValve.setMaxDays(properties.getAccessLog().getMaxDays());
  }
}
