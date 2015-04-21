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

    if ($scope.sid) {
      var params = $.param({
        sid: $scope.sid,
        serialNumber: $routeParams.serialNumber
      });

      $http.get('ws/session?' + params).success(function(json) {
        $scope.instanceNumber = json.instanceNumber;
        $scope.sessionTerminationEnabled = json.sessionTerminationEnabled;
        $scope.result = json.result;
        switch ($scope.result) {
          case 'sessionFound':
            $scope.session = json.session;
            break;
          case 'multipleSessionsFound':
            $scope.candidates = json.candidates;
            break;
          case 'sessionNotFound':
            setMessage('Session not found', 'alert-warning');
            break;
        }
      });
    } else {
      $http.get('ws/sessions').success(function(json) {
        $scope.instanceNumber = json.instanceNumber;
        $scope.sessionTerminationEnabled = json.sessionTerminationEnabled;
        $scope.sessions = json.sessions;
      });
    }

    $scope.killSession = function() {
      var params = $.param({
        sid: $scope.session.sid,
        serialNumber: $scope.session.serialNumber
      });
      $http.post('ws/session/kill', params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }).success(function() {
        setMessage('Session killed', 'alert-info');
      }).error(function(response) {
        setMessage('Error', 'alert-danger');
      });
    }
  }

  module.controller('SessionCtrl', ['$scope', '$routeParams', '$http',
    SessionCtrl]);

})();