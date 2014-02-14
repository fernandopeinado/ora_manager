package br.com.cas10.oraman.worker

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.context.ContextLoader;

import br.com.cas10.oraman.service.SchedulerService;

@DisallowConcurrentExecution
class WorkerJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		SchedulerService schedService = ContextLoader.getCurrentWebApplicationContext().getBean(SchedulerService.class)
		schedService.doWork(context.jobDetail.jobDataMap.workerType);
	}
	
}
