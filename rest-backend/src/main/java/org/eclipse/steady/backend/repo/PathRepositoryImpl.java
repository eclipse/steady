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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.Path;
import org.eclipse.steady.backend.model.PathNode;
import org.eclipse.steady.backend.util.ReferenceUpdater;
import org.eclipse.steady.shared.util.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>PathRepositoryImpl class.</p>
 *
 */
public class PathRepositoryImpl implements PathRepositoryCustom {

  @Autowired ApplicationRepository appRepository;

  @Autowired LibraryRepository libRepository;

  @Autowired ConstructIdRepository cidRepository;

  @Autowired PathRepository pathRepository;

  @Autowired ReferenceUpdater refUpdater;

  @Autowired DependencyRepository depRepository;

  @Autowired BugRepository bugRepository;

  /**
   * <p>customSave.</p>
   *
   * @return the saved bug
   * @param _app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param _paths an array of {@link org.eclipse.steady.backend.model.Path} objects.
   * @throws javax.persistence.PersistenceException if any.
   */
  public List<Path> customSave(Application _app, Path[] _paths) throws PersistenceException {
    final StopWatch sw =
        new StopWatch("Save [" + _paths.length + "] paths for app " + _app).start();

    // To be returned
    List<Path> paths = new ArrayList<Path>();

    // Helpers needed for updating the properties of the provided traces
    Path managed_path = null;
    ConstructId managed_cid = null;
    Library managed_lib = null;

    for (Path provided_path : _paths) {

      // Update constructs and libs of the nodes of the path
      for (PathNode node : provided_path.getPath()) {
        try {
          managed_cid =
              ConstructIdRepository.FILTER.findOne(
                  this.cidRepository.findConstructId(
                      node.getConstructId().getLang(),
                      node.getConstructId().getType(),
                      node.getConstructId().getQname()));
        } catch (EntityNotFoundException e) {
          managed_cid = this.cidRepository.save(node.getConstructId());
        }
        node.setConstructId(managed_cid);

        if (node.getLib() != null) {
          try {
            managed_lib =
                LibraryRepository.FILTER.findOne(
                    this.libRepository.findByDigest(node.getLib().getDigest()));
            node.setLib(managed_lib);
          } catch (EntityNotFoundException e) {
            // TODO: What if the library does not exist
            node.setLib(null);
          }
        }
      }

      // Set managed objects (and throw exception if any of them cannot be found)
      try {
        provided_path.setApp(_app);
        provided_path.setBug(
            BugRepository.FILTER.findOne(
                this.bugRepository.findByBugId(provided_path.getBug().getBugId())));

        // Infer start and end construct and library
        provided_path.inferStartConstructId();
        provided_path.inferEndConstructId();
        provided_path.inferLibrary();
      } catch (EntityNotFoundException e1) {
        throw new PersistenceException("Referenced entity not found: " + e1);
      }

      // Create or update
      try {
        managed_path =
            PathRepository.FILTER.findOne(
                this.pathRepository.findPath(
                    provided_path.getApp(),
                    provided_path.getBug(),
                    provided_path.getSource(),
                    provided_path.getStartConstructId(),
                    provided_path.getEndConstructId()));

        // Found existing path
        provided_path.setId(managed_path.getId());
      } catch (EntityNotFoundException e1) {
      }

      // Save
      try {
        managed_path = this.pathRepository.save(provided_path);
        paths.add(managed_path);
      } catch (Exception e) {
        throw new PersistenceException(
            "Error while saving path " + provided_path + ": " + e.getMessage());
      }
    }
    sw.stop();
    return paths;
  }
}
