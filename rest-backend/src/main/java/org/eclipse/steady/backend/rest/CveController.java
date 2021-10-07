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
package org.eclipse.steady.backend.rest;

import org.eclipse.steady.backend.cve.Cve;
import org.eclipse.steady.backend.cve.CveReader2;
import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.repo.BugRepository;
import org.eclipse.steady.shared.util.StopWatch;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * CveController class.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/cves")
public class CveController {

  private static Logger log = LoggerFactory.getLogger(CveController.class);

  private static String CACHE_REFRESH_ALL = "vulas.backend.cveCache.refetchAllMs";
  private static String CACHE_REFRESH_SNG = "vulas.backend.cveCache.refetchSingleMs";

  private Thread cveCacheFetch = null;

  private final BugRepository bugRepository;

  /**
   * Creates a thread pre-fetching the CVEs for all bugs. This thread shall be started by using the
   * REST endpoint {@link #startRefresh()}. Note: If it would be started right away, multiple
   * backend instances would update the database in parallel.
   */
  @Autowired
  CveController(BugRepository _bug_repo) {
    this.bugRepository = _bug_repo;
    final BugRepository bug_repo = _bug_repo;

    // Refresh CVE cache
    final long refresh_all =
        VulasConfiguration.getGlobal().getConfiguration().getLong(CACHE_REFRESH_ALL, -1);
    final long refresh_sng =
        VulasConfiguration.getGlobal().getConfiguration().getLong(CACHE_REFRESH_SNG, 60000);

    if (refresh_all == -1) {
      log.warn("Periodic update of cached CVE data: Disabled");
    } else {
      log.info(
          "Periodic update of cached CVE data: Every " + StringUtil.msToMinString(refresh_all));
      this.cveCacheFetch =
          new Thread(
              new Runnable() {
                public void run() {
                  boolean force = false;

                  while (true) {

                    // Loop all bugs
                    final StopWatch sw =
                        new StopWatch("Refresh of cached CVE data")
                            .setTotal(bug_repo.count())
                            .start();
                    for (Bug b : bug_repo.findAll()) {
                      try {
                        final boolean update_happened = bug_repo.updateCachedCveData(b, force);
                        if (update_happened)
                          Thread.sleep((long) (refresh_sng + Math.random() * refresh_sng));
                      } catch (InterruptedException e) {
                        CveController.log.error(
                            "Interrupted exception while refreshing cached CVE data of bug ["
                                + b.getBugId()
                                + "]: "
                                + e.getMessage());
                      }
                      sw.progress();
                    }
                    sw.stop();

                    // Wait before entering the loop another time
                    try {
                      Thread.sleep((long) (refresh_all + Math.random() * refresh_all));
                    } catch (InterruptedException e) {
                      CveController.log.error(
                          "Interrupted exception while refreshing cached CVE data: "
                              + e.getMessage());
                    }

                    // Force refresh
                    force = true;
                  }
                }
              },
              "CveCacheFetch");
      this.cveCacheFetch.setPriority(Thread.MIN_PRIORITY);
    }
  }

  /**
   * Returns the {@link Cve} with the given ID, e.g., CVE-2014-0050.
   *
   * @return 404 {@link HttpStatus#NOT_FOUND} if the CVE with given ID does not exist, 200 {@link
   *     HttpStatus#OK} if the CVE is found
   * @param id a {@link java.lang.String} object.
   */
  @RequestMapping(
      value = "/{id}",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<Cve> getCve(@PathVariable String id) {
    try {
      final Cve cve = CveReader2.read(id);
      if (cve != null) {
        return new ResponseEntity<Cve>(cve, HttpStatus.OK);
      } else {
        return new ResponseEntity<Cve>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception enfe) {
      return new ResponseEntity<Cve>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Refreshes the cached data of the {@link Bug} with the given ID, e.g., CVE-2014-0050.
   *
   * @return true if the bug data got updated, false otherwise
   * @param id a {@link java.lang.String} object.
   */
  @RequestMapping(value = "/refreshCache/{id}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> updateCve(@PathVariable String id) {
    try {
      final Bug b = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(id));
      final boolean update_happened = this.bugRepository.updateCachedCveData(b, true);
      return new ResponseEntity<Boolean>(update_happened, HttpStatus.OK);
    } catch (Exception enfe) {
      return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Starts a thread that reads CVE information and replicates it in the local database. The thread
   * behavior is configured using {@link #CACHE_REFRESH_ALL} and {@link #CACHE_REFRESH_SNG}, both
   * described in the file steady-rest-backend.properties.
   *
   * @return true if the thread got started, false otherwise
   */
  @RequestMapping(value = "/refreshCache", method = RequestMethod.POST)
  public ResponseEntity<Boolean> startRefresh() {
    boolean started = false;
    try {
      if (this.cveCacheFetch == null) {
        log.info("CVE cache refresh disabled");
      } else if (this.cveCacheFetch.isAlive()) {
        log.info("CVE cache refresh already running");
      } else {
        this.cveCacheFetch.start();
        started = true;
        log.info("CVE cache refresh started");
      }
      return new ResponseEntity<Boolean>(started, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Exception when starting CVE cache refresh: " + e.getMessage(), e);
      return new ResponseEntity<Boolean>(started, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
