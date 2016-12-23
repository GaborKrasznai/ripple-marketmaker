'use strict';

/**
 * @ngdoc function
 * @name staticApp.controller:MainCtrl
 * @description # MainCtrl Controller of the staticApp
 */
angular.module('staticApp').controller('MainCtrl', function($scope, $http) {
	// this.awesomeThings = [ 'HTML5 Boilerplate', 'AngularJS', 'Karma' ];

	$scope.instruments = {};
	$scope.instanceId;

	$scope.requestAvalancheSetup = function() {

		if ($scope.instanceId != null) {
			console.log($scope.instanceId);
			$http({
				method : 'GET',
				url : '/rippex/setup/' + $scope.instanceId
			}).then(function successCallback(response) {
				var values = angular.fromJson(response);
				console.log(values);
				if (values.data == null) {
					alert('Nao encontrado');
				} else {
					$scope.instruments = values.data;
				}
			}, function errorCallback(response) {
				alert(response);
			});
		}
	}

	$scope.saveAvalancheSetup = function() {

		if ($scope.instanceId != null) {
			$http({
				method : 'POST',
				url : '/rippex/setup/' + $scope.instanceId,
				data : $scope.instruments
			}).then(function successCallback(response) {
				alert(response);
			}, function errorCallback(response) {
				alert(response);
			});
		}
	}

	// $scope.instruments;
	// $scope.value;
	// $scope.key;
	//
	// $scope.requestInstruments = function() {
	//
	// $http({
	// method : 'GET',
	// url : '/rippex/instruments',
	// data : {}
	// }).then(function successCallback(response) {
	// var values = angular.fromJson(response);
	// $scope.instruments = values.data;
	// console.log($scope.instruments);
	// }, function errorCallback(response) {
	//
	// });
	// }
	//	
	// $scope.select = function(a) {
	// console.log(a);
	// }

	// $scope.updateInstrument = function() {
	// $http({
	// method : 'POST',
	// url : '/rippex/instruments',
	// headers: {
	// 'Content-Type': "application/json"
	// },
	// data : angular.toJson($scope.instruments)
	// }).then(function successCallback(response) {
	// $scope.requestInstruments();
	// }, function errorCallback(response) {
	//
	// });
	// }
	//
	// $scope.requestInstruments();

});
