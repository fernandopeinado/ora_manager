package br.com.cas10.oraman.oracle;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;

import br.com.cas10.oraman.util.StringTemplate;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class SqlFileLoader {

  @Autowired
  private ObjectMappings mappings;

  /**
   * Loads a SQL statement from a classpath file ({@code sql} directory).
   *
   * @param filename the SQL file.
   */
  String load(String filename) {
    checkNotNull(filename);
    String path = "sql/" + filename;
    try (InputStream is = currentThread().getContextClassLoader().getResourceAsStream(path);
        InputStreamReader isr = new InputStreamReader(is, UTF_8)) {
      String template = CharStreams.toString(isr);
      return StringTemplate.render(template, mappings::lookup);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
