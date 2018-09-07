package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.SqlFiles.loadSqlStatement;
import static com.google.common.base.Preconditions.checkNotNull;

import br.com.cas10.oraman.oracle.data.Cursor;
import br.com.cas10.oraman.oracle.data.ExecutionPlan;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class Cursors {

  private final String childCursorsSql = loadSqlStatement("execution_plans.sql");
  private final String cursorBySqlId = loadSqlStatement("cursor_by_sqlid.sql");
  private final String sqlFullTextBySqlId = loadSqlStatement("sql_fulltext.sql");
  private final String planTableOutputBySqlIdAndChildNumber =
      loadSqlStatement("plan_table_output.sql");

  @Autowired
  @Qualifier("monitoring")
  private NamedParameterJdbcTemplate jdbc;

  /**
   * Returns details of the specified parent cursor or {@code null} if the cursor was not found.
   *
   * @param sqlId the identifier of a parent cursor.
   */
  public Cursor getCursor(String sqlId) {
    checkNotNull(sqlId);
    List<Cursor> list = jdbc.query(cursorBySqlId, ImmutableMap.of("sqlId", sqlId), (rs, rowNum) -> {
      Cursor cursor = new Cursor();
      cursor.sqlId = sqlId;
      cursor.sqlText = rs.getString("sql_text");
      cursor.command = rs.getString("command_name");
      return cursor;
    });
    return DataAccessUtils.singleResult(list);
  }

  /**
   * Returns the SQL text for the specified parent cursor ({@code v$sqlarea}, {@code sql_fulltext})
   * or {@code null} if the cursor was not found.
   *
   * @param sqlId the identifier of a parent cursor.
   */
  public String getSqlFullText(String sqlId) {
    checkNotNull(sqlId);
    List<String> list =
        jdbc.queryForList(sqlFullTextBySqlId, ImmutableMap.of("sqlId", sqlId), String.class);
    return DataAccessUtils.singleResult(list);
  }

  /**
   * Returns the execution plans of the children of the specified parent cursor.
   *
   * @param sqlId the identifier of a parent cursor.
   */
  public List<ExecutionPlan> getExecutionPlans(String sqlId) {
    checkNotNull(sqlId);
    List<ExecutionPlan> plans =
        jdbc.query(childCursorsSql, ImmutableMap.of("sqlId", sqlId), (rs, rowNum) -> {

          ExecutionPlan plan = new ExecutionPlan();

          plan.sqlId = sqlId;
          plan.planHashValue = rs.getLong("plan_hash_value");
          plan.minChild = rs.getLong("min_child");

          plan.childCursors = rs.getLong("child_cursors");
          plan.loads = rs.getLong("loads");
          plan.invalidations = rs.getLong("invalidations");

          plan.parseCalls = rs.getLong("parse_calls");
          plan.executions = rs.getLong("executions");
          plan.fetches = rs.getLong("fetches");
          plan.rowsProcessed = rs.getLong("rows_processed");
          plan.bufferGets = rs.getLong("buffer_gets");
          plan.diskReads = rs.getLong("disk_reads");
          plan.directWrites = rs.getLong("direct_writes");

          plan.elapsedTime = rs.getLong("elapsed_time");
          plan.cpuTime = rs.getLong("cpu_time");
          plan.applicationWaitTime = rs.getLong("application_wait_time");
          plan.clusterWaitTime = rs.getLong("cluster_wait_time");
          plan.concurrencyWaitTime = rs.getLong("concurrency_wait_time");
          plan.userIoWaitTime = rs.getLong("user_io_wait_time");

          return plan;
        });
    for (ExecutionPlan plan : plans) {
      plan.planText = getPlan(plan.sqlId, plan.minChild);
    }
    return plans;
  }

  private String getPlan(String sqlId, long childNumber) {
    List<String> lines = jdbc.queryForList(planTableOutputBySqlIdAndChildNumber,
        ImmutableMap.of("sqlId", sqlId, "childNumber", childNumber), String.class);
    return String.join("\n", lines);
  }
}
