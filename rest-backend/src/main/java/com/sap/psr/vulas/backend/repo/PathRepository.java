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

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.Path;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;

/**
 * <p>PathRepository interface.</p>
 *
 */
@Repository
public interface PathRepository extends CrudRepository<Path, Long>, PathRepositoryCustom {
	
	/** Constant <code>FILTER</code> */
	public static final ResultSetFilter<Path> FILTER = new ResultSetFilter<Path>();
	
	/**
	 * <p>findByApp.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @return a {@link java.util.List} object.
	 */
	List<Path> findByApp(@Param("app") Application app);
	
	/**
	 * Deletes all paths collected in the context of the given {@link Application}.
	 * Called by goal {@link GoalType#CLEAN}.
	 *
	 * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 */
	@Modifying
	@Transactional
	@Query("DELETE FROM Path AS p WHERE p.app = :app")
	void deleteAllPathsForApp(@Param("app") Application app);
	
	/**
	 * <p>findPathsForApp.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT p FROM Path AS p WHERE p.app = :app")
	List<Path> findPathsForApp(@Param("app") Application app);
	
	//@Query("SELECT p FROM Path AS p WHERE p.app = :app AND p.lib = :lib AND p.bug = :bug")
	//List<Path> findPathsForLibraryBug(@Param("app") Application app, @Param("lib") Library lib, @Param("bug") Bug bug);
	
	/**
	 * <p>findPathsForLibraryBug.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @param bug_id a {@link java.lang.Long} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT p FROM Path AS p WHERE p.app = :app AND p.lib = :lib AND p.bug.id = :bug_id")
	List<Path> findPathsForLibraryBug(@Param("app") Application app, @Param("lib") Library lib, @Param("bug_id") Long bug_id);
	
	/**
	 * <p>findPathsForLibraryBugConstructName.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 * @param qname a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT p FROM Path p WHERE (p.endConstructId.qname = :qname OR p.startConstructId.qname = :qname) AND p.app = :app AND p.lib = :lib AND p.bug = :bug")
	List<Path> findPathsForLibraryBugConstructName(@Param("app") Application app, @Param("lib") Library lib, @Param("bug") Bug bug, @Param("qname") String qname);
	
	/**
	 * <p>findPath.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 * @param source a {@link com.sap.psr.vulas.shared.enums.PathSource} object.
	 * @param startConstructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
	 * @param endConstructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT p FROM Path AS p WHERE p.app = :app AND p.bug = :bug AND p.source = :source AND p.startConstructId = :startConstructId AND p.endConstructId = :endConstructId")
	List<Path> findPath(@Param("app") Application app, @Param("bug") Bug bug, @Param("source") PathSource source, @Param("startConstructId") ConstructId startConstructId, @Param("endConstructId") ConstructId endConstructId);
}
