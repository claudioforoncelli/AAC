package it.smartcommunitylab.aac.openid.scope;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.model.ScopeType;
import it.smartcommunitylab.aac.scope.Scope;

public class OpenIdScope extends Scope {

    public static final String SCOPE = Config.SCOPE_OPENID;

    @Override
    public String getResourceId() {
        return OpenIdResource.RESOURCE_ID;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.USER;
    }

    @Override
    public String getScope() {
        return SCOPE;
    }

    // TODO replace with keys for i18n
    @Override
    public String getName() {
        return "OpenId";
    }

    @Override
    public String getDescription() {
        return "User identity information (username and identifier). Read access only.";
    }
}
