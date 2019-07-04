(function() {

	var module = angular.module('ash', [ 'averageActiveSessions' ]);

	var series = [ [ 'CPU + CPU Wait', '#04ce04' ], [ 'Scheduler', '#87fd88' ],
			[ 'User I/O', '#044ae4' ], [ 'System I/O', '#0993e1' ],
			[ 'Concurrency', '#8b1a04' ], [ 'Application', '#bd2b06' ],
			[ 'Commit', '#e46a04' ], [ 'Configuration', '#584615' ],
			[ 'Administrative', '#73745e' ], [ 'Network', '#9b9378' ],
			[ 'Queueing', '#cbb796' ], [ 'Cluster', '#cec4a7' ],
			[ 'Other', '#f070ac' ] ];

	function activityBar(activityMap, topActivity) {
		var result = [];
		series.forEach(function(s) {
			var width = Math.floor((activityMap[s[0]] * 100) / topActivity);
			if (width > 0) {
				result.push({
					title : s[0],
					style : {
						'width' : width + '%',
						'background-color' : s[1]
					}
				});
			}
		});
		return result;
	}

	function AshCtrl($scope, $http) {		
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
			var selStart = $scope.selectedInterval.start;
			var selEnd = $scope.selectedInterval.end;

			// If multiple updates are triggered in rapid succession, it's
			// necessary to check if the data received matches the current
			// selection.
			if (data.intervalStart != selStart.getTime()
					|| data.intervalEnd != selEnd.getTime())
				return;

			$scope.intervalStart = selStart;
			$scope.intervalEnd = selEnd;
			updateTopSql(data.topSql);
			updateTopSessions(data.topSessions);
		}

		$scope.series = series;
		$scope.preprocessor = function(json) {
			// Optimization: caches the default interval data (5 minutes) in
			// order to avoid unnecessary requests when the 'selectedInterval'
			// watcher is triggered.
			$scope.cachedAshData = json;
			return json.averageActiveSessions;
		};

		$scope.topQueriesCountOptions = [10, 25, 50, 100];
		$scope.selectedInterval = {
			topQueriesCount: 10
		};

		$scope.$watchCollection('selectedInterval', function() {
			var selStart = $scope.selectedInterval.start;
			var selEnd = $scope.selectedInterval.end;
			var topQueriesCount = $scope.selectedInterval.topQueriesCount;

			// Page initialization
			if (!selStart || !selEnd)
				return;

			// Cache hit
			if (selStart.getTime() == $scope.cachedAshData.intervalStart
					&& selEnd.getTime() == $scope.cachedAshData.intervalEnd
					&& topQueriesCount == $scope.cachedAshData.topQueriesCount) {
				updateIntervalData($scope.cachedAshData);
				return;
			}

			var url = 'ws/ash/ash-interval?start=' + selStart.getTime()
					+ '&end=' + selEnd.getTime() + '&topQueriesCount=' + topQueriesCount;
			$http.get(url).success(updateIntervalData);
		});
	}

	module.controller('AshCtrl', [ '$scope', '$http', AshCtrl ]);

})();
