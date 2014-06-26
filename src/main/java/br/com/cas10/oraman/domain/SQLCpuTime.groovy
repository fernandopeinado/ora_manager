package br.com.cas10.oraman.domain

class SQLCpuTime implements Comparable<SQLCpuTime> {
	String sqlId
	String sql
	Long cpuTime
	Long totalCpuTime
	
	SQLCpuTime(String sqlId, String sql, Long cpuTime) {
		this.sqlId = sqlId
		this.sql = sql
		this.cpuTime = cpuTime
	}
	
	Double getPercent() {
		100d * cpuTime / (double) totalCpuTime
	}
	
	@Override
	public int compareTo(SQLCpuTime o) {
		-this.cpuTime.compareTo(o.cpuTime);
	}
}
