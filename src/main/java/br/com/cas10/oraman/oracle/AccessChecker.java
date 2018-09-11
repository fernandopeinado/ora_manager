package br.com.cas10.oraman.oracle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessChecker {

  private static final Logger logger = LoggerFactory.getLogger(AccessChecker.class);

  private static final String QUERYABLE_TEMPLATE = "select 1 from %s where rownum = 0";

  @Autowired
  private JdbcTemplate jdbc;
  @Autowired
  private ObjectMappings mappings;

  private final ConcurrentHashMap<String, Boolean> queryableCache = new ConcurrentHashMap<>();

  /**
   * Checks if the application can execute SELECT statements on the specified object.
   *
   * @param object an object (table, view, synonym, etc.).
   * @return if the object can be accessed.
   */
  @Transactional(readOnly = true)
  public boolean isQueryable(OracleObject object) {
    checkNotNull(object);

    String cacheKey = object.name.toLowerCase();
    Boolean queryable = queryableCache.get(cacheKey);
    if (queryable != null) {
      return queryable;
    }

    String testQuery = String.format(QUERYABLE_TEMPLATE, mappings.lookup(object.name));
    try {
      jdbc.queryForList(testQuery, Integer.class);
      queryable = true;
    } catch (Exception e) {
      logger.debug("Access check failed for object: " + object.name, e);
      queryable = false;
    }
    queryableCache.put(cacheKey, queryable);
    return queryable;
  }
}
