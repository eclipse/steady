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
package org.eclipse.steady.cg.soot;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.cg.CallgraphConstructException;
import org.eclipse.steady.cg.spi.ICallgraphConstructor;
import org.eclipse.steady.java.JavaConstructorId;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.JavaMethodId;
import org.eclipse.steady.java.monitor.ClassVisitor;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

import soot.G;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

/**
 * Callgraph constructor using soot framework; implementing the interface ICallgraphConstructor
 */
public class SootCallgraphConstructor implements ICallgraphConstructor {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** Constant <code>FRAMEWORK="soot"</code> */
  public static final String FRAMEWORK = "soot";

  private long buildTimeNano = -1;

  private long start_nanos = -1;

  /**
   * The context information of the application JAR to be analyzed
   */
  private Application appContext = null;

  private VulasConfiguration vulasConfiguration = null;

  /**
   * The JAR to be analyzed.
   */
  // private String appJar = null;
  protected String classpath = null;

  private String appClasspath = null;
  protected final List<SootMethod> entrypoints = new ArrayList<>();
  private final Set<org.eclipse.steady.shared.json.model.ConstructId> filteredEP = new HashSet<>();

  private CallGraph callgraph = null;

  /**
   * {@inheritDoc}
   *
   * Set the context of the application to be analyzed
   */
  public void setAppContext(Application _ctx) {
    this.appContext = _ctx;
  }

  /** {@inheritDoc} */
  public void setVulasConfiguration(VulasConfiguration _cfg) {
    this.vulasConfiguration = _cfg;
  }

  /**
   * <p>Getter for the field <code>appContext</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public Application getAppContext() {
    return this.appContext;
  }

  /**
   * <p>getFramework.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFramework() {
    return SootCallgraphConstructor.FRAMEWORK;
  }

  /** {@inheritDoc} */
  public void setDepClasspath(String _dependenciesClasspath) {
    if (this.classpath != null) {
      this.classpath += System.getProperty("path.separator");
      this.classpath += _dependenciesClasspath;
    } else this.classpath = _dependenciesClasspath;
    SootCallgraphConstructor.log.info(
        "Add to soot classpath the dependencies: [" + this.classpath + "]");
  }

  /** {@inheritDoc} */
  public void setAppClasspath(String _cp) {
    if (this.classpath != null) {
      this.classpath += System.getProperty("path.separator");
      this.classpath += _cp;
    } else this.classpath = _cp;
    this.appClasspath = _cp;

    SootCallgraphConstructor.log.info(
        "Add to soot classpath the application: [" + this.classpath + "]");
  }

  /**
   * Returns the time required for building the call graph (in nanoseconds), or -1 if the construction did not finish.
   *
   * @return the time required for building the call graph (in nanoseconds)
   */
  public long getConstructionTime() {
    return this.buildTimeNano;
  }

  /**
   * Returns a human-readable description of the constructor's specific configuration.
   *
   * @return a {@link org.apache.commons.configuration.Configuration} object.
   */
  public Configuration getConstructorConfiguration() {
    return this.vulasConfiguration
        .getConfiguration()
        .subset(SootConfiguration.SOOT_CONFIGURATION_SETTINGS);
  }

  /*
   * First resets and then configures Soot's Options
   */

