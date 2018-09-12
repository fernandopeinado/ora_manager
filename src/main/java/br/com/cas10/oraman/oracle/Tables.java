package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.OracleObject.DBA_INDEXES;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_IND_COLUMNS;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_LOBS;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_SEGMENTS;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_TABLES;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_TAB_COLUMNS;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_USERS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import br.com.cas10.oraman.oracle.data.Column;
import br.com.cas10.oraman.oracle.data.Index;
import br.com.cas10.oraman.oracle.data.Table;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Tables {

  private static final Logger logger = LoggerFactory.getLogger(Tables.class);

  private static final RowMapper<Table> tableRowMapper = (rs, rowNum) -> {
    Table bean = new Table();
    bean.owner = rs.getString("owner");
    bean.name = rs.getString("table_name");
    bean.tablespace = rs.getString("tablespace_name");
    bean.rows = rs.getLong("num_rows");
    bean.lastAnalyzed = rs.getDate("last_analyzed");
    return bean;
  };

  private static final RowMapper<Table> fullTableRowMapper = (rs, rowNum) -> {
    Table bean = tableRowMapper.mapRow(rs, rowNum);
    bean.dataSizeMb = rs.getDouble("table_bytes") / 1024 / 1024;
    bean.lobSizeMb = rs.getDouble("lob_bytes") / 1024 / 1024;
    bean.indexSizeMb = rs.getDouble("index_bytes") / 1024 / 1024;
    return bean;
  };

  private static final RowMapper<Column> columnRowMapper = (rs, rowNum) -> {
    Column bean = new Column();
    bean.name = rs.getString("column_name");
    bean.dataType = rs.getString("data_type");
    bean.length = rs.getLong("data_length");
    bean.precision = rs.getLong("data_precision");
    bean.scale = rs.getLong("data_scale");
    bean.nullable = "Y".equalsIgnoreCase(rs.getString("nullable"));
    bean.lastAnalyzed = rs.getTimestamp("last_analyzed");
    return bean;
  };

  private static final RowMapper<Index> indexRowMapper = (rs, rowNum) -> {
    Index bean = new Index();
    bean.name = rs.getString("index_name");
    bean.columns = ImmutableList.copyOf(rs.getString("columns").split(","));
    bean.unique = "UNIQUE".equalsIgnoreCase(rs.getString("uniqueness"));
    bean.tablespace = rs.getString("tablespace_name");
    bean.logging = "YES".equalsIgnoreCase(rs.getString("logging"));
    bean.blevel = rs.getLong("blevel");
    bean.leafBlocks = rs.getLong("leaf_blocks");
    bean.distinctKeys = rs.getLong("distinct_keys");
    bean.rows = rs.getLong("num_rows");
    bean.sampleSize = rs.getLong("sample_size");
    bean.lastAnalyzed = rs.getTimestamp("last_analyzed");
    bean.sizeMb = rs.getDouble("bytes") / 1024 / 1024;
    return bean;
  };

  private final String allSchemasSql;
  private final String allTablesSql;
  private final String tableSql;
  private final String tableColumnsSql;
  private final String tableIndexesSql;

  @Autowired
  private AccessChecker accessChecker;
  @Autowired
  private NamedParameterJdbcTemplate jdbc;

  private boolean requiredObjectsAccessible;

  @Autowired
  public Tables(SqlFileLoader loader) {
    allSchemasSql = loader.load("all_schemas.sql");
    allTablesSql = loader.load("all_tables.sql");
    tableSql = loader.load("table.sql");
    tableColumnsSql = loader.load("table_columns.sql");
    tableIndexesSql = loader.load("table_indexes.sql");
  }

  @PostConstruct
  private void init() {
    List<OracleObject> requiredObjects = asList(DBA_IND_COLUMNS, DBA_INDEXES, DBA_LOBS,
        DBA_SEGMENTS, DBA_TAB_COLUMNS, DBA_TABLES, DBA_USERS);

    Predicate<OracleObject> accessiblePredicate = accessChecker::isQueryable;
    List<OracleObject> notAccessible =
        requiredObjects.stream().filter(accessiblePredicate.negate()).collect(toList());

    requiredObjectsAccessible = notAccessible.isEmpty();
    if (!notAccessible.isEmpty()) {
      logger.warn("Not accessible: {}", Joiner.on(", ").join(notAccessible));
    }
  }

  @Transactional(readOnly = true)
  public List<String> getSchemas() {
    if (!requiredObjectsAccessible) {
      return ImmutableList.of();
    }
    return jdbc.getJdbcOperations().queryForList(allSchemasSql, String.class);
  }

  @Transactional(readOnly = true)
  public List<Table> getTables(String owner) {
    return jdbc.query(allTablesSql, ImmutableMap.of("owner", owner), tableRowMapper);
  }

  @Transactional(readOnly = true)
  public Table getFullTable(String owner, String tableName) {
    Map<String, ?> params = ImmutableMap.of("owner", owner, "tableName", tableName);

    Table table = jdbc.queryForObject(tableSql, params, fullTableRowMapper);
    jdbc.query(tableColumnsSql, params, columnRowMapper).stream()
        .forEach(c -> table.columns.put(c.name, c));
    jdbc.query(tableIndexesSql, params, indexRowMapper).stream()
        .forEach(i -> table.indexes.put(i.name, i));
    return table;
  }
}
