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

package it.smartcommunitylab.aac.attributes.store;

import java.io.Serializable;
import java.util.Map;

/*
 * Read only interface for accessing attribute stores
 *
 * could be used for implementing persistent or in memory services
 */
public interface AttributeService {
    public Serializable getAttribute(String entityId, String key);

    public Map<String, Serializable> findAttributes(String entityId);
}
