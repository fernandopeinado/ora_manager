package br.com.cas10.oraman.oracle;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.oracle.data.GlobalSession;
import br.com.cas10.oraman.oracle.data.LockedObject;
import br.com.cas10.oraman.oracle.data.Session;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Sessions {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sessions.class);

  private static final RowMapper<SessionBean> SESSION_ROW_MAPPER = (rs, rownum) -> {
    SessionBean bean = new SessionBean();
    bean.session.sid = rs.getLong("sid");
    bean.session.serialNumber = rs.getLong("serial#");
    bean.session.username = rs.getString("username");
    bean.session.program = rs.getString("program");
    bean.isBlocked = "VALID".equals(rs.getString("blocking_session_status"));
    bean.blockingInstance = rs.getLong("blocking_instance");
    bean.blockingSession = rs.getLong("blocking_session");
    return bean;
  };

  private final String alterSystemPrivilegeSql;
  private final String allSessionsSql;
  private final String activeSessionsSql;
  private final String lockedObjectsSql;
  private final String sessionBySidSql;
  private final String sessionBySidAndSerialSql;
  private final String globalSessionsByInstanceAndSidSql;
  private final String killSessionSql;

  @Autowired
  @VisibleForTesting
  NamedParameterJdbcTemplate jdbc;

  @VisibleForTesting
  boolean sessionTerminationEnabled;

  @Autowired
  public Sessions(SqlFileLoader loader) {
    alterSystemPrivilegeSql = loader.load("alter_system_privilege.sql");
    allSessionsSql = loader.load("all_sessions.sql");
    activeSessionsSql = loader.load("active_sessions.sql");
    lockedObjectsSql = loader.load("locked_objects.sql");
    sessionBySidSql = loader.load("session_by_sid.sql");
    sessionBySidAndSerialSql = loader.load("session_by_sid_and_serial.sql");
    globalSessionsByInstanceAndSidSql = loader.load("global_sessions_by_instance_and_sid.sql");
    killSessionSql = loader.load("kill_session.sql");
  }

  @PostConstruct
  private void init() {
    Integer alterSystemPrivilegeResult =
        jdbc.getJdbcOperations().queryForObject(alterSystemPrivilegeSql, Integer.class);
    sessionTerminationEnabled = alterSystemPrivilegeResult > 0;
  }

  @Transactional(readOnly = true)
  public Session getSession(long sessionId, long serialNumber) {
    List<SessionBean> list = jdbc.query(sessionBySidAndSerialSql,
        ImmutableMap.of("sid", sessionId, "serialNumber", serialNumber), SESSION_ROW_MAPPER);
    return convert(DataAccessUtils.singleResult(list));
  }

  @Transactional(readOnly = true)
  public List<Session> getSessions(long sessionId) {
    List<SessionBean> list =
        jdbc.query(sessionBySidSql, ImmutableMap.of("sid", sessionId), SESSION_ROW_MAPPER);
    return list.stream().map(this::convert).collect(toList());
  }

  private Session convert(SessionBean bean) {
    if (bean == null) {
      return null;
    }
    Session session = bean.session;
    if (bean.isBlocked) {
      session.blockingSession = getGlobalSession(bean.blockingInstance, bean.blockingSession);
    }
    session.lockedObjects = getLockedObjects(session.sid);
    return session;
  }

  private GlobalSession getGlobalSession(long instanceNumber, long sessionId) {
    return jdbc.queryForObject(globalSessionsByInstanceAndSidSql,
        ImmutableMap.of("instance", instanceNumber, "sid", sessionId), (rs, rowNum) -> {
          GlobalSession session = new GlobalSession();
          session.instanceNumber = instanceNumber;
          session.sid = sessionId;
          session.serialNumber = rs.getLong("serial#");
          session.username = rs.getString("username");
          session.program = rs.getString("program");
          return session;
        });
  }

  private List<LockedObject> getLockedObjects(long sessionId) {
    return jdbc.query(lockedObjectsSql, ImmutableMap.of("sid", sessionId), (rs, rowNum) -> {
      LockedObject lo = new LockedObject();
      lo.owner = rs.getString("owner");
      lo.name = rs.getString("object_name");
      lo.type = rs.getString("object_type");
      lo.lockMode = LockMode.valueOf(rs.getInt("locked_mode")).getLabel();
      return lo;
    });
  }

  @Transactional(readOnly = true)
  public List<ActiveSession> getActiveSessions() {
    return jdbc.query(activeSessionsSql, (rs, rowNum) -> {
      ActiveSession s = new ActiveSession();
      s.sid = rs.getString("sid").intern();
      s.serialNumber = rs.getString("serial#").intern();
      s.username = nullSafeIntern(rs.getString("username"));
      s.program = nullSafeIntern(rs.getString("program"));
      s.sqlId = nullSafeIntern(rs.getString("sql_id"));
      s.sqlChildNumber = nullSafeIntern(rs.getString("sql_child_number"));
      s.event = rs.getString("event").intern();
      s.waitClass = rs.getString("wait_class").intern();
      return s;
    });
  }

  @Transactional(readOnly = true)
  public List<ActiveSession> getAllSessions() {
    return jdbc.query(allSessionsSql, (rs, rowNum) -> {
      ActiveSession s = new ActiveSession();
      s.sid = rs.getString("sid").intern();
      s.serialNumber = rs.getString("serial#").intern();
      s.username = nullSafeIntern(rs.getString("username"));
      s.program = nullSafeIntern(rs.getString("program"));
      s.sqlId = nullSafeIntern(rs.getString("sql_id"));
      s.sqlChildNumber = nullSafeIntern(rs.getString("sql_child_number"));
      s.event = rs.getString("event").intern();
      s.waitClass = rs.getString("wait_class").intern();
      return s;
    });
  }

  private static String nullSafeIntern(String string) {
    return string == null ? null : string.intern();
  }

  public void killSession(long sessionId, long serialNumber) {
    checkArgument(sessionId >= 0);
    checkArgument(serialNumber >= 0);

    if (sessionTerminationEnabled) {
      jdbc.getJdbcOperations().execute(
          killSessionSql.replace(":fullSid", String.format("'%d,%d'", sessionId, serialNumber)));
      LOGGER.info(String.format("Session killed: %d (SID), %d (Serial#)", sessionId, serialNumber));
    }
  }

  public boolean sessionTerminationEnabled() {
    return sessionTerminationEnabled;
  }

  private static class SessionBean {
    final Session session = new Session();
    boolean isBlocked;
    long blockingInstance;
    long blockingSession;
  }
}
