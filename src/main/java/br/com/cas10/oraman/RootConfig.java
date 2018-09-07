package br.com.cas10.oraman;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({"br.com.cas10.oraman.agent", "br.com.cas10.oraman.oracle"})
@EnableTransactionManagement(proxyTargetClass = true)
class RootConfig {

  @Bean
  DataSource oramanDataSource() {
    String jdbcUrl = System.getProperty("oraman.oracle.jdbc.url");
    String jdbcUsername = System.getProperty("oraman.oracle.jdbc.username");
    String jdbcPassword = System.getProperty("oraman.oracle.jdbc.password");

    HikariConfig config = new HikariConfig();
    config.setPoolName("oraman");
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(jdbcUsername);
    config.setPassword(jdbcPassword);
    config.setMinimumIdle(1);
    config.setMaximumPoolSize(5);
    config.setReadOnly(true);
    config.addDataSourceProperty("v$session.program", "OraManager");
    return new TransactionAwareDataSourceProxy(new HikariDataSource(config));
  }

  @Bean
  DataSourceTransactionManager oramanTransactionManager() {
    return new DataSourceTransactionManager(oramanDataSource());
  }

  @Bean
  NamedParameterJdbcTemplate oramanJdbc() {
    return new NamedParameterJdbcTemplate(oramanDataSource());
  }

  @Bean
  @Primary
  TaskScheduler defaultTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(2);
    return taskScheduler;
  }
}
