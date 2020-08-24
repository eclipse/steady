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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sap.psr.vulas.cg.spi.CallgraphConstructorFactory;
import com.sap.psr.vulas.cg.spi.ICallgraphConstructor;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.wala.util.graph.Graph;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.monitor.ClassPoolUpdater;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.ThreadUtil;

/**
 * Reachability Analyzer
 */
public class ReachabilityAnalyzer implements Runnable {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static int THREAD_COUNT = 0;

  /**
   * The application JAR to be analyzed and its context information
   */
  private Application app_ctx = null;

  /**
   * The goal context (ideally, this should be kept out of this class)
   */
  private GoalContext goalContext = null;

  private Set<Path> app_classpaths = null;
  private Set<Path> dep_classpaths = null;

  /**
   * All the constructs used as entry points and its source description (e.g., 'app' or 'traces').
   */
  private Set<com.sap.psr.vulas.shared.json.model.ConstructId> entrypoints = null;

  private PathSource source = null;
  /**
   * Whether apply a strict policy to callgraph construction; that is whether allowing any entrypoint missing in the callgraph
   */
  private boolean strictPolicy = false;

  /**
   * The call graph constructed for reachability analysis
   */
  private Callgraph callgraph = null;

  /**
   * <p>Getter for the field <code>callgraph</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.cg.Callgraph} object.
   */
  public Callgraph getCallgraph() {
    return this.callgraph;
  }
  ;

  // private String bugid = null;

  private Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> targetConstructs = null;

  /**
   * All the packages to be excluded during callgraph construction
   */
  private String excludedPackages = null;

  private Map<String, Long> stats = new HashMap<String, Long>();

  // contains the result of the attacksurface analysis performed during the a2c goal
  // private  List<NodeMetaInformation> libraryEntryPoints = null;

  /**
   * Application constructs (identical to entry points in case of {@link GoalType#A2C}).
   */
  private Set<com.sap.psr.vulas.shared.json.model.ConstructId> appConstructs = null;

  /**
   * Library touch points (stored as a set of linked lists with two elements each) per library (SHA1).
   */
  private Map<String, Set<List<NodeMetaInformation>>> touchPoints =
      new HashMap<String, Set<List<NodeMetaInformation>>>();

  /**
   * Reachable constructs per library (SHA1).
   */
  private Map<String, Set<NodeMetaInformation>> reachableConstructs =
      new HashMap<String, Set<NodeMetaInformation>>();

  /**
   * The constructor used to build the call graph (implementations exist for Wala and Soot).
   */
  private ICallgraphConstructor constructor = null;

  /**
   * All paths for each bugid
   */
  private HashMap<String, List<List<com.sap.psr.vulas.shared.json.model.ConstructId>>> rcPaths =
      new HashMap<String, List<List<com.sap.psr.vulas.shared.json.model.ConstructId>>>();

  private static final Runtime runtime = Runtime.getRuntime();

  /**
   * <p>Constructor for ReachabilityAnalyzer.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   */
  public ReachabilityAnalyzer(GoalContext _ctx) {
    this.goalContext = _ctx;
    this.app_ctx = _ctx.getApplication();
  }

  /**
   * Sets the directories where to find compiled application classes. Each path is typically a directory, e.g., 'WEB-INF/classes' in
   * case of uncompressed WARs or 'target/classes' in case of Maven projects.
   *
   * @param _paths a {@link java.util.Set} object.
   */
  public void setAppClasspaths(Set<Path> _paths) {
    this.app_classpaths = _paths;
  }

  /**
   * <p>getAppClasspath.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAppClasspath() {
    StringBuilder b = new StringBuilder();
    int i = 0;
    if (this.app_classpaths != null) {
      for (Path p : this.app_classpaths) {
        if (i++ > 0) b.append(System.getProperty("path.separator"));
        b.append(p.toString());
      }
    }
    return b.toString();
  }

  /**
   * Sets the directories where to find the classes of application dependencies. Each path is typically a JAR file, e.g., 'WEB-INF/lib'
   * in case of uncompressed WARs or 'target/dependencies' in case of Maven projects.
   *
   * @param _paths a {@link java.util.Set} object.
   */
  public void setDependencyClasspaths(Set<Path> _paths) {
    this.dep_classpaths = _paths;
  }

