package br.com.cas10.oraman.oracle.data;

import java.io.Serializable;

public class ActiveSession implements Serializable {

  private static final long serialVersionUID = 2282212278946348587L;

  public String sid;
  public String serialNumber;
  public String username;
  public String program;
  public String sqlId;
  public String sqlChildNumber;
  public String event;
  public String waitClass;

}
