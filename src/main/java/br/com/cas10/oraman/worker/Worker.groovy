package br.com.cas10.oraman.worker

import groovy.util.logging.Log4j;

import javax.annotation.PostConstruct;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.cas10.oraman.analitics.CircularList;
import br.com.cas10.oraman.analitics.Snapshot;
import br.com.cas10.oraman.analitics.Snapshots;

@Log4j
abstract class Worker implements Runnable {
	protected String type
	protected String cron
	
	Worker(String type, String cron) {
		this.type = type
		this.cron = cron
	}
	
	@PostConstruct
	private void initialize() {
		log.info("Worker $type - $cron starting")
	}
	
	@Override
	void run() {
		
	}
	
	void clean() {
		
	}
}
