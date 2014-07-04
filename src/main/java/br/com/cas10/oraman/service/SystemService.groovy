package br.com.cas10.oraman.service

import groovy.util.logging.Log4j

import java.util.regex.Matcher

import org.springframework.stereotype.Service

@Service
@Log4j
class SystemService {

	Map<String, String> getBasicInfo() {
		String release = execute('cat /etc/debian_version /etc/redhat-release')
		return [
			'name' : execute('uname -n'),
			'kernel' : execute('uname -omrv'),
			'release' : release ?: 'Unknown'
		]
	}

	List<Map<String, String>> getProcessorInfo() {
		Map<String, String> processorsMap = new LinkedHashMap()
		Map<String, String> processor = [:]
		execute('cat /proc/cpuinfo').eachLine { line ->
			if (line.trim()) {
				def (key, value) = split(line, ':')
				processor[key] = value
			} else {
				// only one sibling is necessary for each physical processor
				processorsMap[processor['physical id']] = processor
				processor = [:]
			}
		}
		return processorsMap.values().collect() {
			[
				'model': it['model name'],
				'cores' : it['cpu cores'],
				'siblings': it['siblings'],
				'cache' : it['cache size']
			]
		}
	}

	Map<String, String> getMemoryInfo() {
		Map<String, String> data = [:]
		execute('cat /proc/meminfo').eachLine { String line ->
			def (key, value) = split(line, ':')
			data[key] = value
		}
		return [
			'total' : data['MemTotal'],
			'swap' : data['SwapTotal']
		]
	}

	List<Map<String, String>> getNetworkInfo() {
		List<Map<String, String>> interfaces = []
		execute('/sbin/ifconfig').eachLine { line ->
			Matcher m = line =~ /^(\w+)\s+.+HWaddr\s+(.*)/
			if (m.matches()) {
				interfaces.add([
					'name' : m.group(1),
					'mac' : m.group(2).trim()
				])
			}
		}
		for (iface in interfaces) {
			execute("ethtool ${iface.name}").eachLine {
				String line = it.trim()
				if (line.startsWith('Speed:') || line.startsWith('Duplex:')) {
					def (key, value) = split(line, ':')
					iface[key.toLowerCase()] = value
				}
			}
		}
		return interfaces
	}

	List<Map<String, String>> getStorageInfo() {
		List<Map<String, String>> devices = []
		execute('lsblk -d -o NAME,TYPE,SIZE').eachLine { line ->
			def (name, type, size) = line.split()
			if (type == 'disk') {
				devices.add(['name' : name, 'size' : size])
			}
		}
		return devices
	}

	List<List<String>> getSysctl() {
		List<List<String>> props = []
		execute('sysctl -a').eachLine { line ->
			def (key, value) = split(line, '=')
			props.add([key, value])
		}
		return props
	}

	private String execute(String command) {
		StringBuffer out = new StringBuffer()
		StringBuffer err = new StringBuffer()
		Process process = command.execute()
		process.waitForProcessOutput(out, err)
		if (log.isDebugEnabled() && err)
			log.debug(err.toString())
		return out.toString().trim()
	}

	private split(String line, String separator) {
		int separatorIndex = line.indexOf(separator)
		String key = line.substring(0, separatorIndex).trim()
		String value = line.substring(separatorIndex + 1).trim()
		return [key, value]
	}
}
