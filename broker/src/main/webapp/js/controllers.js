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

    $scope.version = "Broker Version 2.x";
    var config = {headers: {
        'Accept': 'text/plain'
    }
};
    $http.get("status/info",config).success(function(data) {
        $scope.version = data;
    });

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

    $scope.$watch('itemUri', function() {
        $http.get("status/items?parts=true&uri=" + $scope.itemUri).success(function (data) {
            $scope.itemData = data[0];
        });
    });
});


brokerApp.controller("InjectItemCtrl", function($scope,$http,$upload) {

    $scope.itemUri;
    $scope.itemAssetfiles;
    $scope.itemAssetType;
    $scope.itemAssetName;
    $scope.state = "Created";
    $scope.itemAssetLocation
    $scope.itemCreated
    $scope.alerts = [
//                     { type: 'danger', msg: 'Oh snap! Change a few things up and try submitting again.' },
//                     { type: 'success', msg: 'Well done! You successfully read this important alert message.' }
                   ];
    $scope.workflowDescriptions = [];

     $http.get("workflow/routes").success(function(data) {

         var response = [];

         /** TODO Change Webservice to return "meaningful" data, to avoid the following...." */
         var workflowIds = Object.keys(data);
         for (var i=0; i< workflowIds.length; i++)  {
             response.push(
                 {id: workflowIds[i], description: data[workflowIds[i]]}
             )

         }

         $scope.workflowDescriptions=response;
        });



    $scope.createItem = function() {

        var file = $scope.itemAssetfiles[0];
        var fileReader = new FileReader();
        fileReader.readAsArrayBuffer(file);
        fileReader.onload = function(e) {
            $upload.http({
                url: "inject/create" + "?type=" + $scope.itemAssetType + "&name=" + file.name,
                method: 'POST',
                data: e.target.result
            }).success(function (data) {
                $scope.itemUri = data["itemUri"];
                $scope.assetLocation = data["assetLocation"];
                $scope.itemCreated = data["created"];
            });
        }
    };

    $scope.submitItem = function() {
        $http.post("inject/submit?item=" + $scope.itemUri).success(function() {
            $scope.state = "Submitted";
        });
    };


    $scope.submitItem3 = function() {

        var routeId = document.getElementById("workflowSelector").value;

        var injectUrl = "inject/submit?item=" + $scope.itemUri
            + "&route=" + routeId;

        console.log("Calling: " + injectUrl);

        $http.post(
            injectUrl
        ).success(function(msg) {
            $scope.state = "Submitted";
            $scope.addAlert('success',msg.message);
        }).error(function(msg){
            $scope.addAlert('danger',msg.message);
        });
    };

    $scope.addAlert = function(t, msg) {
        $scope.alerts.push({type: t, msg: msg});
      };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
      };
});