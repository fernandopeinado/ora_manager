package br.com.cas10.oraman.oracle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

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
    JdbcTemplate template = mock(JdbcTemplate.class);
    Sessions sessions = new Sessions();
    setField(sessions, "adminJdbc", template);

    sessions.killSession(1, 2);

    verify(template).execute("alter system kill session '1,2' immediate");
  }

  @Test
  public void testSessionTerminationEnabled() {
    Sessions sessions = new Sessions();
    assertFalse(sessions.sessionTerminationEnabled());
    setField(sessions, "adminJdbc", mock(JdbcTemplate.class));
    assertTrue(sessions.sessionTerminationEnabled());
  }
}
