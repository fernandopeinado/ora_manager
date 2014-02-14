package br.com.cas10.oraman.service

import groovy.transform.CompileStatic;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SystemService {
	
	public Map<String, String> getBasicInfo() {
		return [
			"name" : "uname -n".execute().text,
			"kernel" : "uname -o -r -m -v".execute().text,
			"release" : "cat /etc/debian_version || cat /etc/redhat-release".execute().text	
		]
	}	
	
	public Map<String, Object> getProcessorInfo() {
		String text = "cat /proc/cpuinfo".execute().text
		Map<String, Integer> cpus = new LinkedHashMap()
		Map<String, String> caches = new LinkedHashMap()
		int groupid = 0;
		String lastModel = null
		text.eachLine { String line ->
			if (line.trim() == "") {
				groupid++
			}
			else {
				String[] data = line.split("\\s*\\:\\s*")
				if (data[0] == "model name") {
					lastModel = data[1];
					Integer i = cpus[data[1]];
					if (i == null) {
						cpus[data[1]] = 1
					}
					else {
						cpus[data[1]] = cpus[data[1]] + 1
					}
				}
				if (data[0] == "cache size") {
					caches[lastModel] = data[1];
				}
			}
		}
		return [
			"cpus" : cpus,
			"caches" : caches
		]
	}

	public Map<String, Object> getMemoryInfo() {
		String text = "cat /proc/meminfo".execute().text
		String total = null
		String swap = null
		text.eachLine { String line ->
			String[] data = line.split("\\:\\s*")
			if (data[0] == "MemTotal") {
				total = data[1]
			}
			if (data[0] == "SwapTotal") {
				swap = data[1]
			}
		}
		return [
			"total" : total,
			"swap" : swap
		]
	}
	
	public Map<String, Object> getNetworkInfo() {
		String text = "/sbin/ifconfig".execute().text
		Map<String, Object> interfaces = new LinkedHashMap()
		text.eachLine() { String line ->
			Matcher m = line =~ /^(\w+)\s+(.+HWaddr\s+)(.*)/
			if (m.matches()) {
				interfaces[m.group(1)] = new LinkedHashMap<String, Object>()
				interfaces[m.group(1)]['mac'] = m.group(3).trim()
			}
		}
		interfaces.keySet().each { String iface ->
			String ethtoolText = "ethtool ${iface}".execute().text
			ethtoolText.eachLine { String line ->
				Matcher m = line =~ /^\s+Speed:\s*(.*)/
				Matcher m2 = line =~ /^\s+Duplex:\s*(.*)/
				if (m.matches()) {
					interfaces[iface]['speed'] = m.group(1)
				}
				else if (m2.matches()) {
					interfaces[iface]['duplex'] = m2.group(1)
				}
			}
		}		
		return [
			"interfaces" : interfaces
		]
	}

	public Map<String, Object> getStorageInfo() {
		String text = "lsblk -d".execute().text
		Map<String, Object> devices = new LinkedHashMap()
		text.eachLine() { String line ->
			String[] data = line.split("\\s+");
			if (data[5] == "disk") {
				devices[data[0]] = data[3]
			}
		}
		return [
			"devices" : devices
		]
	}
	
	public Map<String, Object> getSysctl() {
		String text = "sysctl -a".execute().text
		Map<String, Object> props = new LinkedHashMap()
		text.eachLine() { String line ->
			println line
			String[] data = line.split("\\s=\\s")
			if (data.length == 2) {
				props[data[0]] = data[1]
			}
		}
		return [
			"props" : props
		]
	}

}
