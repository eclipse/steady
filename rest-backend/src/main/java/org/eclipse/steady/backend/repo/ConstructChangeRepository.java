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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.repo;

import java.util.List;

import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.ConstructChange;
import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// TODO: Make read-only, as construct ids should only be created by other APIs
/**
 * <p>ConstructChangeRepository interface.</p>
 *
 */
@RepositoryRestResource(collectionResourceRel = "constructChanges", path = "constructChanges")
public interface ConstructChangeRepository
    extends PagingAndSortingRepository<ConstructChange, Long> {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<ConstructChange> FILTER =
      new ResultSetFilter<ConstructChange>();

  /**
   * <p>findByRepoPathCommitCidBug.</p>
   *
   * @param repo a {@link java.lang.String} object.
   * @param path a {@link java.lang.String} object.
   * @param commit a {@link java.lang.String} object.
   * @param cid a {@link org.eclipse.steady.backend.model.ConstructId} object.
   * @param bug a {@link org.eclipse.steady.backend.model.Bug} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT c FROM ConstructChange AS c WHERE c.repo=:repo AND "
          + "c.repoPath =:path AND "
          + "c.commit =:commit AND "
          + "c.constructId=:cid AND "
          + "c.bug =:bug")
  List<ConstructChange> findByRepoPathCommitCidBug(
      @Param("repo") String repo,
      @Param("path") String path,
      @Param("commit") String commit,
      @Param("cid") ConstructId cid,
      @Param("bug") Bug bug);
}
