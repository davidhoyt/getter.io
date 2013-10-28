
var config = {
  debug: true,

  adminSettingsURI: function() {
    return "/admin/v1/settings";
  },
  updateGitRepositoryURI: function() {
    return "/admin/v1/update"
  },
  clearGitRepositoryURI: function() {
    return "/admin/v1/clear"
  },
  log: function(msg) {
    if (this.debug && !(typeof(console) === 'undefined'))
      console.log(msg);
  }
};



var getter_io = angular.module('getter-io', ['ui.bootstrap']);

getter_io.controller('AppController', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {

}]);

getter_io.controller('AdminController', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
  $scope.alerts = [];

  $scope.original = $scope.working = {
    rootDirectory: '',
    git: {
      repositories: []
    }
  };

  $scope.anyUnsavedChanges = function() {
    if (typeof($scope.working) === 'undefined' || typeof($scope.original) === 'undefined')
      return false;
    var result = !angular.equals($scope.working, $scope.original);
    //config.log("Checking for unsaved changes...");
    return result;
  };

  $scope.undoChanges = function() {
    $scope.working = angular.copy($scope.original);
  };

  $scope.addAlert = function(success, msg, timeout) {
    var alert = { type: (success ? 'success' : 'error'), msg: msg };

    $scope.alerts.push(alert);

    if (timeout) {
      $timeout(function() {
        $scope.closeAlert($scope.alerts.indexOf(alert));
      }, timeout);
    }
  };

  $scope.closeAlert = function(index) {
    $scope.alerts.splice(index, 1);
  };

  $scope.createEmptyGitRepositoryInstance = function() {
    return {
      serverPrefix: '/',
      cloneURL: ''
    };
  };

  $scope.addRepository = function() {
    $scope.working.git.repositories.push($scope.createEmptyGitRepositoryInstance());
  };

  $scope.updateGitRepository = function(repo) {
    var prefix = repo.serverPrefix;
    var data = { serverPrefix: prefix };

    config.log("POSTing the following to " + config.updateGitRepositoryURI());
    config.log(data);

    $http({
      url: config.updateGitRepositoryURI(),
      method: 'POST',
      data: data,
      headers: {'Content-type': 'application/json'}
    })
    .success(function(data, status, headers, conf) {
      config.log(data);
      $scope.addAlert(true, prefix + " has been updated successfully", 10 * 1000);
    })
    .error(function(data, status, headers, conf) {
      config.log(data);
      $scope.addAlert(false, prefix + " was unable to be updated successfully: " + data.message, 10 * 1000);
    });
  };

  $scope.deleteGitRepository = function(repo) {
    config.log("Removing the following...");
    config.log(repo);

    $scope.working.git.repositories.splice(
      $scope.working.git.repositories.indexOf(alert),
      1
    );
  };

  $scope.clearGitRepository = function(repo) {
    var prefix = repo.serverPrefix;
    var data = { serverPrefix: prefix };

    config.log("POSTing the following to " + config.clearGitRepositoryURI());
    config.log(data);

    $http({
      url: config.clearGitRepositoryURI(),
      method: 'POST',
      data: data,
      headers: {'Content-type': 'application/json'}
    })
    .success(function(data, status, headers, conf) {
      config.log(data);
      $scope.addAlert(true, prefix + " has been cleared successfully", 10 * 1000);
    })
    .error(function(data, status, headers, conf) {
      config.log(data);
      $scope.addAlert(false, prefix + " was unable to be cleared successfully: " + data.message, 10 * 1000);
    });
  };

  $scope.refreshSettings = function(callback) {
    return $http({ method: 'GET', url: config.adminSettingsURI() }).success(function(data) {
      config.log(data);

      $scope.original = data;
      $scope.working = angular.copy(data);
    }).then(function(data) {
      if (callback != null) {
        callback($scope);
      }
    });
  }

  $scope.saveSettings = function() {
    config.log("POSTing the following...");
    config.log($scope.working);
    $http({
      url: config.adminSettingsURI(),
      method: 'POST',
      data: $scope.working,
      headers: {'Content-type': 'application/json'}
    })
    .success(function(data, status, headers, conf) {
      config.log(data);
      $scope.original = angular.copy($scope.working);
      $scope.addAlert(true, "Settings saved successfully", 10 * 1000);
    })
    .error(function(data, status, headers, conf) {
        config.log(data);
        $scope.addAlert(false, "Unable to save successfully: " + data.message, 10 * 1000);
    });
  };

  $scope.run = function() {
    $scope.refreshSettings(null);
  };

  $scope.run();
}]);
