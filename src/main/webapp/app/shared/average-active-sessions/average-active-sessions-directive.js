(function() {

	var module = angular.module('averageActiveSessions', []);

	module.directive('oramanAasChart', [ '$interval', '$http',
			OramanAasChartDirective ]);

	function OramanAasChartDirective($interval, $http) {
		return {
			restrict : 'A',
			scope : {
				url : '@aasUrl',
				autoRefresh : '@aasAutoRefresh',
				title : '@aasTitle',
				noDataMessage : '@aasNoDataMessage',
				series : '=aasSeries',
				preprocessor : '=aasPreprocessor',
				selectedInterval : '=aasSelectedInterval'
			},
			link : function(scope, element) {
				scope.lockedForUpdate = false;

				var preprocessor = scope.preprocessor || function(json) {
					return json;
				};

				function refresh() {
					if (scope.lockedForUpdate || !scope.url) {
						return;
					}
					$http.get(scope.url).success(function(json) {
						destroyChart(element);
						updateChart(preprocessor(json), scope, element);
					});
				}

				refresh();
				if (scope.autoRefresh) {
					var refreshPromise = $interval(refresh,
							parseInt(scope.autoRefresh));
					scope.$on('$destroy', function() {
						$interval.cancel(refreshPromise);
					});
				}
			}
		};
	}

	function destroyChart(element) {
		d3.select(element[0]).select('svg').remove();
	}

	function updateChart(json, scope, element) {

		if (json.data.length < 2) {
			if (scope.noDataMessage) {
				d3.select(element[0]).append('svg').attr({
					width : element.width(),
					height : element.height(),
					'class' : 'oraman-aas'
				}).append('text').attr({
					x : element.width() / 2,
					y : element.height() / 2,
					'text-anchor' : 'middle',
					'dominant-baseline' : 'central',
					'class' : 'no-data'
				}).text(scope.noDataMessage);
			}
			return;
		}

		json.data.forEach(function(d) {
			d[0] = new Date(d[0]);
		});

		// --------------------------------------------------------------------
		// CONSTANTS
		// --------------------------------------------------------------------

		var xTicks = 10;
		var yTicks = 8;

		// --------------------------------------------------------------------
		// MARGINS
		// --------------------------------------------------------------------

		var margin = {
			top : 35,
			right : 25,
			bottom : 35,
			left : 45
		};
		if (scope.title) {
			margin.top += 15;
		}
		var longestKey = d3.max(json.keys, function(key) {
			return key.length;
		});
		margin.right += 55; // legend circles + padding
		margin.right += 5 * longestKey; // 5px per letter

		// --------------------------------------------------------------------
		// SVG
		// --------------------------------------------------------------------

		var width = element.width() - margin.left - margin.right;
		var height = element.height() - margin.top - margin.bottom;

		var svg = d3.select(element[0]).append('svg').attr({
			width : width + margin.left + margin.right,
			height : height + margin.top + margin.bottom,
			'class' : 'oraman-aas'
		}).append('g').attr('transform',
				'translate(' + margin.left + ',' + margin.top + ')');

		if (scope.title) {
			svg.append('text').attr({
				x : margin.left + width / 2,
				y : -15,
				'text-anchor' : 'middle',
				'class' : 'title'
			}).text(scope.title);
		}

		// --------------------------------------------------------------------
		// SCALES
		// --------------------------------------------------------------------

		var maxY = d3.max(json.data, function(d) {
			return d3.sum(d[1]);
		});
		if (json.cpuThreads && json.cpuCores) {
			if (maxY < json.cpuCores) {
				maxY = json.cpuCores;
			} else if (maxY < json.cpuThreads) {
				maxY = json.cpuThreads;
			}
		}

		var x = d3.time.scale().domain(d3.extent(json.data, function(d) {
			return d[0];
		})).range([ 0, width ]);
		var y = d3.scale.linear().domain([ 0, maxY ]).range([ height, 0 ])
				.nice(yTicks);

		// --------------------------------------------------------------------
		// GRID
		// --------------------------------------------------------------------

		svg.selectAll('.h-grid').data(y.ticks(yTicks)).enter().append('line')
				.attr({
					x1 : 0,
					y1 : y,
					x2 : width,
					y2 : y,
					'class' : 'grid'
				});
		svg.selectAll('.v-grid').data(x.ticks(xTicks)).enter().append('line')
				.attr({
					x1 : x,
					y1 : 0,
					x2 : x,
					y2 : height,
					'class' : 'grid'
				});
		svg.append('line').attr({
			x1 : width,
			y1 : 0,
			x2 : width,
			y2 : height,
			'class' : 'grid'
		});

		// --------------------------------------------------------------------
		// DATA
		// --------------------------------------------------------------------

		var stack = d3.layout.stack().values(function(d) {
			return d.values;
		});
		var data = stack(scope.series.map(function(s, index) {
			var keyIndex = json.keys.indexOf(s[0]);
			return {
				index : index,
				key : s[0],
				color : s[1],
				values : json.data.map(function(d) {
					return {
						x : d[0],
						y : d[1][keyIndex]
					};
				})
			};
		}));
		var area = d3.svg.area().x(function(d) {
			return x(d.x);
		}).y0(function(d) {
			return y(d.y0);
		}).y1(function(d) {
			return y(d.y0 + d.y);
		});
		svg.selectAll('.aas-data').data(data).enter().append('path').attr({
			d : function(d) {
				return area(d.values);
			},
			fill : function(d) {
				return d.color;
			},
			'class' : 'aas-data'
		});
		svg.selectAll('.aas-data').on('mouseover.area', function() {
			d3.select(this).attr('fill-opacity', 0.8);
		}).on('mouseout.area', function() {
			d3.select(this).attr('fill-opacity', 1);
		});

		// --------------------------------------------------------------------
		// AXES
		// --------------------------------------------------------------------

		var xAxis = d3.svg.axis().scale(x).orient('bottom').tickFormat(
				d3.time.format('%H:%M')).ticks(xTicks);
		var yAxis = d3.svg.axis().scale(y).orient('left').ticks(yTicks);

		svg.append('g').attr('class', 'axis').attr('transform',
				'translate(0,' + height + ')').call(xAxis);
		svg.append('g').attr('class', 'axis').call(yAxis);

		// --------------------------------------------------------------------
		// CPU LINES
		// --------------------------------------------------------------------

		function cpuLine(value, cssClass) {
			svg.append('line').attr({
				x1 : 0,
				y1 : y(value),
				x2 : width,
				y2 : y(value),
				'class' : 'cpu ' + cssClass
			});
		}
		if (json.cpuCores && json.cpuThreads) {
			cpuLine(json.cpuCores, 'cores');
			cpuLine(json.cpuThreads, 'threads');
		}

		// --------------------------------------------------------------------
		// LEGEND
		// --------------------------------------------------------------------

		var legItemHeight = 20;
		var legItemInnerSpacing = 5;
		var legItemRadius = 6;
		var legLeftMargin = 15;
		var legY0 = (height + (scope.series.length - 1) * legItemHeight) / 2;

		function legItemY(d, i) {
			return legY0 - i * legItemHeight;
		}

		var legItem = svg.append('g').attr('transform',
				'translate(' + (width + legLeftMargin) + ',0)').selectAll(
				'.legend-item').data(scope.series).enter().append('g').attr(
				'class', 'legend-item');
		legItem.append('circle').attr({
			cx : legItemRadius,
			cy : legItemY,
			r : legItemRadius,
			fill : function(d) {
				return d[1];
			}
		});
		var legItemText = legItem.append('text').attr({
			x : legItemRadius * 2 + legItemInnerSpacing,
			y : legItemY,
			'dominant-baseline' : 'central'
		}).text(function(d) {
			return d[0];
		});
		svg.selectAll('.aas-data').on('mouseover.legend', function(s) {
			legItemText.style('fill', function(d, i) {
				return (s.index == i) ? 'black' : '#ccc';
			});
		});
		svg.selectAll('.aas-data').on('mouseout.legend', function() {
			legItemText.style('fill', null);
		});

		// --------------------------------------------------------------------
		// INTERVAL SELECTION
		// --------------------------------------------------------------------

		var selInterval = scope.selectedInterval;

		if (!selInterval)
			return;

		var selMinWidth = 50;
		var selHandleHalfWidth = 10;

		var fiveMinutes = 5 * 60 * 1000;
		var defaultIntervalEnd = json.data[json.data.length - 1][0];
		var defaultIntervalStart = new Date(Math.max(defaultIntervalEnd
				- fiveMinutes, json.data[0][0].getTime()));

		if (scope.manualSelection) {
			selInterval.start = d3.max([ selInterval.start, json.data[0][0] ]);

			if (x(selInterval.end) - x(selInterval.start) < selMinWidth) {
				selInterval.start = defaultIntervalStart;
				selInterval.end = defaultIntervalEnd;
				scope.manualSelection = false;
			}
		} else {
			selInterval.start = defaultIntervalStart;
			selInterval.end = defaultIntervalEnd;
		}

		var selectionArea = svg.append('rect').attr({
			x : x(selInterval.start),
			y : 0,
			width : x(selInterval.end) - x(selInterval.start),
			height : height,
			'class' : 'selection-area'
		});

		function selectionHandleLine(date) {
			return svg.append('line').attr({
				x1 : x(date),
				y1 : 0,
				x2 : x(date),
				y2 : height,
				'class' : 'selection-handle-line'
			});
		}
		var selectionLHandleLine = selectionHandleLine(selInterval.start);
		var selectionRHandleLine = selectionHandleLine(selInterval.end);

		function selectionHandle(date) {
			return svg.append('rect').attr({
				x : x(date) - selHandleHalfWidth,
				y : 0,
				width : selHandleHalfWidth * 2,
				height : height,
				'class' : 'selection-handle'
			});
		}
		var selectionLHandle = selectionHandle(selInterval.start);
		var selectionRHandle = selectionHandle(selInterval.end);

		function updateSelection(newX, newWidth) {
			selectionArea.attr({
				x : newX,
				width : newWidth
			});
			selectionLHandleLine.attr({
				x1 : newX,
				x2 : newX
			});
			selectionRHandleLine.attr({
				x1 : newX + newWidth,
				x2 : newX + newWidth
			});
			selectionLHandle.attr('x', newX - selHandleHalfWidth);
			selectionRHandle.attr('x', newX - selHandleHalfWidth + newWidth);
		}
		function updateInterval(manualSelection) {
			scope.manualSelection = manualSelection;

			var newX = parseFloat(selectionArea.attr('x'));
			var newWidth = parseFloat(selectionArea.attr('width'));

			var start = x.invert(newX);
			var end = x.invert(newX + newWidth);

			// Optimization: avoids the invocation of $apply if the values were
			// not modified
			if (selInterval.start.getTime() != start.getTime()
					|| selInterval.end.getTime() != end.getTime()) {
				scope.$apply(function() {
					selInterval.start = start;
					selInterval.end = end;
				});
			}
		}

		selectionArea.on('dblclick', function() {
			var newX = x(defaultIntervalStart);
			var newWidth = x(defaultIntervalEnd) - newX;
			updateSelection(newX, newWidth);
			updateInterval(false);
		});

		function drag() {
			return d3.behavior.drag().on('dragstart', function() {
				scope.lockedForUpdate = true;
			}).on('dragend', function() {
				scope.lockedForUpdate = false;
				updateInterval(true);
			});
		}

		var selectionDrag = drag().origin(function(d) {
			return {
				x : selectionArea.attr('x'),
				y : 0
			};
		}).on('drag', function() {
			var selWidth = parseFloat(selectionArea.attr('width'));
			var newX = Math.max(0, Math.min((width - selWidth), d3.event.x));
			updateSelection(newX, selWidth);
		});
		selectionArea.call(selectionDrag);

		var selectionLResize = drag().on('drag', function() {
			var r = parseFloat(selectionRHandle.attr('x'));
			var l = d3.event.x;
			l = Math.min(r - selMinWidth, l);
			l = Math.max(-selHandleHalfWidth, l);
			updateSelection(l + selHandleHalfWidth, r - l);
		});
		selectionLHandle.call(selectionLResize);

		var selectionRResize = drag().on('drag', function() {
			var l = parseFloat(selectionLHandle.attr('x'));
			var r = d3.event.x;
			r = Math.min(width - selHandleHalfWidth, r);
			r = Math.max(l + selMinWidth, r);
			updateSelection(l + selHandleHalfWidth, r - l);
		});
		selectionRHandle.call(selectionRResize);
	}

})();
