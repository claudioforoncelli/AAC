package it.smartcommunitylab.aac.api.scopes;

import java.util.Collections;
import java.util.Set;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.model.ScopeType;

public class ApiUsersScope extends ApiScope {

    public static final String SCOPE = "aac.api.users";

    @Override
    public String getScope() {
        return SCOPE;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.GENERIC;
    }

    // TODO replace with keys for i18n
    @Override
    public String getName() {
        return "Manage users";
    }

    @Override
    public String getDescription() {
        return "Manage user identities, accounts and attributes.";
    }

    @Override
    public Set<String> getAuthorities() {
        return Collections.singleton(Config.R_ADMIN);
    }

}
