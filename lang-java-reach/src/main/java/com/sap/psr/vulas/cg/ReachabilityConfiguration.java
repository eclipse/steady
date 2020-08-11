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
package com.sap.psr.vulas.cg;

/**
 * <p>ReachabilityConfiguration class.</p>
 *
 */
public class ReachabilityConfiguration {

    /** Constant <code>REACH_BUGS="vulas.reach.bugs"</code> */
    public final static String REACH_BUGS = "vulas.reach.bugs";
    /** Constant <code>REACH_FWK="vulas.reach.fwk"</code> */
    public final static String REACH_FWK = "vulas.reach.fwk";
    /** Constant <code>REACH_EXIT_UNKOWN_EP="vulas.reach.exitOnUnknownEntryPoints"</code> */
    public final static String REACH_EXIT_UNKOWN_EP = "vulas.reach.exitOnUnknownEntryPoints";
    /** Constant <code>REACH_CONSTR_FILTER="vulas.reach.constructFilter"</code> */
    public final static String REACH_CONSTR_FILTER = "vulas.reach.constructFilter";
    /** Constant <code>REACH_EXCL_JARS="vulas.reach.excludeJars"</code> */
    public final static String REACH_EXCL_JARS = "vulas.reach.excludeJars";
    /** Constant <code>REACH_EXCL_PACK="vulas.reach.excludePackages"</code> */
    public final static String REACH_EXCL_PACK = "vulas.reach.excludePackages";
    /** Constant <code>REACH_PREPROCESS="vulas.reach.preprocessDependencies"</code> */
    public final static String REACH_PREPROCESS = "vulas.reach.preprocessDependencies";
    /** Constant <code>REACH_TIMEOUT="vulas.reach.timeout"</code> */
    public final static String REACH_TIMEOUT = "vulas.reach.timeout";
    /** Constant <code>REACH_MAX_PATH="vulas.reach.maxPathPerChangeListElement"</code> */
    public final static String REACH_MAX_PATH = "vulas.reach.maxPathPerChangeListElement";

    /** Constant <code>REACH_BL_CLASS_JRE="vulas.reach.blacklist.classes.jre"</code> */
    public final static String REACH_BL_CLASS_JRE = "vulas.reach.blacklist.classes.jre";
    /** Constant <code>REACH_BL_CLASS_CUST="vulas.reach.blacklist.classes.custom"</code> */
    public final static String REACH_BL_CLASS_CUST = "vulas.reach.blacklist.classes.custom";

    /** Constant <code>REACH_TOUCHPOINTS="vulas.reach.identifyTouchpoints"</code> */
    public final static String REACH_TOUCHPOINTS = "vulas.reach.identifyTouchpoints";

    /** Constant <code>REACH_SEARCH_SHORTEST="vulas.reach.searchShortest"</code> */
    public final static String REACH_SEARCH_SHORTEST = "vulas.reach.searchShortest";

    /** Constant <code>REACH_WRITE_TO="vulas.reach.callgraph.writeTo"</code> */
    public final static String REACH_WRITE_TO = "vulas.reach.callgraph.writeTo";
    /** Constant <code>REACH_READ_FROM="vulas.reach.callgraph.readFrom"</code> */
    public final static String REACH_READ_FROM = "vulas.reach.callgraph.readFrom";

    /** Constant <code>CLI_PLUGIN_DIR="vulas.reach.cli.plugins.dir"</code> */
    public final static String CLI_PLUGIN_DIR = "vulas.reach.cli.plugins.dir";
}
