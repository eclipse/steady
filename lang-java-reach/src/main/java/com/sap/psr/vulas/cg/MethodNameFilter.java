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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.cg;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Checks whether a given {@link ConstructId} is part of the application, a library and
 * whether it is blacklisted. The method is used when searching for touch points.
 */
public class MethodNameFilter {

  private static Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static MethodNameFilter instance = null;

  private StringList classnameBlacklist = null;

  /**
   * Singleton.
   */
  private MethodNameFilter() {
    final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();
    this.classnameBlacklist = new StringList();
    this.classnameBlacklist.addAll(
        cfg.getStringArray(ReachabilityConfiguration.REACH_BL_CLASS_JRE));
    this.classnameBlacklist.addAll(
        cfg.getStringArray(ReachabilityConfiguration.REACH_BL_CLASS_CUST));
  }

  /**
   * If there is a MethodNameFilter instance available it will return it,
   * otherwise instantiate it and then return
   *
   * @return an instantiation of the class MethodNameFilter
   */
  public static synchronized MethodNameFilter getInstance() {
    if (MethodNameFilter.instance == null) MethodNameFilter.instance = new MethodNameFilter();
    return MethodNameFilter.instance;
  }

  /**
   * Check if the argument is a string contained in the list of the blacklisted
   * methods
   *
   * @param value a string representing the complete name of a method
   * @return a boolean value stating if value is blacklisted or not
   */
  public boolean isBlackListed(String value) {
    return this.classnameBlacklist.contains(
        value, StringList.ComparisonMode.STARTSWITH, StringList.CaseSensitivity.CASE_SENSITIVE);
  }

  /**
   * Check if the argument is a string representing a method of an external library
   * (meaning not of my application). To define the domain of the application we use
   * the entries on which we built the graph.
   *
   * @param app_entries Entry methods that we used to define the app domain. Use {@link BackendConnector#getAppConstructIds(com.sap.psr.vulas.goals.GoalContext, com.sap.psr.vulas.shared.json.model.Application)} to get them.
   * @param value a string representing the complete name of a method
   * @return a boolean value stating if value is part of a library or not
   */
  public boolean isLibraryMethod(
      Set<com.sap.psr.vulas.shared.json.model.ConstructId> app_entries, String value) {
    return !this.isAnAppMethod(app_entries, value);
  }

  /**
   * Check if the argument is a string representing a method of my application
   * (meaning not of an external library). To define the domain of the application we use
   * the entries on which we built the graph.
   *
   * @param app_entries Entry methods that we used to define the app domain. Use {@link BackendConnector#getAppConstructIds(com.sap.psr.vulas.goals.GoalContext, com.sap.psr.vulas.shared.json.model.Application)} to get them.
   * @param value  a string representing the complete name of a method
   * @return a boolean value stating if value is part of the application or not
   */
  public boolean isAnAppMethod(
      Set<com.sap.psr.vulas.shared.json.model.ConstructId> app_entries, String value) {
    // this method take as input the constructs of the app but if the inner class
    // constructor is modified (e.g.  a$1(a)==>a$1() ) we have problems so we
    // can just add a workaround here.
    // NB: even is the name of the class contains a $ and is not an innerclass
    // the method result is right anyway
    if (value.contains("$"))
      value = value.substring(0, value.indexOf("$")); // this solve problems with inner classes
    for (com.sap.psr.vulas.shared.json.model.ConstructId entry : app_entries)
      if (entry.getQname().startsWith(value)) return true;
    return false;
  }
}
