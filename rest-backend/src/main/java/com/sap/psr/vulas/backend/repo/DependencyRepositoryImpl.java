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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.TouchPoint;
import com.sap.psr.vulas.backend.util.ReferenceUpdater;

/**
 * <p>DependencyRepositoryImpl class.</p>
 *
 */
public class DependencyRepositoryImpl implements DependencyRepositoryCustom {

  private static Logger log = LoggerFactory.getLogger(DependencyRepositoryImpl.class);

  @Autowired ReferenceUpdater refUpdater;

  @Autowired DependencyRepository depRepository;

  @Autowired ConstructIdRepository cidRepository;

  /**
   * <p>saveReachableConstructIds.</p>
   *
   * @param _dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _construct_ids an array of {@link com.sap.psr.vulas.backend.model.ConstructId} objects.
   * @return a {@link java.util.Set} object.
   */
  public Set<ConstructId> saveReachableConstructIds(Dependency _dep, ConstructId[] _construct_ids) {
    final List<ConstructId> provided_construct_ids = Arrays.asList(_construct_ids);
    final Set<ConstructId> managed_construct_ids = new HashSet<ConstructId>();
    managed_construct_ids.addAll(this.refUpdater.saveNestedConstructIds(provided_construct_ids));
    _dep.addReachableConstructIds(managed_construct_ids);
    this.depRepository.save(_dep);
    return managed_construct_ids;
  }

  /**
   * <p>saveTouchPoints.</p>
   *
   * @param _dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _touch_points an array of {@link com.sap.psr.vulas.backend.model.TouchPoint} objects.
   * @return a {@link java.util.Set} object.
   */
  public Set<TouchPoint> saveTouchPoints(Dependency _dep, TouchPoint[] _touch_points) {
    final Set<TouchPoint> provided_touch_points =
        new HashSet<TouchPoint>(Arrays.asList(_touch_points));
    final Set<TouchPoint> managed_touch_points = new HashSet<TouchPoint>();
    ConstructId managed_cid = null, provided_cid = null;
    for (TouchPoint tp : provided_touch_points) {
      provided_cid = tp.getFrom();
      try {
        managed_cid =
            ConstructIdRepository.FILTER.findOne(
                this.cidRepository.findConstructId(
                    provided_cid.getLang(), provided_cid.getType(), provided_cid.getQname()));
      } catch (EntityNotFoundException e) {
        managed_cid = this.cidRepository.save(provided_cid);
      }
      tp.setFrom(managed_cid);

      provided_cid = tp.getTo();
      try {
        managed_cid =
            ConstructIdRepository.FILTER.findOne(
                this.cidRepository.findConstructId(
                    provided_cid.getLang(), provided_cid.getType(), provided_cid.getQname()));
      } catch (EntityNotFoundException e) {
        managed_cid = this.cidRepository.save(provided_cid);
      }
      tp.setTo(managed_cid);
      managed_touch_points.add(tp);
    }
    _dep.addTouchPoints(managed_touch_points);
    this.depRepository.save(_dep);
    return managed_touch_points;
  }
}
