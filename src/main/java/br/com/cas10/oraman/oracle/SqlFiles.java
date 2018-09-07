package br.com.cas10.oraman.oracle;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class SqlFiles {

  /**
   * Loads an SQL statement from a classpath file ({@code sql} directory).
   *
   * @param filename the SQL file.
   */
  static String loadSqlStatement(String filename) {
    checkNotNull(filename);
    String path = "sql/" + filename;
    try (InputStream is = currentThread().getContextClassLoader().getResourceAsStream(path);
        InputStreamReader isr = new InputStreamReader(is, UTF_8)) {
      return CharStreams.toString(isr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private SqlFiles() {}
}
