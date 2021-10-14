angular.module('aac.controllers.realmservices', [])

  .service('RealmServices', function ($q, $http) {
    var rsService = {};

    rsService.getServices = function (realm) {
      return $http.get('console/dev/realms/' + realm + '/services').then(function (data) {
        return data.data;
      });
    }
    rsService.getService = function (realm, serviceId) {
      return $http.get('console/dev/realms/' + realm + '/services/' + serviceId).then(function (data) {
        return data.data;
      });
    }

    rsService.addService = function (realm, service) {
      return $http.post('console/dev/realms/' + realm + '/services', service).then(function (data) {
        return data.data;
      });
    }
    rsService.updateService = function (realm, service) {
      return $http.put('console/dev/realms/' + realm + '/services/' + service.serviceId, service).then(function (data) {
        return data.data;
      });
    }
    rsService.deleteService = function (realm, serviceId) {
      return $http.delete('console/dev/realms/' + realm + '/services/' + serviceId).then(function (data) {
        return data.data;
      });
    }
    rsService.importService = function (realm, file) {
      var fd = new FormData();
      fd.append('file', file);
      return $http({
        url: 'console/dev/realms/' + realm + '/services',
        headers: { "Content-Type": undefined }, //set undefined to let $http manage multipart declaration with proper boundaries
        data: fd,
        method: "PUT"
      }).then(function (data) {
        return data.data;
      });
    }
    rsService.checkServiceNamespace = function (realm, serviceNs) {
      return $http.get('console/dev/realms/' + realm + '/nsexists?ns=' + encodeURIComponent(serviceNs)).then(function (data) {
        return data.data;
      });
    }

    //claims
    rsService.getClaims = function (realm, serviceId) {
      return $http.get('console/dev/realms/' + realm + '/services/' + serviceId + '/claims').then(function (data) {
        return data.data;
      });
    }
    rsService.addClaim = function (realm, serviceId, claim) {
      return $http.post('console/dev/realms/' + realm + '/services/' + serviceId + '/claims', claim).then(function (data) {
        return data.data;
      });
    }
    rsService.updateClaim = function (realm, serviceId, id, claim) {
      return $http.put('console/dev/realms/' + realm + '/services/' + serviceId + '/claims/' + id, claim).then(function (data) {
        return data.data;
      });
    }
    rsService.deleteClaim = function (realm, serviceId, key) {
      return $http.delete('console/dev/realms/' + realm + '/services/' + serviceId + '/claims/' + key).then(function (data) {
        return data.data;
      });
    }
    rsService.validateClaims = function (realm, serviceId, mapping) {
      return $http.post('console/dev/realms/' + realm + '/services/' + serviceId + '/claims/validate', mapping).then(function (data) {
        return data.data;
      });
    }

    //scopes
    rsService.getScopes = function (realm, serviceId) {
      return $http.get('console/dev/realms/' + realm + '/services/' + serviceId + '/scopes').then(function (data) {
        return data.data;
      });
    }
    rsService.addScope = function (realm, serviceId, scope) {
      return $http.post('console/dev/realms/' + realm + '/services/' + serviceId + '/scopes', scope).then(function (data) {
        return data.data;
      });
    }
    rsService.updateScope = function (realm, serviceId, id, scope) {
      return $http.put('console/dev/realms/' + realm + '/services/' + serviceId + '/scopes/' + id, scope).then(function (data) {
        return data.data;
      });
    }
    rsService.deleteScope = function (realm, serviceId, scope) {
      return $http.delete('console/dev/realms/' + realm + '/services/' + serviceId + '/scopes/' + scope).then(function (data) {
        return data.data;
      });
    }

    //approvals
    rsService.getApprovals = function (realm, serviceId) {
      return $http.get('console/dev/realms/' + realm + '/services/' + serviceId + '/approvals').then(function (data) {
        return data.data;
      });
    }

    rsService.addApproval = function (realm, serviceId, approval) {
      return $http.post('console/dev/realms/' + realm + '/services/' + serviceId + '/scopes/' + approval.scope + '/approvals?clientId=' + approval.clientId).then(function (data) {
        return data.data;
      });
    }
    rsService.deleteApproval = function (realm, serviceId, approval) {
      return $http.delete('console/dev/realms/' + realm + '/services/' + serviceId + '/scopes/' + approval.scope + '/approvals?clientId=' + approval.clientId).then(function (data) {
        return data.data;
      });
    }

    //clients
    rsService.getClients = function (realm, serviceId) {
      return $http.get('console/dev/realms/' + realm + '/services/' + serviceId + '/clients').then(function (data) {
        return data.data;
      });
    }
    rsService.getClient = function (realm, serviceId, clientId) {
      return $http.get('console/dev/realms/' + realm + '/services/' + serviceId + '/clients/' + clientId).then(function (data) {
        return data.data;
      });
    }
    rsService.addClient = function (realm, serviceId, client) {
      return $http.post('console/dev/realms/' + realm + '/services/' + serviceId + '/clients', client).then(function (data) {
        return data.data;
      });
    }
    rsService.deleteClient = function (realm, serviceId, clientId) {
      return $http.delete('console/dev/realms/' + realm + '/services/' + serviceId + '/clients/' + clientId).then(function (data) {
        return data.data;
      });
    }

    return rsService;
  })

  /**
   * Service list management controller.
   * @param $scope
   * @param $resource
   * @param $http
   * @param $timeout
   */
  .controller('RealmServicesController', function ($scope, $state, $stateParams, RealmServices, Utils) {
    var slug = $stateParams.realmId;
    $scope.editService = null;

    $scope.load = function () {

      RealmServices.getServices(slug)
        .then(function (services) {
          $scope.services = services;
        })
        .catch(function (err) {
          Utils.showError('Failed to load realm services: ' + err.data.message);
        });

    };

    var init = function () {
      $scope.load();
    };


    /** 
     * initiate creation of new service
     */
    $scope.newService = function () {
      $scope.editService = {};
      $('#serviceModal').modal({ backdrop: 'static', focus: true })
      Utils.refreshFormBS();
    };

    $scope.saveService = function () {
      RealmServices.addService(slug, $scope.editService)
        .then(function () {
          $('#serviceModal').modal('hide');
          $scope.load();
        })
        .catch(function (err) {
          Utils.showError('Failed to save service: ' + err.data.message);
        });
    }

    $scope.deleteServiceDlg = function (service) {
      $scope.modService = service;
      //add confirm field
      $scope.modService.confirmId = '';
      $('#deleteServiceConfirm').modal({ keyboard: false });
    }

    $scope.deleteService = function () {
      $('#deleteServiceConfirm').modal('hide');
      if ($scope.modService.serviceId === $scope.modService.confirmId) {
        RealmServices.deleteService($scope.realm.slug, $scope.modService.serviceId).then(function () {
          $scope.load();
          Utils.showSuccess();
        }).catch(function (err) {
          Utils.showError(err.data.message);
        });
      } else {
        Utils.showError("confirmId not valid");
      }
    }

    $scope.importServiceDlg = function () {
      $('#importServiceDlg').modal({ keyboard: false });
    }


    $scope.importService = function () {
      $('#importServiceDlg').modal('hide');
      var file = $scope.importFile;
      var mimeTypes = ['text/yaml', 'text/yml', 'application/x-yaml'];
      if (file == null || !mimeTypes.includes(file.type) || file.size == 0) {
        Utils.showError("invalid file");
      } else {
        RealmServices.importService(slug, file)
          .then(function (res) {
            $scope.importFile = null;
            $state.go('realm.service', { realmId: res.realm, serviceId: res.serviceId });
            Utils.showSuccess();
          })
          .catch(function (err) {
            Utils.showError(err.data.message);
          });
      }
    }


    $scope.changeNS = function () {
      $scope.nsError = false;
      if ($scope.nsChecking) {
        clearTimeout($scope.nsChecking);
      }
      $scope.nsChecking = setTimeout(doCheck, 300);
    }

    var doCheck = function () {
      var oldCheck = $scope.nsChecking;
      $scope.nsError = true;
      RealmServices.checkServiceNamespace(slug, $scope.editService.namespace).then(function (data) {
        if (!data) {
          $scope.nsError = false;
        }
        if ($scope.nsChecking == oldCheck) $scope.nsChecking = null;
      });
    }

    init();
  })


  /**
   * Service management controller.
   * @param $scope
   * @param $resource
   * @param $http
   * @param $timeout
   */
  .controller('RealmServiceController', function ($scope, $state, $stateParams, RealmServices, RealmAppsData, Utils) {
    var slug = $stateParams.realmId;
    var serviceId = $stateParams.serviceId;
    $scope.formView = 'overview';

    $scope.aceOption = {
      mode: 'javascript',
      theme: 'monokai',
      maxLines: 30,
      minLines: 6
    };

    $scope.activeView = function (view) {
      return view == $scope.formView ? 'active' : '';
    };

    $scope.switchView = function (view) {
      $scope.formView = view;
      Utils.refreshFormBS(300);
    }



    //TODO unpack loaders for scopes, claims, clients, approvals..
    $scope.load = function () {

      //TODO load mock/context data for claim mapping
      RealmServices.getService(slug, serviceId)
        .then(function (data) {
          $scope.service = data;
          $scope.servicename = data.name;
          $scope.servicedescription = data.description;
          return data;
        })
        .then(function (data) {
          $scope.scopes = data.scopes;
          return data;
        })
        .then(function (data) {
          $scope.approvals = data.approvals;
          return data;
        })
        .then(function (data) {
          $scope.claims = data.claims;
          return data;
        })
        .then(function (data) {

          //extract claimMapping
          var claimMapping = {
            'client': {
              enabled: false,
              code: "",
              context: {},
              scopes: [],
              result: null,
              error: null
            },
            'user': {
              enabled: false,
              code: "",
              context: {},
              scopes: [],
              result: null,
              error: null
            }
          };


          if (data.claimMapping && !!data.claimMapping['user']) {
            claimMapping['user'].enabled = true;
            claimMapping['user'].code = atob(data.claimMapping['user']);
          }

          if (data.claimMapping && !!data.claimMapping['client']) {
            claimMapping['client'].enabled = true;
            claimMapping['client'].code = atob(data.claimMapping['client']);
          }

          $scope.claimMapping = claimMapping;

          return data;
        })
        .then(function (data) {
          return Promise.all(
            data.clients.map(c => {
              return RealmAppsData.getClientApp(slug, c.clientId);
            })
          );
        }).then(function (clients) {
          $scope.clients = clients;
        })
        .catch(function (err) {
          Utils.showError('Failed to load realm service: ' + err.data.message);
        });
    };

    $scope.reload = function () {
      //reload basic settings
      RealmServices.getService(slug, serviceId)
        .then(function (data) {
          $scope.service = data;
          $scope.servicename = data.name;
          $scope.servicedescription = data.description;
        })
        .catch(function (err) {
          Utils.showError('Failed to load realm service: ' + err.data.message);
        });
    }

    var init = function () {
      $scope.load();
    };


    $scope.removeService = function () {
      $scope.doDelete = function () {
        $('#deleteConfirm').modal('hide');
        RealmServices.deleteService($scope.service.realm, $scope.service.serviceId)
          .then(function () {
            Utils.showSuccess();
            $state.go('realm.services', { realmId: $stateParams.realmId });
          })
          .catch(function (err) {
            Utils.showError('Failed to load realm service: ' + err.data.message);
          });
      }
      $('#deleteConfirm').modal({ keyboard: false });
    };



    $scope.exportService = function () {
      window.open('console/dev/realms/' + $scope.service.realm + '/services/' + $scope.service.serviceId + '/yaml');
    };

    $scope.saveService = function () {
      var service = $scope.service;

      //save only basic settings
      var data = {
        realm: slug,
        serviceId: service.serviceId,
        namespace: service.namespace,
        name: service.name,
        description: service.description,
        //TODO cleanup mapping from basic update, should stay with claims
        claimMapping: service.claimMapping
      };

      RealmServices.updateService(slug, data)
        .then(function (res) {
          Utils.showSuccess();
          $scope.reload();
        })
        .catch(function (err) {
          Utils.showError('Failed to save service: ' + err.data.message);
        });
    }

    /*
    * Scopes
    */

    $scope.loadScopes = function () {
      RealmServices.getScopes(slug, serviceId)
        .then(function (data) {
          $scope.scopes = data;
        })
        .catch(function (err) {
          Utils.showError('Failed to load service scopes: ' + err.data.message);
        });
    }

    $scope.createScopeDlg = function () {
      $scope.modScope = {
        id: null,
      };
      $scope.approvalFunction = { checked: false };

      $('#scopeModal').modal({ keyboard: false });
      Utils.refreshFormBS();
    }

    $scope.editScopeDlg = function (scope) {
      if (scope) {
        $scope.modScope = {
          id: scope.scope,
          ...scope
        }
        $scope.approvalFunction = { checked: !!scope.approvalFunction };

        $('#scopeModal').modal({ keyboard: false });
        Utils.refreshFormBS();
      }
    }

    $scope.saveScope = function () {
      $('#scopeModal').modal('hide');
      if ($scope.modScope) {
        var scope = $scope.modScope;
        if (scope.id) {
          RealmServices.updateScope($scope.service.realm, $scope.service.serviceId, scope.id, scope)
            .then(function () {
              Utils.showSuccess();
              $scope.loadScopes();
            })
            .catch(function (err) {
              Utils.showError('Failed to save scope: ' + err.data.message);
            });
        } else {
          RealmServices.addScope($scope.service.realm, $scope.service.serviceId, scope)
            .then(function () {
              Utils.showSuccess();
              $scope.loadScopes();
            })
            .catch(function (err) {
              Utils.showError('Failed to save scope: ' + err.data.message);
            });
        }

        $scope.modScope = null;
      }
    }

    $scope.toggleScopeApprovalFunction = function () {
      if (!$scope.approvalFunction.checked) {
        $scope.scope.approvalFunction = null;
      } else {
        $scope.scope.approvalFunction =
          '/**\n * DEFINE YOUR OWN APPROVAL FUNCTION HERE\n' +
          ' * input is a map containing user, client, and scopes\n' +
          '**/\n' +
          'function approver(inputData) {\n   return {};\n}';
      }
    }

    $scope.removeScope = function (scope) {
      $scope.doDelete = function () {
        $('#deleteConfirm').modal('hide');
        RealmServices.deleteScope($scope.service.realm, $scope.service.serviceId, scope.scope)
          .then(function () {
            Utils.showSuccess();
            $scope.loadScopes();
          })
          .catch(function (err) {
            Utils.showError('Failed to load delete scope: ' + err.data.message);
          });
      }
      $('#deleteConfirm').modal({ keyboard: false });
    };

    /*
    * Claims
    */
    $scope.loadClaims = function () {
      RealmServices.getClaims(slug, serviceId)
        .then(function (data) {
          $scope.claims = data;
        })
        .catch(function (err) {
          Utils.showError('Failed to load service claims: ' + err.data.message);
        });
    }

    $scope.filterClaims = function (q) {
      return $scope.claims.filter(function (c) {
        return c.key.toLowerCase().indexOf(q.toLowerCase()) >= 0;
      }).map(function (c) {
        return c.key;
      });
    }

    $scope.createClaimDlg = function () {
      $scope.modClaim = {
        id: null
      };
      $('#claimModal').modal({ keyboard: false });
    };

    $scope.editClaimDlg = function (claim) {
      if (claim) {
        $scope.modClaim = {
          id: claim.key,
          ...claim
        };

        $('#claimModal').modal({ keyboard: false });
      }
    };

    $scope.saveClaim = function () {
      $('#claimModal').modal('hide');
      if ($scope.modClaim) {
        var claim = $scope.modClaim;

        if (claim.id) {

          RealmServices.updateClaim($scope.service.realm, $scope.service.serviceId, claim.id, claim)
            .then(function () {
              Utils.showSuccess();
              $scope.loadClaims();
            })
            .catch(function (err) {
              Utils.showError('Failed to save claim: ' + err.data.message);
            });
        } else {
          RealmServices.addClaim($scope.service.realm, $scope.service.serviceId, claim)
            .then(function () {
              Utils.showSuccess();
              $scope.loadClaims();
            })
            .catch(function (err) {
              Utils.showError('Failed to save claim: ' + err.data.message);
            });
        }

        $scope.modClaim = null;
      }
    };

    $scope.removeClaim = function (claim) {
      $scope.doDelete = function () {
        $('#deleteConfirm').modal('hide');
        RealmServices.deleteClaim($scope.service.realm, $scope.service.serviceId, claim.key)
          .then(function () {
            Utils.showSuccess();
            $scope.loadClaims();
          })
          .catch(function (err) {
            Utils.showError('Failed to load delete claim: ' + err.data.message);
          });
      }
      $('#deleteConfirm').modal({ keyboard: false });
    };

    /**
     * Toggle claim mapping text
     */
    // $scope.toggleClaimMapping = function (m) {
    //   if (!$scope.service.claimMapping) $scope.service.claimMapping = {};
    //   if (!!$scope.service.claimMapping[m]) {
    //     $scope.service.claimMapping[m] = null;
    //   } else {
    //     $scope.service.claimMapping[m] =
    //       '/**\n * DEFINE YOUR OWN CLAIM MAPPING HERE\n' +
    //       '**/\n' +
    //       'function claimMapping(context) {\n let client = context.client; \n let user = context.user; \n let scopes = context.scopes; \n  return {};\n}';
    //   }
    // }

    $scope.toggleClaimMapping = function (m) {
      var claimMapping = $scope.claimMapping[m];

      if (claimMapping.enabled && claimMapping.code == '') {
        claimMapping.code =
          '/**\n * DEFINE YOUR OWN CLAIM MAPPING HERE\n' +
          '**/\n' +
          'function claimMapping(context) {\n let client = context.client; \n let user = context.user; \n let scopes = context.scopes; \n  return {};\n}';
      }

      claimMapping.error = null;
      claimMapping.result = null;

      $scope.claimMapping[m] = claimMapping;

    }


    $scope.validateClaims = function (m) {
      var mapping = $scope.claimMapping[m];
      if (!mapping) {
        Utils.showError("invalid mapping");
        return;
      }

      var functionCode = mapping.code
      if (!functionCode || functionCode == '') {
        Utils.showError("empty function code");
        return;
      }

      var data = {
        name: m,
        code: btoa(functionCode),
        scopes: mapping.scopes.map(function (s) { return s.text })
      };

      RealmServices.validateClaims($scope.service.realm, $scope.service.serviceId, data).then(function (res) {
        $scope.claimMapping[m].result = res.result;
        $scope.claimMapping[m].errors = res.errors;
      }).catch(function (err) {
        $scope.claimMapping[m].result = null;
        $scope.claimMapping[m].errors = [err.data.message];
      });


      // $scope.validationResult[m] = '';
      // $scope.validationError[m] = '';

      // RealmServices.validateClaims($scope.service.realm, $scope.service.serviceId, m, $scope.service.claimMapping[m], $scope.claimEnabled[m].scopes.map(function (s) { return s.text }))
      //   .then(function (data) {
      //     $scope.validationResult[m] = data;
      //   })
      //   .catch(function (e) {
      //     $scope.validationResult[m] = '';
      //     $scope.validationError[m] = e.data.message;
      //   });
    }




    $scope.saveClaimMapping = function (m) {
      var data = Object.assign({}, $scope.service);
      if (!data.claimMapping) data.claimMapping = {};

      //claim mapping
      var claimMapping = $scope.claimMapping;
      if (claimMapping['user'].enabled == true && claimMapping['user'].code != null && claimMapping['user'].code != "") {
        data.claimMapping['user'] = btoa(claimMapping['user'].code);
      } else {
        delete data.claimMapping['user'];
      }
      if (claimMapping['client'].enabled == true && claimMapping['client'].code != null && claimMapping['client'].code != "") {
        data.claimMapping['client'] = btoa(claimMapping['client'].code);
      } else {
        delete data.claimMapping['client'];
      }


      // if (!copy.claimMapping) copy.claimMapping = {};
      // copy.claimMapping[m] = $scope.claimEnabled[m].checked ? $scope.service.claimMapping[m] : null;

      RealmServices.updateService($scope.service.realm, data)
        .then(function (res) {
          $scope.reload();
          Utils.showSuccess();
        })
        .catch(function (err) {
          Utils.showError('Failed to save claim mapping: ' + err.data.message);
        });
    }



    /*
    * Clients
    */

    $scope.loadClients = function () {
      RealmServices.getClients(slug, serviceId)
        .then(function (data) {
          return Promise.all(
            data.map(c => {
              return RealmAppsData.getClientApp(slug, c.clientId);
            })
          );
        })
        .then(function (clients) {
          $scope.clients = clients;
        })
        .catch(function (err) {
          Utils.showError('Failed to load service clients: ' + err.data.message);
        });
    }

    $scope.createClientDlg = function (type) {
      if (!type) {
        type = 'introspect';
      }
      $scope.modClient = {
        type: type,
      };

      $('#clientModal').modal({ keyboard: false });
      Utils.refreshFormBS();
    }


    $scope.createClient = function () {
      $('#clientModal').modal('hide');
      if ($scope.modClient) {
        var type = $scope.modClient.type;
        if (!type) {
          type = 'introspect';
        }

        var data = {
          realm: $scope.service.realm,
          serviceId: $scope.service.serviceId,
          type: type
        }

        RealmServices.addClient($scope.service.realm, $scope.service.serviceId, data)
          .then(function () {
            Utils.showSuccess();
            $scope.loadClients();
          })
          .catch(function (err) {
            Utils.showError('Failed to create client: ' + err.data.message);
          });


        $scope.modClient = null;
      }
    }


    $scope.removeClient = function (client) {
      $scope.doDelete = function () {
        $('#deleteConfirm').modal('hide');
        RealmServices.deleteClient($scope.service.realm, $scope.service.serviceId, client.clientId)
          .then(function () {
            Utils.showSuccess();
            $scope.loadClients();
          })
          .catch(function (err) {
            Utils.showError('Failed to delete client: ' + err.data.message);
          });
      }
      $('#deleteConfirm').modal({ keyboard: false });
    };

    /*
    * Approvals
    */

    $scope.loadApprovals = function () {
      RealmServices.getApprovals(slug, serviceId)
        .then(function (data) {
          $scope.approvals = data;
        })
        .catch(function (err) {
          Utils.showError('Failed to load service approvals: ' + err.data.message);
        });
    }

    $scope.createApprovalDlg = function () {
      $scope.modApproval = {};
      $('#approvalModal').modal({ keyboard: false });
      Utils.refreshFormBS();
    }

    $scope.saveApproval = function () {
      if ($scope.modApproval) {
        RealmServices.addApproval($scope.service.realm, $scope.service.serviceId, $scope.modApproval)
          .then(function () {
            Utils.showSuccess();
            $scope.loadApprovals();
          })
          .catch(function (err) {
            Utils.showError('Failed to save service approval: ' + err.data.message);
          });
      }

      $('#approvalModal').modal('hide');
    }

    $scope.removeApproval = function (approval) {
      $scope.doDelete = function () {
        $('#deleteConfirm').modal('hide');
        RealmServices.deleteApproval($scope.service.realm, $scope.service.serviceId, approval)
          .then(function () {
            Utils.showSuccess();
            $scope.loadApprovals();
          })
          .catch(function (err) {
            Utils.showError('Failed to load delete approval: ' + err.data.message);
          });
      }
      $('#deleteConfirm').modal({ keyboard: false });
    };

    init();
  })

  /**
   * Service list management controller.
   * @param $scope
   * @param $resource
   * @param $http
   * @param $timeout
   */
  .controller('RealmServiceApprovalsController', function ($scope, $state, $stateParams, RealmServices, Utils) {
    var slug = $stateParams.realmId;
    var serviceId = $stateParams.serviceId;


    $scope.load = function () {

      RealmServices.getApprovals(slug, serviceId)
        .then(function (approvals) {
          $scope.approvals = approvals;
        })
        .catch(function (err) {
          Utils.showError('Failed to load service approvals: ' + err.data.message);
        });
    }

    var init = function () {
      RealmServices.getService(slug, serviceId)
        .then(function (service) {
          $scope.service = service;
        })
        .then(function () {
          $scope.load();
        })
        .catch(function (err) {
          Utils.showError('Failed to load service: ' + err.data.message);
        });
    }


    $scope.createApproval = function () {
      $scope.modApproval = {};
      $('#approvalModal').modal({ keyboard: false });
      Utils.refreshFormBS();
    }

    $scope.saveApproval = function () {
      if ($scope.modApproval) {
        RealmServices.addApproval($scope.service.realm, $scope.service.serviceId, $scope.modApproval)
          .then(function () {
            $scope.load();
          })
          .catch(function (err) {
            Utils.showError('Failed to save service approval: ' + err.data.message);
          });
      }

      $('#approvalModal').modal('hide');
    }

    $scope.removeApproval = function (approval) {
      $scope.doDelete = function () {
        $('#deleteConfirm').modal('hide');
        RealmServices.deleteApproval($scope.service.realm, $scope.service.serviceId, approval)
          .then(function () {
            Utils.showSuccess();
            $scope.load();
          })
          .catch(function (err) {
            Utils.showError('Failed to load delete approval: ' + err.data.message);
          });
      }
      $('#deleteConfirm').modal({ keyboard: false });
    };

    init();
  })
  ;