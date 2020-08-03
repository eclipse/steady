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
package com.sap.psr.vulas.cg.wala;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;


import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.sap.psr.vulas.cg.CallgraphConstructException;
import com.sap.psr.vulas.cg.spi.ICallgraphConstructor;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Callgraph constructor using wala framework; implementing the interface ICallgraphConstructor
 */
public class WalaCallgraphConstructor implements ICallgraphConstructor {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(WalaCallgraphConstructor.class);
    /** Constant <code>FRAMEWORK="wala"</code> */
    public static final String FRAMEWORK = "wala";

    // Packages to be excluded for call graph construction, which is read from wala-cfg.properties
    private File excludedPackagesFile = null;

    /**
     * The context information of the application JAR to be analyzed
     */
    private Application appContext = null;
    
    private VulasConfiguration vulasConfiguration = null;

    /**
     * <p>getFramework.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFramework() { return WalaCallgraphConstructor.FRAMEWORK; }
    
    /**
     * <p>Getter for the field <code>appContext</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
     */
    public Application getAppContext() {
        return this.appContext;
    }
    
    /** {@inheritDoc} */
    public void setVulasConfiguration(VulasConfiguration _cfg) {
    	this.vulasConfiguration = _cfg;
    }

    /**
     * The JAR to be analyzed.
     */
    //private String appJar = null;
    private String classpath = null;
    private Iterable<Entrypoint> entrypoints = null;
    private final Set<com.sap.psr.vulas.shared.json.model.ConstructId> filteredEP = new HashSet<com.sap.psr.vulas.shared.json.model.ConstructId>();
    // represents code to be analyzed
    private AnalysisScope scope = null;
    // a class hierarchy for name resolution, etc.
    private IClassHierarchy cha = null;

    private CallGraph callgraph = null;

    private long buildTimeNano = -1;


    /** {@inheritDoc} */
    public void setAppContext(Application _ctx) {
        this.appContext = _ctx;
    }

    /** {@inheritDoc} */
    public void setAppClasspath(String _cp) {
        if (this.classpath != null) {
            this.classpath += System.getProperty("path.separator");
            this.classpath += _cp;
        } else
            this.classpath = _cp;
        WalaCallgraphConstructor.log.info("Add to wala classpath the application: [" + this.classpath + "]");
    }

    /** {@inheritDoc} */
    public void setDepClasspath(String _dependenciesClasspath) {
        if (this.classpath != null) {
            this.classpath += System.getProperty("path.separator");
            this.classpath += _dependenciesClasspath;
        } else
            this.classpath = _dependenciesClasspath;
        WalaCallgraphConstructor.log.info("Add to wala classpath the dependencies: [" + this.classpath + "]");
    }

