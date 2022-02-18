package it.smartcommunitylab.aac.oauth.endpoint;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.jwt.JWTSigningAndValidationService;
import it.smartcommunitylab.aac.oauth.model.AuthenticationMethod;
import it.smartcommunitylab.aac.openid.endpoint.OpenIDMetadataEndpoint;

/*
 * OAuth2 Authorization Server Metadata
 * https://tools.ietf.org/html/rfc8414
 * 
 * extends OIDC discovery metadata 
 */
@Controller
@Tag(name = "OAuth 2.0 Authorization Server Metadata" )
public class OAuth2MetadataEndpoint {

    public static final String OAUTH2_CONFIGURATION_URL = Config.WELL_KNOWN_URL + "/oauth-authorization-server";

    private static Map<String, Object> configuration;

    @Value("${application.url}")
    private String applicationURL;

    @Autowired
    OpenIDMetadataEndpoint oidcMetadataEndpoint;

    @Autowired
    private JWTSigningAndValidationService signService;

    @Operation(summary = "Get authorization server metadata")
    @RequestMapping(method = RequestMethod.GET, value = OAUTH2_CONFIGURATION_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Map<String, Object> serverMetadata() {
        return getConfiguration();
    }

    private Map<String, Object> getConfiguration() {
        if (configuration == null) {
            // auth server metadata
            Map<String, Object> m = getAuthServerMetadata();
            // add session metadata
            m.putAll(oidcMetadataEndpoint.getSessionMetadata());
            // cache
            configuration = m;
        }
        return configuration;
    }

    public Map<String, Object> getAuthServerMetadata() {
        String baseUrl = applicationURL;

        // fetch oidc provider metadata
        // oauth2 metadata are an extension compatible with OIDC
        Map<String, Object> m = oidcMetadataEndpoint.getDiscoveryMetadata();
        //@formatter:off
        /**
         * extend metadata for oauth2 from OIDC Provider Metadata
         * 
             revocation_endpoint 
                 OPTIONAL. URL of the authorization server's OAuth 2.0 revocation endpoint
             revocation_endpoint_auth_methods_supported
                 OPTIONAL.  JSON array containing a list of client authentication
                 methods supported by this revocation endpoint.
             revocation_endpoint_auth_signing_alg_values_supported
                    OPTIONAL.  JSON array containing a list of the JWS signing
                    algorithms ("alg" values) supported by the revocation endpoint for
                    the signature on the JWT
             introspection_endpoint
                    OPTIONAL.  URL of the authorization server's OAuth 2.0
                    introspection endpoint
             introspection_endpoint_auth_methods_supported
                    OPTIONAL.  JSON array containing a list of client authentication
                    methods supported by this introspection endpoint.
             introspection_endpoint_auth_signing_alg_values_supported
                    OPTIONAL.  JSON array containing a list of the JWS signing
                    algorithms ("alg" values) supported by the introspection endpoint
                    for the signature on the JWT
             code_challenge_methods_supported
                     OPTIONAL.  JSON array containing a list of Proof Key for Code
                     Exchange (PKCE) [RFC7636] code challenge methods supported by this
                     authorization server.                   
             authorization_response_iss_parameter_supported
                   Boolean parameter indicating whether the authorization server provides the "iss"
                   parameter in the authorization response as defined in Section 2.
                   If omitted, the default value is false.

         */
        //@formatter:on
        // load all signing alg
        // TODO check support

        List<String> signAlgorithms = signService.getAllSigningAlgsSupported().stream()
                .map(a -> a.getName()).collect(Collectors.toList());

        m.put("revocation_endpoint", baseUrl + TokenRevocationEndpoint.TOKEN_REVOCATION_URL);

        List<String> authMethods = Stream.of(AuthenticationMethod.CLIENT_SECRET_BASIC,
                AuthenticationMethod.CLIENT_SECRET_POST)
                .map(t -> t.getValue()).collect(Collectors.toList());

        m.put("revocation_endpoint", baseUrl + TokenRevocationEndpoint.TOKEN_REVOCATION_URL);
        m.put("revocation_endpoint_auth_methods_supported", authMethods);
        m.put("revocation_endpoint_auth_signing_alg_values_supported", signAlgorithms);

        m.put("introspection_endpoint", baseUrl + TokenIntrospectionEndpoint.TOKEN_INTROSPECTION_URL);
        m.put("introspection_endpoint_auth_methods_supported", authMethods);
        m.put("introspection_endpoint_auth_signing_alg_values_supported", signAlgorithms);

        m.put("code_challenge_methods_supported", Collections.singleton("S256")); // as per spec do not expose plain

        m.put("authorization_response_iss_parameter_supported", true);
        return m;
    }

}
