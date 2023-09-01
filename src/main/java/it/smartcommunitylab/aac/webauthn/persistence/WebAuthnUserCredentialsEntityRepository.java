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

package it.smartcommunitylab.aac.webauthn.persistence;

import it.smartcommunitylab.aac.repository.CustomJpaRepository;
import it.smartcommunitylab.aac.repository.DetachableJpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface WebAuthnUserCredentialsEntityRepository
    extends
        CustomJpaRepository<WebAuthnUserCredentialEntity, String>,
        DetachableJpaRepository<WebAuthnUserCredentialEntity> {
    List<WebAuthnUserCredentialEntity> findByRepositoryId(String repositoryId);

    List<WebAuthnUserCredentialEntity> findByRealm(String realm);

    List<WebAuthnUserCredentialEntity> findByRepositoryIdAndUserId(String repositoryId, String userId);

    WebAuthnUserCredentialEntity findByRepositoryIdAndUserHandleAndCredentialId(
        String repositoryId,
        String userHandle,
        String credentialId
    );

    List<WebAuthnUserCredentialEntity> findByRepositoryIdAndCredentialId(String repositoryId, String credentialId);

    // List<WebAuthnUserCredentialEntity> findByRepositoryIdAndUsername(String repositoryId, String username);

    List<WebAuthnUserCredentialEntity> findByRepositoryIdAndUserHandle(String repositoryId, String userHandle);
}
