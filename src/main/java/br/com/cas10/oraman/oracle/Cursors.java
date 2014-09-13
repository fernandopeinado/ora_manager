package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.SqlFiles.loadSqlStatement;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.cas10.oraman.oracle.data.ExecutionPlan;

import com.google.common.collect.ImmutableMap;

@Service
@Transactional(readOnly = true)
public class Cursors {

  private final String childCursorsSql = loadSqlStatement("execution_plans.sql");

  @Autowired
  @Qualifier("monitoring")
  private NamedParameterJdbcTemplate jdbc;

  /**
   * @param sqlId the identifier of a parent cursor.
   * @return the first thousand characters of the SQL text for the specified parent cursor (
   *         {@code v$sqlarea}, {@code sql_text}). Returns {@code null} if the cursor was not found.
   */
  public String getSqlText(String sqlId) {
    checkNotNull(sqlId);
    List<String> list =
        jdbc.queryForList("select sql_text from v$sqlarea where sql_id = :sqlId",
            ImmutableMap.of("sqlId", sqlId), String.class);
    return DataAccessUtils.singleResult(list);
  }

  /**
   * @param sqlId the identifier of a parent cursor.
   * @return the SQL text for the specified parent cursor ({@code v$sqlarea}, {@code sql_fulltext}).
   *         Returns {@code null} if the cursor was not found.
   */
  public String getSqlFullText(String sqlId) {
    checkNotNull(sqlId);
    List<String> list =
        jdbc.queryForList("select sql_fulltext from v$sqlarea where sql_id = :sqlId",
            ImmutableMap.of("sqlId", sqlId), String.class);
    return DataAccessUtils.singleResult(list);
  }

  /**
   * @param sqlId the identifier of a parent cursor.
   * @return the execution plans of the children of the specified parent cursor.
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
    List<String> lines =
        jdbc.queryForList(
            "select plan_table_output from table(dbms_xplan.display_cursor(:sqlId, :childNumber))",
            ImmutableMap.of("sqlId", sqlId, "childNumber", childNumber), String.class);
    return String.join("\n", lines);
  }
}
