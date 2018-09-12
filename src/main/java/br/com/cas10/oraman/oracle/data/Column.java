package br.com.cas10.oraman.oracle.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class Column {

  public String name;
  public String dataType;
  public Long length;
  public Long precision;
  public Long scale;
  public Boolean nullable;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  public Date lastAnalyzed;

}
