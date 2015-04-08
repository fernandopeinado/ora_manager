package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.SqlFiles.loadSqlStatement;
import br.com.cas10.oraman.oracle.data.Table;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.cas10.oraman.oracle.data.TablespaceUsage;

import com.google.common.collect.ImmutableMap;

@Service
public class Tables {

  private static final RowMapper<String> SCHEMA_ROW_MAPPER = (rs, rownum) -> {
    return rs.getString("owner");
  };

  private static final RowMapper<Table> TABLE_ROW_MAPPER = (rs, rownum) -> {
    Table bean = new Table();
    bean.owner = rs.getString("owner");
    bean.name = rs.getString("table_name");
    bean.tablespace = rs.getString("tablespace_name");
    bean.logging = "YES".equalsIgnoreCase(rs.getString("logging"));
    bean.rows = rs.getLong("num_rows");
    bean.avgRowLength = rs.getLong("avg_row_len");
    bean.sampleSize = rs.getLong("sample_size");
    bean.lastAnalyzed = rs.getDate("last_analyzed");
    return bean;
  };

  private final String allSchemasSql = loadSqlStatement("all_schemas.sql");
  private final String allTablesSql = loadSqlStatement("all_tables.sql");

  @Autowired
  @Qualifier("monitoring")
  private NamedParameterJdbcTemplate jdbc;

  @Transactional(readOnly = true)
  public List<String> getSchemas() {
    return jdbc.query(allSchemasSql, ImmutableMap.of(), SCHEMA_ROW_MAPPER);
  }

  @Transactional(readOnly = true)
  public List<Table> getTables(String owner) {
    return jdbc.query(allTablesSql, ImmutableMap.of("owner", owner), TABLE_ROW_MAPPER);
  }

}
