(function() {

	var module = angular.module('ash-archive', [ 'averageActiveSessions',
			'jqPlotHelper' ]);

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

	function AshArchiveCtrl($scope, $http, AverageActiveSessions, JqPlotHelper) {

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

		function updateAasPlot(data) {
			if (data.data.length < 2) {
				JqPlotHelper.destroyAllPlots($scope);
				return;
			}
			var targetId = 'ash-archive-aas';
			var params = [ data, series, {}, targetId ];
			JqPlotHelper.plot($scope, targetId,
					AverageActiveSessions.plotGraph, params);
		}

		$scope.loadData = function() {
			var url = 'ws/ash/ash-archive/' + $scope.year + '/' + $scope.month;
			url += '/' + $scope.day + '/' + $scope.hour;

			$http.get(url).success(function(json) {
				updateTopSql(json.topSql);
				updateTopSessions(json.topSessions);
				updateAasPlot(json.averageActiveSessions);
			});
		}
	}

	module.controller('AshArchiveCtrl', [ '$scope', '$http',
			'AverageActiveSessions', 'JqPlotHelper', AshArchiveCtrl ]);

})();
