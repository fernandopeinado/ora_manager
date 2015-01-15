(function() {

	var module = angular.module('session', []);

	function SessionCtrl($scope, $routeParams, $http) {
		$scope.sid = $routeParams.sid;
		$scope.message = null;
		$scope.messageClass = '';

		function setMessage(message, messageClass) {
			$scope.message = message;
			$scope.messageClass = messageClass;
		}

		var params = $.param({
			sid : $scope.sid,
			serialNumber : $routeParams.serialNumber
		});

		$http.get('ws/session?' + params).success(function(json) {
			$scope.status = json.status;
			if (json.status == 'sessionFound') {
				$scope.session = json.session;
			} else if (json.status == 'multipleSessionsFound') {
				$scope.sessions = json.sessions;
			} else if (json.status == 'sessionNotFound') {
				setMessage('Session not found', 'alert-warning');
			}
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