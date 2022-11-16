package it.smartcommunitylab.aac.core.authorities;

import it.smartcommunitylab.aac.core.model.ConfigMap;
import it.smartcommunitylab.aac.core.model.ConfigurableCredentialsProvider;
import it.smartcommunitylab.aac.core.model.UserCredentials;
import it.smartcommunitylab.aac.core.provider.CredentialsServiceConfig;
import it.smartcommunitylab.aac.core.provider.AccountCredentialsService;

public interface CredentialsServiceAuthority<S extends AccountCredentialsService<R, M, C>, R extends UserCredentials, M extends ConfigMap, C extends CredentialsServiceConfig<M>>
        extends ConfigurableProviderAuthority<S, R, ConfigurableCredentialsProvider, M, C> {

}
