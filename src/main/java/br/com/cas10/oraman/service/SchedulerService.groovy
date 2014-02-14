package br.com.cas10.oraman.service


import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;

import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import br.com.cas10.oraman.worker.Worker
import br.com.cas10.oraman.worker.WorkerJob

@Service
class SchedulerService {

	private Map<String, Worker> workers
	
	@Autowired
	private Scheduler scheduler

	@Autowired(required = false)
	void setWorkers(List<Worker> workers) {
		this.workers = new HashMap<String, Worker>()
		for (Worker worker in workers) {
			this.workers.put(worker.type, worker)
			schedule(worker)
		}
	}
	
	List<Worker> getWorkers() {
		new ArrayList<Worker>(workers.values())
	}
	
	private void schedule(Worker worker) {
		JobDetail job = newJob(WorkerJob.class)
				.withIdentity(worker.type, "workers")
				.usingJobData("workerType", worker.type)
				.build()
		Trigger trigger = newTrigger()
				.withIdentity("${worker.type}Trigger", "workers")
				.startNow()
				.withSchedule(cronSchedule(worker.cron))
				.build()
		scheduler.scheduleJob(job, trigger)
	}
	
	public void doWork(String type) {
		Worker worker = workers[type]
		if (worker) {
			worker.run()
		}
	}
}
