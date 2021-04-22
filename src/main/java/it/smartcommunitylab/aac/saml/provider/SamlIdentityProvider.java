package it.smartcommunitylab.aac.saml.provider;

import java.util.Collection;
import java.util.Map.Entry;

import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.attributes.store.AttributeStore;
import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.core.auth.ExtendedAuthenticationProvider;
import it.smartcommunitylab.aac.core.auth.UserAuthenticatedPrincipal;
import it.smartcommunitylab.aac.core.base.AbstractProvider;
import it.smartcommunitylab.aac.core.base.ConfigurableProvider;
import it.smartcommunitylab.aac.core.model.UserAccount;
import it.smartcommunitylab.aac.core.model.UserAttributes;
import it.smartcommunitylab.aac.core.model.UserIdentity;
import it.smartcommunitylab.aac.core.provider.AccountProvider;
import it.smartcommunitylab.aac.core.provider.AccountService;
import it.smartcommunitylab.aac.core.provider.AttributeProvider;
import it.smartcommunitylab.aac.core.provider.CredentialsService;
import it.smartcommunitylab.aac.core.provider.IdentityProvider;
import it.smartcommunitylab.aac.core.provider.IdentityService;
import it.smartcommunitylab.aac.core.provider.SubjectResolver;
import it.smartcommunitylab.aac.saml.SamlIdentityAuthority;
import it.smartcommunitylab.aac.saml.persistence.SamlUserAccountRepository;

public class SamlIdentityProvider extends AbstractProvider implements IdentityService {
    // services
    private final SamlUserAccountRepository accountRepository;
    private final AttributeStore attributeStore;

    private final SamlIdentityProviderConfig providerConfig;
    private final RelyingPartyRegistration relyingPartyRegistration;

    // internal providers
    private final SamlAccountProvider accountProvider;
    private final SamlAttributeProvider attributeProvider;
    private final SamlAuthenticationProvider authenticationProvider;
    private final SamlSubjectResolver subjectResolver;

    @Override
    public String getType() {
        return SystemKeys.RESOURCE_IDENTITY;
    }

    public SamlIdentityProvider(
            String providerId,
            SamlUserAccountRepository accountRepository, AttributeStore attributeStore,
            ConfigurableProvider configurableProvider,
            String realm) {
        super(SystemKeys.AUTHORITY_SAML, providerId, realm);
        Assert.notNull(accountRepository, "account repository is mandatory");
        Assert.notNull(configurableProvider, "configuration is mandatory");

        // internal data repositories
        this.accountRepository = accountRepository;
        this.attributeStore = attributeStore;

        // translate configuration
        Assert.isTrue(SystemKeys.AUTHORITY_SAML.equals(configurableProvider.getAuthority()),
                "configuration does not match this provider");
        Assert.isTrue(providerId.equals(configurableProvider.getProvider()),
                "configuration does not match this provider");
        Assert.isTrue(realm.equals(configurableProvider.getRealm()), "configuration does not match this provider");

        providerConfig = SamlIdentityProviderConfig.fromConfigurableProvider(configurableProvider);
        relyingPartyRegistration = providerConfig.toRelyingPartyRegistration();

        // build resource providers, we use our providerId to ensure consistency
        this.accountProvider = new SamlAccountProvider(providerId, accountRepository, realm);
        this.attributeProvider = new SamlAttributeProvider(providerId, accountRepository, attributeStore, realm);
        this.authenticationProvider = new SamlAuthenticationProvider(providerId, accountRepository, realm);
        this.subjectResolver = new SamlSubjectResolver(providerId, accountRepository, realm);

    }

    @Override
    public ExtendedAuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public AccountProvider getAccountProvider() {
        return accountProvider;
    }

    @Override
    public AttributeProvider getAttributeProvider() {
        return attributeProvider;
    }

    @Override
    public SubjectResolver getSubjectResolver() {
        return subjectResolver;
    }

    public RelyingPartyRegistration getRelyingPartyRegistration() {
        return relyingPartyRegistration;
    }

    @Override
    public UserIdentity convertIdentity(UserAuthenticatedPrincipal principal, String subjectId)
            throws NoSuchUserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserIdentity getIdentity(String subject, String userId) throws NoSuchUserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserIdentity getIdentity(String subject, String userId, boolean fetchAttributes) throws NoSuchUserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<UserIdentity> listIdentities(String subject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthenticationUrl() {
        // TODO build a realm-bound url, need updates on filters
        return SamlIdentityAuthority.AUTHORITY_URL
                + "authenticate/" + getProvider();
    }

    @Override
    public AuthenticationEntryPoint getAuthenticationEntryPoint() {
        // we don't have one
        return null;
    }

    @Override
    public boolean canRegister() {
        return false;
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public AccountService getAccountService() {
        // TODO implement a delete-only accountService
        return null;
    }

    @Override
    public CredentialsService getCredentialsService() {
        // nothing to handle
        return null;
    }

    @Override
    public UserIdentity registerIdentity(
            String subject, UserAccount account,
            Collection<UserAttributes> attributes)
            throws NoSuchUserException, RegistrationException {
        throw new RegistrationException("registration not supported");
    }

    @Override
    public UserIdentity updateIdentity(String subject,
            String userId, UserAccount account,
            Collection<UserAttributes> attributes)
            throws NoSuchUserException, RegistrationException {
        throw new RegistrationException("update not supported");

    }

    @Override
    public void deleteIdentity(String subjectId, String userId) throws NoSuchUserException {
        // TODO delete via service

    }

    @Override
    public void deleteIdentities(String subjectId) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getRegistrationUrl() {
        return null;
    }

    @Override
    public String getName() {
        return providerConfig.getName();
    }
}
