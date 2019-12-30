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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassPoolUpdater;
import com.sap.psr.vulas.monitor.ClassVisitor;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;


/**
 * A general call graph representation for both wala and soot framework.
 * Usage: Graph path computation; rendering/visualization
 */
public class Callgraph {

	private static final Log log = LogFactory.getLog(Callgraph.class);

	private int nodeCount = 0;
	private int edgeCount = 0;

	private Graph<Integer> idgraph = SlowSparseNumberedGraph.make();
	/**
	 * <p>getGraph.</p>
	 *
	 * @return a {@link com.ibm.wala.util.graph.Graph} object.
	 */
	public Graph<Integer> getGraph() {
		return this.idgraph;
	}

	/**
	 * Maps all callgraph constructs to integers (useful when wanting to have the unique integer ID for a given construct).
	 * The IDs are equivalant to the ones in nodeId.
	 * @see Callgraph#nodeId
	 */
	private final HashMap<com.sap.psr.vulas.shared.json.model.ConstructId, Integer> nodeMap = new HashMap<com.sap.psr.vulas.shared.json.model.ConstructId, Integer>();

	/**
	 * Maps all callgraph metaInformation to integers (useful when wanting to have the unique integer ID for a given construct).
	 * The IDs are equivalant to the ones in nodeId.
	 * @see Callgraph#nodeId
	 */
	private final HashMap<Integer, NodeMetaInformation> nodeInfoMap = new HashMap<Integer, NodeMetaInformation>();

	/**
	 * For each URL contains a JarAnalyzer Object. This allows us to get every
	 * information about the JAR of each node without the need to create
	 * an instance of JarAnalyzer for all of them. Every node is a method so 
	 * most of them share the JAR with others methods
	 */
	private final HashMap<URL, JarAnalyzer> jarAnalyzersCache = new HashMap<URL, JarAnalyzer>();

	/**
	 * Cache of JAR URLs for given construct Ids.
	 */
	private final Map<JavaId,URL> cachedJarUrls = new HashMap<JavaId,URL>();

	/**
	 * Constructs for which no JAR URL information can be obtained.
	 */
	private final Set<com.sap.psr.vulas.shared.json.model.ConstructId> constructsWithoutJarUrl = new HashSet<com.sap.psr.vulas.shared.json.model.ConstructId>();

	/**
	 * Returns the unique integer ID of the given construct in the context of this callgraph, or -1 if no such identifier exists.
	 *
	 * @param _c a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @return the unique integer ID of the construct
	 */
	public int getIdForConstruct(com.sap.psr.vulas.shared.json.model.ConstructId _c) {
		final Integer i = this.nodeMap.get(_c);
		if(i==null) return -1;
		else return i.intValue();
	}

	/**
	 * Returns true if the given construct exists in this callgraph, false otherwise.
	 *
	 * @param _c a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @return a boolean.
	 */
	public boolean existsInCallgraph(com.sap.psr.vulas.shared.json.model.ConstructId _c) {
		return this.getIdForConstruct(_c)!=-1;
	}

	/**
	 * Contains all callgraph constructs (useful when wanting a construct for a given integer ID).
	 * The IDs are equivalant to the ones in nodeMap.
	 * @see Callgraph#nodeMap
	 */
	private final ArrayList<com.sap.psr.vulas.shared.json.model.ConstructId> nodeId = new ArrayList<com.sap.psr.vulas.shared.json.model.ConstructId>();
	/**
	 * <p>Getter for the field <code>nodeId</code>.</p>
	 *
	 * @return a {@link java.util.ArrayList} object.
	 */
	public ArrayList<com.sap.psr.vulas.shared.json.model.ConstructId> getNodeId() { return this.nodeId; }
	/**
	 * <p>getConstructForId.</p>
	 *
	 * @param _id a int.
	 * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 */
	public com.sap.psr.vulas.shared.json.model.ConstructId getConstructForId(int _id) { return this.nodeId.get(_id); }    

