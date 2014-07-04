package br.com.cas10.oraman.oracle

enum WaitClass {

	ADMINISTRATIVE('Administrative'),

	APPLICATION('Application'),

	CLUSTER('Cluster'),

	CONCURRENCY('Concurrency'),

	COMMIT('Commit'),

	CONFIGURATION('Configuration'),

	NETWORK('Network'),

	OTHER('Other'),

	QUEUEING('Queueing'),

	SCHEDULER('Scheduler'),

	SYSTEM_IO('System I/O'),

	USER_IO('User I/O')

	static final List<WaitClass> VALUES = (values() as List).asImmutable()

	final String waitClassName

	private WaitClass(String waitClassName) {
		this.waitClassName = waitClassName
	}
}
