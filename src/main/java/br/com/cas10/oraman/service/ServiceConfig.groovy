package br.com.cas10.oraman.service

import javax.naming.NamingException
import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jndi.JndiTemplate
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

import br.com.cas10.oraman.agent.AgentConfig
import br.com.cas10.oraman.worker.WorkerConfig

@Configuration
@ComponentScan(basePackages=['br.com.cas10.oraman.service'])
@Import([ AgentConfig, WorkerConfig])
class ServiceConfig {

  @Bean
  @Qualifier('admin')
  DataSource adminDataSource() {
    JndiTemplate jndi = new JndiTemplate()
    try {
      return jndi.lookup('java:comp/env/jdbc/oramanAdmin', DataSource)
    } catch (NamingException e) {
      return null
    }
  }

  @Bean
  @Qualifier('agents')
  ThreadPoolTaskScheduler agentsTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler()
    int threads = Math.ceil(Runtime.runtime.availableProcessors() / 2.0d)
    taskScheduler.setPoolSize(threads)
    return taskScheduler
  }

  @Bean
  @Qualifier('workers')
  ThreadPoolTaskScheduler workersTaskScheduler() {
    new ThreadPoolTaskScheduler()
  }
}
