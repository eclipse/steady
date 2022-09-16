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
package org.eclipse.steady.kb.rest;

import java.util.HashMap;

import org.eclipse.steady.kb.ImportCommand;
import org.eclipse.steady.kb.Manager;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for kb-importer
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/import")
public class ImportController {

  private static Logger log = LoggerFactory.getLogger(ImportController.class);

  /**
   * The import thread that uses the {@link Manager} to pull and import statements.
   */
  private Thread importerThread = null;

  /**
   * Immport threads can be started and stopped using the respective endpoints.
   */
  private int threadsCreated = 0;

  /**
   * Used by the importer thread.
   */
  private final Manager manager = new Manager();

  /**
   * Default wait time between subsequent imports.
   */
  static final long waitTimeMs =
      VulasConfiguration.getGlobal()
          .getConfiguration()
          .getLong("vulas.kb-importer.refetchAllMs", 86400000);

  /**
   * Creates a new importer thread.
   * 
   * @param _args
   * @param _wait_time_ms
   * @return
   */
  private final Thread createImporterThread(HashMap<String, Object> _args, Long _wait_time_ms) {
    this.threadsCreated++;
    Thread t = new Thread(
      new Runnable() {
        public void run() {
          log.debug("Importer thread started");
          try {
            // Until InterruptException: Import and sleep
            while (true) {
              manager.start(_args);
              log.info(
                  "Waiting "
                      + StringUtil.formatMinString(_wait_time_ms)
                      + " until next execution...");
              Thread.sleep(_wait_time_ms);
            }
          }
          // Happens if the controller's start endpoint is called
          catch (InterruptedException e) {
            log.info("Thread [" + Thread.currentThread().getName() + "] interrupted");
          }
        }
      },
      "kb-importer-" + this.threadsCreated);
    t.setPriority(Thread.MIN_PRIORITY);
    return t;
  }

  /**
   * Creates the controller and starts the importer thread with default
   * configuration settings.
   */
  @Autowired
  ImportController() {
    // Create the thread with default config
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.OVERWRITE_OPTION, false);
    args.put(ImportCommand.UPLOAD_CONSTRUCT_OPTION, false);
    args.put(ImportCommand.VERBOSE_OPTION, false);
    args.put(ImportCommand.SKIP_CLONE_OPTION, true);
    this.importerThread = this.createImporterThread(args, waitTimeMs);
    
    // Start the thread
    try {
      this.importerThread.start();      
    } catch (Exception e) {
      log.error("Exception when starting importer thread: " + e.getMessage(), e);
    }
  }

  /**
   * Recreates the importer thread with the given configuration, unless an
   * import is currently on-going.
   *
   * @param overwrite a boolean
   * @param upload a boolean
   * @param verbose a boolean
   * @param skipClone a boolean
   * @param waitTimeMs a long indicating the wait time between subsequent imports
   * @return a {@link org.springframework.http.ResponseEntity} object
   */
  @RequestMapping(value = "/start", method = RequestMethod.POST)
  public ResponseEntity<Boolean> start(
      @RequestParam(defaultValue = "false") boolean overwrite,
      @RequestParam(defaultValue = "false") boolean upload,
      @RequestParam(defaultValue = "false") boolean verbose,
      @RequestParam(defaultValue = "true") boolean skipClone,
      @RequestParam(defaultValue = "86400000") long waitTimeMs) {
    
    // On-going import: don't do anything
    if (this.manager.isRunningStart()) {
      log.info("On-going import, no action is taken");
      return new ResponseEntity<Boolean>(false, HttpStatus.OK);
    }

    // No on-going import: interrupt and re-create thread (with potentially different configuration)
    try {
      log.info("No on-going import, re-create importer thread with given configuration");
      if (this.importerThread != null && this.importerThread.isAlive()) {
        this.importerThread.interrupt();
      }

      // Create the thread with given config
      HashMap<String, Object> args = new HashMap<String, Object>();
      args.put(ImportCommand.OVERWRITE_OPTION, overwrite);
      args.put(ImportCommand.UPLOAD_CONSTRUCT_OPTION, upload);
      args.put(ImportCommand.VERBOSE_OPTION, verbose);
      args.put(ImportCommand.SKIP_CLONE_OPTION, skipClone);
      long time_to_wait_ms = waitTimeMs != 0 ? waitTimeMs : waitTimeMs;
      this.importerThread = this.createImporterThread(args, time_to_wait_ms);

      // Start the thread
      this.importerThread.start();
      return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Exception when starting importer thread: " + e.getMessage(), e);
      return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Stops the import and interrupts the importer thread.
   *
   * @return a {@link org.springframework.http.ResponseEntity} object
   */
  @RequestMapping(value = "/stop", method = RequestMethod.POST)
  public ResponseEntity<Boolean> stop() {
    boolean stopped = false;
    try {
      if (this.manager.isRunningStart()
          || (this.importerThread != null && this.importerThread.isAlive())) {
        stopped = true;
        this.manager.stop();
        this.importerThread.interrupt();
        log.info("Importer thread stopped");
      } else {
        log.info("Importer thread not running");
      }
      return new ResponseEntity<Boolean>(stopped, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Exception when stopping importer thread: " + e.getMessage(), e);
      return new ResponseEntity<Boolean>(stopped, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Imports a single vulnerability whose statement.yaml is expected to be in
   * the correct folder.
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
        log.info("Importer thread already running");
        return new ResponseEntity<Boolean>(false, HttpStatus.SERVICE_UNAVAILABLE);
      } else {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put(ImportCommand.OVERWRITE_OPTION, overwrite);
        args.put(ImportCommand.UPLOAD_CONSTRUCT_OPTION, upload);
        args.put(ImportCommand.VERBOSE_OPTION, verbose);
        args.put(ImportCommand.SKIP_CLONE_OPTION, skipClone);
        manager.importSingleVuln(args, id);
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
