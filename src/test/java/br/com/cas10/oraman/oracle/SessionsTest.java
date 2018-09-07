package br.com.cas10.oraman.oracle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SessionsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testKillSessionInvalidSid() {
    Sessions sessions = new Sessions();
    sessions.killSession(-1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testKillSessionInvalidSerialNumber() {
    Sessions sessions = new Sessions();
    sessions.killSession(0, -1);
  }

  @Test
  public void testKillSessionCommand() {
    NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
    JdbcOperations operations = mock(JdbcOperations.class);
    when(template.getJdbcOperations()).thenReturn(operations);

    Sessions sessions = new Sessions();
    sessions.jdbc = template;
    sessions.sessionTerminationEnabled = true;

    sessions.killSession(1, 2);

    verify(operations).execute("alter system kill session '1,2' immediate");
  }
}
