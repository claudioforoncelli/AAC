package it.smartcommunitylab.aac.oauth.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.NoSuchClientException;
import it.smartcommunitylab.aac.core.service.ClientAppService;
import it.smartcommunitylab.aac.model.ClientApp;
import it.smartcommunitylab.aac.oauth.client.OAuth2Client;

/*
 * OAuth2 clients service
 */

@Service
public class OAuth2ClientAppService implements ClientAppService {

    private final OAuth2ClientService clientService;

    public OAuth2ClientAppService(OAuth2ClientService clientService) {
        Assert.notNull(clientService, "client service is mandatory");
        this.clientService = clientService;
    }

    @Override
    public Collection<ClientApp> listClients(String realm) {
        List<OAuth2Client> clients = clientService.listClients(realm);

        // reset credentials, accessible only if single fetch
        clients.stream()
                .forEach(c -> c.setClientSecret(null));

        return clients.stream()
                .map(c -> toApp(c))
                .collect(Collectors.toList());
    }

    @Override
    public ClientApp getClient(String realm, String clientId) throws NoSuchClientException {
        OAuth2Client client = clientService.getClient(clientId);

        // check realm match
        if (!client.getRealm().equals(realm)) {
            throw new AccessDeniedException("realm mismatch");
        }

        return toApp(client);
    }

    @Override
    public ClientApp registerClient(String realm, String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("empty name is invalid");
        }

        OAuth2Client client = clientService.addClient(realm, name);

        return toApp(client);
    }

    @Override
    public ClientApp registerClient(String realm, ClientApp app) {

        if (app.getConfiguration() == null) {
            // add as new
            OAuth2Client client = clientService.addClient(realm,
                    app.getClientId(),
                    app.getName(), app.getDescription(),
                    app.getScopes(), app.getResourceIds(),
                    app.getProviders(),
                    null,
                    null, null,
                    null,
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null);

            return toApp(client);
        } else {

            // unpack
            Map<String, Serializable> configMap = app.getConfiguration();
            configMap.put("realm", realm);
            // we set a placeholder for clientId to use the converter
            // TODO fix properly
            configMap.put("clientId", "<clientId>");
            OAuth2Client clientApp = OAuth2Client.convert(configMap);

            // register with autogenerated clientId

            // add as new
            OAuth2Client client = clientService.addClient(realm,
                    app.getClientId(),
                    app.getName(), app.getDescription(),
                    app.getScopes(), app.getResourceIds(),
                    app.getProviders(),
                    clientApp.getSecret(),
                    clientApp.getAuthorizedGrantTypes(), clientApp.getRedirectUris(),
                    clientApp.getTokenType(), clientApp.getAuthenticationScheme(),
                    clientApp.getFirstParty(), clientApp.getAutoApproveScopes(),
                    clientApp.getAccessTokenValidity(), clientApp.getRefreshTokenValidity(),
                    clientApp.getJwtSignAlgorithm(),
                    clientApp.getJwtEncMethod(), clientApp.getJwtEncAlgorithm(),
                    clientApp.getJwks(), clientApp.getJwksUri(),
                    clientApp.getAdditionalInformation());

            return toApp(client);
        }
    }

    @Override
    public ClientApp updateClient(String realm, String clientId, ClientApp app) throws NoSuchClientException {
        OAuth2Client client = clientService.getClient(clientId);

        // check realm match
        if (!client.getRealm().equals(realm)) {
            throw new AccessDeniedException("realm mismatch");
        }

        // unpack
        Map<String, Serializable> configMap = app.getConfiguration();
        configMap.put("realm", realm);
        // we set a placeholder for clientId to use the converter
        // TODO fix properly
        configMap.put("clientId", "<clientId>");
        OAuth2Client clientApp = OAuth2Client.convert(configMap);

        // update
        client = clientService.updateClient(clientId,
                app.getName(), app.getDescription(),
                app.getScopes(), app.getResourceIds(),
                app.getProviders(),
                app.getHookFunctions(),
                clientApp.getAuthorizedGrantTypes(), clientApp.getRedirectUris(),
                clientApp.getTokenType(), clientApp.getAuthenticationScheme(),
                clientApp.getFirstParty(), clientApp.getAutoApproveScopes(),
                clientApp.getAccessTokenValidity(), clientApp.getRefreshTokenValidity(),
                clientApp.getJwtSignAlgorithm(),
                clientApp.getJwtEncMethod(), clientApp.getJwtEncAlgorithm(),
                clientApp.getJwks(), clientApp.getJwksUri(),
                clientApp.getAdditionalInformation());

        return toApp(client);

    }

    @Override
    public void deleteClient(String realm, String clientId) {
        OAuth2Client client = clientService.findClient(clientId);
        if (client != null) {
            // check realm match
            if (!client.getRealm().equals(realm)) {
                throw new AccessDeniedException("realm mismatch");
            }

            clientService.deleteClient(clientId);
        }

    }

    /*
     * helpers
     */
    private ClientApp toApp(OAuth2Client client) {
        ClientApp app = new ClientApp();
        app.setClientId(client.getClientId());
        app.setType(SystemKeys.CLIENT_TYPE_OAUTH2);

        if (StringUtils.hasText(client.getName())) {
            app.setName(client.getName());
        }
        if (StringUtils.hasText(client.getDescription())) {
            app.setDescription(client.getDescription());
        }

        app.setRealm(client.getRealm());

        app.setScopes(client.getScopes());
        app.setResourceIds(client.getResourceIds());
        app.setProviders(client.getProviders());
        app.setHookFunctions(client.getHookFunctions());

        // flatten configuration
        app.setConfiguration(client.getConfigurationMap());

        return app;
    }

}
