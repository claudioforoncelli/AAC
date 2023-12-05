/*
 * Copyright 2023 the original author or authors
 *
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

package it.smartcommunitylab.aac.openidfed.provider;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.accounts.persistence.UserAccountService;
import it.smartcommunitylab.aac.accounts.provider.AccountProvider;
import it.smartcommunitylab.aac.attributes.model.UserAttributes;
import it.smartcommunitylab.aac.claims.ScriptExecutionService;
import it.smartcommunitylab.aac.core.service.ResourceEntityService;
import it.smartcommunitylab.aac.identity.base.AbstractIdentityProvider;
import it.smartcommunitylab.aac.oidc.model.OIDCUserAccount;
import it.smartcommunitylab.aac.oidc.model.OIDCUserAuthenticatedPrincipal;
import it.smartcommunitylab.aac.oidc.model.OIDCUserIdentity;
import it.smartcommunitylab.aac.oidc.provider.OIDCAccountPrincipalConverter;
import it.smartcommunitylab.aac.oidc.provider.OIDCAttributeProvider;
import it.smartcommunitylab.aac.oidc.provider.OIDCLoginProvider;
import it.smartcommunitylab.aac.oidc.provider.OIDCSubjectResolver;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class OpenIdFedIdentityProvider
    extends AbstractIdentityProvider<OIDCUserIdentity, OIDCUserAccount, OIDCUserAuthenticatedPrincipal, OpenIdFedIdentityProviderConfigMap, OpenIdFedIdentityProviderConfig> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // providers
    private final OpenIdFedAccountService accountService;
    private final OIDCAccountPrincipalConverter principalConverter;
    private final OIDCAttributeProvider attributeProvider;
    private final OpenIdFedAuthenticationProvider authenticationProvider;
    private final OIDCSubjectResolver subjectResolver;

    public OpenIdFedIdentityProvider(
        String providerId,
        UserAccountService<OIDCUserAccount> userAccountService,
        OpenIdFedIdentityProviderConfig config,
        String realm
    ) {
        this(SystemKeys.AUTHORITY_OPENIDFED, providerId, userAccountService, config, realm);
    }

    public OpenIdFedIdentityProvider(
        String authority,
        String providerId,
        UserAccountService<OIDCUserAccount> userAccountService,
        OpenIdFedIdentityProviderConfig config,
        String realm
    ) {
        super(authority, providerId, config, realm);
        logger.debug(
            "create openid federation provider for authority {} with id {}",
            String.valueOf(authority),
            String.valueOf(providerId)
        );

        // build resource providers, we use our providerId to ensure consistency
        OpenIdFedAccountServiceConfigConverter configConverter = new OpenIdFedAccountServiceConfigConverter();
        this.accountService =
            new OpenIdFedAccountService(providerId, userAccountService, configConverter.convert(config), realm);

        this.principalConverter = new OIDCAccountPrincipalConverter(authority, providerId, userAccountService, realm);
        this.principalConverter.setTrustEmailAddress(config.trustEmailAddress());
        this.principalConverter.setAlwaysTrustEmailAddress(config.alwaysTrustEmailAddress());

        this.attributeProvider = new OIDCAttributeProvider(authority, providerId, realm);
        this.subjectResolver =
            new OIDCSubjectResolver(authority, providerId, userAccountService, config.getRepositoryId(), realm);
        this.subjectResolver.setLinkable(config.isLinkable());

        // build custom authenticator
        this.authenticationProvider =
            new OpenIdFedAuthenticationProvider(providerId, userAccountService, config, realm);

        // function hooks from config
        if (
            config.getHookFunctions() != null &&
            StringUtils.hasText(config.getHookFunctions().get(ATTRIBUTE_MAPPING_FUNCTION))
        ) {
            // this.authenticationProvider.setCustomMappingFunction(
            //         config.getHookFunctions().get(ATTRIBUTE_MAPPING_FUNCTION)
            //     );
        }
    }

    public void setExecutionService(ScriptExecutionService executionService) {
        // this.authenticationProvider.setExecutionService(executionService);
    }

    public void setResourceService(ResourceEntityService resourceService) {
        this.accountService.setResourceService(resourceService);
    }

    @Override
    public OpenIdFedAuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public AccountProvider<OIDCUserAccount> getAccountProvider() {
        return accountService;
    }

    @Override
    public OpenIdFedAccountService getAccountService() {
        return accountService;
    }

    @Override
    public OIDCAccountPrincipalConverter getAccountPrincipalConverter() {
        return principalConverter;
    }

    @Override
    public OIDCAttributeProvider getAttributeProvider() {
        return attributeProvider;
    }

    @Override
    public OIDCSubjectResolver getSubjectResolver() {
        return subjectResolver;
    }

    @Override
    protected OIDCUserIdentity buildIdentity(
        OIDCUserAccount account,
        OIDCUserAuthenticatedPrincipal principal,
        Collection<UserAttributes> attributes
    ) {
        // build identity
        OIDCUserIdentity identity = new OIDCUserIdentity(getAuthority(), getProvider(), getRealm(), account, principal);
        identity.setAttributes(attributes);

        return identity;
    }

    @Override
    public String getAuthenticationUrl() {
        return "/auth/" + getAuthority() + "/authorize/" + getProvider();
    }

    @Override
    public OIDCLoginProvider getLoginProvider() {
        OIDCLoginProvider lp = new OIDCLoginProvider(getAuthority(), getProvider(), getRealm(), getName());
        lp.setTitleMap(getTitleMap());
        lp.setDescriptionMap(getDescriptionMap());

        lp.setLoginUrl(getAuthenticationUrl());
        lp.setPosition(getConfig().getPosition());

        return lp;
    }
}
