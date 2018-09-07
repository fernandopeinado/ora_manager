package br.com.cas10.oraman.oracle.data;

public class ExecutionPlan {

  public String sqlId;
  public long planHashValue;
  public long minChild;
  public String planText;

  public long childCursors;
  public long loads;
  public long invalidations;

  public long parseCalls;
  public long executions;
  public long fetches;
  public long rowsProcessed;
  public long bufferGets;
  public long diskReads;
  public long directWrites;

  public long elapsedTime;
  public long cpuTime;
  public long applicationWaitTime;
  public long clusterWaitTime;
  public long concurrencyWaitTime;
  public long userIoWaitTime;

}
