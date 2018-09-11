package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.OracleObject.V_EVENT_NAME;

import br.com.cas10.oraman.oracle.data.Wait;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Waits {

  private static final Logger logger = LoggerFactory.getLogger(Waits.class);

  /** Fallback list. Valid for Oracle 11 and 12 */
  private static final List<String> WAIT_CLASSES =
      ImmutableList.of("Administrative", "Application", "Cluster", "Commit", "Concurrency",
          "Configuration", "Network", "Other", "Queueing", "Scheduler", "System I/O", "User I/O");

  private final String waitClassesSql;
  private final String waitsSql;

  @Autowired
  private AccessChecker accessChecker;
  @Autowired
  private JdbcTemplate jdbc;

  private List<String> waitClasses;

  @Autowired
  public Waits(SqlFileLoader loader) {
    waitClassesSql = loader.load("wait_classes.sql");
    waitsSql = loader.load("waits.sql");
  }

  @PostConstruct
  private void init() {
    List<String> list;
    if (accessChecker.isQueryable(V_EVENT_NAME)) {
      list = jdbc.queryForList(waitClassesSql, String.class);
    } else {
      logger.warn(
          "v$event_name is not accessible. Falling back to the built-in list of wait classes");
      list = WAIT_CLASSES;
    }
    waitClasses = ImmutableList.copyOf(list);
  }

  /**
   * Returns the waits from {@code v$system_event} aggregated by wait class and the CPU usage from
   * {@code v$sys_time_model}.
   */
  @Transactional(readOnly = true)
  public List<Wait> getWaits() {
    return jdbc.query(waitsSql, (rs, rownum) -> {
      Wait wait = new Wait();
      wait.waitClass = rs.getString("wait_class").intern();
      wait.timeWaitedMicros = rs.getLong("time_waited_micros");
      return wait;
    });
  }

  /**
   * Returns the wait classes from {@code v$event_name}, except {@code Idle}.
   */
  public List<String> getWaitClasses() {
    return this.waitClasses;
  }
}
