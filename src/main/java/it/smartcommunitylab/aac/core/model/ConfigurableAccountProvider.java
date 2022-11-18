package it.smartcommunitylab.aac.core.model;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConstructorBinding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.smartcommunitylab.aac.SystemKeys;

@Valid
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ConstructorBinding
public class ConfigurableAccountProvider extends ConfigurableProvider {

    private String repositoryId;
    private String persistence;

    public ConfigurableAccountProvider(String authority, String provider, String realm) {
        super(authority, provider, realm, SystemKeys.RESOURCE_ACCOUNT);
    }

    /**
     * Private constructor for JPA and other serialization tools.
     * 
     * We need to implement this to enable deserialization of resources via
     * reflection
     */
    @SuppressWarnings("unused")
    private ConfigurableAccountProvider() {
        this((String) null, (String) null, (String) null);
    }

    @Override
    public void setType(String type) {
        // not supported
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getPersistence() {
        return persistence;
    }

    public void setPersistence(String persistence) {
        this.persistence = persistence;
    }

}