  /**
   * <p>sootSetup.</p>
   */
  protected void sootSetup() {

    // start with a clean run of Soot
    G.v().resetSpark();
    G.reset();

    boolean verbose =
        this.vulasConfiguration.getConfiguration().getBoolean(SootConfiguration.SOOT_VERBOSE);

    // set default excluded list to empty list
    // Options.v().set_include_all(true);

    String excludedPackages =
        this.vulasConfiguration.getConfiguration().getString(SootConfiguration.SOOT_EXCLUSIONS);
    List<String> excludedList = Arrays.asList(excludedPackages.split(";"));
    Options.v().set_exclude(excludedList);

    // add the rt.jar and jce.jar to the classpath; WALA does this silently in the background
    Options.v().set_prepend_classpath(true);

    // Read from soot-cfg.properties
    Options.v()
        .set_allow_phantom_refs(
            this.vulasConfiguration
                .getConfiguration()
                .getBoolean(SootConfiguration.SOOT_ALLOW_PHANTOM));
    Options.v().set_verbose(verbose);
    Options.v()
        .set_app(
            this.vulasConfiguration.getConfiguration().getBoolean(SootConfiguration.SOOT_APP_MODE));
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg", "safe-forname:" + false);

    // additional options for easy debugging
    Options.v().set_keep_line_number(true);
    Options.v().set_throw_analysis(Options.throw_analysis_unit);
    Options.v().setPhaseOption("cg", "verbose:" + verbose);
    Options.v().set_debug(verbose);
    Options.v().set_debug_resolver(verbose);

    // do not release bodies after running the packs
    Options.v().set_output_format(Options.output_format_none);

    // with this option we get only method signatures but not their bodies
    Options.v()
        .set_no_bodies_for_excluded(
            this.vulasConfiguration
                .getConfiguration()
                .getBoolean(SootConfiguration.SOOT_NOBODY_FOR_X));

    if (this.vulasConfiguration.getConfiguration().getBoolean(SootConfiguration.SOOT_SPARK)) {
      String s = "on";
      if (this.vulasConfiguration.getConfiguration().getBoolean(SootConfiguration.SOOT_SPARK_OTF))
        s += ",on-fly-cg:true";
      else s += ",on-fly-cg:false";
      if (this.vulasConfiguration.getConfiguration().getBoolean(SootConfiguration.SOOT_SPARK_VTA))
        s += ",vta:true";
      else s += ",vta:false";
      if (this.vulasConfiguration.getConfiguration().getBoolean(SootConfiguration.SOOT_SPARK_RTA))
        s += ",rta:true";
      else s += ",rta:false";
      SootCallgraphConstructor.log.info("Enabled cg.spark with settings [" + s + "]");
      Options.v().setPhaseOption("cg.spark", s);
    }

    ArrayList<String> processDirs = new ArrayList<>();
    processDirs.add(this.appClasspath);
    Options.v().set_process_dir(processDirs);

    Options.v().set_soot_classpath(this.classpath);
  }

