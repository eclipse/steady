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
package org.eclipse.steady.kb;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * REST Controller for kb-importer
 */
@RestController
@CrossOrigin("*")
public class ImporterController {

  private static Logger log = LoggerFactory.getLogger(ImporterController.class);

  private Thread importerCacheFetch = null;

  static final long defaultRefetchAllMs =
      VulasConfiguration.getGlobal()
          .getConfiguration()
          .getLong("vulas.kb-importer.refetchAllMs", -1);

  static final String statementsPath =
      VulasConfiguration.getGlobal()
          .getConfiguration()
          .getString("vulas.kb-importer.statementsPath");

  static final String statementsKaybeePath = ".kaybee/repositories/github.com_sap.project-kb_vulnerability-data/statements";

  private final Manager manager;

  @Autowired
  ImporterController() {
    this.manager = new Manager(BackendConnector.getInstance());
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.OVERWRITE_OPTION, false);
    args.put(ImportCommand.UPLOAD_CONSTRUCT_OPTION, false);
    args.put(ImportCommand.VERBOSE_OPTION, false);
    args.put(ImportCommand.SKIP_CLONE_OPTION, true);
    long timeToWait = defaultRefetchAllMs;

    this.importerCacheFetch =
        new Thread(
            new Runnable() {
              public void run() {
                while (true) {
                  manager.start(statementsPath, args);

                  try {
                    log.info(
                        "Wait "
                            + Long.toString(timeToWait / 1000)
                            + " seconds for next execution");
                    Thread.sleep(timeToWait);
                  } catch (InterruptedException e) {
                    ImporterController.log.error("Interrupted exception: " + e.getMessage());
                  }
                }
              }
            },
            "ImporterCacheFetch");
    this.importerCacheFetch.setPriority(Thread.MIN_PRIORITY);
    try{
      this.importerCacheFetch.start();
      log.info("Importer started");
    } catch (Exception e) {
      log.error("Exception when starting CVE cache refresh: " + e.getMessage(), e);
    }
  }

  /**
   * <p>start.</p>
   *
   * @param overwrite a boolean
   * @param upload a boolean
   * @param verbose a boolean
   * @param skipClone a boolean
   * @param refetchAllMs a {@link java.lang.String} object
   * @return a {@link org.springframework.http.ResponseEntity} object
   */
  @RequestMapping(value = "/start", method = RequestMethod.POST)
  public ResponseEntity<Boolean> start(
      @RequestParam(defaultValue = "false") boolean overwrite,
      @RequestParam(defaultValue = "false") boolean upload,
      @RequestParam(defaultValue = "false") boolean verbose,
      @RequestParam(defaultValue = "true") boolean skipClone,
      @RequestParam(defaultValue = "0") String refetchAllMs) {
    boolean started = false;
    try {
      if (this.manager.isRunningStart()) {
        log.info("Importer already running");
      } else {
        if (this.importerCacheFetch != null && this.importerCacheFetch.isAlive()) {
          this.importerCacheFetch.interrupt();
        }
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put(ImportCommand.OVERWRITE_OPTION, overwrite);
        args.put(ImportCommand.UPLOAD_CONSTRUCT_OPTION, upload);
        args.put(ImportCommand.VERBOSE_OPTION, verbose);
        args.put(ImportCommand.SKIP_CLONE_OPTION, skipClone);
        long timeToWait;
        if (Long.parseLong(refetchAllMs) != 0) {
          timeToWait = Long.parseLong(refetchAllMs);
        } else {
          timeToWait = defaultRefetchAllMs;
        }

        this.importerCacheFetch =
            new Thread(
                new Runnable() {
                  public void run() {
                    while (true) {
                      manager.start(statementsPath, args);

                      try {
                        log.info(
                            "Wait "
                                + Long.toString(timeToWait / 1000)
                                + " seconds for next execution");
                        Thread.sleep(timeToWait);
                      } catch (InterruptedException e) {
                        ImporterController.log.error("Interrupted exception: " + e.getMessage());
                      }
                    }
                  }
                },
                "ImporterCacheFetch");
        this.importerCacheFetch.setPriority(Thread.MIN_PRIORITY);

        this.importerCacheFetch.start();
        started = true;
        log.info("Importer started");
      }
      return new ResponseEntity<Boolean>(started, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Exception when starting CVE cache refresh: " + e.getMessage(), e);
      return new ResponseEntity<Boolean>(started, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>stop.</p>
   *
   * @return a {@link org.springframework.http.ResponseEntity} object
   */
  @RequestMapping(value = "/stop", method = RequestMethod.POST)
  public ResponseEntity<Boolean> stop() {
    boolean stopped = false;
    try {
      if (this.manager.isRunningStart()
          || (this.importerCacheFetch != null && this.importerCacheFetch.isAlive())) {
        stopped = true;
        this.manager.stop();
        this.importerCacheFetch.interrupt();
        log.info("Importer stopped");
      } else {
        log.info("Importer not running");
      }
      return new ResponseEntity<Boolean>(stopped, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Exception when starting kb-importer cache refresh: " + e.getMessage(), e);
      return new ResponseEntity<Boolean>(stopped, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>importSingleVuln.</p>
   *
   * @param id a {@link java.lang.String} object
   * @param overwrite a boolean
   * @param upload a boolean
   * @param verbose a boolean
   * @param skipClone a boolean
   * @return a {@link org.springframework.http.ResponseEntity} object
   */
  @RequestMapping(value = "/start/{id}", method = RequestMethod.POST)
  public ResponseEntity<Boolean> importSingleVuln(
      @PathVariable String id,
      @RequestParam(defaultValue = "false") boolean overwrite,
      @RequestParam(defaultValue = "false") boolean upload,
      @RequestParam(defaultValue = "false") boolean verbose,
      @RequestParam(defaultValue = "true") boolean skipClone) {

    try {
      if (this.manager.isRunningStart()) {
        log.info("Importer already running");
        return new ResponseEntity<Boolean>(false, HttpStatus.SERVICE_UNAVAILABLE);
      } else {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put(ImportCommand.OVERWRITE_OPTION, overwrite);
        args.put(ImportCommand.UPLOAD_CONSTRUCT_OPTION, upload);
        args.put(ImportCommand.VERBOSE_OPTION, verbose);
        args.put(ImportCommand.SKIP_CLONE_OPTION, skipClone);
        manager.importSingleVuln(statementsPath + File.separator + id, args, id);
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error("Exception when importing vulnerability: " + e.getMessage(), e);
      return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>status.</p>
   *
   * @return a {@link java.lang.String} object
   */
  @GetMapping("/status")
  public String status() {
    return manager.status();
  }

  /**
   * <p>statusSingleVuln.</p>
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   */
  @GetMapping(value = "/status/{id}")
  public String statusSingleVuln(@PathVariable String id) {
    String statusStr = manager.getVulnStatus(id).toString();
    if (statusStr == null) {
      return "Vulnerability not found";
    } else return statusStr;
  }
}
