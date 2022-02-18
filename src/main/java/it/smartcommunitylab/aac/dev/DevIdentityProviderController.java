package it.smartcommunitylab.aac.dev;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import io.swagger.v3.oas.annotations.Hidden;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.NoSuchClientException;
import it.smartcommunitylab.aac.common.NoSuchProviderException;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.common.SystemException;
import it.smartcommunitylab.aac.controller.BaseIdentityProviderController;
import it.smartcommunitylab.aac.core.ClientManager;
import it.smartcommunitylab.aac.core.base.ConfigurableIdentityProvider;
import it.smartcommunitylab.aac.core.base.ConfigurableProvider;
import it.smartcommunitylab.aac.model.ClientApp;

@RestController
@Hidden
@RequestMapping("/console/dev")
public class DevIdentityProviderController extends BaseIdentityProviderController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TypeReference<Map<String, List<ConfigurableIdentityProvider>>> typeRef = new TypeReference<Map<String, List<ConfigurableIdentityProvider>>>() {
    };

    @Autowired
    private ClientManager clientManager;

    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    /*
     * Providers
     */

    @GetMapping("/idptemplates/{realm}")
    public Collection<ConfigurableProvider> getRealmProviderTemplates(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm)
            throws NoSuchRealmException {

        Collection<ConfigurableProvider> providers = providerManager
                .listProviderConfigurationTemplates(realm, ConfigurableProvider.TYPE_IDENTITY);

        return providers;
    }

    @Override
    @GetMapping("/idp/{realm}/{providerId}")
    public ConfigurableIdentityProvider getIdp(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String providerId)
            throws NoSuchProviderException, NoSuchRealmException {
        ConfigurableIdentityProvider provider = super.getIdp(realm, providerId);

        // fetch also configuration schema
        JsonSchema schema = providerManager.getConfigurationSchema(realm, provider.getType(), provider.getAuthority());
        provider.setSchema(schema);

        return provider;
    }

    @Override
    @PostMapping("/idp/{realm}")
    public ConfigurableIdentityProvider addIdp(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestBody @Valid @NotNull ConfigurableIdentityProvider registration) throws NoSuchRealmException {
        ConfigurableIdentityProvider provider = super.addIdp(realm, registration);

        // fetch also configuration schema
        JsonSchema schema = providerManager.getConfigurationSchema(realm, provider.getType(), provider.getAuthority());
        provider.setSchema(schema);

        return provider;
    }

    @Override
    @PutMapping("/idp/{realm}/{providerId}")
    public ConfigurableIdentityProvider updateIdp(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String providerId,
            @RequestBody @Valid @NotNull ConfigurableIdentityProvider registration,
            @RequestParam(required = false, defaultValue = "false") Optional<Boolean> force)
            throws NoSuchRealmException, NoSuchProviderException {
        ConfigurableIdentityProvider provider = super.updateIdp(realm, providerId, registration, Optional.of(false));

        // fetch also configuration schema
        JsonSchema schema = providerManager.getConfigurationSchema(realm, provider.getType(), provider.getAuthority());
        provider.setSchema(schema);

        return provider;
    }

    /*
     * Import/export for console
     */
    @PutMapping("/idp/{realm}")
    public Collection<ConfigurableIdentityProvider> importRealmProvider(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestParam(required = false, defaultValue = "false") boolean reset,
            @RequestParam("file") @Valid @NotNull @NotBlank MultipartFile file) throws RegistrationException {
        logger.debug("import idp(s) to realm {}", StringUtils.trimAllWhitespace(realm));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("empty file");
        }

        if (file.getContentType() == null) {
            throw new IllegalArgumentException("invalid file");
        }

        if (!SystemKeys.MEDIA_TYPE_YAML.toString().equals(file.getContentType())
                && !SystemKeys.MEDIA_TYPE_YML.toString().equals(file.getContentType())
                && !SystemKeys.MEDIA_TYPE_XYAML.toString().equals(file.getContentType())) {
            throw new IllegalArgumentException("invalid file");
        }

        try {
            List<ConfigurableIdentityProvider> providers = new ArrayList<>();
            boolean multiple = false;

            // read as raw yaml to check if collection
            Yaml yaml = new Yaml();
            Map<String, Object> obj = yaml.load(file.getInputStream());
            multiple = obj.containsKey("providers");

            if (multiple) {
                Map<String, List<ConfigurableIdentityProvider>> list = yamlObjectMapper.readValue(file.getInputStream(),
                        typeRef);

                for (ConfigurableIdentityProvider registration : list.get("providers")) {
                    // unpack and build model
                    String id = registration.getProvider();
                    if (reset) {
                        // reset id
                        id = null;
                    }
                    String authority = registration.getAuthority();
                    String name = registration.getName();
                    String description = registration.getDescription();
                    String displayMode = registration.getDisplayMode();
                    String persistence = registration.getPersistence();
                    String events = registration.getEvents();
                    Map<String, Serializable> configuration = registration.getConfiguration();
                    Map<String, String> hookFunctions = registration.getHookFunctions();

                    ConfigurableIdentityProvider provider = new ConfigurableIdentityProvider(authority, id, realm);
                    provider.setName(name);
                    provider.setDescription(description);
                    provider.setDisplayMode(displayMode);
                    provider.setEnabled(false);
                    provider.setPersistence(persistence);
                    provider.setEvents(events);
                    provider.setConfiguration(configuration);
                    provider.setHookFunctions(hookFunctions);

                    provider = providerManager.addIdentityProvider(realm, provider);

                    // fetch also configuration schema
                    JsonSchema schema = providerManager.getConfigurationSchema(realm, provider.getType(),
                            provider.getAuthority());
                    provider.setSchema(schema);
                    providers.add(provider);
                }
            } else {
                // try single element
                ConfigurableIdentityProvider registration = yamlObjectMapper.readValue(file.getInputStream(),
                        ConfigurableIdentityProvider.class);

                // unpack and build model
                String id = registration.getProvider();
                if (reset) {
                    // reset id
                    id = null;
                }
                String authority = registration.getAuthority();
                String type = registration.getType();
                String name = registration.getName();
                String description = registration.getDescription();
                String displayMode = registration.getDisplayMode();
                String persistence = registration.getPersistence();
                String events = registration.getEvents();
                Map<String, Serializable> configuration = registration.getConfiguration();
                Map<String, String> hookFunctions = registration.getHookFunctions();

                ConfigurableIdentityProvider provider = new ConfigurableIdentityProvider(authority, id, realm);
                provider.setName(name);
                provider.setDescription(description);
                provider.setDisplayMode(displayMode);
                provider.setType(type);
                provider.setEnabled(false);
                provider.setPersistence(persistence);
                provider.setEvents(events);
                provider.setConfiguration(configuration);
                provider.setHookFunctions(hookFunctions);

                provider = providerManager.addIdentityProvider(realm, provider);

                // fetch also configuration schema
                JsonSchema schema = providerManager.getConfigurationSchema(realm, provider.getType(),
                        provider.getAuthority());
                provider.setSchema(schema);
                providers.add(provider);
            }
            return providers;

        } catch (Exception e) {
            logger.error("error importing providers: " + e.getMessage());
            throw new RegistrationException(e.getMessage());
        }

    }

    @GetMapping("/idp/{realm}/{providerId}/export")
    public void exportRealmProvider(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String providerId,
            HttpServletResponse res)
            throws NoSuchProviderException, NoSuchRealmException, SystemException, IOException {
        logger.debug("export idp {} for realm {}",
                StringUtils.trimAllWhitespace(providerId), StringUtils.trimAllWhitespace(realm));

        ConfigurableIdentityProvider provider = providerManager.getIdentityProvider(realm, providerId);

//      String s = yaml.dump(clientApp);
        String s = yamlObjectMapper.writeValueAsString(provider);

        // write as file
        res.setContentType("text/yaml");
        res.setHeader("Content-Disposition", "attachment;filename=idp-" + provider.getName() + ".yaml");
        ServletOutputStream out = res.getOutputStream();
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();

    }

    /*
     * Clients
     */
    @PutMapping("/idp/{realm}/{providerId}/apps/{clientId}")
    public ResponseEntity<ClientApp> updateRealmProviderClientApp(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String providerId,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String clientId,
            @RequestBody @Valid @NotNull ClientApp app)
            throws NoSuchRealmException, NoSuchUserException, NoSuchClientException, SystemException,
            NoSuchProviderException {

        ClientApp clientApp = clientManager.getClientApp(realm, clientId);
        // update providers only for this id
        Set<String> providers = new HashSet<>(Arrays.asList(clientApp.getProviders()));
        boolean enabled = Arrays.stream(app.getProviders()).anyMatch(p -> providerId.equals(p));
        if (enabled) {
            if (!providers.contains(providerId)) {
                providers.add(providerId);
                clientApp.setProviders(providers.toArray(new String[0]));
                clientApp = clientManager.updateClientApp(realm, clientId, clientApp);
            }
        } else {
            if (providers.contains(providerId)) {
                providers.remove(providerId);
                clientApp.setProviders(providers.toArray(new String[0]));
                clientApp = clientManager.updateClientApp(realm, clientId, clientApp);
            }
        }

        return ResponseEntity.ok(clientApp);
    }
}
