package it.smartcommunitylab.aac.oauth.flow;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.util.Assert;

import it.smartcommunitylab.aac.core.UserDetails;
import it.smartcommunitylab.aac.core.auth.UserAuthentication;
import it.smartcommunitylab.aac.core.service.UserService;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.oauth.model.OAuth2ClientDetails;
import it.smartcommunitylab.aac.oauth.service.OAuth2ClientDetailsService;

public class OAuthFlowExtensionsHandler implements UserApprovalHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final FlowExtensionsService flowExtensionsService;
    private final OAuth2ClientDetailsService clientService;
    private UserService userService;

    public OAuthFlowExtensionsHandler(FlowExtensionsService flowExtensionsService,
            OAuth2ClientDetailsService clientService) {
        Assert.notNull(flowExtensionsService, "flow extensions service is required");
        Assert.notNull(clientService, "client details service is required");
        this.flowExtensionsService = flowExtensionsService;
        this.clientService = clientService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuth) {
        try {

            // short circuit for not approved, we don't want to flip
            if (!authorizationRequest.isApproved()) {
                return authorizationRequest.isApproved();
            }

            Set<String> scopes = authorizationRequest.getScope();
            OAuth2ClientDetails clientDetails = clientService.loadClientByClientId(authorizationRequest.getClientId());
            String realm = clientDetails.getRealm();

            UserDetails userDetails = null;
            User user = null;

            // check if userAuth is present
            if (userAuth != null && userAuth instanceof UserAuthentication) {
                userDetails = ((UserAuthentication) userAuth).getUser();
                if (userService != null) {
                    user = userService.getUser(userDetails, realm);
                } else {
                    user = new User(userDetails);
                }

            }

            // get extension if set
            OAuthFlowExtensions ext = flowExtensionsService.getOAuthFlowExtensions(clientDetails);
            if (ext == null) {
                return authorizationRequest.isApproved();
            }

            // call extension and check if we get a valid response
            Optional<Boolean> approved = ext.onAfterUserApproval(scopes, user, clientDetails);
            if (approved.isEmpty()) {
                return authorizationRequest.isApproved();
            }

            return approved.get().booleanValue();

        } catch (ClientRegistrationException e) {
            // block the request
            throw new OAuth2AccessDeniedException();
        }

    }

    @Override
    public AuthorizationRequest checkForPreApproval(AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {
        // nothing to do here
        return null;
    }

    @Override
    public AuthorizationRequest updateAfterApproval(AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {
        // nothing to do here
        return authorizationRequest;
    }

    @Override
    public Map<String, Object> getUserApprovalRequest(AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {
        // nothing to do here
        return null;
    }

}
