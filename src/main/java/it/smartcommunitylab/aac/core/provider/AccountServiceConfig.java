package it.smartcommunitylab.aac.core.provider;

import it.smartcommunitylab.aac.core.model.ConfigMap;
import it.smartcommunitylab.aac.core.model.ConfigurableAccountProvider;
import it.smartcommunitylab.aac.model.PersistenceMode;

public interface AccountServiceConfig<M extends ConfigMap> extends ProviderConfig<M, ConfigurableAccountProvider> {

    public String getRepositoryId();

    public PersistenceMode getPersistence();

}
