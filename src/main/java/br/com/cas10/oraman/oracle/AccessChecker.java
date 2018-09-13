package br.com.cas10.oraman.oracle;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccessChecker {

  private static final Logger logger = LoggerFactory.getLogger(AccessChecker.class);

  private static final String QUERYABLE_TEMPLATE = "select 1 from %s where rownum = 0";

  @Autowired
  private JdbcTemplate jdbc;
  @Autowired
  private ObjectMappings mappings;

  private Map<OracleObject, Boolean> queryable;

  @PostConstruct
  private void init() {
    List<String> notAccessible = new ArrayList<>();
    EnumMap<OracleObject, Boolean> queryableTemp = new EnumMap<>(OracleObject.class);
    for (OracleObject object : OracleObject.values()) {
      String testQuery = String.format(QUERYABLE_TEMPLATE, mappings.lookup(object.name));
      try {
        jdbc.queryForList(testQuery, Integer.class);
        queryableTemp.put(object, true);
      } catch (Exception e) {
        queryableTemp.put(object, false);
        notAccessible.add(object.name);
      }
    }
    queryable = ImmutableMap.copyOf(queryableTemp);
    if (!notAccessible.isEmpty()) {
      logger.warn("Not accessible: {}. Some features will not be available", notAccessible);
    }
  }

  /**
   * Checks if the application can execute SELECT statements on the specified object.
   *
   * @param object an object (table, view, synonym, etc.).
   * @return if the object can be accessed.
   */
  public boolean isQueryable(OracleObject object) {
    return queryable.get(checkNotNull(object));
  }
}
