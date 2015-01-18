package br.com.cas10.oraman.oracle;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSystem {

  @Autowired
  @Qualifier("monitoring")
  private NamedParameterJdbcTemplate jdbc;

  private int cpuCores;
  private int cpuThreads;
  private long instanceNumber;

  @PostConstruct
  private void init() {
    instanceNumber =
        jdbc.getJdbcOperations().queryForObject("select instance_number from v$instance",
            Long.class);

    int xeQueryResult =
        jdbc.getJdbcOperations().queryForObject(
            "select count(1) from v$version where banner like 'Oracle Database%Express Edition%'",
            Integer.class);
    boolean expressEdition = xeQueryResult > 0;

    if (expressEdition) {
      cpuCores = 1;
      cpuThreads = 1;
    } else {
      cpuCores =
          jdbc.getJdbcOperations().queryForObject(
              "select value from v$osstat where stat_name = 'NUM_CPU_CORES'", Integer.class);
      cpuThreads =
          jdbc.getJdbcOperations().queryForObject(
              "select value from v$osstat where stat_name = 'NUM_CPUS'", Integer.class);
    }
  }

  /**
   * Returns the number of CPU cores available (the value of {@code NUM_CPU_CORES} on
   * {@code v$osstat}). On Express Edition databases, returns {@code 1}.
   *
   * @return the number of CPU cores available.
   */
  public int getCpuCores() {
    return cpuCores;
  }

  /**
   * Returns the number of CPUs available (the value of {@code NUM_CPUS} on {@code v$osstat}). On
   * Express Edition databases, returns {@code 1}.
   *
   * @return the number of CPUs available.
   */
  public int getCpuThreads() {
    return cpuThreads;
  }

  /**
   * @return the ID of the monitored instance, retrieved from {@code v$instance} (column
   *         {@code INSTANCE_NUMBER}).
   */
  public long getInstanceNumber() {
    return instanceNumber;
  }
}
