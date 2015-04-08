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
class Index {

  public Table table;
  public String name;
  public Boolean unique;
  public String tablespace;
  public Boolean logging;
  public Long blevel;
  public Long leafBlocks;
  public Long distinctKeys;
  public Long rows;
  public Long sampleSize;
  public Date lastAnalyzed;
  
  public Long sizeMb;
}
