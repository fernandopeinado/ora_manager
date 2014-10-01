package br.com.cas10.oraman;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import br.com.cas10.oraman.service.ServiceConfig;

@Configuration
@Import(ServiceConfig.class)
@ComponentScan({"br.com.cas10.oraman.agent", "br.com.cas10.oraman.oracle"})
@EnableTransactionManagement(proxyTargetClass = true)
class RootConfig {

  @Bean
  DataSource monitoringDataSource() throws NamingException {
    return new JndiTemplate().lookup("java:comp/env/jdbc/oraman", DataSource.class);
  }

  @Bean
  DataSourceTransactionManager monitoringTransactionManager() throws NamingException {
    return new DataSourceTransactionManager(monitoringDataSource());
  }

  @Bean
  @Qualifier("monitoring")
  NamedParameterJdbcTemplate monitoringJdbc() throws NamingException {
    return new NamedParameterJdbcTemplate(monitoringDataSource());
  }

  @Bean
  DataSource adminDataSource() {
    try {
      return new JndiTemplate().lookup("java:comp/env/jdbc/oramanAdmin", DataSource.class);
    } catch (NamingException e) {
      return null;
    }
  }

  @Bean
  @Qualifier("admin")
  JdbcTemplate adminJdbc() throws NamingException {
    DataSource dataSource = adminDataSource();
    return dataSource == null ? null : new JdbcTemplate(dataSource);
  }

  @Bean
  @Primary
  TaskScheduler defaultTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(1);
    return taskScheduler;
  }
}
