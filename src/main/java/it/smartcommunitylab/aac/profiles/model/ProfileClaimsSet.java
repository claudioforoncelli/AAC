package it.smartcommunitylab.aac.profiles.model;

import java.util.Collection;
import java.util.Collections;
import it.smartcommunitylab.aac.claims.Claim;
import it.smartcommunitylab.aac.claims.ClaimsSet;
import it.smartcommunitylab.aac.claims.model.SerializableClaim;

public class ProfileClaimsSet implements ClaimsSet {

    public static final String RESOURCE_ID = "aac.profile";

    private String scope;
    private String key;
    private String namespace;
    private boolean isUser;

    private AbstractProfile profile;

    public AbstractProfile getProfile() {
        return profile;
    }

    public void setProfile(AbstractProfile profile) {
        this.profile = profile;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public boolean isUser() {
        return isUser;
    }

    @Override
    public boolean isClient() {
        return !isUser;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

//    @Override
//    public Map<String, Serializable> getClaims() {
//        if (profile == null) {
//            return Collections.emptyMap();
//        }
//
//        return profile.toMap();
//    }

    @Override
    public String getResourceId() {
        return RESOURCE_ID;
    }

    @Override
    public Collection<Claim> getClaims() {
        if (profile == null) {
            return Collections.emptyList();
        }

        SerializableClaim claim = new SerializableClaim(key);
        claim.setValue(profile.toMap());

        return Collections.singleton(claim);
    }

}
