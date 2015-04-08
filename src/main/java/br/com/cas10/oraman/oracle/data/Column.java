/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.cas10.oraman.oracle.data;

import java.util.Date;

/**
 *
 * @author kasten
 */
public class Column {

  public Table table;
  public String name;
  public String dataType;
  public Long id;
  public Long length;
  public Long precision;
  public Long scale;
  public Boolean nullable;
  public Date lastAnalyzed;
}
