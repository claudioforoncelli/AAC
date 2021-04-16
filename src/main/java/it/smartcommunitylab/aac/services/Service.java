package it.smartcommunitylab.aac.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.smartcommunitylab.aac.SystemKeys;

/*
 * A service defines an api composed of a namespace (used as audience)
 */

@Valid
@JsonInclude(Include.NON_NULL)
public class Service {

    @Pattern(regexp = SystemKeys.SLUG_PATTERN)
    private String serviceId;

    private String realm;

    private String name;
    private String description;

    @NotBlank
    @Pattern(regexp = SystemKeys.NAMESPACE_PATTERN)
    private String namespace;

    private Map<String, String> claimMapping = new HashMap<>();

    private Collection<ServiceScope> scopes = Collections.emptyList();
    private Collection<ServiceClaim> claims = Collections.emptyList();

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, String> getClaimMapping() {
        return claimMapping;
    }

    public void setClaimMapping(Map<String, String> claimMapping) {
        this.claimMapping = claimMapping;
    }

    public Collection<ServiceScope> getScopes() {
        return scopes;
    }

    public void setScopes(Collection<ServiceScope> scopes) {
        this.scopes = scopes;
    }

    public Collection<ServiceClaim> getClaims() {
        return claims;
    }

    public void setClaims(Collection<ServiceClaim> claims) {
        this.claims = claims;
    }

    @JsonIgnore
    public String getUserClaimMapping() {
        if (claimMapping != null && claimMapping.containsKey("user")) {
            return claimMapping.get("user");
        }

        return null;
    }

    @JsonIgnore
    public String getClientClaimMapping() {
        if (claimMapping != null && claimMapping.containsKey("client")) {
            return claimMapping.get("client");
        }

        return null;
    }

    @JsonIgnore
    public String getScopeClaimMapping(String scope) {
        if (claimMapping != null && claimMapping.containsKey(scope)) {
            return claimMapping.get(scope);
        }

        return null;
    }

    @JsonIgnore
    public String getUserClaimMapping(String scope) {
        if (claimMapping != null && claimMapping.containsKey("user:" + scope)) {
            return claimMapping.get("user:" + scope);
        }

        return null;
    }

    @JsonIgnore
    public String getClientClaimMapping(String scope) {
        if (claimMapping != null && claimMapping.containsKey("client:" + scope)) {
            return claimMapping.get("client:" + scope);
        }

        return null;
    }
}
