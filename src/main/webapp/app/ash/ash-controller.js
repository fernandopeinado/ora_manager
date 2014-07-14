(function() {

	var module = angular.module('ash', [ 'averageActiveSessions',
			'jqPlotHelper' ]);

	var series = [ [ 'CPU + CPU Wait', '#04ce04' ], [ 'Scheduler', '#87fd88' ],
			[ 'User I/O', '#044ae4' ], [ 'System I/O', '#0993e1' ],
			[ 'Concurrency', '#8b1a04' ], [ 'Application', '#bd2b06' ],
			[ 'Commit', '#e46a04' ], [ 'Configuration', '#584615' ],
			[ 'Administrative', '#73745e' ], [ 'Network', '#9b9378' ],
			[ 'Queueing', '#cbb796' ], [ 'Cluster', '#cec4a7' ],
			[ 'Other', '#f070ac' ] ];

	function dateToHMS(dateInMillis) {
		var date = new Date(dateInMillis);
		var h = date.getHours();
		var m = date.getMinutes();
		var s = date.getSeconds();
		return (h > 9 ? h : '0' + h) + ':' + (m > 9 ? m : '0' + m) + ':'
				+ (s > 9 ? s : '0' + s);
	}

	function activityBar(activityMap, topActivity) {
		var result = [];
		series.forEach(function(s) {
			var width = Math.floor((activityMap[s[0]] * 100) / topActivity);
			if (width > 0) {
				result.push({
					'width' : width + '%',
					'background-color' : s[1]
				});
			}
		});
		return result;
	}

	function AshCtrl($scope, $interval, $http, AverageActiveSessions,
			JqPlotHelper) {

		function updateTopSql(data) {
			var top = (data.length > 0) ? data[0].activity : null;
			data.forEach(function(sql) {
				sql.percentageFixed = sql.percentageTotalActivity.toFixed(0);
				sql.aasFixed = sql.averageActiveSessions.toFixed(2);
				sql.activityBar = activityBar(sql.activityByWaitClass, top);
			});
			$scope.topSql = data;
		}

		function updateTopSessions(data) {
			var top = (data.length > 0) ? data[0].activity : null;
			data.forEach(function(sess) {
				sess.percentageFixed = sess.percentageTotalActivity.toFixed(0);
				sess.activityBar = activityBar(sess.activityByWaitClass, top);
			});
			$scope.topSessions = data;
		}

		function updateIntervalData(data) {
			$scope.intervalStart = dateToHMS(data.intervalStart);
			$scope.intervalEnd = dateToHMS(data.intervalEnd);
			updateTopSql(data.topSql);
			updateTopSessions(data.topSessions);
		}

		function updateAasPlot(data) {
			if (data.data.length < 2) {
				return;
			}

			var targetId = 'ash_aas';
			var options = {
				cursor : {
					show : true,
					showTooltip : false,
					zoom : true,
					zoomProxy : true,
					constrainZoomTo : 'x'
				}
			}
			var params = [ data, series, options, targetId ];
			var plot = JqPlotHelper.plot($scope, targetId,
					AverageActiveSessions.plotGraph, params);

			plot.target.bind('jqplotZoom', function(ev, gridpos, datapos, plot,
					cursor) {
				$scope.autoUpdate = false;

				var point1 = Math.floor(cursor._zoom.axes.start.xaxis);
				var point2 = Math.floor(datapos.xaxis);

				var start = Math.min(point1, point2);
				var end = Math.max(point1, point2);

				$http.get('ws/ash/ash-interval?start=' + start + '&end=' + end)
						.success(updateIntervalData);
			});

			plot.target.bind('jqplotResetZoom', function(ev, plot, cursor) {
				$scope.$apply(function() {
					updateIntervalData($scope.ashData)
				});
				$scope.autoUpdate = true;
			});
		}

		function update() {
			if (!$scope.autoUpdate) {
				return;
			}
			$http.get('ws/ash/ash').success(function(json) {
				$scope.ashData = json;
				updateIntervalData(json);
				updateAasPlot(json.averageActiveSessions);
			});
		}

		$scope.autoUpdate = true;
		update();
		var updatePromise = $interval(update, 60000);

		$scope.$on('$destroy', function() {
			$interval.cancel(updatePromise);
		});
	}

	module.controller('AshCtrl', [ '$scope', '$interval', '$http',
			'AverageActiveSessions', 'JqPlotHelper', AshCtrl ]);

})();
