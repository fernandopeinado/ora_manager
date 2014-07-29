(function() {

	var mainApp = angular.module('mainApp', [ 'home', 'ash', 'ash-archive',
			'sql', 'session', 'system', 'ngRoute' ]);

	mainApp.config([ '$routeProvider', function($routeProvider) {
		$routeProvider.when('/', {
			templateUrl : 'app/brand/brand.html',
		}).when('/home', {
			templateUrl : 'app/home/home.html',
			controller : 'HomeCtrl'
		}).when('/ash', {
			templateUrl : 'app/ash/ash.html',
			controller : 'AshCtrl'
		}).when('/ash-archive', {
			templateUrl : 'app/ash/ash-archive.html',
			controller : 'AshArchiveCtrl'
		}).when('/sql/:sqlId', {
			templateUrl : 'app/sql/sql.html',
			controller : 'SqlCtrl'
		}).when('/session', {
			templateUrl : 'app/session/session.html',
			controller : 'SessionCtrl'
		}).when('/system', {
			templateUrl : 'app/system/system.html',
			controller : 'SystemCtrl'
		}).otherwise({
			redirectTo : '/'
		});
	} ]);

})();
