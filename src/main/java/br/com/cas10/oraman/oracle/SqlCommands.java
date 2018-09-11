package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.OracleObject.V_SQLCOMMAND;
import static java.lang.Integer.parseInt;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.max;
import static java.util.stream.Collectors.toMap;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

@Service
class SqlCommands {

  private static final Logger logger = LoggerFactory.getLogger(SqlCommands.class);

  private static final String FALLBACK_COMMANDS_FILE = "vsqlcommand-oracle12.txt";

  private final String sqlCommandsSql;

  @Autowired
  private AccessChecker accessChecker;
  @Autowired
  private JdbcTemplate jdbc;

  private String[] commands;

  @Autowired
  SqlCommands(SqlFileLoader loader) {
    sqlCommandsSql = loader.load("sql_commands.sql");
  }

  @PostConstruct
  private void init() {
    Map<Integer, String> typesToNames;
    if (accessChecker.isQueryable(V_SQLCOMMAND)) {
      typesToNames = new HashMap<>();
      jdbc.query(sqlCommandsSql,
          (RowCallbackHandler) rs -> typesToNames.put(rs.getInt(1), rs.getString(2)));
    } else {
      logger.warn("{} is not accessible. Falling back to the built-in list of SQL commands",
          V_SQLCOMMAND.name);
      typesToNames = loadFallbackFile();
    }

    commands = new String[max(typesToNames.keySet()) + 1];
    typesToNames.forEach((k, v) -> commands[k] = v.trim());
  }

  private static Map<Integer, String> loadFallbackFile() {
    ClassLoader classLoader = currentThread().getContextClassLoader();
    List<String> lines;
    try (InputStream is = classLoader.getResourceAsStream(FALLBACK_COMMANDS_FILE);
        InputStreamReader isr = new InputStreamReader(is, UTF_8)) {
      lines = CharStreams.readLines(isr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines.stream().map(l -> l.split(",")).collect(toMap(x -> parseInt(x[0]), x -> x[1]));
  }

  String getCommandName(int commandType) {
    return (commandType < 0 || commandType >= commands.length) ? null : commands[commandType];
  }
}
