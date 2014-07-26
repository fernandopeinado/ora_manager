package br.com.cas10.oraman.agent

import groovy.transform.CompileStatic
import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.analitics.Snapshots

@CompileStatic
abstract class Agent implements Runnable {

	final long interval
	protected final Snapshots snapshots

	Agent(long interval, int storageSize) {
		this.interval = interval
		this.snapshots = new Snapshots(storageSize)
	}

	public <T extends Snapshot> List<T> getData() {
		snapshots.asList()
	}
}
