package br.com.cas10.oraman.worker

import groovy.transform.CompileStatic

@CompileStatic
abstract class Worker implements Runnable {

	final String cronExpression

	Worker(String cronExpression) {
		this.cronExpression = cronExpression
	}
}
