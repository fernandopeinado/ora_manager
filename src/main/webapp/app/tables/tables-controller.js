(function () {

  var module = angular.module('tables', []);

  function TablesCtrl($scope, $routeParams, $http) {
    $scope.schemas;
    $scope.schema;
    $scope.tables;
    $scope.filteredTables;
    $scope.currentPage = 0;
    $scope.pageSize = 14;
    $scope.totalTables = 0;
    $scope.totalPages = 0;
    $scope.page = null;
    $scope.message;
    $scope.tableFilter = "";

    $scope.goToPage = function (page) {
      $scope.newPageSize = $scope.pageSize;
      if (typeof page == "string") {
        page = parseInt(page) - 1;
      }
      var pageSize = $scope.pageSize;

      $scope.page = null;
      $scope.currentPage = page;
      if ($scope.totalPages > page) {
        var idx = page * pageSize;
        var stopIdx = idx + pageSize;
        if (stopIdx >= $scope.totalTables) {
          stopIdx = $scope.totalTables;
        }
        var newPage = [];
        for (idx; idx < stopIdx; idx++) {
          newPage.push($scope.filteredTables[idx]);
        }
        $scope.page = newPage;
      }
      else {
        $scope.message = "Page not found";
      }
    }

    $scope.nextPage = function () {
      if ($scope.currentPage + 1 <= $scope.totalPages) {
        $scope.goToPage($scope.currentPage + 1);
      }
    }

    $scope.previousPage = function () {
      if ($scope.currentPage - 1 >= 0) {
        $scope.goToPage($scope.currentPage - 1);
      }
    }

    $scope.changePageSize = function (newSize) {
      $scope.pageSize = newSize;
      $scope.totalPages = Math.ceil($scope.totalTables / $scope.pageSize);
      $scope.goToPage(0);
    }

    $scope.filterTables = function () {
      if (!$scope.tableFilter) {
        $scope.filteredTables = $scope.tables;
      }
      else {
        console.log('filtrando...');
        var newFilteredTables = [];
        for (var i in $scope.tables) {
          var t = $scope.tables[i];
          var tirar = false;
          if ($scope.tableFilter && t.name.toUpperCase().indexOf($scope.tableFilter.toUpperCase()) != 0) {
            tirar = true;
          }
          if (!tirar) {
            newFilteredTables.push(t);
          }
        }
        console.log(newFilteredTables)
        $scope.filteredTables = newFilteredTables;
        console.log('filtrou...');
      }
      if ($scope.filteredTables) {
        $scope.totalTables = $scope.filteredTables.length;
        $scope.totalPages = Math.ceil($scope.totalTables / $scope.pageSize);
      }
    }

    $scope.loadTables = function () {
      $http.get('ws/tables/' + $scope.schema).success(function (json) {
        $scope.tables = json.tables;
        $scope.filterTables();
        $scope.goToPage(0);
      });
    }

    $scope.$watch('tableFilter', function (newValue, oldValue) {
      $scope.filterTables();
      $scope.goToPage(0);
    });

    $http.get('ws/schemas').success(function (json) {
      $scope.schemas = json.schemas;
    });

  }

  module.controller('TablesCtrl', ['$scope', '$routeParams', '$http',
    TablesCtrl]);

})();