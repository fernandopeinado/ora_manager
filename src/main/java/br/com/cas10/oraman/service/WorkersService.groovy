package br.com.cas10.oraman.service

import groovy.transform.CompileStatic

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger

import br.com.cas10.oraman.worker.Worker

//FIXME
//@Service
@CompileStatic
class WorkersService {

	@Autowired
	private List<Worker> workers
	@Autowired
	@Qualifier('workers')
	private TaskScheduler taskScheduler

	@PostConstruct
	void init() {
		for (worker in workers) {
			taskScheduler.schedule(worker, new CronTrigger(worker.cronExpression))
		}
	}
}