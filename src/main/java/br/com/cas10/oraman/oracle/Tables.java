package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.SqlFiles.loadSqlStatement;
import br.com.cas10.oraman.oracle.data.Column;
import br.com.cas10.oraman.oracle.data.Index;
import br.com.cas10.oraman.oracle.data.Table;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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

  private static class TableColumnRowMapper implements RowMapper<Column> {

    private Table table;

    public TableColumnRowMapper(Table table) {
      this.table = table;
    }

    @Override
    public Column mapRow(ResultSet rs, int i) throws SQLException {
      Column bean = new Column();
      bean.name = rs.getString("column_name");
      bean.dataType = rs.getString("data_type");
      bean.length = rs.getLong("data_length");
      bean.precision = rs.getLong("data_precision");
      bean.scale = rs.getLong("data_scale");
      bean.nullable = "Y".equalsIgnoreCase(rs.getString("nullable"));
      bean.id = rs.getLong("column_id");
      bean.lastAnalyzed = rs.getTimestamp("last_analyzed");
      table.columns.put(bean.name, bean);
      return bean;
    }
  };

  private static class IndexColumnRowMapper implements RowMapper<Index> {

    private Table table;

    public IndexColumnRowMapper(Table table) {
      this.table = table;
    }

    @Override
    public Index mapRow(ResultSet rs, int i) throws SQLException {
      Index bean = new Index();
      bean.name = rs.getString("index_name");
      bean.unique = "UNIQUE".equalsIgnoreCase(rs.getString("uniqueness"));
      bean.tablespace = rs.getString("tablespace_name");
      bean.logging = "YES".equalsIgnoreCase(rs.getString("logging"));
      bean.blevel = rs.getLong("blevel");
      bean.leafBlocks = rs.getLong("leaf_blocks");
      bean.distinctKeys = rs.getLong("distinct_keys");
      bean.rows = rs.getLong("num_rows");
      bean.sampleSize = rs.getLong("sample_size");
      bean.lastAnalyzed = rs.getTimestamp("last_analyzed");
      table.indexes.put(bean.name, bean);
      return bean;
    }
  };

  private static class SizeRowMapper implements RowMapper<Object> {

    private Table table;

    public SizeRowMapper(Table table) {
      this.table = table;
      table.dataSizeMb = 0.0;
      table.indexSizeMb = 0.0;
      table.lobSizeMb = 0.0;
    }

    @Override
    public Object mapRow(ResultSet rs, int i) throws SQLException {
      String type = rs.getString("type");
      String indexName = rs.getString("index_name");
      Double sizeMb = rs.getDouble("sizeMb");
      switch (type) {
        case "TABLE":
          table.dataSizeMb += sizeMb;
          break;
        case "INDEX":
          table.indexSizeMb += sizeMb;
          Index index = table.indexes.get(indexName);
          index.sizeMb = sizeMb;
          break;
        default:
          table.lobSizeMb += sizeMb;
          break;
      }
      return sizeMb;
    }
  };

  private final String allSchemasSql = loadSqlStatement("all_schemas.sql");
  private final String allTablesSql = loadSqlStatement("all_tables.sql");
  private final String tableSql = loadSqlStatement("table.sql");
  private final String tableColumnsSql = loadSqlStatement("table_columns.sql");
  private final String tableIndexesSql = loadSqlStatement("table_indexes.sql");
  private final String tableSizeSql = loadSqlStatement("table_size.sql");

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

  @Transactional(readOnly = true)
  public Table getFullTable(String owner, String tableName) {
    Table table = jdbc.queryForObject(tableSql, ImmutableMap.of("owner", owner, "tableName", tableName), TABLE_ROW_MAPPER);
    jdbc.query(tableColumnsSql, ImmutableMap.of("owner", owner, "tableName", tableName), new TableColumnRowMapper(table));
    jdbc.query(tableIndexesSql, ImmutableMap.of("owner", owner, "tableName", tableName), new IndexColumnRowMapper(table));
    jdbc.query(tableSizeSql, ImmutableMap.of("owner", owner, "tableName", tableName), new SizeRowMapper(table));
    return table;
  }

}