  /**
   * <p>getDependencyClasspath.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDependencyClasspath() {
    StringBuilder b = new StringBuilder();
    int i = 0;
    if (this.dep_classpaths != null) {
      for (Path p : this.dep_classpaths) {
        if (i++ > 0) b.append(System.getProperty("path.separator"));
        b.append(p.toString());
      }
    }
    return b.toString();
  }

  /**
   * Sets the application constructs.
   *
   * @param _constructs a {@link java.util.Set} object.
   */
  public void setAppConstructs(Set<com.sap.psr.vulas.shared.json.model.ConstructId> _constructs) {
    this.appConstructs = _constructs;
  }

  /**
   * Sets the entry points that will be used as a starting point for the call graph construction.
   * The entry points used depend on the {@link GoalType}.
   *
   * @param _constructs a {@link java.util.Set} object.
   * @param _source a {@link com.sap.psr.vulas.shared.enums.PathSource} object.
   * @param _throw_exception a boolean.
   */
  public void setEntryPoints(
      Set<com.sap.psr.vulas.shared.json.model.ConstructId> _constructs,
      PathSource _source,
      boolean _throw_exception) {
    this.entrypoints = _constructs;
    this.source = _source;
    this.strictPolicy = _throw_exception;
  }

  /*public String getClasspath() {
  	final StringBuilder b = new StringBuilder();
  	int i = 0;
  	if(this.app_classpaths!=null) {
  		for(Path p: this.app_classpaths) {
  			if(i++>0) b.append(System.getProperty("path.separator"));
  			b.append(p.toString());
  		}
  	}
  	if(this.dep_classpaths!=null) {
  		for(Path p: this.dep_classpaths) {
  			if(i++>0) b.append(System.getProperty("path.separator"));
  			b.append(p.toString());
  		}
  	}
  	return b.toString();
  }*/

  /**
   * <p>setExcludePackages.</p>
   *
   * @param _packages a {@link java.lang.String} object.
   */
  public void setExcludePackages(String _packages) {
    if ((_packages != null) && (!_packages.isEmpty())) this.excludedPackages = _packages;
  }

  /**
   * Sets the change list elements of the given bug(s) as target constructs. If no bugs are passed, all bugs
   * relevant for the application will be considered (cf. {@link BackendConnector#getAppBugs(GoalContext, Application)}).
   *
   * @param _filter Comma-separated list of bug identifiers
   */
  public void setTargetConstructs(String _filter) {
    try {
      final Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> change_lists =
          BackendConnector.getInstance().getAppBugs(this.goalContext, this.app_ctx, _filter);
      if (change_lists == null || change_lists.size() == 0) {
        if (_filter == null || _filter.equals("")) {
          ReachabilityAnalyzer.log.info("No change list found, i.e., no target points can be set");
        } else {
          ReachabilityAnalyzer.log.info(
              "No change list found for bug(s) ["
                  + _filter
                  + "], i.e., no target points can be set");
        }
      } else {
        ReachabilityAnalyzer.log.info(
            "Found change lists for ["
                + change_lists.size()
                + "] bugs, their constructs will be used as target points");
        this.setTargetConstructs(change_lists);
      }
    } catch (BackendConnectionException e) {
      ReachabilityAnalyzer.log.error(
          "Error retrieving change lists from the backend: " + e.getMessage());
    }
  }

  /**
   * <p>Setter for the field <code>targetConstructs</code>.</p>
   *
   * @param _target_constructs a {@link java.util.Map} object.
   */
  public void setTargetConstructs(
      Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> _target_constructs) {
    this.targetConstructs = _target_constructs;
  }

  /**
   * <p>setCallgraphConstructor.</p>
   *
   * @param analysisFramework a {@link java.lang.String} object.
   * @param _is_cli a boolean.
   */
  public void setCallgraphConstructor(String analysisFramework, boolean _is_cli) {
    this.constructor =
        CallgraphConstructorFactory.buildCallgraphConstructor(
            analysisFramework, this.app_ctx, _is_cli);
    this.constructor.setVulasConfiguration(this.goalContext.getVulasConfiguration());
  }

