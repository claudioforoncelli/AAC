package it.smartcommunitylab.aac.profiles.scope;

import it.smartcommunitylab.aac.Config;

public class BasicProfileScope extends AbstractProfileScope {

    public static final String SCOPE = Config.SCOPE_BASIC_PROFILE;

    @Override
    public String getScope() {
        return SCOPE;
    }

    // TODO replace with keys for i18n
    @Override
    public String getName() {
        return "Read user's basic profile";
    }

    @Override
    public String getDescription() {
        return "Basic profile of the current platform user. Read access only.";
    }
}
