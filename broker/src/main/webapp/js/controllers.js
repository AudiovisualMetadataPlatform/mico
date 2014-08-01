var brokerApp = angular.module('brokerApp', ['ui.bootstrap']);

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.href);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

brokerApp.controller("StatusCtrl", function($scope,$http) {

    $scope.services = [];

    $http.get("status/services").success(function(data) {
        $scope.services = data;
    });


    $scope.items = [];
    $http.get("status/items?parts=false").success(function(data) {
        $scope.items = data;
    });
});


brokerApp.controller("ShowItemCtrl", function($scope,$http) {

    $scope.itemUri = getParameterByName("uri");

    $scope.itemData;


    $scope.$watch('itemUri', function(newValue, oldValue) {

        $http.get("status/items?parts=true&uri=" + $scope.itemUri).success(function (data) {
            $scope.itemData = data[0];
        });
    });

});