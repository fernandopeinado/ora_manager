(function() {

  var module = angular.module('session', []);

  function SessionCtrl($scope, $routeParams, $http) {
    $scope.sid = $routeParams.sid;

    function sqlActivityBar(activityMap, topActivity) {
      var result = [];
      $scope.series.forEach(function(s) {
        var width = Math.floor((activityMap[s[0]] * 100) / topActivity);
        if (width > 0) {
          result.push({
            title: s[0],
            style: {
              'width': width + '%',
              'background-color': s[1]
            }
          });
        }
      });
      return result;
    }

    function fillTopSql(data) {
      var top = (data.length > 0) ? data[0].activity : null;
      data.forEach(function(sql) {
        sql.percentageFixed = sql.percentageTotalActivity.toFixed(0);
        sql.aasFixed = sql.averageActiveSessions.toFixed(2);
        sql.activityBar = sqlActivityBar(sql.activityByEvent, top);
      });
      $scope.topSql = data;
    }

    $scope.series = [];
    $scope.preprocessor = function(json) {
      var colors = d3.scale.category20();
      json.keys.forEach(function(key, i) {
        $scope.series.push([key, colors(i)]);
      });
      fillTopSql(json.topSql);
      return json;
    };

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
    };
  }

  module.controller('SessionCtrl', ['$scope', '$routeParams', '$http',
    SessionCtrl]);

})();