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
package com.sap.psr.vulas.monitor.slice;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.AbstractInstrumentor;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.FileUtil;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * Adds a configurable guard to all methods that have not been traced or found reachable.
 */
public class SliceInstrumentor extends AbstractInstrumentor {

  // ====================================== STATIC MEMBERS

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  // ====================================== INSTANCE MEMBERS

  /**
   * Blacklist of constructs not to be instrumented.
   */
  private Set<ConstructId> blacklistedConstructsNotToInstrument = new HashSet<ConstructId>();

  /**
   * Whitelist of constructs to be instrumented.
   */
  private Set<ConstructId> whitelistedConstructsToInstrument = new HashSet<ConstructId>();
  ;

  /**
   * <p>Constructor for SliceInstrumentor.</p>
   */
  public SliceInstrumentor() {
    try {
      this.determineConstructs();
    } catch (Exception e) {
      SliceInstrumentor.log.error(
          "[" + e.getClass().getSimpleName() + "] during instantiation: " + e.getMessage());
      throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
    }
  }

  /**
   * Builds the whitelist (blacklist) of constructs that will (not) be instrumented. The respective {@link ConstructId}s are
   * either loaded from disk or read from the backend, depending on the presence of the configuration settings {@link CoreConfiguration#INSTR_SLICE_WHITELIST} and  {@link CoreConfiguration#INSTR_SLICE_BLACKLIST}.
   *
   * @throws ConfigurationException
   * @throws IllegalStateException
   * @throws BackendConnectionException
   */
  private void determineConstructs()
      throws ConfigurationException, IllegalStateException, BackendConnectionException,
          IOException {
    if (!this.vulasConfiguration.isEmpty(CoreConfiguration.INSTR_SLICE_WHITELIST)
        || !this.vulasConfiguration.isEmpty(CoreConfiguration.INSTR_SLICE_BLACKLIST)) {
      // Whitelist
      if (!this.vulasConfiguration.isEmpty(CoreConfiguration.INSTR_SLICE_WHITELIST)) {
        final com.sap.psr.vulas.shared.json.model.ConstructId[] wl =
            (com.sap.psr.vulas.shared.json.model.ConstructId[])
                JacksonUtil.asObject(
                    FileUtil.readFile(
                        Paths.get(
                            this.vulasConfiguration
                                .getConfiguration()
                                .getString(CoreConfiguration.INSTR_SLICE_WHITELIST))),
                    com.sap.psr.vulas.shared.json.model.ConstructId[].class);
        whitelistedConstructsToInstrument.addAll(JavaId.toCoreType(Arrays.asList(wl)));
      }
      // Blacklist
      if (!this.vulasConfiguration.isEmpty(CoreConfiguration.INSTR_SLICE_BLACKLIST)) {
        final com.sap.psr.vulas.shared.json.model.ConstructId[] bl =
            (com.sap.psr.vulas.shared.json.model.ConstructId[])
                JacksonUtil.asObject(
                    FileUtil.readFile(
                        Paths.get(
                            this.vulasConfiguration
                                .getConfiguration()
                                .getString(CoreConfiguration.INSTR_SLICE_BLACKLIST))),
                    com.sap.psr.vulas.shared.json.model.ConstructId[].class);
        blacklistedConstructsNotToInstrument.addAll(JavaId.toCoreType(Arrays.asList(bl)));
      }
    } else {
      this.blacklistedConstructsNotToInstrument.addAll(
          JavaId.toCoreType(
              BackendConnector.getInstance()
                  .getAppTraces(
                      CoreConfiguration.buildGoalContextFromConfiguration(this.vulasConfiguration),
                      CoreConfiguration.getAppContext(this.vulasConfiguration))));
      final Set<Dependency> reached_dependencies =
          BackendConnector.getInstance()
              .getAppDependencies(
                  CoreConfiguration.buildGoalContextFromConfiguration(this.vulasConfiguration),
                  CoreConfiguration.getAppContext(this.vulasConfiguration));
      for (Dependency d : reached_dependencies) {
        blacklistedConstructsNotToInstrument.addAll(
            JavaId.toCoreType(d.getReachableConstructIds()));
      }
    }
    log.info(
        "Construct whitelist comprises ["
            + this.whitelistedConstructsToInstrument.size()
            + "] items, construct blacklist comprises ["
            + this.blacklistedConstructsNotToInstrument.size()
            + "] items");
  }

  /**
   * {@inheritDoc}
   *
   * Returns true if the following two conditions hold, false otherwise:
   * (1) The given {@link JavaId} is whitelisted or there is no whitelist
   * (2) The given {@link JavaId} is NOT blacklisted or there is no blacklist.
   *
   * If there is neither a blacklist nor a whitelist, the method always returns false.
   */
  @Override
  public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) {
    final boolean whitelisted =
        this.whitelistedConstructsToInstrument.isEmpty()
            || this.whitelistedConstructsToInstrument.contains(_jid)
            || this.whitelistedConstructsToInstrument.contains(_jid.getCompilationUnit())
            || this.whitelistedConstructsToInstrument.contains(_jid.getJavaPackageId());
    final boolean blacklisted =
        !this.blacklistedConstructsNotToInstrument.isEmpty()
            && (this.blacklistedConstructsNotToInstrument.contains(_jid)
                || this.blacklistedConstructsNotToInstrument.contains(_jid.getCompilationUnit())
                || this.blacklistedConstructsNotToInstrument.contains(_jid.getJavaPackageId()));
    final boolean r = whitelisted && !blacklisted;
    log.info(
        _jid
            + " is whitelisted ["
            + whitelisted
            + "] and blacklisted ["
            + blacklisted
            + "]: Accepted for instrumentation ["
            + r
            + "]");
    return r;
  }

  /** {@inheritDoc} */
  public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv)
      throws CannotCompileException {
    _code
        .append("final boolean is_open = Boolean.parseBoolean(System.getProperty(\"")
        .append(CoreConfiguration.INSTR_SLICE_GUARD_OPEN)
        .append("\"));");
    _code.append(
        "System.err.println(\"Execution of "
            + _jid.toString()
            + "\" + (is_open ? \"allowed\" : \"prevented\") + \" by Vulas guarding condition\");");
    _code.append(
        "if(!is_open) throw new IllegalStateException(\"Execution of "
            + _jid.toString()
            + " prevented by Vulas guarding condition\");");
  }

  /**
   * {@inheritDoc}
   *
   * Implementation does not do anything.
   */
  @Override
  public void upladInformation(AbstractGoal _exe, int _batch_size) {
    ;
  }

  /**
   * {@inheritDoc}
   *
   * Implementation does not do anything.
   */
  @Override
  public void awaitUpload() {
    ;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Long> getStatistics() {
    final Map<String, Long> stats = new HashMap<String, Long>();
    // TODO: Add number of instrumented methods
    return stats;
  }
}