  /**
   * Gets the SootMethod that correspond to the given constructs
   *
   * @param _constructs a {@link java.util.Set} object.
   * @throws org.eclipse.steady.cg.CallgraphConstructException
   */
  protected void sootMethods4entrypoints(
      Set<org.eclipse.steady.shared.json.model.ConstructId> _constructs)
      throws CallgraphConstructException {

    SootMethod method = null;
    SootClass ep = null;
    // No entrypoints set, search for a main (if existing)
    if (_constructs.isEmpty()) {
      SootCallgraphConstructor.log.info(
          "No customized entrypoints set; search for main as default entry point");
      Iterator<SootClass> classes = Scene.v().getApplicationClasses().iterator();
      try {
        while (classes.hasNext()) {
          ep = classes.next();
          method = ep.getMethodByName("main");
          if (method != null) {
            this.filteredEP.add(getCid(method));
            this.entrypoints.add(method);
            break;
          }
        }
      } catch (Exception e) {
        SootCallgraphConstructor.log.error(
            "Error while searching for main method in class [" + ep + "]: " + e.getMessage());
      }
      if (this.entrypoints.isEmpty())
        throw new CallgraphConstructException(
            "No main method found that can be used as entry point", null);
    }

    // Use entrypoints passed as arg
    else {

      Iterator<SootMethod> iter = null;
      for (org.eclipse.steady.shared.json.model.ConstructId cid : _constructs) {

        final JavaId jcid = (JavaId) JavaId.toCoreType(cid);

        // when it's a java method
        if (jcid instanceof org.eclipse.steady.java.JavaMethodId) {
          JavaMethodId mid = (JavaMethodId) jcid;
          try {
            ep = Scene.v().getSootClass(mid.getDefinitionContext().getQualifiedName());

            // Loop over methods to find the one :/
            iter = ep.methodIterator();
            while (iter.hasNext()) {
              method = iter.next();
              if (method.isConcrete()) {
                // Compare the JavaMethodId qname
                if (getCid(method).getQname().equals(mid.getQualifiedName())) {
                  this.filteredEP.add(cid);
                  this.entrypoints.add(method);
                  break;
                }
              }
            }
          } catch (Exception e) {
            SootCallgraphConstructor.log.error(
                "Error while searching for method " + mid.toString() + ": " + e.getMessage());
          }
        }
        // when it's a java object constructor
        else if (jcid instanceof org.eclipse.steady.java.JavaConstructorId) {
          JavaConstructorId jconsid = (JavaConstructorId) jcid;
          try {
            ep = Scene.v().getSootClass(jconsid.getDefinitionContext().getQualifiedName());
            // Loop over constructors to find the one :/
            iter = ep.methodIterator();
            while (iter.hasNext()) {
              method = iter.next();
              if (method.isConstructor()) {
                // Compare the JavaConstructorId qname
                if (getCid(method).getQname().equals(jconsid.getQualifiedName())) {
                  this.filteredEP.add(cid);
                  this.entrypoints.add(method);
                  break;
                }
              }
            }
          } catch (Exception e) {
            SootCallgraphConstructor.log.error(
                "Error while searching for method " + jcid.toString() + ": " + e.getMessage());
          }
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * Filter and find all entrypoints in Scene
   */
  public void setEntrypoints(Set<org.eclipse.steady.shared.json.model.ConstructId> _constructs)
      throws CallgraphConstructException {

    start_nanos = System.nanoTime();

    // setup soot
    this.sootSetup();

    // load the all necessary classes
    Scene.v().loadNecessaryClasses();

    // determine the entrypoints
    this.sootMethods4entrypoints(_constructs);

    if (!this.entrypoints.isEmpty()) {
      Scene.v().setEntryPoints(createEntryPoint4Soot(this.entrypoints));

      SootCallgraphConstructor.log.info("[" + this.entrypoints.size() + "] entry points set");
    } else {
      throw new CallgraphConstructException(
          "No entry points could be set, which will not allow to build the callgraph", null);
    }
  }

  private ArrayList<SootMethod> createEntryPoint4Soot(Collection<SootMethod> selectedEntrypoints) {
    String slcEntrypointGenerator =
        this.vulasConfiguration
            .getConfiguration()
            .getString(SootConfiguration.SOOT_ENTRYPOINT_GENERATOR);

    if (slcEntrypointGenerator.toLowerCase().equals("none")) {
      return new ArrayList<>(selectedEntrypoints);
    }

    ArrayList<String> methodsToCall = new ArrayList<>();
    for (SootMethod sm : selectedEntrypoints) {
      methodsToCall.add(sm.getSignature());
    }

    try {
      final Class cls = Class.forName(slcEntrypointGenerator);
      Constructor constructor = cls.getDeclaredConstructor(Collection.class);
      IEntryPointCreator entryPointCreator =
          (IEntryPointCreator) constructor.newInstance(methodsToCall);
      SootMethod dummyMain = entryPointCreator.createDummyMain();
      ArrayList<SootMethod> generatedEntrypoint = new ArrayList<>();
      generatedEntrypoint.add(dummyMain);
      return generatedEntrypoint;

    } catch (Throwable e) {
      SootCallgraphConstructor.log.error(
          "Error while creating entrypoint generator of class ["
              + slcEntrypointGenerator
              + "]: "
              + e.getMessage(),
          e);
    }

    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   *
   * Read all configurations and parse command line options, and then build callgraph based on these properties
   */
  public void buildCallgraph(boolean _policy) throws CallgraphConstructException {
    SootCallgraphConstructor.log.info(
        "Starting call graph construction for " + this.appContext.toString(false));

    try {
      PackManager.v().runPacks();

      this.callgraph = Scene.v().getCallGraph();
      this.buildTimeNano = System.nanoTime() - start_nanos;
      SootCallgraphConstructor.log.info(
          "Construction completed in "
              + StringUtil.nanoToFlexDurationString(this.buildTimeNano)
              + ", call graph has ["
              + callgraph.size()
              + "] edges]");
      checkEntrypoints(_policy);
    } catch (CallgraphConstructException e) {
      SootCallgraphConstructor.log.error("Error building call graph: " + e.getMessage());
      throw new CallgraphConstructException("Error building call graph", e);
    }
  }

  /**
   * check whether all entrypoints are existing in callgraph
   *
   * @throws CallgraphConstructException if no callgraph could be built
   */
  private void checkEntrypoints(boolean _policy) throws CallgraphConstructException {
    final HashSet<SootMethod> check_ep = new HashSet<>();
    check_ep.addAll(this.entrypoints);

    Iterator<MethodOrMethodContext> src_nodes = callgraph.sourceMethods();
    SootMethod method = null;
    MethodOrMethodContext src_node = null;
    Iterator<Edge> edges = null;
    while (src_nodes.hasNext()) {
      src_node = src_nodes.next();
      method = src_node.method();
      if (this.entrypoints.contains(method)) check_ep.remove(method);
      edges = this.callgraph.edgesOutOf(src_node);
      while (edges.hasNext()) {
        method = edges.next().tgt();
        if (this.entrypoints.contains(method)) check_ep.remove(method);
      }
    }
    int diff = check_ep.size();

    if (_policy && diff != 0) {
      for (SootMethod m : check_ep)
        SootCallgraphConstructor.log.warn("[ " + m.getSignature() + " ] is missing");
      throw new CallgraphConstructException(
          "Strict policy applied; terminating as there are ["
              + diff
              + "] entry points missing in call graph",
          null);
    }
    // Throw exception if number of missing EPs exceeds threshold
    if (this.entrypoints.size() - diff == 0)
      throw new CallgraphConstructException(
          "[0/" + this.entrypoints.size() + "] entry points found in call graph", null);

    // Print warning for missing entry points
    if (diff > 0) {
      SootCallgraphConstructor.log.warn(
          "There should be "
              + this.entrypoints.size()
              + " entrypoints set; but "
              + diff
              + " entrypoints missing in the call graph");
      for (SootMethod m : check_ep)
        SootCallgraphConstructor.log.warn("[ " + m.getSignature() + " ] is missing");
    } else {
      SootCallgraphConstructor.log.info(
          "All [" + this.entrypoints.size() + "] entry points existing in the call graph");
    }
  }

  /**
   * Given a SootMethod, return its ConstructId
   *
   * @param _method the {@link soot.SootMethod} to compute the ConstructId for
   * @return computed ConstructId
   */
  private static org.eclipse.steady.shared.json.model.ConstructId getCid(SootMethod _method) {
    String qname = null;
    org.eclipse.steady.shared.json.model.ConstructId cid = null;
    String signature = _method.getSignature();
    if (_method.getName().equals("<clinit>")) {
      qname = signature.substring(1, _method.getSignature().indexOf(":"));
      cid = JavaId.toSharedType(JavaId.parseClassQName(qname).getClassInit());
    } else {
      if (_method.isConstructor()) qname = signature.substring(1, signature.indexOf(":"));
      else qname = signature.substring(1, signature.indexOf(":")) + "." + _method.getName();
      signature = _method.getSubSignature();
      qname += signature.substring(signature.indexOf("("), signature.indexOf(")") + 1);
      qname = ClassVisitor.removeParameterQualification(qname);
      if (_method.isConstructor()) cid = JavaId.toSharedType(JavaId.parseConstructorQName(qname));
      else cid = JavaId.toSharedType(JavaId.parseMethodQName(qname));
    }
    return cid;
  }

  /**
   * Normalizing a soot callgraph to a general graph represented by ConstructId
   *
   * @return a {@link com.ibm.wala.util.graph.Graph} object.
   */
  public Graph<org.eclipse.steady.shared.json.model.ConstructId> getCallgraph() {
    final Graph<org.eclipse.steady.shared.json.model.ConstructId> graph =
        SlowSparseNumberedGraph.make();

    if (this.callgraph != null) {
      int edges_no = 0;
      org.eclipse.steady.shared.json.model.ConstructId src_cid = null, tgt_cid = null;
      MethodOrMethodContext src_node = null;
      Iterator<Edge> edges = null;

      final Iterator<MethodOrMethodContext> src_nodes = callgraph.sourceMethods();
      while (src_nodes.hasNext()) {
        src_node = src_nodes.next();
        src_cid = getCid(src_node.method());
        graph.addNode(src_cid);

        // add edges
        edges = this.callgraph.edgesOutOf(src_node);
        while (edges.hasNext()) {
          tgt_cid = getCid(edges.next().tgt());
          graph.addNode(tgt_cid);
          if (!graph.hasEdge(src_cid, tgt_cid)) {
            graph.addEdge(src_cid, tgt_cid);
            edges_no++;
          }
        }
      }

      SootCallgraphConstructor.log.info(
          "Normalized call graph has ["
              + graph.getNumberOfNodes()
              + " nodes] (with distinct ConstructId) and ["
              + edges_no
              + "] edges");
    }
    // No callgraph exists
    else {
      throw new IllegalStateException("There exists no call graph");
    }
    return graph;
  }

  /**
   * <p>Getter for the field <code>entrypoints</code>.</p>
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<org.eclipse.steady.shared.json.model.ConstructId> getEntrypoints() {
    return this.filteredEP;
  }

  /** {@inheritDoc} */
  public void setExcludePackages(String _packages) {
    // Overwrite configuration (if requested)
    if (_packages != null && !_packages.equals(""))
      this.vulasConfiguration.setProperty(SootConfiguration.SOOT_EXCLUSIONS, _packages);
    SootCallgraphConstructor.log.info(
        "Set packages to be excluded [ "
            + this.vulasConfiguration
                .getConfiguration()
                .getString(SootConfiguration.SOOT_EXCLUSIONS)
            + " ]");
  }
}
