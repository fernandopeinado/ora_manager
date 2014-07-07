(function() {

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
					'width' : width + '%',
					'background-color' : s[1]
				});
			}
		});
		return result;
	}

	function updateTopSql(data, $scope) {
		var top = data[0].activity;
		data.forEach(function(sql) {
			sql.percentageFixed = sql.percentageTotalActivity.toFixed(0);
			sql.aasFixed = sql.averageActiveSessions.toFixed(2);
			sql.activityBar = activityBar(sql.activityByWaitClass, top);
		});
		$scope.topSql = data;
	}

	function updateAasPlot(data, $scope, AverageActiveSessions, JqPlotHelper) {
		var targetId = 'ash_aas';
		var params = [ data, series, targetId ];
		JqPlotHelper.plot($scope, targetId, AverageActiveSessions.plotGraph,
				params);
	}

	function AshCtrl($scope, $interval, $http, AverageActiveSessions,
			JqPlotHelper) {

		function update() {
			$http.get('ws/ash/ash').success(
					function(json) {
						updateAasPlot(json.averageActiveSessions, $scope,
								AverageActiveSessions, JqPlotHelper);
						updateTopSql(json.topSql, $scope);
					});
		}

		update();
		var updatePromise = $interval(update, 60000);

		$scope.$on('$destroy', function() {
			$interval.cancel(updatePromise);
		});
	}

	var module = angular.module('ash', [ 'averageActiveSessions',
			'jqPlotHelper' ]);

	module.controller('AshCtrl', [ '$scope', '$interval', '$http',
			'AverageActiveSessions', 'JqPlotHelper', AshCtrl ]);

})();