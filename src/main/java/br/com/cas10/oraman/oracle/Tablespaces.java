package br.com.cas10.oraman.oracle;

import br.com.cas10.oraman.oracle.data.TablespaceUsage;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Tablespaces {

  private static final RowMapper<TablespaceUsage> TBLSPACE_USAGE_ROW_MAPPER = (rs, rownum) -> {
    TablespaceUsage bean = new TablespaceUsage();
    bean.tablespace = rs.getString("tablespace");
    bean.usedMb = rs.getLong("usedMb");
    bean.freeMb = rs.getLong("freeMb");
    bean.totalMb = rs.getLong("totalMb");
    return bean;
  };

  private final String tablespaceUsageSql;

  @Autowired
  public Tablespaces(SqlFileLoader loader) {
    tablespaceUsageSql = loader.load("tablespaces_usage.sql");
  }

  @Autowired
  private NamedParameterJdbcTemplate jdbc;

  @Transactional(readOnly = true)
  public List<TablespaceUsage> getUsage() {
    return jdbc.query(tablespaceUsageSql, ImmutableMap.of(), TBLSPACE_USAGE_ROW_MAPPER);
  }
}
