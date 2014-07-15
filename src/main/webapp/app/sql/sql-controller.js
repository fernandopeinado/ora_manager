(function() {

	var module = angular.module('sql', []);

	function SqlCtrl($scope, $routeParams, $http) {
		$http.get('ws/sql/' + $routeParams.sqlId).success(function(json) {
			$scope.executionPlans = json.executionPlans;
		});
	}

	var c = [ '$scope', '$routeParams', '$http', SqlCtrl ];
	module.controller('SqlCtrl', c);

})();