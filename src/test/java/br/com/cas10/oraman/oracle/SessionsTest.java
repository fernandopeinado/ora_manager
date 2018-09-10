package br.com.cas10.oraman.oracle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import br.com.cas10.oraman.OramanProperties;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SessionsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testKillSessionInvalidSid() {
    Sessions sessions = newSessions();
    sessions.killSession(-1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testKillSessionInvalidSerialNumber() {
    Sessions sessions = newSessions();
    sessions.killSession(0, -1);
  }

  @Test
  public void testKillSessionCommand() {
    NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
    JdbcOperations operations = mock(JdbcOperations.class);
    when(template.getJdbcOperations()).thenReturn(operations);

    Sessions sessions = newSessions();
    sessions.jdbc = template;
    sessions.sessionTerminationEnabled = true;

    sessions.killSession(1, 2);

    verify(operations).execute("alter system kill session '1,2' immediate");
  }

  private static Sessions newSessions() {
    SqlFileLoader loader = new SqlFileLoader();
    setField(loader, "mappings", new ObjectMappings(new OramanProperties()));
    return new Sessions(loader);
  }
}
