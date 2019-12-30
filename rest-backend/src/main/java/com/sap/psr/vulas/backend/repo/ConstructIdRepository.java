/**
 * This file is part of Eclipse Steady.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.repo;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

/**
 * <p>ConstructIdRepository interface.</p>
 *
 */
@RepositoryRestResource(collectionResourceRel = "constructIds", path = "constructIds")
public interface ConstructIdRepository extends PagingAndSortingRepository<ConstructId, Long> {

	/** Constant <code>FILTER</code> */
	public static final ResultSetFilter<ConstructId> FILTER = new ResultSetFilter<ConstructId>();
	
	/**
	 * {@inheritDoc}
	 *
	 * This method must not be exposed, as {@link ConstructId}s are only created when
	 * other entities are uploaded.
	 */
	@Override
	@RestResource(exported = false)
	<S extends ConstructId> S save(S entity);
	
	//@RestResource(path = "byMaxPrice")
	/**
	 * <p>findConstructId.</p>
	 *
	 * @param lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
	 * @param type a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
	 * @param qname a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT cid FROM ConstructId AS cid WHERE cid.lang = :lang AND cid.type = :type AND cid.qname = :qname")
	List<ConstructId> findConstructId(@Param("lang") ProgrammingLanguage lang, @Param("type") ConstructType type, @Param("qname") String qname);
	
	/**
	 * <p>findByType.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	List<ConstructId> findByType(@Param("type") String type);
	
	/**
	 * <p>findByLang.</p>
	 *
	 * @param lang a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	List<ConstructId> findByLang(@Param("lang") String lang);
	
	//@Query("SELECT cid FROM ConstructId AS cid WHERE cid.qname LIKE = :qname")
	//List<ConstructId> findByQnamePrefix(@Param("lang") String lang, @Param("type") String type, @Param("qname") String qname);
	
	
	/**
	 * <p>countReachableExecConstructsLibrary.</p>
	 *
	 * @param dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 * @return a {@link java.lang.Integer} object.
	 */
	@Query(value="select count(*) from "
			+ " (select reachable_construct_ids_id from app_dependency_reachable_construct_ids where dependency_id =:dep) AS j"
			+ "  inner join (select id from construct_id where type='METH' or type='CONS' or type='INIT') as c  "
			+ "  on j.reachable_construct_ids_id=c.id   ",nativeQuery=true)
	Integer countReachableExecConstructsLibrary(@Param("dep") Dependency dep);
}
