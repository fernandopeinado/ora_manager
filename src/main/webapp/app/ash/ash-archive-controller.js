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

		$scope.year = parseInt($routeParams.year);
		$scope.month = parseInt($routeParams.month);
		$scope.day = parseInt($routeParams.day);
		$scope.hour = parseInt($routeParams.hour);

		if ([ $scope.year, $scope.month, $scope.day, $scope.hour ].some(isNaN)) {
			var now = new Date(Date.now() - 60 * 60 * 1000);
			$location.path('/ash-archive/' + now.getFullYear() + '/'
					+ (now.getMonth() + 1) + '/' + now.getDate() + '/'
					+ now.getHours());
			return;
		}

		$scope.url = 'ws/ash/ash-archive/' + $scope.year + '/' + $scope.month
				+ '/' + $scope.day + '/' + $scope.hour;
		$scope.series = series;
		$scope.preprocessor = function(json) {
			updateTopSql(json.topSql);
			updateTopSessions(json.topSessions);
			return json.averageActiveSessions;
		};

		$scope.loadData = function() {
			$location.path('/ash-archive/' + $scope.year + '/' + $scope.month
					+ '/' + $scope.day + '/' + $scope.hour);
		};
	}

	module.controller('AshArchiveCtrl', [ '$scope', '$routeParams',
			'$location', AshArchiveCtrl ]);

})();
