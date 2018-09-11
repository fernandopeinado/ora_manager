package br.com.cas10.oraman.oracle;

public enum OracleObject {

  V_EVENT_NAME("v$event_name"),

  V_SYS_TIME_MODEL("v$sys_time_model"),

  V_SYSTEM_EVENT("v$system_event");

  public final String name;

  OracleObject(String name) {
    this.name = name;
  }
}
