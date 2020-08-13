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

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.sap.psr.vulas.backend.cve.Cve;
import com.sap.psr.vulas.backend.cve.CveReader2;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.util.StopWatch;

/**
 * <p>BugRepositoryImpl class.</p>
 *
 */
public class BugRepositoryImpl implements BugRepositoryCustom {

  private static Logger log = LoggerFactory.getLogger(BugRepositoryImpl.class);

  /**
   * Required by {@link BugRepositoryImpl#customSave(Bug)}.
   */
  @Autowired BugRepository bugRepository;

  /**
   * Required by {@link BugRepositoryImpl#customSave(Bug)}.
   */
  @Autowired ConstructIdRepository cidRepository;

  @Autowired ConstructChangeRepository ccRepository;

  @Autowired ApplicationRepository appRepository;

  /**
   * {@inheritDoc}
   *
   * Saves the given {@link Bug} together with all the nested {@link ConstructId}s.
   * This method has to be used in favor of the save method provided by the
   * {@link CrudRepository}.
   */
  @CacheEvict(value = "bug", key = "#_bug.bugId")
  @Override
  @Transactional
  public Bug customSave(Bug _bug, Boolean _considerCC) throws PersistenceException {
    final StopWatch sw = new StopWatch("Save bug [" + _bug.getBugId() + "]").start();

    // The external ID
    final String ext_id = _bug.getBugId();

    // Does this bug exist already?
    Bug managed_bug = null;
    try {
      managed_bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(ext_id));
      _bug.setId(managed_bug.getId());
      if (_considerCC) _bug = this.updateConstructChanges(managed_bug, _bug);
      _bug.setCreatedAt(managed_bug.getCreatedAt());
    } catch (EntityNotFoundException e1) {
    }

    // Update refs to independent entities
    if (_considerCC) _bug = this.saveNestedConstructIds(_bug);
    sw.lap("Updated refs to nested constructs");

    // Save
    try {
      managed_bug = this.bugRepository.save(_bug);

      // Update vulnChange timestamp for apps with construct changes among its dependencies'
      // constructs
      // this needs to be done after the bug has been created as we need the construct changes to
      // exist in the database to avoid querying by fields (lang, type, qname)
      if (managed_bug.getConstructChanges() != null && !managed_bug.getConstructChanges().isEmpty())
        appRepository.refreshVulnChangebyChangeList(managed_bug.getConstructChanges());

      sw.stop();
    } catch (Exception e) {
      sw.stop(e);
      throw new PersistenceException("Error while saving bug [" + ext_id + "]: " + e.getMessage());
    }

    return managed_bug;
  }

  /**
   *
   * @param _provided_bug
   */
  private Bug updateConstructChanges(@NotNull Bug _managed_bug, @NotNull Bug _provided_bug) {
    for (ConstructChange cc_provided : _provided_bug.getConstructChanges()) {
      for (ConstructChange cc_managed : _managed_bug.getConstructChanges()) {
        if (cc_provided.equals(cc_managed)) {
          cc_provided.setId(cc_managed.getId());
          break;
        }
      }
    }
    return _provided_bug;
  }

  /**
   * Checks whether any of the referenced {@link ConstructId}s already exist in the database.
   * If yes, they are read and replaced from the database so that the internal ID is set.
   * Otherwise, if the internal ID of referenced {@link ConstructId}s is null, it would always
   * result in the saving of a new {@link ConstructId}, which in turn results in the violation of
   * unique constraints defined in {@link ConstructId}.
   * @param _bug
   */
  private Bug saveNestedConstructIds(@NotNull Bug _bug) {
    ConstructId provided_cid = null, managed_cid = null;
    for (ConstructChange cc : _bug.getConstructChanges()) {
      provided_cid = cc.getConstructId();
      try {
        managed_cid =
            ConstructIdRepository.FILTER.findOne(
                this.cidRepository.findConstructId(
                    provided_cid.getLang(), provided_cid.getType(), provided_cid.getQname()));
      } catch (EntityNotFoundException e) {
        managed_cid = this.cidRepository.save(provided_cid);
      }
      cc.setConstructId(managed_cid);
    }
    return _bug;
  }

  /**
   * Returns true if a CVE identifier can be extracted from {@link Bug#getBugId()} or {@link Bug#getBugIdAlt()} using {@link Cve#extractCveIdentifier(String)),
   * and the bug description, CVSS score or CVSS version is null.
   *
   * @param _bug
   * @return
   */
  private boolean needsCveData(Bug _bug) {
    return (Cve.extractCveIdentifier(_bug.getBugId()) != null
            || Cve.extractCveIdentifier(_bug.getBugIdAlt()) != null)
        && (_bug.getDescription() == null
            || _bug.getCvssScore() == null
            || _bug.getCvssVersion() == null);
  }

  /**
   * Checks whether the database has a description, CVSS score and version for the given {@link Bug}.
   * If not, or in case the parameter _force is true, the CVE data for the given {@link Bug} is read using the {@link CveReader2},
   * and the database is updated if the CVE's summary, CVSS score, version or vector is empty or outdated.
   *
   * @param _b the {@link Bug} whose CVE data is read
   * @param _force
   */
  @Override
  public boolean updateCachedCveData(Bug _b, boolean _force) {
    boolean update_happened = false;
    if (_force || this.needsCveData(_b)) {
      try {
        // Get the CVE ID to read from cache
        String cve_id = Cve.extractCveIdentifier(_b.getBugId());
        if (cve_id == null) cve_id = Cve.extractCveIdentifier(_b.getBugIdAlt());

        // Read cache (note that cve_id can be null)
        final Cve cve = CveReader2.read(cve_id);

        if (cve != null) {
          boolean to_save = false;
          if (cve.getSummary() != null
              && (_b.getDescription() == null || !(cve.getSummary().equals(_b.getDescription())))) {
            _b.setDescription(cve.getSummary());
            to_save = true;
          }
          if (cve.getCvssScore() != null
              && (_b.getCvssScore() == null || !(cve.getCvssScore().equals(_b.getCvssScore())))) {
            _b.setCvssScore(cve.getCvssScore());
            to_save = true;
          }
          if (cve.getCvssVersion() != null
              && (_b.getCvssVersion() == null
                  || !(cve.getCvssVersion().equals(_b.getCvssVersion())))) {
            _b.setCvssVersion(cve.getCvssVersion());
            to_save = true;
          }
          if (cve.getCvssVector() != null
              && (_b.getCvssVector() == null
                  || !(cve.getCvssVector().equals(_b.getCvssVector())))) {
            _b.setCvssVector(cve.getCvssVector());
            to_save = true;
          }

          // Something changed, update database
          if (to_save) {
            log.debug(
                "CVE data of bug ["
                    + _b.getBugId()
                    + "] changed, triggering update of local database");
            this.customSave(_b, false);
            update_happened = true;
          }
          // Nothing changed
          else {
            log.debug(
                "CVE data of bug ["
                    + _b.getBugId()
                    + "] did not change, no update of local database needed");
          }
        }
      } catch (CacheException e) {
        log.error(
            "Cache exception when refreshing CVE data of bug ["
                + _b.getBugId()
                + "]: "
                + e.getMessage());
      } catch (PersistenceException e) {
        log.error(
            "Cannot save bug [" + _b.getBugId() + "] with refreshed CVE data: " + e.getMessage());
      }
    } else {
      log.debug("Bug [" + _b.getBugId() + "] does not need a refresh of cached CVE data");
    }
    return update_happened;
  }
}