	/**
	 * Creates a callgraph and populates an internal map of constructs and integer indices (which allow faster processing and more compact representations).
	 *
	 * @param _g a {@link com.ibm.wala.util.graph.Graph} object.
	 */
	public Callgraph (Graph<com.sap.psr.vulas.shared.json.model.ConstructId> _g) {
		if( _g!=null ) {
			
			// The problems described in Jira ticket VULAS-1429 look as if the caches survive A2C executions on different modules. Check and clear explicitly.
			if(!this.cachedJarUrls.isEmpty()) {
				log.warn("JAR URL cache not empty, clearing now...");
				this.cachedJarUrls.clear();
			}
			if(!this.jarAnalyzersCache.isEmpty()) {
				log.warn("JarAnalyzer cache not empty, clearing now ...");
				this.jarAnalyzersCache.clear();
			}
			
			Iterator<com.sap.psr.vulas.shared.json.model.ConstructId> iter = _g.iterator();
			com.sap.psr.vulas.shared.json.model.ConstructId src_node = null, tgt_node = null;
			Iterator<com.sap.psr.vulas.shared.json.model.ConstructId> succNodes = null;
			Integer src_id = null, tgt_id = null, count = -1;

			// Populate the map of constructs and integers
			while (iter.hasNext()) {
				this.nodeCount++;
				src_node = iter.next();
				src_id = this.nodeMap.get(src_node);
				if(src_id == null) {
					src_id = ++count;
					this.nodeMap.put(src_node, src_id);
					this.nodeInfoMap.put(src_id, this.createNodeMetaInformation(src_node, src_id));
					this.nodeId.add(src_node);
					this.idgraph.addNode(src_id);
				}
				//targets
				succNodes = _g.getSuccNodes(src_node);
				while(succNodes.hasNext()){
					this.edgeCount++;
					tgt_node = succNodes.next();
					tgt_id = this.nodeMap.get(tgt_node);
					if(tgt_id == null) {
						tgt_id = ++count;
						this.nodeMap.put(tgt_node, tgt_id);
						this.nodeInfoMap.put(tgt_id, this.createNodeMetaInformation(tgt_node, tgt_id));
						this.nodeId.add(tgt_node);
						this.idgraph.addNode(tgt_id);
					}
					//add edges
					this.idgraph.addEdge(src_id, tgt_id);
				}
			}
			Callgraph.log.info("Built Graph<Integer> of " + this.idgraph.getNumberOfNodes() + " nodes");
		}
	}

	/** 
	 * Given a {@link ConstructId} and its ID (look {@link Callgraph#nodeId}) return
	 * the NodeMetaInformation object that represent this node.
	 * @param target
	 * @param target_id
	 * @return 
	 */
	private NodeMetaInformation createNodeMetaInformation(com.sap.psr.vulas.shared.json.model.ConstructId target, Integer target_id){
		URL jar_url = this.collectArchiveInformation(target);
		String archiveID = null;
		if(jar_url!=null && jar_url.toString().startsWith("file:")) {
			archiveID = this.getShaFromCachedJarAnalyzer(jar_url);
		}
		else {
			jar_url = null;
		}
		return new NodeMetaInformation(target, this.parseNonStaticInnerClassConstruct(target), jar_url, archiveID);
	}

	/**
	 * <p>getInformationForConstructId.</p>
	 *
	 * @param target a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @return a {@link com.sap.psr.vulas.cg.NodeMetaInformation} object.
	 */
	public NodeMetaInformation getInformationForConstructId(com.sap.psr.vulas.shared.json.model.ConstructId target){
		return this.nodeInfoMap.get(this.getIdForConstruct(target));
	}

