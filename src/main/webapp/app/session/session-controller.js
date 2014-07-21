(function() {

	var module = angular.module('session', []);

	function SessionCtrl($scope, $routeParams, $http) {
		$scope.sid = $routeParams.sid;
		$scope.serialNumber = $routeParams.serialNumber;
		$scope.message = null;
		$scope.messageClass = '';

		function setMessage(message, messageClass) {
			$scope.message = message;
			$scope.messageClass = messageClass;
		}

		var params = $.param({
			sid : $scope.sid,
			serialNumber : $scope.serialNumber
		});

		$http.get('ws/session?' + params).success(function(json) {
			// AngularJS - Issue #2191
			if (!json || json == 'null') {
				setMessage('Session not found', 'alert-warning');
				return;
			}
			$scope.user = json.user;
			$scope.program = json.program;
			$scope.sessionTerminationEnabled = json.sessionTerminationEnabled;
		});

		$scope.killSession = function() {
			$http.post('ws/session/kill', params, {
				headers : {
					'Content-Type' : 'application/x-www-form-urlencoded'
				}
			}).success(function() {
				setMessage('Session killed', 'alert-info');
			}).error(function(response) {
				setMessage('Error', 'alert-danger');
			});
		}
	}

	module.controller('SessionCtrl', [ '$scope', '$routeParams', '$http',
			SessionCtrl ]);

})();