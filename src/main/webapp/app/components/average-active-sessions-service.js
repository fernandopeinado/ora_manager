(function() {

	var module = angular.module('averageActiveSessions', [ 'jqPlotHelper' ]);

	function AverageActiveSessions(JqPlotHelper) {

		function yAxisOptions(data, cpuCores, cpuThreads) {
			var maxValue = 0;
			data.forEach(function(row) {
				var total = 0;
				row[1].forEach(function(value) {
					if (!isNaN(value))
						total += value;
				});
				maxValue = Math.max(maxValue, total);
			});

			var max;
			if (maxValue > cpuThreads) {
				// top padding
				max = maxValue * 1.2;
			} else if (maxValue > cpuCores) {
				max = cpuThreads;
			} else {
				max = cpuCores;
			}

			var numberTicks = 11;
			var tickInterval = (max / (numberTicks - 1)).toFixed(1);

			return {
				numberTicks : numberTicks,
				tickInterval : tickInterval
			};
		}

		function plotGraph(json, series, divId) {
			var trans = JqPlotHelper.transform(json.keys, json.data);

			var plotData = series.map(function(s) {
				return trans.data[s[0]];
			});
			var plotSeries = series.map(function(s) {
				return {
					label : s[0],
					color : s[1]
				};
			});
			var yAxis = yAxisOptions(json.data, json.cpuCores, json.cpuThreads);

			var options = JqPlotHelper.buildOptions({
				title : 'Average Active Sessions',
				seriesDefaults : {
					fill : true
				},
				series : plotSeries,
				stackSeries : true,
				axes : {
					xaxis : {
						renderer : $.jqplot.DateAxisRenderer,
						tickOptions : {
							formatString : '%H:%M'
						},
						min : trans.minTimestamp,
						max : trans.maxTimestamp,
						numberTicks : 12,
					},
					yaxis : {
						min : 0,
						tickInterval : yAxis.tickInterval,
						numberTicks : yAxis.numberTicks
					}
				},
				canvasOverlay : {
					show : true,
					objects : [ {
						dashedHorizontalLine : {
							show : (json.cpuCores != json.cpuThreads),
							color : 'red',
							lineWidth : 1,
							y : json.cpuCores
						}
					}, {
						horizontalLine : {
							color : 'red',
							lineWidth : 1,
							y : json.cpuThreads
						}
					} ]
				}
			});

			return $.jqplot(divId, plotData, options);
		}

		return {
			plotGraph : plotGraph
		};
	}

	module.factory('AverageActiveSessions', [ 'JqPlotHelper',
			AverageActiveSessions ]);

})();
