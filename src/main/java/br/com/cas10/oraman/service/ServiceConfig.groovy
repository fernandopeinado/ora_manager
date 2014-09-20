package br.com.cas10.oraman.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

import br.com.cas10.oraman.agent.AgentConfig
import br.com.cas10.oraman.worker.WorkerConfig

@Configuration
@ComponentScan(basePackages=['br.com.cas10.oraman.service'])
@Import([ AgentConfig, WorkerConfig])
class ServiceConfig {

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
