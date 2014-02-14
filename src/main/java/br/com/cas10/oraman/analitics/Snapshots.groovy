package br.com.cas10.oraman.analitics

class Snapshots {
	private Map<String, CircularList<Snapshot>> storage = new HashMap()
	
	synchronized void add(Snapshot s) {
		CircularList<Snapshot> list = getStorage(s.type)
		list.add(s)
	}
	
	synchronized CircularList<Snapshot> getStorage(String type) {
		return getStorage(type, 300)
	}
	
	synchronized CircularList<Snapshot> getStorage(String type, int size) {
		CircularList<Snapshot> list = storage[type];
		if (!list) {
			list = buildList(size)
			storage[type] = list
		}
		return list
	}
		
	private CircularList<Snapshot> buildList(int samples) {
		def deltaCalculation = { last, current -> current.calculateDelta(last) }
		new CircularList<Snapshot>(samples, deltaCalculation);
	}
}
