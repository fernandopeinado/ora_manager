/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.cas10.oraman.oracle.data;

import java.util.Date;
import java.util.List;

/**
 *
 * @author kasten
 */
public class Table {

  public String owner;
  public String name;
  public String tablespace;
  public Boolean logging;
  public Long rows;
  public Long avgRowLength;
  public Long sampleSize;
  public Date lastAnalyzed;

  public List<Column> columns;
  public List<Index> indexes;

  public Long dataSizeMb;
  public Long lobSizeMb;
  public Long indexSizeMb;

}
