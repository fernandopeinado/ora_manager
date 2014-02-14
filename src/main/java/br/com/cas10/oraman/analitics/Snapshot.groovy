package br.com.cas10.oraman.analitics

class Snapshot {
	String type
	long timestamp
	Map<String, Object> observations = new HashMap()
	Map<String, Object> deltaObs = new HashMap()
	
	void calculateDelta(Snapshot prev) {
		for (obs in observations.entrySet()) {
			Object lastVal = prev.observations[obs.key];
			deltaObs[obs.key] = delta(lastVal, obs.value);
		}
		postDeltas(prev);
	}
	
	Object delta(Object last, Object current) {
		if (last != null) {
			return current - last;
		}
		return null;
	}

	String getTime() {
		new java.sql.Time(timestamp);
	}
	
	String getDateTime() {
		new java.sql.Timestamp(timestamp);
	}
		
	void postDeltas(Snapshot prev) {
	}
}
