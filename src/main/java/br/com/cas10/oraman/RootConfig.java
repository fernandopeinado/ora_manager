package br.com.cas10.oraman;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
class RootConfig {

  @Value("${oraman.oracle.jdbc.url}")
  private String jdbcUrl;
  @Value("${oraman.oracle.jdbc.username}")
  private String jdbcUsername;
  @Value("${oraman.oracle.jdbc.password}")
  private String jdbcPassword;

  @Bean
  DataSource oramanDataSource() {
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
  @Primary
  TaskScheduler defaultTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(2);
    return taskScheduler;
  }

  @Bean
  @Qualifier("ash")
  TaskScheduler ashTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(1);
    return taskScheduler;
  }
}
