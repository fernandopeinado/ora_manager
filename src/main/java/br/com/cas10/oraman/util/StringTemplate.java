package br.com.cas10.oraman.util;

import com.google.common.annotations.VisibleForTesting;

/**
 * Simple string templating engine.
 *
 * <p>Replaces variables like <code>{{ myVariable }}</code>.
 */
public class StringTemplate {

  private enum State {
    COPY, LEFT, VAR, RIGHT;
  }

  public interface Data {

    String getValue(String variableName);
  }

  public static String render(String template, Data data) {
    if (!template.contains("{{")) {
      return template;
    }

    StringBuilder out = new StringBuilder(template.length());
    State state = State.COPY;
    int variableStart = -1;

    for (int i = 0; i < template.length(); i++) {
      char c = template.charAt(i);
      switch (state) {
        case COPY:
          if (c == '{') {
            variableStart = i;
            state = State.LEFT;
          } else {
            out.append(c);
          }
          break;
        case LEFT:
          if (c == '{') {
            state = State.VAR;
          } else {
            copy(template, variableStart, i + 1, out);
            state = State.COPY;
          }
          break;
        case VAR:
          if (c == '}') {
            state = State.RIGHT;
          }
          break;
        case RIGHT:
          if (c == '}') {
            String variableName = template.substring(variableStart + 2, i - 1).trim();
            String value = data.getValue(variableName);
            if (value == null) {
              throw new StringTemplateException(variableName, template);
            }
            out.append(value);
          } else {
            copy(template, variableStart, i + 1, out);
          }
          state = State.COPY;
          break;
        default:
          throw new AssertionError();
      }
    }
    if (state != State.COPY) {
      copy(template, variableStart, template.length(), out);
    }
    return out.toString();
  }

  private static void copy(String src, int start, int end, StringBuilder dest) {
    for (int i = start; i < end; i++) {
      dest.append(src.charAt(i));
    }
  }

  @VisibleForTesting
  static class StringTemplateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    StringTemplateException(String variable, String template) {
      super("Value is null or missing: " + variable + ". Template: " + template);
    }
  }
}
