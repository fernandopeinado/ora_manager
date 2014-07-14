(function() {

	var module = angular.module('jqPlotHelper', []);

	module.factory('JqPlotHelper', function() {

		function transform(keys, data) {
			var transData = {};
			keys.forEach(function(key) {
				transData[key] = [];
			});
			data.forEach(function(row) {
				keys.forEach(function(key, index) {
					transData[key].push([ row[0], row[1][index] ]);
				});
			});

			var minTimestamp = (data.length > 0) ? data[0][0] : null;
			var maxTimestamp = (data.length > 0) ? data[data.length - 1][0]
					: null;

			return {
				data : transData,
				minTimestamp : minTimestamp,
				maxTimestamp : maxTimestamp,
				length : data.length
			};
		}

		function buildOptions(options) {
			var defaults = {
				seriesDefaults : {
					shadow : false
				},
				axesDefaults : {
					labelRenderer : $.jqplot.CanvasAxisLabelRenderer,
					tickRenderer : $.jqplot.CanvasAxisTickRenderer,
					tickOptions : {
						fontSize : '8pt'
					}
				},
				grid : {
					drawBorder : false,
					shadow : false,
					background : '#ffffff',
					gridLineColor : '#e5e5e5'
				},
				legend : {
					show : true,
					location : 'e',
					placement : 'outside'
				}
			};
			return $.extend(true, defaults, options);
		}

		function plot($scope, targetId, plotFunction, plotFunctionArgs) {
			if (!$scope.plots) {
				$scope.plots = {};
			}
			if ($scope.plots[targetId]) {
				destroyPlot($scope.plots[targetId]);
			}
			var plot = plotFunction.apply(null, plotFunctionArgs);
			$scope.plots[targetId] = plot;
		}

		function destroyPlot(plot) {
			plot.destroy();
		}

		return {
			transform : transform,
			buildOptions : buildOptions,
			plot : plot,
			destroyPlot : destroyPlot
		};
	});

})();
