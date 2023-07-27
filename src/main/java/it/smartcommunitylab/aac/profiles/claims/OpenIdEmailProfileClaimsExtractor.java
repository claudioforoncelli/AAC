/*
 * Copyright 2023 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.smartcommunitylab.aac.profiles.claims;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.common.InvalidDefinitionException;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.profiles.extractor.OpenIdProfileExtractor;
import it.smartcommunitylab.aac.profiles.model.OpenIdProfile;
import java.util.Collection;
import java.util.Collections;

public class OpenIdEmailProfileClaimsExtractor extends ProfileClaimsExtractor {

    private final OpenIdProfileExtractor extractor;

    public OpenIdEmailProfileClaimsExtractor() {
        this.extractor = new OpenIdProfileExtractor();
    }

    @Override
    public Collection<String> getScopes() {
        return Collections.singleton(Config.SCOPE_EMAIL);
    }

    @Override
    public String getKey() {
        // no key for email, we merge to TLD
        return null;
    }

    @Override
    protected OpenIdProfile buildUserProfile(User user, Collection<String> scopes) throws InvalidDefinitionException {
        if (!scopes.contains(Config.SCOPE_OPENID)) {
            return null;
        }

        OpenIdProfile profile = extractor.extractUserProfile(user);

        // narrow down to dedicated profile
        return profile.toEmailProfile();
    }
}
