var framework = framework || 
(function() {
	function pageScript(config, containerSelector) {
		if (config) {
			for (bindName in config) {
				var bindsFound = null;
				if (containerSelector) {
					bindsFound = $(containerSelector + ' [data-bind='+ bindName + ']');
				}
				else {
					bindsFound = $('[data-bind='+ bindName + ']');
				}
				for (var eventName in config[bindName]) {
					if (typeof config[bindName][eventName] == 'function') {
						bindsFound.on(eventName, {method: config[bindName][eventName]}, methodCallback);
					}
				}
			}
		}
		var dataLoads = $('div[data-load]');
		var i = 0, size = dataLoads.length;
		for (i = 0; i < size; i++) {
			var url = $(dataLoads[i]).data().load;
			$(dataLoads[i]).removeData("load");
			$(dataLoads[i]).load(url);
		}
	}
	
	function methodCallback(event) {
		var target = $(event.delegateTarget);
		event.data.method(target.data(), target);
	}
	
	function getUrl(url) {
		var contextPath = $('body').data().contextpath;
		if (contextPath) {
			url = contextPath + "/" + url;
		}
		return url;
	}
	
	return {
		pageScript: pageScript,
		getUrl: getUrl
	};
})();

framework.timedSeries = framework.timedSeries || 
(function() {

	/**
	 * @param matrix ex:
	 * <pre>
	 * | timestamp 				| serie1 	| serie2	| serie3	|
	 * | '2013-10-10 10:00:00'	| 0 		| 1			| 2			|
	 * | '2013-10-10 11:00:00'	| 3 		| 4 		| 5 		|
	 * | '2013-10-10 12:00:00'	| 2 		| 3 		| 0 		|
	 * </pre>
	 * 
	 * @return object like
	 * <pre>
	 * {
	 * 	data: [
	 * 			[['2013-10-10 10:00:00', 0], ['2013-10-10 11:00:00', 3],['2013-10-10 12:00:00', 2]],
	 * 			[['2013-10-10 10:00:00', 1], ['2013-10-10 11:00:00', 4],['2013-10-10 12:00:00', 3]],
	 * 			[['2013-10-10 10:00:00', 2], ['2013-10-10 11:00:00', 5],['2013-10-10 12:00:00', 0]]
	 * 	],
	 *  labels: [ 'serie1', 'serie2', 'serie3' ],
	 *  minTime: '2013-10-10 10:00:00'
	 *  maxTime: '2013-10-10 12:00:00'
	 * }
	 * </pre>
	 */
	function decompose(matrix) {
		var data = [];
		var labels = [];
		var minTime = matrix[1][0];
		var maxTime = matrix[matrix.length-1][0];
		console.log(maxTime);
		if (matrix && matrix[0]) {
			var r, c, rows = cols = matrix.length, cols = matrix[0].length;
			for (c = 1; c < cols; c++) {
				labels[c-1] = { label: matrix[0][c] };						
			}
			for (r = 1; r < rows; r++) {
				for (c = 1; c < cols; c++) {
					if (!data[c-1]) {
						data[c-1] = [];
					}
					data[c-1][r-1] = [matrix[r][0], matrix[r][c]];
				}
			} 
		}
		
		return {
			data: data,
			labels: labels,
			minTime: minTime,
			maxTime: maxTime
		}
	}
	

	return {
		decompose: decompose,
	};
})();