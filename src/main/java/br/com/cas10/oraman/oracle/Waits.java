package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.SqlFiles.loadSqlStatement;

import br.com.cas10.oraman.oracle.data.Wait;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Waits {

  private final String waitClassesSql = loadSqlStatement("wait_classes.sql");
  private final String waitsSql = loadSqlStatement("waits.sql");

  @Autowired
  @Qualifier("monitoring")
  private NamedParameterJdbcTemplate jdbc;

  private List<String> waitClasses;

  @PostConstruct
  private void init() {
    List<String> list = jdbc.getJdbcOperations().queryForList(waitClassesSql, String.class);
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