	/**
	 * <p>Getter for the field <code>constructsWithoutJarUrl</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<com.sap.psr.vulas.shared.json.model.ConstructId> getConstructsWithoutJarUrl() {
		return constructsWithoutJarUrl;
	}

	/**
	 * <p>getInformationForId.</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 * @return a {@link com.sap.psr.vulas.cg.NodeMetaInformation} object.
	 */
	public NodeMetaInformation getInformationForId(Integer id){
		return this.nodeInfoMap.get(id);
	}

	/**
	 * Returns the number of nodes in this graph.
	 *
	 * @see #getEdgeCount()
	 * @return the number of nodes in this graph
	 */
	public int getNodeCount() { return this.nodeCount; }

	/**
	 * Returns the number of edges in this graph.
	 *
	 * @see #getNodeCount()
	 * @return the number of edges in this graph
	 */
	public int getEdgeCount() { return this.edgeCount; }

	/**
	 * Given a target (changes), compute the shortest distance from all nodes to this target
	 *
	 * @param _tgt a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<com.sap.psr.vulas.shared.json.model.ConstructId, Integer> getDist (com.sap.psr.vulas.shared.json.model.ConstructId _tgt) {

		// Initialize the distances map, whereby the distance from source to source is 0,
		// from all other nodes to source is Int.max (= infinite by initialization)
		Map<Integer, Integer> dist = new HashMap<Integer, Integer>();
		final int tgt_id = this.nodeMap.get(_tgt);
		final Iterator<Integer> nodes = this.idgraph.iterator();
		while (nodes.hasNext()) {
			final int node = nodes.next();
			if(node == tgt_id)
				dist.put(node, 0);
			else
				dist.put(node, Integer.MAX_VALUE);
		}

		// Now compute all distances
		dist = this.computeDist(tgt_id, dist);

		// Map to be returned: ConstructId->Integer
		final Map<com.sap.psr.vulas.shared.json.model.ConstructId, Integer> result = new HashMap<com.sap.psr.vulas.shared.json.model.ConstructId, Integer>();
		for(Map.Entry<Integer, Integer> entry : dist.entrySet())
			result.put(this.nodeId.get(entry.getKey()), entry.getValue());

		return result;
	}

	/**
	 * Recursive method of getDist, updating the Map _dist
	 * @param _tgt
	 * @param _dist
	 * @return
	 */
	private Map<Integer, Integer> computeDist(Integer _tgt, Map<Integer, Integer> _dist) {

		// For all predecessor nodes of _tgt, distance to the original target plus one
		final int distance = _dist.get(_tgt) + 1;

		// Loop all predecessor nodes of _tgt
		final Iterator<Integer> pred_nodes = this.idgraph.getPredNodes(_tgt);
		while (pred_nodes.hasNext()) {
			final Integer prednode = pred_nodes.next();

			// Only update _dist when the distance becomes shorter (-1 = infinite)
			// HP, 29.11: Changed from -1 for infinite to Integer.MAX_VALUE, which safes one comparison
			if (_dist.get(prednode) > distance) {					
				_dist.put(prednode, distance);
				_dist = this.computeDist(prednode, _dist);
			}	
		}
		return _dist;
	}

