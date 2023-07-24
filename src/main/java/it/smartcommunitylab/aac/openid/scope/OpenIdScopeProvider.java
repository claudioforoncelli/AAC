package it.smartcommunitylab.aac.openid.scope;

import it.smartcommunitylab.aac.scope.Resource;
import it.smartcommunitylab.aac.scope.Scope;
import it.smartcommunitylab.aac.scope.ScopeApprover;
import it.smartcommunitylab.aac.scope.ScopeProvider;
import it.smartcommunitylab.aac.scope.WhitelistScopeApprover;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OpenIdScopeProvider implements ScopeProvider {

    private static final OpenIdResource resource = new OpenIdResource();
    public static final Set<Scope> scopes;
    private static final Map<String, WhitelistScopeApprover> approvers;

    static {
        Set<Scope> s = new HashSet<>();
        s.add(new OpenIdScope());
        s.add(new OfflineAccessScope());

        scopes = Collections.unmodifiableSet(s);
        resource.setScopes(scopes);

        Map<String, WhitelistScopeApprover> a = new HashMap<>();
        for (Scope sc : scopes) {
            a.put(sc.getScope(), new WhitelistScopeApprover(null, sc.getResourceId(), sc.getScope()));
        }

        approvers = a;
    }

    @Override
    public String getResourceId() {
        return OpenIdResource.RESOURCE_ID;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public Collection<Scope> getScopes() {
        return scopes;
    }

    @Override
    public ScopeApprover getApprover(String scope) {
        return approvers.get(scope);
    }
}
