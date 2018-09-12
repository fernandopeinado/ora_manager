package br.com.cas10.oraman.oracle;

public enum OracleObject {

  DBA_DATA_FILES("dba_data_files"),

  DBA_FREE_SPACE("dba_free_space"),

  DBA_IND_COLUMNS("dba_ind_columns"),

  DBA_INDEXES("dba_indexes"),

  DBA_LOBS("dba_lobs"),

  DBA_OBJECTS("dba_objects"),

  DBA_SEGMENTS("dba_segments"),

  DBA_TAB_COLUMNS("dba_tab_columns"),

  DBA_TABLES("dba_tables"),

  DBA_USERS("dba_users"),

  V_EVENT_NAME("v$event_name"),

  V_SQLCOMMAND("v$sqlcommand"),

  V_SYS_TIME_MODEL("v$sys_time_model"),

  V_SYSTEM_EVENT("v$system_event");

  public final String name;

  OracleObject(String name) {
    this.name = name;
  }
}