	/**
	 * Given a target construct (e.g., a change list element of a security patch), the method computes the shortest
	 * path from all callgraph nodes to this target (if any).
	 * The method internally performs a back search, starting from the target node.
	 *
	 * @param _tgt a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @param _stop_if_path_found a {@link java.util.Set} object.
	 * @return a map of shortest paths from constructs (= keys of the map) to the target node
	 * @see Callgraph#computeShortestPath(Integer, Map, Set)
	 */
	public Map<com.sap.psr.vulas.shared.json.model.ConstructId, LinkedList<Integer>> getShortestPath(com.sap.psr.vulas.shared.json.model.ConstructId _tgt, final Set<com.sap.psr.vulas.shared.json.model.ConstructId> _stop_if_path_found) {

		// Get the integer index for the target construct to be reached
		final int tgt_id = this.nodeMap.get(_tgt);

		// Initialize the shortest path map (which maps integers to constructs), whereby
		// the path from target to target is empty (size of linked list = 0), from all other nodes to target is null (= no path) 
		Map<Integer, LinkedList<Integer>> path = new HashMap<Integer, LinkedList<Integer>>();
		final Iterator<Integer> nodes = this.idgraph.iterator();
		while (nodes.hasNext()) {
			final int node = nodes.next();
			if(node == tgt_id)
				path.put(node, new LinkedList<Integer>());
			else
				path.put(node, null);
		}

		// Convert the set of search stops
		Set<Integer> stop_nodes = null;
		if(_stop_if_path_found!=null) {
			stop_nodes = new HashSet<Integer>();
			for(com.sap.psr.vulas.shared.json.model.ConstructId cid: _stop_if_path_found) {
				final Integer node_id = this.nodeMap.get(cid);
				if(node_id!=null)
					stop_nodes.add(node_id);
			}
		}

		// Compute the shortest path to target, thereby updating the path map
		path = this.computeShortestPath(tgt_id, path, stop_nodes);

		// Create the return map (which maps constructs to integers), thereby inverting the shortest path map used above
		final Map<com.sap.psr.vulas.shared.json.model.ConstructId, LinkedList<Integer>> result = new HashMap<com.sap.psr.vulas.shared.json.model.ConstructId, LinkedList<Integer>>();
		for(Map.Entry<Integer, LinkedList<Integer>> entry : path.entrySet())
			result.put(this.nodeId.get(entry.getKey()), entry.getValue());
		return result;
	}

	/**
	 * Recursive method of getShortestPath, updating the Map _path
	 * @param _tgt
	 * @param _paths
	 * @return
	 */
	private Map<Integer, LinkedList<Integer>> computeShortestPath(Integer _tgt, Map<Integer, LinkedList<Integer>> _paths, final Set<Integer> _stop_nodes) {

		final LinkedList<Integer> new_path = new LinkedList<Integer>();
		new_path.addAll(_paths.get(_tgt));
		// Create the new path by adding the current target node (i.e. _tgt)
		new_path.add(_tgt);

		final int new_path_length = new_path.size();

		final Iterator<Integer> pred_nodes = this.idgraph.getPredNodes(_tgt);
		while (pred_nodes.hasNext()) {
			final int prednode = pred_nodes.next();

			// Only update when there is no path or when the path becomes shorter 
			if ( _paths.get(prednode) == null || (_paths.get(prednode).size() > new_path_length) ) {					
				_paths.put(prednode, new_path);

				// Only continue searching if there are no stop nodes, or none of them has been reached yet
				if(_stop_nodes==null || !this.existsPath(_paths, _stop_nodes))
					_paths = this.computeShortestPath(prednode, _paths, _stop_nodes);
			}


		}
		return _paths;
	}

	/**
	 * Returns true if there exists a path with length > 0 for at least one of the nodes.
	 * 
	 * @param _paths
	 * @param _nodes
	 * @return
	 */
	private final boolean existsPath(Map<Integer, LinkedList<Integer>> _paths, final Set<Integer> _nodes) {
		for(Integer n: _nodes) {
			if(_paths.containsKey(n) && _paths.get(n)!=null && _paths.get(n).size()>0) {
				return true;
			}
		}
		return false;
	}

	private synchronized URL collectArchiveInformation(com.sap.psr.vulas.shared.json.model.ConstructId tgt_node) {
		URL url = null;
		final JavaId jid = ((JavaId)JavaId.toCoreType(tgt_node)).getCompilationUnit();

		// We should always have a Java ID, since packages are not part of the callgraph
		if(jid!=null) {
			// Not in cache -> put in cache
			if(!this.cachedJarUrls.containsKey(jid)) {
				url = jid.getJarUrl();
				this.cachedJarUrls.put(jid,  url);

				// Warn if we do not find a URL
				if(url==null)
					constructsWithoutJarUrl.add(tgt_node);
			}
			// Read from cache
			url = this.cachedJarUrls.get(jid);
		}

		return url;
	}

