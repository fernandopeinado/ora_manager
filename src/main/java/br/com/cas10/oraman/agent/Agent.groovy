package br.com.cas10.oraman.agent

import groovy.util.logging.Log4j

import javax.annotation.PostConstruct

import br.com.cas10.oraman.analitics.CircularList
import br.com.cas10.oraman.analitics.Snapshot

@Log4j
abstract class Agent implements Runnable {

	protected String type
	protected long interval
	protected CircularList<Snapshot> snapshots

	Agent(String type, long interval, int storageSize) {
		this.type = type
		this.interval = interval
		def deltaCalculation = { last, current -> current.calculateDelta(last) }
		this.snapshots = new CircularList<Snapshot>(storageSize, deltaCalculation);
	}

	@PostConstruct
	private void initialize() {
		long time = interval * snapshots.size / 60000
		log.info("Agent $type - ${interval} milisec (${time} min) starting")
	}

	List<Snapshot> getData() {
		return snapshots.asList()
	}
}
