package br.com.cas10.oraman.oracle;

public enum OracleObject {

  V_EVENT_NAME("v$event_name");

  final String name;

  OracleObject(String name) {
    this.name = name;
  }
}
