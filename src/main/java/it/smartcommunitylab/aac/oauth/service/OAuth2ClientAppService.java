package it.smartcommunitylab.aac.oauth.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.NoSuchClientException;
import it.smartcommunitylab.aac.core.service.ClientAppService;
import it.smartcommunitylab.aac.model.ClientApp;
import it.smartcommunitylab.aac.oauth.client.OAuth2Client;
import it.smartcommunitylab.aac.oauth.client.OAuth2ClientConfigMap;

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
    public ClientApp findClient(String clientId) {
        OAuth2Client client = clientService.findClient(clientId);
        if (client == null) {
            return null;
        }

        return toApp(client);
    }

    @Override
    public ClientApp getClient(String clientId) throws NoSuchClientException {
        OAuth2Client client = clientService.getClient(clientId);

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

        String name = app.getName();
        String description = app.getDescription();

        if (StringUtils.hasText(name)) {
            name = Jsoup.clean(name, Whitelist.none());
        }
        if (StringUtils.hasText(description)) {
            description = Jsoup.clean(description, Whitelist.none());
        }

        if (app.getConfiguration() == null) {
            // add as new
            OAuth2Client client = clientService.addClient(realm,
                    app.getClientId(),
                    name, description,
                    Arrays.asList(app.getScopes()), Arrays.asList(app.getResourceIds()),
                    Arrays.asList(app.getProviders()),
                    app.getHookFunctions(), app.getHookWebUrls(), app.getHookUniqueSpaces(),
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
            Map<String, Serializable> configuration = app.getConfiguration();

            // convert to proper model
            OAuth2ClientConfigMap configMap = new OAuth2ClientConfigMap(configuration);

            // we don't want to set a user submitted secret
            String clientSecret = null;

            // register with autogenerated clientId
            // add as new
            OAuth2Client client = clientService.addClient(realm,
                    app.getClientId(),
                    name, description,
                    Arrays.asList(app.getScopes()), Arrays.asList(app.getResourceIds()),
                    Arrays.asList(app.getProviders()),
                    app.getHookFunctions(), app.getHookWebUrls(), app.getHookUniqueSpaces(),
                    clientSecret,
                    configMap.getAuthorizedGrantTypes(), configMap.getRedirectUris(),
                    configMap.getTokenType(), configMap.getAuthenticationMethods(),
                    configMap.getFirstParty(), configMap.getAutoApproveScopes(),
                    configMap.getAccessTokenValidity(), configMap.getRefreshTokenValidity(),
                    configMap.getJwtSignAlgorithm(),
                    configMap.getJwtEncMethod(), configMap.getJwtEncAlgorithm(),
                    configMap.getJwks(), configMap.getJwksUri(),
                    configMap.getAdditionalInformation());

            return toApp(client);
        }
    }

    @Override
    public ClientApp updateClient(String clientId, ClientApp app) throws NoSuchClientException {
        OAuth2Client client = clientService.getClient(clientId);

        String name = app.getName();
        String description = app.getDescription();

        if (StringUtils.hasText(name)) {
            name = Jsoup.clean(name, Whitelist.none());
        }
        if (StringUtils.hasText(description)) {
            description = Jsoup.clean(description, Whitelist.none());
        }

        // unpack
        Map<String, Serializable> configuration = app.getConfiguration();

        // convert to proper model
        OAuth2ClientConfigMap configMap = new OAuth2ClientConfigMap(configuration);

        // update
        client = clientService.updateClient(clientId,
                name, description,
                Arrays.asList(app.getScopes()), Arrays.asList(app.getResourceIds()),
                Arrays.asList(app.getProviders()),
                app.getHookFunctions(), app.getHookWebUrls(), app.getHookUniqueSpaces(),
                configMap.getAuthorizedGrantTypes(), configMap.getRedirectUris(),
                configMap.getTokenType(), configMap.getAuthenticationMethods(),
                configMap.getFirstParty(), configMap.getAutoApproveScopes(),
                configMap.getAccessTokenValidity(), configMap.getRefreshTokenValidity(),
                configMap.getJwtSignAlgorithm(),
                configMap.getJwtEncMethod(), configMap.getJwtEncAlgorithm(),
                configMap.getJwks(), configMap.getJwksUri(),
                configMap.getAdditionalInformation());

        return toApp(client);

    }

    @Override
    public void deleteClient(String clientId) {
        OAuth2Client client = clientService.findClient(clientId);
        if (client != null) {
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

        app.setScopes(client.getScopes() != null ? client.getScopes().toArray(new String[0]) : null);
        app.setResourceIds(client.getResourceIds() != null ? client.getResourceIds().toArray(new String[0]) : null);
        app.setProviders(client.getProviders() != null ? client.getProviders().toArray(new String[0]) : null);
        if (client.getHookFunctions() != null) {
            app.setHookFunctions(client.getHookFunctions());
        }
        if (client.getHookWebUrls() != null) {
            app.setHookWebUrls(client.getHookWebUrls());
        }
        app.setHookUniqueSpaces(client.getHookUniqueSpaces());

        // flatten configuration
        app.setConfiguration(client.getConfiguration());

        return app;
    }

    @Override
    public JsonSchema getConfigurationSchema() {
        try {
            return OAuth2ClientConfigMap.getConfigurationSchema();
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
