package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.OracleObject.DBA_DATA_FILES;
import static br.com.cas10.oraman.oracle.OracleObject.DBA_FREE_SPACE;

import br.com.cas10.oraman.oracle.data.TablespaceUsage;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Tablespaces {

  private static final Logger logger = LoggerFactory.getLogger(Tablespaces.class);

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
  private AccessChecker accessChecker;
  @Autowired
  private JdbcTemplate jdbc;

  private boolean requiredObjectsAccessible;

  @Autowired
  public Tablespaces(SqlFileLoader loader) {
    tablespaceUsageSql = loader.load("tablespaces_usage.sql");
  }

  @PostConstruct
  private void init() {
    List<String> notAccessible = new ArrayList<>();
    if (!accessChecker.isQueryable(DBA_DATA_FILES)) {
      notAccessible.add(DBA_DATA_FILES.name);
    }
    if (!accessChecker.isQueryable(DBA_FREE_SPACE)) {
      notAccessible.add(DBA_FREE_SPACE.name);
    }

    requiredObjectsAccessible = notAccessible.isEmpty();

    if (!notAccessible.isEmpty()) {
      logger.warn("Not accessible: {}", Joiner.on(", ").join(notAccessible));
    }
  }

  @Transactional(readOnly = true)
  public List<TablespaceUsage> getUsage() {
    if (!requiredObjectsAccessible) {
      return ImmutableList.of();
    }
    return jdbc.query(tablespaceUsageSql, TBLSPACE_USAGE_ROW_MAPPER);
  }
}
