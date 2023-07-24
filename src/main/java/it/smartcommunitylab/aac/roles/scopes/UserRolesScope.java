package it.smartcommunitylab.aac.roles.scopes;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.model.ScopeType;
import it.smartcommunitylab.aac.scope.Scope;

public class UserRolesScope extends Scope {

    @Override
    public String getResourceId() {
        return RolesResource.RESOURCE_ID;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.USER;
    }

    @Override
    public String getScope() {
        return Config.SCOPE_USER_ROLE;
    }

    // TODO replace with keys for i18n
    @Override
    public String getName() {
        return "Read user's roles";
    }

    @Override
    public String getDescription() {
        return "Roles and authorities of the current platform user. Read access only.";
    }
}
