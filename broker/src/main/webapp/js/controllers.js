/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var brokerApp = angular.module('brokerApp', ['ui.bootstrap','angularFileUpload']);

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.href);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

brokerApp.controller("StatusCtrl", function($scope,$http) {
    function updateFilteredItems() {
      var begin = (($scope.currentPage - 1) * $scope.numPerPage),
          end = begin + $scope.numPerPage;
      $scope.filteredItems = $scope.items.slice(begin, end);
    };

    $scope.services = [];
    $http.get("status/services").success(function(data) {
        $scope.services = data;
    });

     $scope.items = []
    ,$scope.filteredItems = []
    ,$scope.currentPage = 1
    ,$scope.numPerPage = 10
    ,$scope.maxSize = 5;
    $http.get("status/items?parts=false").success(function(data) {
        $scope.items = data;
        updateFilteredItems();
    });
    $scope.$watch('currentPage + numPerPage', updateFilteredItems);
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