    /**
     * {@inheritDoc}
     *
     * Filter and find all entrypoints in scope
     */
    public void setEntrypoints(Set<com.sap.psr.vulas.shared.json.model.ConstructId> _constructs) throws CallgraphConstructException {
        try {
            this.scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(this.classpath, this.excludedPackagesFile);

            // The removal of ClassHierarchy.make(AnalysisScope) was made with commit https://github.com/wala/WALA/commit/c9b1006305f3a72fa584792f1e3a47f772c0aaa6#diff-537da437552ca52fe290d9def0cb5b16
            //ClassHierarchy.make(this.scope);
            this.cha = ClassHierarchyFactory.make(this.scope);
        } catch (Exception e) {
            WalaCallgraphConstructor.log.error(e.getMessage());
            throw new IllegalStateException("Could not build class hierarchy", e);
        }
        catch (Error e){
            WalaCallgraphConstructor.log.error(e.getMessage());
            throw new CallgraphConstructException("Could not set entrypoints", e);
        }

        // If no entrypoints found to be set, set the main method as entrypoint
        if (_constructs.isEmpty()) {
            //this.entrypoints = new AllApplicationEntrypoints(this.scope, this.cha);
            this.entrypoints = Util.makeMainEntrypoints(scope, cha);
            Iterator<Entrypoint> iter = this.entrypoints.iterator();
            IMethod m = null;
            while (iter.hasNext()) {
                m = iter.next().getMethod();
                this.filteredEP.add(getCid(m));
            }
            WalaCallgraphConstructor.log.warn("No customized entrypoints found to be set; Therefore set " + this.filteredEP.size() +
                    " application [main] methods as entrypoints by default");
        } else {
            IMethod method = null;
            String method_qname = null;
            HashSet<Entrypoint> ep = new HashSet<Entrypoint>();
            HashSet<String> _constructs_qname = new HashSet<String>();
            for(com.sap.psr.vulas.shared.json.model.ConstructId cid : _constructs) {
            	
            	// Prevent IllegalArgumentException in toCoreType for non-Java app constructs
            	if(ProgrammingLanguage.JAVA.equals(cid.getLang())) {
            		
	                // Only use constructors and methods as entrypoints
            		//TODO: Also static initializers?
	                final JavaId jcid = (JavaId) JavaId.toCoreType(cid);
	                if (jcid instanceof com.sap.psr.vulas.java.JavaConstructorId || jcid instanceof com.sap.psr.vulas.java.JavaMethodId) {
	                    _constructs_qname.add(cid.getQname());
	                    this.filteredEP.add(cid);
	                }
            	}
            }
            
            for (IClass klass : cha) {
                // klass is not an interface and it's application class
                if ((!klass.isInterface()) && (cha.getScope().getApplicationLoader().equals(klass.getClassLoader().getReference()))) {
                    for (Iterator<IMethod> m_iter = klass.getDeclaredMethods().iterator(); m_iter.hasNext(); ) {
                        method = (IMethod) m_iter.next();
                        if (!method.isClinit()) {
                            method_qname = getCid(method).getQname();
                            if (!method.isAbstract() && (_constructs_qname.contains(method_qname))) {
                                ep.add(new ArgumentTypeEntrypoint(method, cha));
                            }
                        }
                    }
                }
            }

            final Iterator<Entrypoint> iter = ep.iterator();
            //Convert HashSet to Iterable
            this.entrypoints = new Iterable<Entrypoint>() {
                public Iterator<Entrypoint> iterator() {
                    return iter;
                }
            };
        }

        if (this.entrypoints.iterator().hasNext())
            WalaCallgraphConstructor.log.info("[" + this.filteredEP.size() + "] entry points set");
        else
            throw new CallgraphConstructException("No entry points could be set, which will not allow to build the call graph", null);
    }


    private static String getRefType(TypeReference _t) {
        String type = null;
        if (_t.isArrayType()) {
            if (_t.equals(TypeReference.BooleanArray)) type = "boolean[]";
            else if (_t.equals(TypeReference.ByteArray)) type = "byte[]";
            else if (_t.equals(TypeReference.CharArray)) type = "char[]";
            else if (_t.equals(TypeReference.DoubleArray)) type = "double[]";
            else if (_t.equals(TypeReference.FloatArray)) type = "float[]";
            else if (_t.equals(TypeReference.IntArray)) type = "int[]";
            else if (_t.equals(TypeReference.LongArray)) type = "long[]";
            else if (_t.equals(TypeReference.ShortArray)) type = "short[]";
            else type = _t.getName().getClassName().toString() + "[]";
        } else if (_t.isPrimitiveType()) {
            if (_t.equals(TypeReference.Boolean)) type = "boolean";
            else if (_t.equals(TypeReference.Byte)) type = "byte";
            else if (_t.equals(TypeReference.Char)) type = "char";
            else if (_t.equals(TypeReference.Double)) type = "double";
            else if (_t.equals(TypeReference.Float)) type = "float";
            else if (_t.equals(TypeReference.Int)) type = "int";
            else if (_t.equals(TypeReference.Long)) type = "long";
            else if (_t.equals(TypeReference.Short)) type = "short";
        } else if (_t.isClassType()) {
            type = _t.getName().getClassName().toString();
        } else {
            type = "other";
        }
        return type;
    }

    /**
     * Given an IMethod, identify whether it's an object constructor&lt;clinit&gt;, class initializer&lt;clinit&gt; or a method, return the ConstructId
     *
     * @param _method
     * @return
     */
    private static com.sap.psr.vulas.shared.json.model.ConstructId getCid(IMethod _method) {
        String qname = "";
        com.sap.psr.vulas.shared.json.model.ConstructId cid = null;
        if (_method.isClinit()) {
            qname = _method.getSignature().substring(0, _method.getSignature().indexOf("<") - 1);
            cid = JavaId.toSharedType(JavaId.parseClassQName(qname).getClassInit());
        } else {
            int p_size = _method.getNumberOfParameters();
            StringBuilder params = new StringBuilder("(");
            String type = "";
            if (_method.isInit()) {
                for (int i = 1; i < p_size; i++) {
                    type = getRefType(_method.getParameterType(i));
                    if (type.contains("$")) {
                        type = type.substring((type.lastIndexOf("$") + 1), type.length());
                    }
                    params.append(type);
                    if (i != p_size - 1) params.append(",");
                }
                params.append(")");
                qname = _method.getSignature().substring(0, _method.getSignature().indexOf("<") - 1) + params;
                cid = JavaId.toSharedType(JavaId.parseConstructorQName(qname));
            } else {
                for (int i = (_method.isStatic() ? 0 : 1); i < p_size; i++) {
                    type = getRefType(_method.getParameterType(i));
                    if (type.contains("$")) {
                        type = type.substring((type.lastIndexOf("$") + 1), type.length());
                    }
                    params.append(type);
                    if (i != p_size - 1) params.append(",");
                }
                params.append(")");
                qname = _method.getSignature().substring(0, _method.getSignature().indexOf("(")) + params;
                qname = ClassVisitor.removeParameterQualification(qname);
                cid = JavaId.toSharedType(JavaId.parseMethodQName(qname));
            }
        }
        return cid;
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
        return this.vulasConfiguration.getConfiguration().subset("vulas.reach.wala");
    }

