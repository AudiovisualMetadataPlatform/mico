var brokerApp = angular.module('brokerApp', ['ui.bootstrap']);

brokerApp.controller("StatusCtrl", function($scope,$http) {

    $scope.services = [];

    $http.get("status/services").success(function(data) {
        $scope.services = data;
    });


    $scope.items = [];
    $http.get("status/items").success(function(data) {
        $scope.items = data;
    });
});