(function() {

  var module = angular.module('sql', []);

  module.filter('microsToSeconds', function() {
    return function(micros) {
      return (micros / 1000000).toFixed(2);
    };
  });

  module.filter('perExecution', function() {
    return function(value, executions) {
      return (value / executions).toFixed(2);
    };
  });

  var series = [['CPU', '#04ce04', 'cpuTime'],
    ['User I/O', '#044ae4', 'userIoWaitTime'],
    ['Concurrency', '#8b1a04', 'concurrencyWaitTime'],
    ['Application', '#bd2b06', 'applicationWaitTime'],
    ['Cluster', '#cec4a7', 'clusterWaitTime'],
    ['Remaining Waits', '#e46a04', 'remainingWaitsTime']];

  function activityBar(plan, total) {
    var result = [];
    series.forEach(function(s) {
      var width = Math.floor((plan[s[2]] * 100) / total);
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

  function preparePlan(plan) {
    var sum = plan.cpuTime + plan.userIoWaitTime + plan.concurrencyWaitTime;
    sum += plan.applicationWaitTime + plan.clusterWaitTime;

    plan.remainingWaitsTime = Math.max(plan.elapsedTime - sum, 0);
    plan.activityBar = activityBar(plan, Math.max(plan.elapsedTime, sum));
  }

  function SqlCtrl($scope, $routeParams, $http) {

    $http.get('ws/sql/' + $routeParams.sqlId).success(function(json) {
      json.executionPlans.forEach(preparePlan);
      $scope.sql = json;

      if (!json.fullText) {
        json.fullText = 'Not Available';
      }
    });
  }

  var c = ['$scope', '$routeParams', '$http', SqlCtrl];
  module.controller('SqlCtrl', c);

})();