    /**
     * {@inheritDoc}
     *
     * Parse command line arguments, and then build callgraph based on these properties
     */
    public void buildCallgraph(boolean _policy) throws CallgraphConstructException {
        WalaCallgraphConstructor.log.info("Starting call graph construction for " + this.appContext.toString(false));
        try {
            //String[] args = new String[]{"-appJar", this.classpath, "-cg", VulasConfiguration.getSingleton().getConfiguration().getString("vulas.reach.wala.callgraph.algorithm")};
            //CommandLine.parse(args);
            String cg_algorithm = this.vulasConfiguration.getConfiguration().getString("vulas.reach.wala.callgraph.algorithm");
            WalaCallgraphConstructor.log.info("Using algorithm [" + cg_algorithm + "], reflection option [" + this.vulasConfiguration.getConfiguration().getString("vulas.reach.wala.callgraph.reflection") + "]");
            final long start_nanos = System.nanoTime();

            // encapsulates various analysis options
            AnalysisOptions options = new AnalysisOptions(this.scope, this.entrypoints);
            options.setReflectionOptions(ReflectionOptions.valueOf(this.vulasConfiguration.getConfiguration().getString("vulas.reach.wala.callgraph.reflection")));
            // callgraph builder based on algorithm
            CallGraphBuilder<?> builder = null;

            // The removal of the default constructor AnalysisCache was removed with commit https://github.com/wala/WALA/commit/d24519e97497e24fe8e4495331a649343905694b#diff-a431691cd25b49752402611d45061d01
            final AnalysisCache cache = new AnalysisCacheImpl(); //AstIRFactory.makeDefaultFactory());

            if (cg_algorithm.equals("RTA")) {
                builder = Util.makeRTABuilder(options, cache, this.cha, this.scope);
            } else if (cg_algorithm.equals("0-CFA")) {
                builder = Util.makeZeroCFABuilder(options, cache, this.cha, this.scope);
            } else if (cg_algorithm.equals("0-ctn-CFA")) {
                builder = Util.makeZeroContainerCFABuilder(options, cache, this.cha, this.scope);
            } else if (cg_algorithm.equals("vanilla-0-1-CFA")) {
                builder = Util.makeVanillaZeroOneCFABuilder(options, cache, this.cha, this.scope);
            } else if (cg_algorithm.equals("0-1-CFA")) {
                builder = Util.makeZeroOneCFABuilder(options, cache, this.cha, this.scope);
            } else if (cg_algorithm.equals("0-1-ctn-CFA")) {
                builder = Util.makeZeroOneContainerCFABuilder(options, cache, this.cha, this.scope);
            } else {
                builder = Util.makeZeroOneCFABuilder(options, cache, this.cha, this.scope);
            }

            // Build callgraph based on options and algorithm
            try {
	            this.callgraph = builder.makeCallGraph(options, null);
	            this.buildTimeNano = System.nanoTime() - start_nanos;
	            WalaCallgraphConstructor.log.info("Call graph construction completed in " + StringUtil.nanoToFlexDurationString(this.buildTimeNano));
	            WalaCallgraphConstructor.log.info("[" + cg_algorithm + "] " + CallGraphStats.getStats(this.callgraph));
	            this.checkEntrypoints(_policy);
            }
            // Wala construction problems sometimes result in NPEs, e.g., if certain language features are not supported
            catch (NullPointerException e) {
                WalaCallgraphConstructor.log.error("NPE during call graph construction", e);
                throw new CallgraphConstructException("Cannot build call graph with framework [" + this.getFramework() + "] and algorithm [" + cg_algorithm + "]");
            }
        }
        // Just re-throw what has been created (NPE during makeCallGraph or checkEntrypoints)
        catch (CallgraphConstructException e) {
            throw e;
        }
        catch (Exception e) {
            WalaCallgraphConstructor.log.error("Error building call graph: " + e.getMessage());
            throw new CallgraphConstructException("Error building call graph", e);
        }
    }

