package br.com.cas10.oraman.analitics

class CircularList<T> {

	private int current = 0
	private int size
	private T[] list
	private T last
	private Closure<Void> deltaCalculation
	
	CircularList(int size, Closure<Void> deltaCalculation) {
		this.size = size
		this.list = new T[size]
		this.deltaCalculation = deltaCalculation
	}
	
	synchronized void add(T t) {
		if (last) {
			deltaCalculation.call(last, t)
		}
		last = t
		list[current] = t
		current = (current + 1) % size
	}
	
	synchronized List<T> asList() {
		List<T> result = new ArrayList()
		for (int i = 0; i < size; i++) {
			int idx = (current + i) % size
			if (list[idx]) result.add(list[idx])	
		}
		return result
	}
}
