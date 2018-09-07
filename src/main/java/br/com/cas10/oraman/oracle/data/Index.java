package br.com.cas10.oraman.oracle.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Index {

  public String name;
  public Boolean unique;
  public String tablespace;
  public Boolean logging;
  public Long blevel;
  public Long leafBlocks;
  public Long distinctKeys;
  public Long rows;
  public Long sampleSize;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  public Date lastAnalyzed;
  public List<String> columns = new ArrayList<>();
  public Double sizeMb;

}
