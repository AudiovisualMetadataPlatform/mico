var brokerApp = angular.module('brokerApp', ['ui.bootstrap','angularFileUpload']);

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


brokerApp.controller("InjectItemCtrl", function($scope,$http,$upload) {

    $scope.itemUri;

    $scope.itemData;

    $scope.state = "Created";

    $scope.files;

    $scope.type="";

    $scope.updateItem = function() {
        if($scope.itemUri) {
            $http.get("inject/items?parts=true&uri=" + $scope.itemUri).success(function (data) {
                $scope.itemData = data[0];
            });
        }

        $scope.type="";
        $scope.files=[];
        document.getElementById("file").value = '';
    };

    $scope.createContentItem = function() {
        $http.post("inject/create").success(function(data) {
            $scope.itemUri = data["uri"];
        });
    };

    $scope.onFileSelect = function($files) {
        $scope.files = $files;
        if($files.length > 0) {
            $scope.type = $files[0].type;
        }
    };

    $scope.addContentPart = function(){
        for (var i = 0; i < $scope.files.length; i++) {
            var file = $scope.files[i];
            var fileReader = new FileReader();
            fileReader.readAsArrayBuffer(file);
            fileReader.onload = function(e) {
                $upload.http({
                    url: "inject/add?ci=" + $scope.itemUri + "&type=" + $scope.type + "&name=" + file.name,
                    method: 'POST',
                    data: e.target.result
                }).success(function (data) {
                    $scope.updateItem();
                });
            }
        }

    };

    $scope.submitContentItem = function() {
        $http.post("inject/submit?ci=" + $scope.itemUri).success(function() {
            $scope.state = "Submitted";
        });
    };

    $scope.$watch('itemUri', function(newValue, oldValue) {
        $scope.updateItem();
    });

});