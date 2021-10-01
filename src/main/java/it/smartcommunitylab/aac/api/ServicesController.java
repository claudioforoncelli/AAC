package it.smartcommunitylab.aac.api;

import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.api.scopes.ApiServicesScope;
import it.smartcommunitylab.aac.common.NoSuchClaimException;
import it.smartcommunitylab.aac.common.NoSuchClientException;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchScopeException;
import it.smartcommunitylab.aac.common.NoSuchServiceException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.model.ClientApp;
import it.smartcommunitylab.aac.services.Service;
import it.smartcommunitylab.aac.services.ServiceClaim;
import it.smartcommunitylab.aac.services.ServiceScope;
import it.smartcommunitylab.aac.services.ServicesManager;

@RestController
@RequestMapping("api")
public class ServicesController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ServicesManager serviceManager;

    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    /*
     * Services
     * 
     * When provided with a fully populated service model, related entities will be
     * updated accordingly.
     */

    @GetMapping("/service/{realm}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Collection<Service> listServices(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm) throws NoSuchRealmException {
        logger.debug("list services for realm " + String.valueOf(realm));
        return serviceManager.listServices(realm);
    }

    @GetMapping("/service/{realm}/{serviceId}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Service getService(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
            throws NoSuchServiceException, NoSuchRealmException {
        logger.debug("get service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        return serviceManager.getService(realm, serviceId);
    }

    @PostMapping("/service/{realm}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Service addService(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestBody @Valid Service s) throws NoSuchRealmException {
        logger.debug("add service for realm " + String.valueOf(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("service bean " + String.valueOf(s));
        }
        return serviceManager.addService(realm, s);
    }

    @PutMapping("/service/{realm}/{serviceId}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Service updateService(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @RequestBody @Valid Service s) throws NoSuchServiceException, NoSuchRealmException {
        logger.debug("update service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("service bean " + String.valueOf(s));
        }
        return serviceManager.updateService(realm, serviceId, s);
    }

    @DeleteMapping("/service/{realm}/{serviceId}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public void deleteService(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
            throws NoSuchServiceException {
        logger.debug("delete service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        serviceManager.deleteService(realm, serviceId);
    }

    @PutMapping("/service/{realm}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Service importService(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestParam("file") @Valid @NotNull @NotBlank MultipartFile file) throws Exception {
        logger.debug("import service to realm " + String.valueOf(realm));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("empty file");
        }

        if (file.getContentType() != null &&
                (!file.getContentType().equals(SystemKeys.MEDIA_TYPE_YAML.toString()) &&
                        !file.getContentType().equals(SystemKeys.MEDIA_TYPE_YML.toString()))) {
            throw new IllegalArgumentException("invalid file");
        }
        try {
            Service s = yamlObjectMapper.readValue(file.getInputStream(),
                    Service.class);

            if (logger.isTraceEnabled()) {
                logger.trace("service bean: " + String.valueOf(s));
            }

            return serviceManager.addService(realm, s);

        } catch (Exception e) {
            logger.error("import service error: " + e.getMessage());
            throw e;
        }

    }

    /*
     * Service scopes
     */

    @GetMapping("/service/{realm}/{serviceId}/scopes")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Collection<ServiceScope> listServiceScopes(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
            throws NoSuchRealmException, NoSuchServiceException {
        logger.debug("list scopes from service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        return serviceManager.listServiceScopes(realm, serviceId);
    }

    @GetMapping("/service/{realm}/{serviceId}/scopes/{scope}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public ServiceScope getServiceScope(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SCOPE_PATTERN) String scope)
            throws NoSuchScopeException, NoSuchRealmException, NoSuchServiceException {
        logger.debug("get scope " + String.valueOf(scope) + " from service " + String.valueOf(serviceId) + " for realm "
                + String.valueOf(realm));

        return serviceManager.getServiceScope(realm, serviceId, scope);
    }

    @PostMapping("/service/{realm}/{serviceId}/scopes")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public ServiceScope addServiceScope(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @RequestBody @Valid ServiceScope s)
            throws NoSuchRealmException, NoSuchServiceException, RegistrationException {
        logger.debug("add scope to service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("scope bean " + String.valueOf(s));
        }
        return serviceManager.addServiceScope(realm, serviceId, s);
    }

    @PutMapping("/service/{realm}/{serviceId}/scopes/{scope}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public ServiceScope updateServiceScope(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SCOPE_PATTERN) String scope,
            @RequestBody @Valid ServiceScope s)
            throws NoSuchScopeException, NoSuchServiceException, RegistrationException {
        logger.debug(
                "update scope " + String.valueOf(scope) + " from service " + String.valueOf(serviceId) + " for realm "
                        + String.valueOf(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("scope bean " + String.valueOf(s));
        }
        return serviceManager.updateServiceScope(realm, serviceId, scope, s);
    }

    @DeleteMapping("/service/{realm}/{serviceId}/scopes/{scope}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public void deleteServiceScope(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SCOPE_PATTERN) String scope)
            throws NoSuchScopeException, NoSuchServiceException {
        logger.debug(
                "delete scope " + String.valueOf(scope) + " from service " + String.valueOf(serviceId) + " for realm "
                        + String.valueOf(realm));
        serviceManager.deleteServiceScope(realm, serviceId, scope);
    }

    /*
     * Service claims
     */
    @GetMapping("/service/{realm}/{serviceId}/claims")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Collection<ServiceClaim> listServiceClaims(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
            throws NoSuchRealmException, NoSuchServiceException {
        logger.debug("list claims from service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        return serviceManager.listServiceClaims(realm, serviceId);
    }

    @GetMapping("/service/{realm}/{serviceId}/claims/{key}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public ServiceClaim getServiceClaim(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.KEY_PATTERN) String key)
            throws NoSuchClaimException, NoSuchRealmException, NoSuchServiceException {
        logger.debug("get claim " + String.valueOf(key) + " from service " + String.valueOf(serviceId) + " for realm "
                + String.valueOf(realm));
        return serviceManager.getServiceClaim(realm, serviceId, key);
    }

    @PostMapping("/service/{realm}/{serviceId}/claims")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public ServiceClaim addServiceClaim(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @RequestBody @Valid ServiceClaim s)
            throws NoSuchRealmException, NoSuchServiceException, RegistrationException {
        logger.debug("add claim to service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("claim bean " + String.valueOf(s));
        }
        return serviceManager.addServiceClaim(realm, serviceId, s);
    }

    @PutMapping("/service/{realm}/{serviceId}/claims/{key}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public ServiceClaim updateServiceClaim(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.KEY_PATTERN) String key,
            @RequestBody @Valid ServiceClaim s)
            throws NoSuchClaimException, NoSuchServiceException, RegistrationException {
        logger.debug(
                "update claim " + String.valueOf(key) + " from service " + String.valueOf(serviceId) + " for realm "
                        + String.valueOf(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("claim bean " + String.valueOf(s));
        }
        return serviceManager.updateServiceClaim(realm, serviceId, key, s);
    }

    @DeleteMapping("/service/{realm}/{serviceId}/claims/{key}")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public void deleteServiceClaim(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.KEY_PATTERN) String key)
            throws NoSuchClaimException, NoSuchServiceException {
        logger.debug(
                "delete claim " + String.valueOf(key) + " from service " + String.valueOf(serviceId) + " for realm "
                        + String.valueOf(realm));
        serviceManager.deleteServiceClaim(realm, serviceId, key);
    }

    /*
     * Service scope approvals
     */

    @GetMapping("/service/{realm}/{serviceId}/scopes/{scope}/approvals")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Collection<Approval> getServiceScopeApprovals(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SCOPE_PATTERN) String scope)
            throws NoSuchRealmException, NoSuchServiceException, NoSuchScopeException {
        logger.debug(
                "list approvals from service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));

        return serviceManager.getServiceScopeApprovals(realm, serviceId, scope);
    }

    @PostMapping("/service/{realm}/{serviceId}/scopes/{scope}/approvals")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public Approval addServiceScopeApproval(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SCOPE_PATTERN) String scope,
            @RequestParam @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String clientId,
            @RequestParam(required = false, defaultValue = "true") boolean approved)
            throws NoSuchRealmException, NoSuchServiceException, NoSuchScopeException {
        logger.debug(
                "add approval for scope " + String.valueOf(scope) + " client " + String.valueOf(clientId)
                        + " to service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
        int duration = SystemKeys.DEFAULT_APPROVAL_VALIDITY;
        return serviceManager.addServiceScopeApproval(realm, serviceId, scope, clientId, duration, approved);
    }

    @DeleteMapping("/service/{realm}/{serviceId}/scopes/{scope}/approvals")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
            + ApiServicesScope.SCOPE + "')")
    public void deleteServiceScopeApproval(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SCOPE_PATTERN) String scope,
            @RequestParam @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String clientId)
            throws NoSuchRealmException, NoSuchScopeException, NoSuchServiceException {
        logger.debug(
                "revoke approval for scope " + String.valueOf(scope) + " client " + String.valueOf(clientId)
                        + " from service " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));

        serviceManager.revokeServiceScopeApproval(realm, serviceId, scope, clientId);
    }

//    /*
//     * Service client
//     */
//    @GetMapping("/service/{realm}/{serviceId}/clients")
//    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
//            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
//            + ApiServicesScope.SCOPE + "')")
//    public ClientApp getServiceClient(
//            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
//            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
//            throws NoSuchServiceException, NoSuchRealmException, NoSuchClientException {
//        logger.debug("get service client " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
//        Service service = serviceManager.getService(realm, serviceId);
//        return serviceManager.getOAuth2Client(realm, service);
//    }
//
//    @PutMapping("/service/{realm}/{serviceId}/client")
//    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
//            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
//            + ApiServicesScope.SCOPE + "')")
//    public ClientApp addServiceClient(
//            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
//            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
//            throws NoSuchServiceException, NoSuchRealmException {
//        logger.debug("add service client " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
//        Service service = serviceManager.getService(realm, serviceId);
//        return serviceManager.createOAuth2Client(realm, service);
//    }
//
//    @DeleteMapping("/service/{realm}/{serviceId}/client")
//    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
//            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')) and hasAuthority('SCOPE_"
//            + ApiServicesScope.SCOPE + "')")
//    public void deleteServiceClient(
//            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
//            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String serviceId)
//            throws NoSuchServiceException, NoSuchRealmException, NoSuchClientException {
//        logger.debug("delete service client " + String.valueOf(serviceId) + " for realm " + String.valueOf(realm));
//        Service service = serviceManager.getService(realm, serviceId);
//        serviceManager.deleteOAuth2Client(realm, service);
//    }

//    /*
//     * Exceptions
//     */
//    @ExceptionHandler({ CustomException1.class, CustomException2.class })
//    public void handleException() {
//        //
//    }
}
