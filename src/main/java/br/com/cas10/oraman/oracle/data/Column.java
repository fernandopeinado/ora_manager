/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.cas10.oraman.oracle.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 *
 * @author kasten
 */
public class Column {

  public String name;
  public String dataType;
  public Long id;
  public Long length;
  public Long precision;
  public Long scale;
  public Boolean nullable;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  public Date lastAnalyzed;
}
