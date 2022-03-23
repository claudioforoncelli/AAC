package it.smartcommunitylab.aac.core.authorities;

import java.util.Collection;
import java.util.List;

import it.smartcommunitylab.aac.common.NoSuchProviderException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.common.SystemException;
import it.smartcommunitylab.aac.core.model.ConfigurableIdentityProvider;
import it.smartcommunitylab.aac.core.provider.IdentityProvider;
import it.smartcommunitylab.aac.core.provider.IdentityService;

public interface IdentityAuthority {

    /*
     * identify
     */
    public String getAuthorityId();

    /*
     * identity providers
     * 
     * Resolve identities via authentication or direct fetch
     */
    public boolean hasIdentityProvider(String providerId);

    public IdentityProvider getIdentityProvider(String providerId) throws NoSuchProviderException;

    public List<IdentityProvider> getIdentityProviders(String realm);
//
//    /*
//     * we also expect auth provider to be able to infer *provider from userId
//     * implementations should build ids predictably via composition
//     * 
//     * also *providers should return the same id for the same user!
//     */
//    public String getUserProviderId(String userId);
//
//    public IdentityProvider getUserIdentityProvider(String userId);

    /*
     * Manage providers
     * 
     * we expect idp to be registered and usable, or removed. To update config
     * implementations should unregister+register, we want identities and sessions
     * to be invalidated if config changes
     */
    public IdentityProvider registerIdentityProvider(ConfigurableIdentityProvider idp)
            throws IllegalArgumentException, RegistrationException, SystemException;

    public void unregisterIdentityProvider(String providerId) throws SystemException;

    /*
     * Identity services
     * 
     * Manage identities read-write. Implementations may choose to return null when
     * identities are not manageable, but at minimum they should return a service
     * with delete. When not provided, identities will be immutable.
     */
    public IdentityService getIdentityService(String providerId);

    public List<IdentityService> getIdentityServices(String realm);

    /*
     * Configuration templates
     * 
     * Optional, expose configurableProvider templates. Where unsupported return
     * either a null or empty collection.
     */

    public Collection<ConfigurableIdentityProvider> getConfigurableProviderTemplates();

    public ConfigurableIdentityProvider getConfigurableProviderTemplate(String templateId)
            throws NoSuchProviderException;

}
