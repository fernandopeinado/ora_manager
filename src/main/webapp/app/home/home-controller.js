(function() {

	var module = angular.module('home', [ 'averageActiveSessions',
			'jqPlotHelper' ]);

	function HomeCtrl($scope, $interval, $http, AverageActiveSessions,
			JqPlotHelper) {

		function plotAasGraph() {
			var series = [ [ 'CPU', '#04ce04' ], [ 'Scheduler', '#87fd88' ],
					[ 'User I/O', '#044ae4' ], [ 'System I/O', '#0993e1' ],
					[ 'Concurrency', '#8b1a04' ], [ 'Application', '#bd2b06' ],
					[ 'Commit', '#e46a04' ], [ 'Configuration', '#584615' ],
					[ 'Administrative', '#73745e' ], [ 'Network', '#9b9378' ],
					[ 'Queueing', '#cbb796' ], [ 'Cluster', '#cec4a7' ],
					[ 'Other', '#f070ac' ] ];
			var targetId = 'cumulative_times_aas';

			$http.get('ws/home/average-active-sessions').success(
					function(json) {
						JqPlotHelper.plot($scope, targetId,
								AverageActiveSessions.plotGraph, [ json,
										series, targetId ]);
					});
		}

		plotAasGraph();
		var plotAasGraphPromise = $interval(plotAasGraph, 60000);

		$scope.$on('$destroy', function() {
			$interval.cancel(plotAasGraphPromise);
		});
	}

	module.controller('HomeCtrl', [ '$scope', '$interval', '$http',
			'AverageActiveSessions', 'JqPlotHelper', HomeCtrl ]);

})();