  private Graph<ConstructId> readFromDisk(String _file) {
    try {
      try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(_file))) {
        final Object object = ois.readObject();
        @SuppressWarnings("unchecked")
        final Graph<ConstructId> g = (Graph<ConstructId>) object;
        log.info("Read call graph with [" + g.getNumberOfNodes() + "] nodes from [" + _file + "]");
        return g;
      }
    } catch (IOException ioe) {
      log.error("I/O error when reading object from [" + _file + "]: " + ioe.getMessage(), ioe);
    } catch (ClassNotFoundException cnfe) {
      log.error(
          "Class not found when reading object from [" + _file + "]: " + cnfe.getMessage(), cnfe);
    }
    return null;
  }

  private void writeToDisk(String _file, Graph<ConstructId> _g) {
    try {
      // Create all parent dirs
      final Path p = Paths.get(_file);
      FileUtil.createDirectory(p.getParent());

      // Write object
      final File f = new File(_file);
      try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
        oos.writeObject(_g);
        log.info("Wrote call graph with [" + _g.getNumberOfNodes() + "] nodes to [" + _file + "]");
      }
    } catch (IOException ioe) {
      log.error("I/O error when writing object to [" + _file + "]: " + ioe.getMessage(), ioe);
    }
  }

  /**
   * Given the bugid and callgraph constructor framework, do the reachability analysis from callgraph construction to callgraph computation
   */
  public void run() { // throws CallgraphConstructException {
    // Entry points should have been set
    if (this.entrypoints == null)
      throw new IllegalStateException("No entry points defined, cannot compute call graph");

    // Factory must be defined
    else if (this.constructor == null)
      throw new IllegalStateException("No call graph constructor defined");

    try {

      // ===== Phase 1: Call graph construction

      // Get a callgraph constructor (using the factory received earlier as argument)
      // AbstractConstructorFactory factory = AbstractConstructorFactory.getFactory(_framework);
      // Create a callgraph constructor, no matter which framework(soot/wala) is used.
      // this.constructor = this.factory.createConstructor(this.app_ctx, this.app_classes_path);
      // ICallgraphConstructor constructor = factory.createConstructor(this.app_ctx, p);

      Graph<ConstructId> g = null;

      // Read the call graph from disk
      /*final String read_from = VulasConfiguration.getConfiguration().getString(ReachabilityConfiguration.REACH_READ_FROM, null);
      if(read_from!=null && FileUtil.isAccessibleFile(read_from)) {
      	g = this.readFromDisk(read_from);
      	callgraph_construction_time = 0;
      }*/

      // Build the callgraph using the specific fwk (1.set classpath; 2. set entrypoints; 3. build
      // callgraph; 4. get callgraph)
      if (g == null) {
        constructor.setDepClasspath(this.getDependencyClasspath());
        constructor.setAppClasspath(this.getAppClasspath());
        constructor.setExcludePackages(this.excludedPackages);
        constructor.setEntrypoints(this.entrypoints);
        constructor.buildCallgraph(this.strictPolicy);
        g = constructor.getCallgraph();
        final long callgraph_construction_time = this.getConstructionTime() / 1000000;

        // Stats
        ReachabilityAnalyzer.log.info(
            "Call graph construction time (ms) : " + callgraph_construction_time);
        this.stats.put("cgConstructionTime (ms)", callgraph_construction_time);

        // Write graph to disk
        /*final String write_to  = VulasConfiguration.getConfiguration().getString(ReachabilityConfiguration.REACH_WRITE_TO, null);
        if(write_to!=null) {
        	this.writeToDisk(write_to, g);
        }*/
      }

      // The call graph we're going to work with, transformed into our representation
      this.callgraph = new Callgraph(g);

      // Warn for all non-app entry points for which no archive info was found
      int no_jar_url = 0;
      for (ConstructId cid : this.callgraph.getConstructsWithoutJarUrl()) {
        if (!this.entrypoints.contains(cid)) {
          log.warn(
              "["
                  + StringUtil.padLeft(++no_jar_url, 4)
                  + "] Cannot determine archive for construct ["
                  + cid.getQname()
                  + "]; size of class pool is ["
                  + ClassPoolUpdater.getInstance().countClasspathElements()
                  + "]");
        }
      }
      this.stats.put("callgraphNodesWithoutJar", (long) Integer.valueOf(no_jar_url));

      // ===== Phase 2: Search for paths to vulnerable methods

      // Target constructs do not exist: Stop here
      if (this.targetConstructs == null || this.targetConstructs.isEmpty()) {
        ReachabilityAnalyzer.log.info(
            "No target points defined, i.e., no vulnerability to check reachability for");
      }
      // Target constructs do exist: Compute paths from entry points to change list elements of the
      // bugs
      else {
        // Sources for the reachability analysis (= always the same, independent of the current bug)
        final Set<com.sap.psr.vulas.shared.json.model.ConstructId> src_ep =
            constructor.getEntrypoints();

        final boolean search_shortest =
            this.goalContext
                .getVulasConfiguration()
                .getConfiguration()
                .getBoolean(ReachabilityConfiguration.REACH_SEARCH_SHORTEST, true);

        final StopWatch sw = new StopWatch("Check reachability of change list elements").start();

        // Thread pool
        final int no_threads = ThreadUtil.getNoThreads();
        final ExecutorService pool = Executors.newFixedThreadPool(no_threads);

        // Create parallel call graph searches
        final Set<CallgraphPathSearch> searches = new HashSet<CallgraphPathSearch>();
        for (Map.Entry<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> e :
            this.targetConstructs.entrySet()) {
          final CallgraphPathSearch search =
              new CallgraphPathSearch()
                  .setEntrypoints(src_ep)
                  .setTargetpoints(e.getValue())
                  .setLabel(e.getKey())
                  .setCallback(this)
                  .setShortestPaths(search_shortest)
                  .setCallgraph(this.callgraph);
          searches.add(search);
          pool.execute(search);
        }

        try {
          // Wait for the thread pool to finish the work
          pool.shutdown();
          while (!pool.awaitTermination(60, TimeUnit.SECONDS))
            log.info("Wait for the completion of call graph searches ...");
        } catch (InterruptedException e) {
          throw new CallgraphConstructException("Interrupt exception", e);
        }

        sw.stop();

        // Compute stats
        long time_max = 0;
        long
            bugs_count =
                (this.targetConstructs == null ? 0 : this.targetConstructs.keySet().size()),
            bugs_reachable = 0;
        long tp_sum = 0, tp_min = Long.MAX_VALUE, tp_max = 0, tp_avg = 0;
        long shortest_path_sum = 0,
            shortest_path_min = Long.MAX_VALUE,
            shortest_path_max = 0,
            shortest_path_avg = 0,
            path_count = 0;
        for (CallgraphPathSearch search : searches) {
          final long dur = search.getStopWatch().getRuntime();
          time_max = (dur > time_max ? dur : time_max);

          if (this.rcPaths.get(search.getLabel()).size() > 0) {
            bugs_reachable++;

            // Get the shortest and longest path
            for (List<com.sap.psr.vulas.shared.json.model.ConstructId> l :
                this.rcPaths.get(search.getLabel())) {
              path_count++;
              shortest_path_sum += l.size();
              shortest_path_avg = (int) Math.abs((double) shortest_path_sum / path_count);
              shortest_path_min = (l.size() < shortest_path_min ? l.size() : shortest_path_min);
              shortest_path_max = (l.size() > shortest_path_max ? l.size() : shortest_path_max);
            }
          }

          tp_sum += this.targetConstructs.get(search.getLabel()).size();
          tp_avg = (int) Math.abs((double) tp_sum / bugs_count);
          tp_min =
              (this.targetConstructs.get(search.getLabel()).size() < tp_min
                  ? this.targetConstructs.get(search.getLabel()).size()
                  : tp_min);
          tp_max =
              (this.targetConstructs.get(search.getLabel()).size() > tp_max
                  ? this.targetConstructs.get(search.getLabel()).size()
                  : tp_max);
        }

        // Write stats to map
        this.stats.put("bugs", bugs_count);
        this.stats.put("bugsReachable", bugs_reachable);
        this.stats.put("targetPointsMin", tp_min);
        this.stats.put("targetPointsMax", tp_max);
        this.stats.put("targetPointsAvg", tp_avg);
        this.stats.put("shortestPathMin", shortest_path_min);
        this.stats.put("shortestPathMax", shortest_path_max);
        this.stats.put("shortestPathAvg", shortest_path_avg);
      }

      // ===== Phase 3: Identify reachable methods and touch points

      this.identifyTouchPoints();

      // Stats
      this.stats.put("reachableArchives", (long) Integer.valueOf(this.reachableConstructs.size()));
      this.stats.put("touchedArchives", (long) Integer.valueOf(this.touchPoints.size()));
      long touch_points = 0;
      for (Map.Entry<String, Set<List<NodeMetaInformation>>> entry : this.touchPoints.entrySet())
        touch_points += entry.getValue().size();
      this.stats.put("touchPointsTotal", Long.valueOf(touch_points));

    } catch (CallgraphConstructException e) {
      ReachabilityAnalyzer.log.info(
          "Call graph cannot be constructed or analyzed, reachability analysis will be"
              + " interrupted: "
              + e.getMessage());
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Loops all nodes of the {@link Callgraph} in order to identify touch points (direct calls
   * from an application construct to a library construct or vice versa) and the total set
   * of reachable constructs per library.
   */
  private void identifyTouchPoints() {
    // Allow to skip the collection of touch points (which is potentially time consuming due to
    // nested looping over app methods and all their edges)
    final boolean identify_touchpoints =
        this.goalContext
            .getVulasConfiguration()
            .getConfiguration()
            .getBoolean(ReachabilityConfiguration.REACH_TOUCHPOINTS, true);
    if (!identify_touchpoints)
      log.warn("Identification of touch points disabled per configuration");

    final StopWatch sw = new StopWatch("Identify reachable constructs and touch points").start();

    // Thread pool
    final int no_threads = ThreadUtil.getNoThreads();
    final ExecutorService pool = Executors.newFixedThreadPool(no_threads);

    // Partition size
    final int size = (int) Math.ceil((double) this.callgraph.getNodeCount() / (double) no_threads);

    // Create parallel call graph searches
    final Set<CallgraphReachableSearch> searches = new HashSet<CallgraphReachableSearch>();
    for (int i = 0; i < no_threads; i++) {
      int min = i * size;
      int max = Math.min((i + 1) * size, this.callgraph.getNodeCount());
      final CallgraphReachableSearch search =
          new CallgraphReachableSearch()
              .setAppConstructs(this.appConstructs)
              .setFindTouchPoints(identify_touchpoints)
              .setCallgraph(this.callgraph)
              .setMinMax(min, max);
      searches.add(search);
      pool.execute(search);
    }

    try {
      // Wait for the thread pool to finish the work
      pool.shutdown();
      while (!pool.awaitTermination(60, TimeUnit.SECONDS))
        log.info("Wait for the completion of call graph searches ...");

      // Join reachable constructs and touch points
      for (CallgraphReachableSearch search : searches) {

        // Reachable constructs
        final Map<String, Set<NodeMetaInformation>> reachable_constructs =
            search.getReachableConstructs();
        for (Map.Entry<String, Set<NodeMetaInformation>> e : reachable_constructs.entrySet()) {
          if (!this.reachableConstructs.containsKey(e.getKey()))
            this.reachableConstructs.put(e.getKey(), new HashSet<NodeMetaInformation>());
          this.reachableConstructs.get(e.getKey()).addAll(e.getValue());
        }

        // Touch points
        final Map<String, Set<List<NodeMetaInformation>>> touch_points = search.getTouchPoints();
        for (Map.Entry<String, Set<List<NodeMetaInformation>>> e : touch_points.entrySet()) {
          if (!this.touchPoints.containsKey(e.getKey()))
            this.touchPoints.put(e.getKey(), new HashSet<List<NodeMetaInformation>>());
          this.touchPoints.get(e.getKey()).addAll(e.getValue());
        }
      }
    } catch (InterruptedException e) {
      log.error("Interrupt exception", e);
    }

    sw.stop();
  }

  /**
   * <p>getStatistics.</p>
   *
   * @return a {@link java.util.Map} object.
   */
  public Map<String, Long> getStatistics() {
    return this.stats;
  }

  /**
   * Returns the time required for building the call graph (in nanoseconds), or -1 if the construction did not finish.
   *
   * @return the time required for building the call graph (in nanoseconds)
   */
  public long getConstructionTime() {
    return (constructor == null ? -1 : constructor.getConstructionTime());
  }

  /**
   * Returns a human-readable description of the constructor's specific configuration.
   *
   * @return a {@link org.apache.commons.configuration.Configuration} object.
   */
  public Configuration getConfiguration() {
    return (constructor == null ? null : constructor.getConstructorConfiguration());
  }

  /**
   * Returns the number of nodes in the constructed call graph (or -1 if the call graph has not been constructed).
   *
   * @return the number of nodes in this graph
   * @see #getEdgeCount()
   */
  public int getNodeCount() {
    return (this.callgraph == null ? -1 : this.callgraph.getNodeCount());
  }

  /**
   * Returns the number of edges in constructed call graph (or -1 if the call graph has not been constructed).
   *
   * @return the number of edges in this graph
   * @see #getNodeCount()
   */
  public int getEdgeCount() {
    return (this.callgraph == null ? -1 : this.callgraph.getEdgeCount());
  }

  /**
   * <p>startAnalysis.</p>
   *
   * @param _ra a {@link com.sap.psr.vulas.cg.ReachabilityAnalyzer} object.
   * @param _timeout_ms timeout in milliseconds (no timeout if negative or 0)
   * @return true if the analysis terminated, false otherwise
   * @throws java.lang.InterruptedException
   */
  public static boolean startAnalysis(ReachabilityAnalyzer _ra, long _timeout_ms)
      throws InterruptedException {
    boolean success = false;
    long start_time = System.currentTimeMillis(), runtime = -1, timeout = -1, wait = -1;

    // Loop until analysis finished or is interrupted
    final Thread t = new Thread(_ra, "vulas-reach-" + ++ReachabilityAnalyzer.THREAD_COUNT);

    // Wait time between checks (15 min if no timeout is given, timeout/10 otherwise)
    if (_timeout_ms <= 0) {
      ReachabilityAnalyzer.log.info(
          "Starting reachability analysis (thread [" + t.getName() + "], no timeout");
      wait = 1000 * 60 * 15;
    } else {
      ReachabilityAnalyzer.log.info(
          "Starting reachability analysis (thread ["
              + t.getName()
              + "], timeout after "
              + StringUtil.formatMinString(_timeout_ms)
              + ")");
      wait = (long) Math.abs((double) _timeout_ms / (double) 10);
    }

    // Print mem info
    ReachabilityAnalyzer.logMemoryConsumption(true);

    t.start();
    while (t.isAlive()) {

      // Wait for analysis to finish
      t.join(wait);

      // Since when and for how long?
      runtime = System.currentTimeMillis() - start_time;
      timeout = (_timeout_ms <= 0 ? -1 : _timeout_ms - runtime);

      // It terminated
      if (!t.isAlive()) {
        success = true;
        break;
      }
      // Still running
      else {
        // Interrupt if a timeout has been specified and we exceed it
        if (_timeout_ms > 0 && runtime > _timeout_ms) {
          ReachabilityAnalyzer.log.warn(
              "[" + t.getName() + "] reached timeout and will be interrupted");
          final StackTraceElement[] stack = t.getStackTrace();
          for (StackTraceElement e : stack) {
            ReachabilityAnalyzer.log.warn("    " + e.toString());
          }
          t.interrupt();
          // Shouldn't be long now
          // -- wait indefinitely
          // t.join();

          success = false;
          break;
        } else {
          // Print runtime info
          if (_timeout_ms <= 0)
            ReachabilityAnalyzer.log.info(
                "[" + t.getName() + "] runs for " + StringUtil.formatMinString(runtime));
          else
            ReachabilityAnalyzer.log.info(
                "["
                    + t.getName()
                    + "] runs for "
                    + StringUtil.formatMinString(runtime)
                    + ", will be interrupted in "
                    + StringUtil.formatMinString(timeout));

          // Print mem info
          ReachabilityAnalyzer.logMemoryConsumption(true);
        }
      }
    }

    if (success)
      ReachabilityAnalyzer.log.info(
          "["
              + t.getName()
              + "] terminated successfully after "
              + StringUtil.formatMinString(runtime));
    else
      ReachabilityAnalyzer.log.error(
          "["
              + t.getName()
              + "] terminated w/o success after "
              + StringUtil.formatMinString(runtime));

    return success;
  }

  private static void logMemoryConsumption(boolean _run_gc) {
    //		if(_run_gc)
    //			ReachabilityAnalyzer.runtime.gc();
    final long mem_total = ReachabilityAnalyzer.runtime.totalMemory();
    final long mem_free = ReachabilityAnalyzer.runtime.freeMemory();
    final long mem_max = ReachabilityAnalyzer.runtime.maxMemory();
    ReachabilityAnalyzer.log.info(
        "Memory stats (used/free/total/max): ["
            + StringUtil.byteToMBString(mem_total - mem_free)
            + "/"
            + StringUtil.byteToMBString(mem_free)
            + "/"
            + StringUtil.byteToMBString(mem_total)
            + "/"
            + StringUtil.byteToMBString(mem_max)
            + "]");
  }

  synchronized void callback(CallgraphPathSearch _search) {
    this.rcPaths.put(_search.getLabel(), _search.getPaths());
    this.uploadBug(_search.getLabel(), _search.getPaths());
  }

  /**
   * Uploads paths for the given bug to the backend. If there is no such path, an empty array will be uploaded
   * to indicate that the analysis ran but did not yield a result.
   *
   * @param _bugid a {@link java.lang.String} object.
   * @param _paths a {@link java.util.List} object.
   */
  public synchronized void uploadBug(String _bugid, List<List<ConstructId>> _paths) {

    // Used to limit the number of paths (per change list element) that will be uploaded to the
    // backend
    final int max_path =
        this.goalContext
            .getVulasConfiguration()
            .getConfiguration()
            .getInt(ReachabilityConfiguration.REACH_MAX_PATH, 10);
    final Map<com.sap.psr.vulas.shared.json.model.ConstructId, Integer> counters =
        new HashMap<com.sap.psr.vulas.shared.json.model.ConstructId, Integer>();
    int count = -1;

    final JsonParser parser = new JsonParser();
    final JsonObject json_app =
        parser.parse(JacksonUtil.asJsonString(this.app_ctx)).getAsJsonObject();
    final JsonArray json_paths = new JsonArray();

    JsonObject json_path = null;
    JsonArray json_path_path = null;
    JsonObject path_node_obj = null;

    counters.clear();

    // Prepare each single path
    for (List<com.sap.psr.vulas.shared.json.model.ConstructId> path : this.rcPaths.get(_bugid)) {

      // Do not serialize more than max_path paths
      com.sap.psr.vulas.shared.json.model.ConstructId target_construct = path.get(path.size() - 1);
      count =
          (counters.get(target_construct) == null ? 0 : counters.get(target_construct).intValue())
              + 1;
      counters.put(target_construct, Integer.valueOf(count));
      if (count > max_path) continue;

      // Serialize
      json_path = new JsonObject();

      json_path.add("app", json_app);
      json_path.addProperty("bug", _bugid);
      json_path.addProperty("source", this.source.toString());
      json_path.addProperty("executionId", "dummy");

      // The actual path
      json_path_path = new JsonArray();
      for (com.sap.psr.vulas.shared.json.model.ConstructId cid : path) {
        path_node_obj = new JsonObject();
        path_node_obj.add("constructId", parser.parse(JacksonUtil.asJsonString(cid)));
        final NodeMetaInformation nmi = this.callgraph.getInformationForConstructId(cid);
        path_node_obj.addProperty("lib", nmi.getArchiveId());
        json_path_path.add(path_node_obj);
      }
      json_path.add("path", json_path_path);
      json_paths.add(json_path);
    }

    // Log if paths will be dropped
    int uploaded_path_count = 0;
    for (Map.Entry<com.sap.psr.vulas.shared.json.model.ConstructId, Integer> e :
        counters.entrySet()) {
      if (e.getValue().intValue() > max_path) {
        ReachabilityAnalyzer.log.warn(
            "["
                + e.getValue().intValue()
                + "] paths lead to construct ["
                + e.getKey()
                + "], only ["
                + max_path
                + "] will be uploaded");
        uploaded_path_count += max_path;
      } else {
        uploaded_path_count += e.getValue().intValue();
      }
    }

    // Save JSON
    ReachabilityAnalyzer.log.info(
        "Upload ["
            + uploaded_path_count
            + "] path(s) for bug ["
            + _bugid
            + "]: "
            + (this.rcPaths.get(_bugid).size() == 0
                ? "Change list NOT reachable "
                : "Change list reachable"));
    try {
      BackendConnector.getInstance()
          .uploadPaths(this.goalContext, this.app_ctx, json_paths.toString());
    } catch (BackendConnectionException e) {
      ReachabilityAnalyzer.log.error(
          "Error while uploading paths for bug [" + _bugid + "]: " + e.getMessage());
    }
  }

  private void appendJarName(String _jar_url, StringBuffer _buffer) {
    if (_jar_url != null) {
      if (_buffer.length() > 0) _buffer.append(", ");
      if (_jar_url.indexOf("/") != -1) {
        _buffer.append(_jar_url.substring(_jar_url.indexOf("/") + 1));
      } else {
        _buffer.append(_jar_url);
      }
    }
  }

  /**
   * Uploads reachable constructs and touch points to the backend.
   */
  public void upload() {

    // 1) Upload reachable constructs (per dependency
    if (!this.reachableConstructs.isEmpty()) {
      final StringBuffer upload_succeeded = new StringBuffer(), upload_failed = new StringBuffer();
      Set<NodeMetaInformation> nodes = null;
      JsonArray json_constructs = null;

      // Loop dependencies
      for (Map.Entry<String, Set<NodeMetaInformation>> e : this.reachableConstructs.entrySet()) {
        nodes = e.getValue();
        json_constructs = new JsonArray();

        // Loop reachable constructs
        String jar_url = null;
        for (NodeMetaInformation nmi : nodes) {
          if (jar_url == null) jar_url = nmi.getJarUrl();
          json_constructs.add(
              new JsonParser()
                  .parse(JacksonUtil.asJsonString(nmi.getConstructId()))
                  .getAsJsonObject());
        }

        // Upload
        try {
          ReachabilityAnalyzer.log.info(
              "Upload ["
                  + nodes.size()
                  + "] reachable construct IDs for library [sha1="
                  + e.getKey()
                  + ", jar URL="
                  + jar_url
                  + "]");
          final boolean success =
              BackendConnector.getInstance()
                  .uploadReachableConstructs(
                      this.goalContext, this.app_ctx, e.getKey(), json_constructs.toString());
          if (success) this.appendJarName(jar_url, upload_succeeded);
          else this.appendJarName(jar_url, upload_failed);
        } catch (BackendConnectionException bce) {
          ReachabilityAnalyzer.log.error(
              "Error while uploading reachable constructs for library [sha1="
                  + e.getKey()
                  + ", jar URL="
                  + jar_url
                  + "]: "
                  + bce.getMessage());
          this.appendJarName(jar_url, upload_failed);
        }
      }

      ReachabilityAnalyzer.log.info(
          "Upload of reachable constructs succeeded for [" + upload_succeeded + "]");
      ReachabilityAnalyzer.log.warn(
          "Upload of reachable constructs failed    for [" + upload_failed + "]");
    }

    // 2) Upload touch points per dependency
    if (!this.touchPoints.isEmpty()) {
      final StringBuffer upload_succeeded = new StringBuffer(), upload_failed = new StringBuffer();
      Set<List<NodeMetaInformation>> touch_points = null;
      JsonArray json_tps = null;
      JsonObject json_tp = null;
      NodeMetaInformation from = null, to = null;

      // Loop dependencies
      for (Map.Entry<String, Set<List<NodeMetaInformation>>> e : this.touchPoints.entrySet()) {
        touch_points = e.getValue();
        json_tps = new JsonArray();

        String jar_url = null;

        // Loop touch points
        for (List<NodeMetaInformation> touch_point : touch_points) {
          json_tp = new JsonObject();
          from = touch_point.get(0);
          to = touch_point.get(1);
          json_tp.add(
              "from",
              new JsonParser()
                  .parse(JacksonUtil.asJsonString(from.getConstructId()))
                  .getAsJsonObject());
          json_tp.add(
              "to",
              new JsonParser()
                  .parse(JacksonUtil.asJsonString(to.getConstructId()))
                  .getAsJsonObject());
          json_tp.addProperty("source", this.source.toString());
          if (e.getKey().equals(to.getArchiveId())) {
            json_tp.addProperty("direction", "A2L");
            jar_url = to.getJarUrl();
          } else {
            json_tp.addProperty("direction", "L2A");
            jar_url = from.getJarUrl();
          }

          json_tps.add(json_tp);
        }

        // Upload
        try {
          ReachabilityAnalyzer.log.info(
              "Upload ["
                  + touch_points.size()
                  + "] touch points for library [sha1="
                  + e.getKey()
                  + ", jar URL="
                  + jar_url
                  + "]");
          final boolean success =
              BackendConnector.getInstance()
                  .uploadTouchPoints(
                      this.goalContext, this.app_ctx, e.getKey(), json_tps.toString());
          if (success) this.appendJarName(jar_url, upload_succeeded);
          else this.appendJarName(jar_url, upload_failed);
        } catch (BackendConnectionException bce) {
          ReachabilityAnalyzer.log.error(
              "Error while uploading touch points for library [sha1="
                  + e.getKey()
                  + ", jar URL="
                  + jar_url
                  + "]: "
                  + bce.getMessage());
          this.appendJarName(jar_url, upload_failed);
        }
      }

      ReachabilityAnalyzer.log.info(
          "Upload of touch points succeeded for [" + upload_succeeded + "]");
      ReachabilityAnalyzer.log.warn("Upload of touch points failed    for [" + upload_failed + "]");
    }
  }
}
