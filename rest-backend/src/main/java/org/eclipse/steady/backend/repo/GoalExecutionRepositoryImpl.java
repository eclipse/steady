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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.GoalExecution;
import org.eclipse.steady.backend.model.Property;
import org.eclipse.steady.backend.util.ReferenceUpdater;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.shared.util.StringList.CaseSensitivity;
import org.eclipse.steady.shared.util.StringList.ComparisonMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>GoalExecutionRepositoryImpl class.</p>
 *
 */
public class GoalExecutionRepositoryImpl implements GoalExecutionRepositoryCustom {

  private static Logger log = LoggerFactory.getLogger(GoalExecutionRepositoryImpl.class);

  @Autowired GoalExecutionRepository gexeRepository;

  @Autowired ApplicationRepository appRepository;

  @Autowired ReferenceUpdater refUpdater;

  // @CacheEvict(value="gexe", key="#_app")
  /** {@inheritDoc} */
  @Override
  public GoalExecution customSave(Application _app, GoalExecution _provided_gexe) {
    GoalExecution managed_gexe = null;

    try {
      managed_gexe =
          GoalExecutionRepository.FILTER.findOne(
              this.gexeRepository.findByExecutionId(_provided_gexe.getExecutionId()));
      _provided_gexe.setId(managed_gexe.getId());
      _provided_gexe.setCreatedAt(managed_gexe.getCreatedAt());
    } catch (EntityNotFoundException e1) {
    }

    // Update refs to independent entities
    _provided_gexe.setApp(_app);
    _provided_gexe.setConfiguration(
        refUpdater.saveNestedProperties(_provided_gexe.getConfiguration()));
    _provided_gexe.setSystemInfo(
        refUpdater.saveNestedProperties(this.filterSystemInfo(_provided_gexe.getSystemInfo())));

    // update the lastScan timestamp of the application (we already have a managed application here)
    appRepository.refreshLastScanbyApp(_app);

    // Save
    try {
      managed_gexe = this.gexeRepository.save(_provided_gexe);
    } catch (Exception e) {
      throw new PersistenceException(
          "Error while saving goal execution [" + _provided_gexe + "]: " + e.getMessage());
    }
    return managed_gexe;
  }

  // @Cacheable(value="gexe", key="#_app", sync=true) //, unless="#result.isEmpty()") // Note that
  // in Spring proxy mode (default), only external calls will be cached. Calls within the class
  // (this....) will not.
  /** {@inheritDoc} */
  @Override
  public GoalExecution findLatestGoalExecution(Application _app, GoalType _type) {
    Long id = null;
    if (_type != null) id = this.gexeRepository.findLatestForApp(_app.getId(), _type.toString());
    else id = this.gexeRepository.findLatestForApp(_app.getId());
    if (id != null) return this.gexeRepository.findById(id).orElse(null);
    else return null;
  }

  /**
   * Only keeps system info properties whose name matches the configured whitelist.
   * @param _in
   * @return
   */
  private Collection<Property> filterSystemInfo(Collection<Property> _in) {
    final StringList env_whitelist =
        VulasConfiguration.getGlobal()
            .getStringList(VulasConfiguration.ENV_VARS, VulasConfiguration.ENV_VARS_CUSTOM);
    final StringList sys_whitelist =
        VulasConfiguration.getGlobal()
            .getStringList(VulasConfiguration.SYS_PROPS, VulasConfiguration.SYS_PROPS_CUSTOM);
    final Collection<Property> out = new HashSet<Property>();
    final Iterator<Property> iter = _in.iterator();
    while (iter.hasNext()) {
      final Property p = iter.next();
      if (sys_whitelist.contains(
              p.getName(), ComparisonMode.STARTSWITH, CaseSensitivity.CASE_INSENSITIVE)
          || env_whitelist.contains(
              p.getName(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE)) out.add(p);
    }
    return out;
  }
}
