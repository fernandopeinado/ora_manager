(function() {

	var module = angular.module('ash-archive', [ 'averageActiveSessions' ]);

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

	var dateRegex = /^(\d{4})-(\d{1,2})-(\d{1,2}) (\d{1,2}):(\d{1,2}):(\d{1,2})$/;

	function dateToString(date) {
		var pad = n => n < 10 ? '0' + n : n;
		return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
				+ ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':'
				+ pad(date.getSeconds());
	}

	function parseDate(string) {
		var match = string.trim().match(dateRegex);
		if (match) {
			var components = match.slice(1)
					.map(x => parseInt(x.startsWith('0') ? x.substring(1) : x));
			components[1] -= 1; // month
			return new Date(...components);
		}
		throw 'Not a valid date: ' + string;
	}

	function AshArchiveCtrl($scope, $routeParams, $location) {

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

		$scope.dateRegex = dateRegex;
		$scope.intervalStart = parseInt($routeParams.start);
		$scope.intervalEnd = parseInt($routeParams.end);

		if (isNaN($scope.intervalStart) || isNaN($scope.intervalEnd)) {
			var end = new Date();
			end.setMinutes(0, 0, 0);
			end = end.getTime();
			var start = end - 60 * 60 * 1000;

			$location.search({ start: start, end: end });
			return;
		}

		$scope.intervalStartString = dateToString(new Date($scope.intervalStart));
		$scope.intervalEndString = dateToString(new Date($scope.intervalEnd));

		$scope.url = 'ws/ash/ash-archive?start=' + $scope.intervalStart
				+ '&end=' + $scope.intervalEnd;
		$scope.series = series;
		$scope.preprocessor = function(json) {
			updateTopSql(json.topSql);
			updateTopSessions(json.topSessions);
			return json.averageActiveSessions;
		};

		$scope.loadData = function() {
			$location.search({
				start: parseDate($scope.intervalStartString).getTime(),
				end: parseDate($scope.intervalEndString).getTime()
			});
		};

		$scope.goForward = function() {
			var shift = $scope.intervalEnd - $scope.intervalStart;
			if (shift > 0) {
				$location.search({
					start: $scope.intervalStart + shift, end: $scope.intervalEnd + shift
				});
			}
		};

		$scope.goBack = function() {
			var shift = $scope.intervalEnd - $scope.intervalStart;
			if (shift > 0) {
				$location.search({
					start: $scope.intervalStart - shift, end: $scope.intervalEnd - shift
				});
			}
		};
	}

	module.controller('AshArchiveCtrl', [ '$scope', '$routeParams',
			'$location', AshArchiveCtrl ]);

})();
