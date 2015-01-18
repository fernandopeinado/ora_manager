package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.SqlFiles.loadSqlStatement;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.cas10.oraman.oracle.data.ActiveSession;
import br.com.cas10.oraman.oracle.data.Session;

import com.google.common.collect.ImmutableMap;

@Service
public class Sessions {

  private static final Logger LOGGER = Logger.getLogger(Sessions.class);

  private final String activeSessionsSql = loadSqlStatement("active_sessions.sql");

  @Autowired(required = false)
  @Qualifier("admin")
  private JdbcTemplate adminJdbc;
  @Autowired
  @Qualifier("monitoring")
  private NamedParameterJdbcTemplate jdbc;

  @Transactional(readOnly = true)
  public Session getSession(long sessionId, long serialNumber) {
    List<Session> list =
        jdbc.query(
            "select username, program from v$session where sid = :sid and serial# = :serialNumber",
            ImmutableMap.of("sid", sessionId, "serialNumber", serialNumber), (rs, rowNum) -> {
              Session session = new Session();
              session.sessionId = sessionId;
              session.serialNumber = serialNumber;
              session.username = rs.getString("username");
              session.program = rs.getString("program");
              return session;
            });
    return DataAccessUtils.singleResult(list);
  }

  @Transactional(readOnly = true)
  public List<Session> getSessions(long sessionId) {
    return jdbc.query("select serial#, username, program from v$session where sid = :sid",
        ImmutableMap.of("sid", sessionId), (rs, rowNum) -> {
          Session session = new Session();
          session.sessionId = sessionId;
          session.serialNumber = rs.getLong("serial#");
          session.username = rs.getString("username");
          session.program = rs.getString("program");
          return session;
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

  private static String nullSafeIntern(String aString) {
    return aString == null ? null : aString.intern();
  }

  public void killSession(long sessionId, long serialNumber) {
    checkArgument(sessionId >= 0);
    checkArgument(serialNumber >= 0);

    if (adminJdbc != null) {
      adminJdbc.execute(String.format("alter system kill session '%d,%d' immediate", sessionId,
          serialNumber));
      LOGGER.info(String.format("Session killed: %d (SID), %d (Serial#)", sessionId, serialNumber));
    }
  }

  public boolean sessionTerminationEnabled() {
    return adminJdbc != null;
  }
}
