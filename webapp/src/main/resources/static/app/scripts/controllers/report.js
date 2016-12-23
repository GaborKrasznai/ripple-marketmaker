'use strict';

/**
 * @ngdoc function
 * @name staticApp.controller:ReportCtrl
 * @description # ReportCtrl Controller of the staticApp
 */
angular.module('staticApp').controller('ReportCtrl', function($scope, $http) {
	this.awesomeThings = [ 'HTML5 Boilerplate', 'AngularJS', 'Karma' ];

	$scope.accountOffers;
	$scope.requestAccountOffers = function() {
		$http({
			method : 'POST',
			url : '/rippex/accountoffers',
			headers : {
				'Content-Type' : "application/json"
			},
			data : {
				key : $scope.key,
				value : $scope.value
			}
		}).then(function successCallback(response) {
			var values = angular.fromJson(response);
			$scope.accountOffers = values.data;
			console.log(values.data);
		}, function errorCallback(response) {

		});
	}

	$scope.avalanche;
	$scope.requestAvalanche = function() {
		$http({
			method : 'POST',
			url : '/rippex/avalanche',
			headers : {
				'Content-Type' : "application/json"
			},
			data : {
				key : $scope.key,
				value : $scope.value
			}
		}).then(function successCallback(response) {
			var values = angular.fromJson(response);
			$scope.avalanche = values.data;
			console.log(values.data);
		}, function errorCallback(response) {

		});
	}
	
	$scope.requestAccountOffers();
	$scope.requestAvalanche();

});
