package br.com.cas10.oraman.agent

import br.com.cas10.oraman.analitics.Snapshot
import br.com.cas10.oraman.analitics.Snapshots

abstract class Agent implements Runnable {

	final long interval
	protected final Snapshots snapshots

	Agent(long interval, int storageSize) {
		this.interval = interval
		this.snapshots = new Snapshots(storageSize)
	}

	List<Snapshot> getData() {
		snapshots.asList()
	}
}
