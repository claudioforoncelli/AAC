/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.smartcommunitylab.aac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.aac.model.ServiceDescriptor;

/**
 * 
 * @author raman
 *
 */
@Repository
public interface ServiceRepository extends JpaRepository<ServiceDescriptor, String> {

	@Query("select s from ServiceDescriptor s where s.ownerId=?1")
	public List<ServiceDescriptor> findByOwnerId(String ownerId);
	
	@Query("select s from ServiceDescriptor s where s.ownerId IS NULL")
	public List<ServiceDescriptor> findByNullOwnerId();
	
	@Query("select s from ServiceDescriptor s where s.apiKey=?1")
	public ServiceDescriptor findByAPIKey(String apiKey);	
}
