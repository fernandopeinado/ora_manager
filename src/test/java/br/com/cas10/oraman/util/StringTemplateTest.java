package br.com.cas10.oraman.util;

import static br.com.cas10.oraman.util.StringTemplate.render;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import br.com.cas10.oraman.util.StringTemplate.StringTemplateException;
import org.junit.Test;

public class StringTemplateTest {

  @Test
  public void test() {
    StringTemplate.Data data;

    data = singletonMap("var", "Hello!")::get;
    assertEquals("Hello!", render("{{var}}", data));
    assertEquals("Hello!", render("{{ var }}", data));
    assertEquals("{{ var", render("{{ var", data));
    assertSame("var", render("var", data));

    data = singletonMap("var", "Hello")::get;
    assertEquals("Hello World!", render("{{ var }} World!", data));

    data = singletonMap("var", "World")::get;
    assertEquals("Hello World!", render("Hello {{ var }}!", data));
    assertEquals("Hello\nWorld!", render("Hello\n{{ var }}!", data));
  }

  @Test(expected = StringTemplateException.class)
  public void testVariableValueMissing() {
    assertEquals("{{ var }}", render("{{ var }}", key -> null));
  }
}
