package br.com.cas10.oraman.service

import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import br.com.cas10.oraman.agent.AshAgent
import br.com.cas10.oraman.analitics.AshSnapshot
import br.com.cas10.oraman.analitics.Snapshot

@Service
@Log4j
@CompileStatic
class AshArchive {

	private static final long ARCHIVE_SIZE = TimeUnit.DAYS.toMillis(7)
	private static final Pattern FILENAME_PATTERN = ~/\d{4}-\d{2}-\d{2}-\d{2}/
	private static final String FILENAME_DATE_PATTERN = 'yyyy-MM-dd-HH'

	private final List<AshSnapshot> buffer = new ArrayList()
	private String currentFile = null

	@Autowired
	private AshAgent agent
	@Autowired
	private AshService service

	synchronized void archive(AshSnapshot snapshot) {
		String file = new Date(snapshot.timestamp).format(FILENAME_DATE_PATTERN)
		if (file != currentFile) {
			if (currentFile != null) {
				File outFile = new File(archiveDirectory, currentFile)
				outFile.withObjectOutputStream { ObjectOutputStream out ->
					out.writeInt(buffer.size())
					for (s in buffer) {
						out.writeObject(s)
					}
				}
				buffer.clear()
			}
			currentFile = file
		}
		buffer.add(snapshot)
	}

	void removeOldFiles() {
		Date reference = new Date(System.currentTimeMillis() - ARCHIVE_SIZE)
		DateFormat format = new SimpleDateFormat(FILENAME_DATE_PATTERN)
		new File(archiveDirectory).eachFileMatch FileType.FILES, FILENAME_PATTERN, { File f ->
			if (format.parse(f.name).before(reference)) {
				log.info("Removing file: ${f.absolutePath}")
				f.delete()
			}
		}
	}

	Map getArchivedData(int year, int month, int dayOfMonth, int hourOfDay) {
		Date date = new GregorianCalendar(year, month - 1, dayOfMonth, hourOfDay, 0).getTime()
		File inFile = new File(archiveDirectory, date.format(FILENAME_DATE_PATTERN))
		if (inFile.exists()) {
			try {
				return inFile.withObjectInputStream { ObjectInputStream is ->
					ArchiveFileIterator iterator = new ArchiveFileIterator(is)
					Map<String, Object> data = (Map<String, Object>) ['snapshots' : iterator.snapshots]
					data << service.intervalData(iterator, Long.MIN_VALUE, Long.MAX_VALUE)
					data.intervalStart = iterator.snapshots ? iterator.snapshots[0].timestamp : null
					data.intervalEnd = iterator.snapshots ? iterator.snapshots[-1].timestamp : null
					return data
				}
			} catch (Exception e) {
				log.error('Failed to load archived data', e)
			}
		}
		return ['snapshots' : [], 'topSql' : [], 'topSessions' : [], 'intervalStart' : null, 'intervalEnd' : null]
	}

	private String getArchiveDirectory() {
		File dir = new File(System.getProperty('java.io.tmpdir'), 'oraman')
		dir.mkdir()
		return dir.getAbsolutePath()
	}

	private static class ArchiveFileIterator implements Iterator<AshSnapshot> {

		final List<Snapshot> snapshots = []
		final ObjectInputStream is
		int counter

		ArchiveFileIterator(ObjectInputStream is) {
			this.is = is
			this.counter = is.readInt()
		}

		@Override
		boolean hasNext() {
			counter > 0
		}

		@Override
		AshSnapshot next() {
			if (!hasNext()) {
				throw new NoSuchElementException()
			}
			counter--
			AshSnapshot original = (AshSnapshot) is.readObject()

			Snapshot copy = new Snapshot()
			copy.timestamp = original.timestamp
			copy.observations << original.observations
			snapshots.add(copy)

			return original
		}

		@Override
		void remove() {
			throw new UnsupportedOperationException()
		}
	}
}
