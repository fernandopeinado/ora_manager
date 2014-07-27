package br.com.cas10.oraman.worker

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import br.com.cas10.oraman.service.AshArchive

@Component
@CompileStatic
class AshArchiveWorker extends Worker {

	@Autowired
	private AshArchive archive

	AshArchiveWorker() {
		super("0 0 * * * *")
	}

	@Override
	void run() {
		archive.removeOldFiles()
	}
}