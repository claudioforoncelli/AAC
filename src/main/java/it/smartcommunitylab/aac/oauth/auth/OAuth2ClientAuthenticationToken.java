package it.smartcommunitylab.aac.oauth.auth;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.core.auth.DefaultClientAuthenticationToken;
import it.smartcommunitylab.aac.oauth.model.OAuth2ClientDetails;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public abstract class OAuth2ClientAuthenticationToken extends DefaultClientAuthenticationToken {

    private static final long serialVersionUID = SystemKeys.AAC_OAUTH2_SERIAL_VERSION;

    // oauth2 client details
    protected OAuth2ClientDetails oauth2Details;

    public OAuth2ClientAuthenticationToken(String clientId) {
        super(clientId);
    }

    public OAuth2ClientAuthenticationToken(String clientId, Collection<? extends GrantedAuthority> authorities) {
        super(clientId, authorities);
    }

    public OAuth2ClientDetails getOAuth2ClientDetails() {
        return oauth2Details;
    }

    @Override
    public void eraseCredentials() {
        // we don't reset clientSecret or jwks because we need those for JWT
    }

    public void setOAuth2ClientDetails(OAuth2ClientDetails details) {
        this.oauth2Details = details;
    }
}
