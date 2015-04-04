(function () {

    var module = angular.module('tablespaces', []);

    function TablespaceCtrl($scope, $routeParams, $http) {

        $http.get('ws/tablespaces').success(function (json) {
            $scope.usages = json.usages;
            $scope.totalMb = 0;
            $scope.usedMb = 0;
            $scope.freeMb = 0;
            for (var i in $scope.usages) {
                var usage = $scope.usages[i];
                usage.usagePct = Math.round(100 * (usage.usedMb / usage.totalMb));
                $scope.totalMb += usage.totalMb;
                $scope.usedMb += usage.usedMb;
                $scope.freeMb += usage.freeMb;
            }
        });
    }

    module.controller('TablespaceCtrl', ['$scope', '$routeParams', '$http',
        TablespaceCtrl]);

})();