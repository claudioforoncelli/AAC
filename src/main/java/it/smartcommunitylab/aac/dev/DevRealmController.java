package it.smartcommunitylab.aac.dev;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Hidden;
import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.audit.AuditManager;
import it.smartcommunitylab.aac.audit.RealmAuditEvent;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.common.SystemException;
import it.smartcommunitylab.aac.core.ClientManager;
import it.smartcommunitylab.aac.core.ProviderManager;
import it.smartcommunitylab.aac.core.RealmManager;
import it.smartcommunitylab.aac.core.UserDetails;
import it.smartcommunitylab.aac.core.auth.UserAuthentication;
import it.smartcommunitylab.aac.dev.DevUsersController.InvitationBean;
import it.smartcommunitylab.aac.model.Developer;
import it.smartcommunitylab.aac.model.Realm;
import it.smartcommunitylab.aac.services.ServicesManager;

@RestController
@Hidden
@RequestMapping("/console/dev")
public class DevRealmController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RealmManager realmManager;

    @Autowired
    private ProviderManager providerManager;

    @Autowired
    private ClientManager clientManager;

    @Autowired
    private ServicesManager serviceManager;
    @Autowired
    private AuditManager auditManager;

    @Autowired
    @Qualifier("yamlObjectMapper")
    private ObjectMapper yamlObjectMapper;

    @GetMapping("/realms")
    public ResponseEntity<Collection<Realm>> myRealms(UserAuthentication userAuth) throws NoSuchRealmException {
        if (userAuth == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDetails user = userAuth.getUser();

        Collection<Realm> realms = user.getRealms().stream()
                .map(r -> {
                    try {
                        return realmManager.getRealm(r);
                    } catch (NoSuchRealmException e) {
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        if (user.hasAuthority(Config.R_ADMIN)) {
            // system admin can access all realms
            realms = realmManager.listRealms();
        }

        return ResponseEntity.ok(realms);
    }

    @GetMapping("/realms/{realm}")
    @PreAuthorize("hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN') or hasAuthority(#realm+':ROLE_DEVELOPER')")
    public ResponseEntity<Realm> getRealm(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm)
            throws NoSuchRealmException {
        return ResponseEntity.ok(realmManager.getRealm(realm));
    }

    @PutMapping("/realms/{realm}")
    @PreAuthorize("hasAuthority('" + Config.R_ADMIN + "') or hasAuthority(#realm+':ROLE_ADMIN')")
    public ResponseEntity<Realm> updateRealm(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestBody @Valid @NotNull Realm r)
            throws NoSuchRealmException {
        return ResponseEntity.ok(realmManager.updateRealm(realm, r));
    }

    @DeleteMapping("/realms/{realm}")
    @PreAuthorize("hasAuthority('" + Config.R_ADMIN + "') or hasAuthority(#realm+':ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRealm(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm)
            throws NoSuchRealmException {
        realmManager.deleteRealm(realm, true);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/realms/{realm}/export")
    @PreAuthorize("hasAuthority('" + Config.R_ADMIN + "') or hasAuthority(#realm+':ROLE_ADMIN')")
    public void exportRealm(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestParam(required = false, defaultValue = "false") boolean custom,
            @RequestParam(required = false, defaultValue = "false") boolean full,
            HttpServletResponse res)
            throws NoSuchRealmException, SystemException, IOException {

        Realm r = realmManager.getRealm(realm);
        Object export = r;
        String key = r.getSlug();

        if (custom) {
            key = r.getSlug() + "-custom";
            export = Collections.singletonMap("customization", r.getCustomization());
        } else if (full) {
            key = r.getSlug() + "-full";
            Map<String, Collection<? extends Object>> map = new HashMap<>();
            map.put("realms", Collections.singleton(r));
            map.put("providers", providerManager.listProviders(realm));
            map.put("clients", clientManager.listClientApps(realm));
            map.put("services", serviceManager.listServices(realm));
            export = map;
        }

//      String s = yaml.dump(clientApp);
        String s = yamlObjectMapper.writeValueAsString(export);

        // write as file
        res.setContentType("text/yaml");
        res.setHeader("Content-Disposition", "attachment;filename=realm-" + key + ".yaml");
        ServletOutputStream out = res.getOutputStream();
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();

    }

    /*
     * Audit events
     */

    @GetMapping("/realms/{realm}/audit")
    @PreAuthorize("hasAuthority('" + Config.R_ADMIN + "') or hasAuthority(#realm+':ROLE_ADMIN')")
    public ResponseEntity<Collection<RealmAuditEvent>> findEvents(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestParam(required = false, name = "type") Optional<String> type,
            @RequestParam(required = false, name = "after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<Date> after,
            @RequestParam(required = false, name = "before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<Date> before)
            throws NoSuchRealmException {

        return ResponseEntity
                .ok(auditManager.findRealmEvents(realm,
                        type.orElse(null), after.orElse(null), before.orElse(null)));

    }

    /*
     * Dev console users
     */
    @GetMapping("/realms/{realm}/developers")
    @PreAuthorize("hasAuthority('" + Config.R_ADMIN + "') or hasAuthority(#realm+':ROLE_ADMIN')")
    public ResponseEntity<Collection<Developer>> getDevelopers(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm)
            throws NoSuchRealmException {
        Collection<Developer> developers = realmManager.getDevelopers(realm);
        return ResponseEntity
                .ok(developers);
    }

    @PostMapping("/realms/{realm}/developers")
    public ResponseEntity<Developer> inviteDeveloper(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestBody @Valid @NotNull InvitationBean bean) throws NoSuchRealmException, NoSuchUserException {
        Developer developer = realmManager.inviteDeveloper(realm, bean.getSubjectId(), bean.getUsername());
        return ResponseEntity.ok(developer);
    }

    @PutMapping("/realms/{realm}/developers/{subjectId}")
    public ResponseEntity<Developer> updateDeveloper(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subjectId,
            @RequestBody @Valid @NotNull Collection<String> roles) throws NoSuchRealmException, NoSuchUserException {
        Developer developer = realmManager.updateDeveloper(realm, subjectId, roles);
        return ResponseEntity.ok(developer);
    }

    @DeleteMapping("/realms/{realm}/developers/{subjectId}")
    public ResponseEntity<Void> removeDeveloper(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subjectId)
            throws NoSuchRealmException, NoSuchUserException {
        realmManager.removeDeveloper(realm, subjectId);
        return ResponseEntity.ok(null);
    }

}
