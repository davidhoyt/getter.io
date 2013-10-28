
var config = new function() {
  this.debug = function() {
    return true;
  };

  this.adminSettingsURI = function() {
    return "/admin/settings";
  };

  this.log = function(msg) {
    if (this.debug() && !(typeof(console) === 'undefined'))
      console.log(msg);
  };
};

var getter_io = angular.module('getter-io', ['ui.bootstrap']);



getter_io.controller('AdminController', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
  $scope.addRepository = function() {
    alert('hi');
  };

  $scope.repositories = [
    {
      "serverPrefix": "/foo",
      "cloneURL": "http://google.com/"
    },
    {
      "serverPrefix": "/bar",
      "cloneURL": "http://yahoo.com/"
    }
  ];

  $scope.refreshSettings = function(callback) {
    return $http({ method: 'GET', url: config.adminSettingsURI() }).success(function(data) {
      config.log(data);

      $scope.settings = data;
      $scope.rootDirectory = data.rootDirectory;
    }).then(function(data) {
      if (callback != null) {
        callback($scope);
      }
    });
  }

  $scope.run = function() {
    $scope.refreshSettings(null);
  };

  $scope.run();
}]);


function AlertDemoCtrl($scope) {
    $scope.alerts = [
        { type: 'error', msg: 'Oh snap! Change a few things up and try submitting again.' },
        { type: 'success', msg: 'Well done! You successfully read this important alert message.' }
    ];

    $scope.addAlert = function() {
        $scope.alerts.push({msg: "Another alert!"});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

}

function AccordionDemoCtrl($scope) {
    $scope.oneAtATime = true;

    $scope.groups = [
        {
            title: "Dynamic Group Header - 1",
            content: "Dynamic Group Body - 1"
        },
        {
            title: "Dynamic Group Header - 2",
            content: "Dynamic Group Body - 2"
        }
    ];

    $scope.items = ['Item 1', 'Item 2', 'Item 3'];

    $scope.addItem = function() {
        var newItemNo = $scope.items.length + 1;
        $scope.items.push('Item ' + newItemNo);
    };
}

var ButtonsCtrl = function ($scope) {

    $scope.singleModel = 1;

    $scope.radioModel = 'Middle';

    $scope.checkModel = {
        left: false,
        middle: true,
        right: false
    };
};

var ModalDemoCtrl = function ($scope, $modal, $log) {

    $scope.items = ['item1', 'item2', 'item3'];

    $scope.open = function () {

        var modalInstance = $modal.open({
            templateUrl: 'myModalContent.html',
            controller: ModalInstanceCtrl,
            resolve: {
                items: function () {
                    return $scope.items;
                }
            }
        });

        modalInstance.result.then(function (selectedItem) {
            $scope.selected = selectedItem;
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });
    };
};

var ModalInstanceCtrl = function ($scope, $modalInstance, items) {

    $scope.items = items;
    $scope.selected = {
        item: $scope.items[0]
    };

    $scope.ok = function () {
        $modalInstance.close($scope.selected.item);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};