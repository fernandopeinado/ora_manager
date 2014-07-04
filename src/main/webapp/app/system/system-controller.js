(function() {

	var module = angular.module('system', []);

	function SystemCtrl($scope, $http) {

		$http.get('ws/system/info').success(function(info) {
			$scope.info = info;
		});
	}

	module.controller('SystemCtrl', [ '$scope', '$http', SystemCtrl ]);

})();