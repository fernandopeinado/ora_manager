(function() {

	var mainApp = angular.module('mainApp', [ 'home', 'ash', 'sql', 'system',
			'ngRoute' ]);

	mainApp.config([ '$routeProvider', function($routeProvider) {
		$routeProvider.when('/', {
			templateUrl : 'app/brand/brand.html',
		}).when('/home', {
			templateUrl : 'app/home/home.html',
			controller : 'HomeCtrl'
		}).when('/ash', {
			templateUrl : 'app/ash/ash.html',
			controller : 'AshCtrl'
		}).when('/sql/:sqlId', {
			templateUrl : 'app/sql/sql.html',
			controller : 'SqlCtrl'
		}).when('/system', {
			templateUrl : 'app/system/system.html',
			controller : 'SystemCtrl'
		}).otherwise({
			redirectTo : '/'
		});
	} ]);

})();
