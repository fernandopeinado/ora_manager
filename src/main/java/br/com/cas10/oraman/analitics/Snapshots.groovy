package br.com.cas10.oraman.analitics

class Snapshots {

	private final int maxSize
	private final LinkedList<Snapshot> elements = new LinkedList()

	Snapshots(int maxSize) {
		this.maxSize = maxSize
	}

	synchronized void add(Snapshot snapshot) {
		snapshot.calculateDeltas(elements ? elements.getLast() : null)
		elements.addLast(snapshot)
		if (elements.size() > maxSize) {
			elements.removeFirst()
		}
	}

	synchronized List<Snapshot> asList() {
		new ArrayList(elements)
	}
}