	/**
	 * This method parse a construct, if is the constructor of a NON static
	 * inner class that take as first argument a reference to the outer class
	 * it will be deleted from the argument list (is added by the compiler).
	 * 
	 * If the ConstructId is not a constructor OR if is not of an inner class
	 * OR if there are no reason to modify this constructor the method return null
	 * 
	 * @param target the {@link ConstructId} to be examined
	 * @return the {@link ConstructId} modified or null if is nothing needs to be changed
	 */
	private com.sap.psr.vulas.shared.json.model.ConstructId parseNonStaticInnerClassConstruct(com.sap.psr.vulas.shared.json.model.ConstructId target) {
		// If is a construct we should check for the constructs modified by the compiler
		// so if is a non-static inner class constructor that has as first arguments the
		// parent class we should change it and delete the first arg.
		// Skip the first constructor parameter (added by the compiler for non-static classes)?
		// For nested classes, get the declaring (outer) class: It is used to skip the first argument in non-static inner classes
		
		final JavaId target_jid = (JavaId)JavaId.toCoreType(target);
		final JavaId comp_unit = target_jid.getCompilationUnit();
		final boolean is_nested_class = comp_unit instanceof JavaClassId && ((JavaClassId)comp_unit).isNestedClass();
		
		if(target_jid instanceof com.sap.psr.vulas.java.JavaConstructorId && is_nested_class) {
			CtClass declaringClass = null;
			CtClass clazz = null;
			try {
				ClassPool cp = ClassPoolUpdater.getInstance().getCustomClassPool();
				if(cp==null)cp=ClassPool.getDefault();
				clazz = cp.get(comp_unit.getQualifiedName());
				declaringClass = clazz.getDeclaringClass();
				final String param_to_skip = ( declaringClass!=null && !Modifier.isStatic(clazz.getModifiers()) ? ClassVisitor.removePackageContext(declaringClass.getName()) : null );
				com.sap.psr.vulas.shared.json.model.ConstructId result = JavaId.toSharedType(JavaId.parseConstructorQName(comp_unit.getType(), ClassVisitor.removeParameterQualification(target.getQname()), param_to_skip));
				return result.getQname().contentEquals(target.getQname())? null : result;
			} catch (NotFoundException e) {
				// means that we cannot find declaringClass or the class itself
				return null;
			}
		}
		else{
			// is not nested ==> we dont need to process it
			return null;
		}
	}

	private synchronized String getShaFromCachedJarAnalyzer(URL _jar_url) {

		// Build the JarAnalyzer (if possible) and put it into the cache
		if(!this.jarAnalyzersCache.containsKey(_jar_url)) {
			JarAnalyzer ja = null;
			try {
				final URI uri = _jar_url.toURI();
				final File file = Paths.get(uri).toFile(); 
				ja = new JarAnalyzer();
				ja.analyze(file);
			} catch (InvalidPathException ex) {
				log.error("Invalid path [" + _jar_url + "]: " + ex.getMessage(), ex);
			} catch (FileAnalysisException ex) {
				log.error("Error analyzing the JAR at [" + _jar_url + "]: " + ex.getMessage(), ex);
			} catch(java.nio.file.FileSystemNotFoundException fsnfe) {
				log.error("File system not found for [" + _jar_url + "]: " + fsnfe.getMessage(), fsnfe);
			} catch (URISyntaxException e) {
				log.error("URI syntax exception for [" + _jar_url + "]: " + e.getMessage(), e);
			}
			this.jarAnalyzersCache.put(_jar_url, ja); // ja can be null
		}

		// Return the digest or null (if the JarAnalyzer could not be built)
		final JarAnalyzer ja = this.jarAnalyzersCache.get(_jar_url);
		if(ja!=null)
			return ja.getSHA1();
		else
			return null;
	}
}
