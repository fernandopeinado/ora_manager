package br.com.cas10.oraman;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({"br.com.cas10.oraman.agent", "br.com.cas10.oraman.oracle"})
@EnableTransactionManagement(proxyTargetClass = true)
class RootConfig {

  @Bean
  DataSource oramanDataSource() throws NamingException {
    return new JndiTemplate().lookup("java:comp/env/jdbc/oraman", DataSource.class);
  }

  @Bean
  DataSourceTransactionManager oramanTransactionManager() throws NamingException {
    return new DataSourceTransactionManager(oramanDataSource());
  }

  @Bean
  NamedParameterJdbcTemplate oramanJdbc() throws NamingException {
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