    /**
     * check whether all entrypoints are existing in callgraph
     *
     * @throws CallgraphConstructException
     */
    private void checkEntrypoints(boolean _policy) throws CallgraphConstructException {
        HashSet<com.sap.psr.vulas.shared.json.model.ConstructId> ep_diff = new HashSet<com.sap.psr.vulas.shared.json.model.ConstructId>();
        com.sap.psr.vulas.shared.json.model.ConstructId cid = null;
        ep_diff.addAll(this.filteredEP);
        for (CGNode node : this.callgraph.getEntrypointNodes()) {
            cid = getCid(node.getMethod());
            ep_diff.remove(cid);
        }
        if (_policy && (!ep_diff.isEmpty()))
            throw new CallgraphConstructException("Strict policy applied; terminating as there are [" + ep_diff.size() + "] entry points missing in call graph", null);
        if (ep_diff.size() == this.filteredEP.size())
            throw new CallgraphConstructException("[0/" + ep_diff.size() + "] entry points found in call graph", null);

        if ((!_policy) && (!ep_diff.isEmpty())) {
            WalaCallgraphConstructor.log.warn("There should be [" + this.filteredEP.size() + "] entrypoints set; but [" +
                    ep_diff.size() + "] entrypoints are missing in the call graph");
            for (com.sap.psr.vulas.shared.json.model.ConstructId m : ep_diff)
                WalaCallgraphConstructor.log.warn("  [" + m.getQname() + "] is missing");
        } else {
            WalaCallgraphConstructor.log.info("All [" + this.filteredEP.size() + "] entrypoints exist in the call graph");
        }
    }

    /**
     * Normalizing a wala callgraph to a general graph represented by ConstructId
     *
     * @return a {@link com.ibm.wala.util.graph.Graph} object.
     */
    public Graph<com.sap.psr.vulas.shared.json.model.ConstructId> getCallgraph() {
        Graph<com.sap.psr.vulas.shared.json.model.ConstructId> graph = SlowSparseNumberedGraph.make();

        if (this.callgraph != null) {
            int edges_no = 0;
            Iterator<CGNode> nodes = this.callgraph.iterator();
            CGNode srcnode = null, tgtnode = null;
            com.sap.psr.vulas.shared.json.model.ConstructId src_cid = null, tgt_cid = null;
            Iterator<CGNode> succNodes = null;
            while (nodes.hasNext()) {
                srcnode = nodes.next();
                src_cid = getCid(srcnode.getMethod());
                graph.addNode(src_cid);
                //add edges
                succNodes = this.callgraph.getSuccNodes(srcnode);
                while (succNodes.hasNext()) {
                    tgtnode = succNodes.next();
                    tgt_cid = getCid(tgtnode.getMethod());
                    graph.addNode(tgt_cid);
                    if (!graph.hasEdge(src_cid, tgt_cid)) {
                        graph.addEdge(src_cid, tgt_cid);
                        edges_no++;
                    }
                }
            }
            WalaCallgraphConstructor.log.info("Normalized call graph has [" + graph.getNumberOfNodes() + " nodes] (with distinct ConstructId) and [" + edges_no + " edges]");
        }
        // No callgrpah exists
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
    public Set<com.sap.psr.vulas.shared.json.model.ConstructId> getEntrypoints() {
        return this.filteredEP;
    }

    /** {@inheritDoc} */
    public void setExcludePackages(String _packages) {

        // Overwrite configuration (if requested)
        if (_packages != null && !_packages.equals(""))
            this.vulasConfiguration.setProperty("vulas.reach.wala.callgraph.exclusions", _packages);

        // Get configuration (original or overwritten)
        final String buffer = this.vulasConfiguration.getConfiguration().getString("vulas.reach.wala.callgraph.exclusions");

        // Write exluded packages into file
        try {
            this.excludedPackagesFile = FileUtil.writeToTmpFile("exclusion", "txt", buffer.replaceAll(";", System.getProperty("line.separator"))).toFile();
        } catch (IOException e) {
            WalaCallgraphConstructor.log.error("Cannot create file with exlcuded Java packages: " + e.getMessage(), e);
        }

        WalaCallgraphConstructor.log.info("Set packages to be excluded [ " + buffer + " ]");
    }
}
