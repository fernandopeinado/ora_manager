package br.com.cas10.oraman.oracle;

import static br.com.cas10.oraman.oracle.OracleObject.V_OSSTAT;
import static com.google.common.collect.Iterables.getOnlyElement;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSystem {

  private final String instanceNumberSql;
  private final String checkExpressEditionSql;
  private final String numCpuCoresSql;
  private final String numCpuThreadsSql;

  @Autowired
  private AccessChecker accessChecker;
  @Autowired
  private JdbcTemplate jdbc;

  private Integer cpuCores;
  private Integer cpuThreads;
  private long instanceNumber;

  @Autowired
  public DatabaseSystem(SqlFileLoader loader) {
    instanceNumberSql = loader.load("instance_number.sql");
    checkExpressEditionSql = loader.load("check_express_edition.sql");
    numCpuCoresSql = loader.load("num_cpu_cores.sql");
    numCpuThreadsSql = loader.load("num_cpu_threads.sql");
  }

  @PostConstruct
  private void init() {
    instanceNumber = jdbc.queryForObject(instanceNumberSql, Long.class);

    int xeQueryResult = jdbc.queryForObject(checkExpressEditionSql, Integer.class);
    boolean expressEdition = xeQueryResult > 0;

    if (expressEdition) {
      cpuCores = 1;
      cpuThreads = 1;
    } else if (accessChecker.isQueryable(V_OSSTAT)) {
      cpuThreads = jdbc.queryForObject(numCpuThreadsSql, Integer.class);
      // NUM_CPU_CORES is not always available (e.g., cloud environments)
      cpuCores = getOnlyElement(jdbc.queryForList(numCpuCoresSql, Integer.class), cpuThreads);
    }
  }

  /**
   * Returns the number of available CPU cores ({@code NUM_CPU_CORES} on {@code v$osstat}).
   *
   * <p>Special cases:
   * <ul>
   * <li>Returns 1 on XE databases</li>
   * <li>Returns the value of {@code NUM_CPUS} if {@code NUM_CPU_CORES} is not available</li>
   * <li>Returns {@code null} if {@code v$osstat} is not available</li>
   * </ul>
   */
  public Integer getCpuCores() {
    return cpuCores;
  }

  /**
   * Returns the number of available CPUs ({@code NUM_CPUS} on {@code v$osstat}).
   *
   * <p>Special cases:
   * <ul>
   * <li>Returns 1 on XE databases</li>
   * <li>Returns {@code null} if {@code v$osstat} is not available</li>
   * </ul>
   */
  public Integer getCpuThreads() {
    return cpuThreads;
  }

  /**
   * Returns the ID of the monitored instance ({@code INSTANCE_NUMBER}).
   */
  public long getInstanceNumber() {
    return instanceNumber;
  }
}
