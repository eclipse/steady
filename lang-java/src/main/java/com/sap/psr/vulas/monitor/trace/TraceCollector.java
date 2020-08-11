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
package com.sap.psr.vulas.monitor.trace;

import java.io.Serializable;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassPoolUpdater;
import com.sap.psr.vulas.monitor.ExecutionMonitor;
import com.sap.psr.vulas.monitor.Loader;
import com.sap.psr.vulas.monitor.LoaderHierarchy;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Offers callback methods used by the two trace instrumentors
 * {@link SingleTraceInstrumentor} and {@link StackTraceInstrumentor}.
 * Prepares and uploads trace information to the backend and triggers
 * the analysis of JAR files.
 */
public class TraceCollector {

	// STATIC MEMBERS

	private static TraceCollector instance = null;

	//private static boolean PAUSE_COLLECTION = false;

	private static Logger log = null;

	// INSTANCE MEMBERS

	/**
	 * The collected traces.
	 */
	private Queue<ConstructUsage> constructUsage = new LinkedList<ConstructUsage>();

	/**
	 * Used to transform a stacktrace into a path of construct IDs.
	 */
	private StackTraceUtil stu = null;

	/**
	 * Stacktraces observed for known vulnerabilities (more precisely: their change list elements) are transformed into a path.
	 */
	private Queue<List<PathNode>> constructUsagePaths = new LinkedList<List<PathNode>>();

	private LoaderHierarchy loaderHierarchy = null;

	private Map<String,JarAnalyzer> jarFiles = new HashMap<String,JarAnalyzer>();
	private StringList jarBlacklist = new StringList();
	private Map<String, Boolean> checkedJars = new HashMap<String, Boolean>();

	private ExecutorService pool = null;
	private int poolSize;

	// Statistics
	private long methodTraceCount = 0;
	private long constructorTraceCount = 0;
	private long clinitTraceCount = 0;
	private long methodBlacklistedCount = 0;
	private long constructorBlacklistedCount = 0;
	private long clinitBlacklistedCount = 0;

	/** Used in different upload methods, set in uploadInformarion(GoalExecution, int). */
	private AbstractGoal exe = null;
	
	/** Java Ids corresponding to classes and packages of executable constructs. */
	private Set<ConstructId> contextConstructs = new HashSet<ConstructId>();

	private TraceCollector() {
		final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();

		this.loaderHierarchy = new LoaderHierarchy();

		// Create thread pool for JAR analysis
		this.poolSize = cfg.getInt("jarAnalysis.poolSize", 4);

		// JAR blacklist (evaluated during addTrace)
		this.jarBlacklist.addAll(cfg.getStringArray(CoreConfiguration.MONI_BLACKLIST_JARS));
	}

	//======================================= STATIC METHODS

	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.monitor.trace.TraceCollector} object.
	 */
	public synchronized static TraceCollector getInstance() {
		if(TraceCollector.instance==null) {
			// Disable trace collection during the instantiation process. As we use a couple of OSS components
			// ourselves, we may end up in an endless loop and StackOverflow exceptions otherwise
			ExecutionMonitor.setPaused(true);//TraceCollector.PAUSE_COLLECTION  = true;

			TraceCollector.instance = new TraceCollector();

			// Trigger the creation of the execution monitor singleton
			ExecutionMonitor.getInstance();

			// 
			ClassPoolUpdater.getInstance();

			BackendConnector.getInstance();

			getLog().info("Completed instantiation of trace collector");

			// Now that the instance has been created, we enable trace collection again
			ExecutionMonitor.setPaused(false);//TraceCollector.PAUSE_COLLECTION = false;
		}
		return TraceCollector.instance;
	}

	private static final Logger getLog() {
		if(TraceCollector.log==null)
			TraceCollector.log = org.apache.logging.log4j.LogManager.getLogger();
		return TraceCollector.log;
	}

	/**
	 * Callback method for instrumented class methods.
	 *
	 * @param _qname the qualified name of the method or constructor instrumented, thus, performing the callback
	 * @param _archive_digest the SHA1 digest of the original JAR archive (optional, must be added to the class during offline instrumentation)
	 * @param _app_groupid the Maven group Id of the application context (optional, can be added to the class during offline instrumentation)
	 * @param _app_artifactid the Maven artifact Id of the application context (optional, see above)
	 * @param _app_version the Maven version of the application context (optional, see above)
	 * @param _class_loader a {@link java.lang.ClassLoader} object.
	 * @param _url a {@link java.net.URL} object.
	 * @param _params a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public static boolean callbackMethod(String _qname, ClassLoader _class_loader, URL _url, String _archive_digest, String _app_groupid, String _app_artifactid, String _app_version, Map<String,Serializable> _params) {
		boolean trace_collected = false;
		if(!ExecutionMonitor.isPaused()) {
			TraceCollector.getInstance().addTrace(JavaId.parseMethodQName(_qname), _class_loader, _url, _archive_digest, _app_groupid, _app_artifactid, _app_version, _params);
			trace_collected = true;
		}
		return trace_collected;
	}

	/**
	 * Callback method for instrumented class constructors.
	 *
	 * @param _qname the qualified name of the method or constructor instrumented, thus, performing the callback
	 * @param _archive_digest the SHA1 digest of the original JAR archive (optional, must be added to the class during static instrumentation)
	 * @param _app_groupid the Maven group Id of the application context (optional, can be added to the class during static instrumentation)
	 * @param _app_artifactid the Maven artifact Id of the application context (optional, see above)
	 * @param _app_version the Maven version of the application context (optional, see above)
	 * @param _class_loader a {@link java.lang.ClassLoader} object.
	 * @param _url a {@link java.net.URL} object.
	 * @param _params a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public static boolean callbackConstructor(String _qname, ClassLoader _class_loader, URL _url, String _archive_digest, String _app_groupid, String _app_artifactid, String _app_version, Map<String,Serializable> _params) {
		boolean trace_collected = false;
		if(!ExecutionMonitor.isPaused()) {
			TraceCollector.getInstance().addTrace(JavaId.parseConstructorQName(_qname), _class_loader, _url, _archive_digest, _app_groupid, _app_artifactid, _app_version, _params);
			trace_collected = true;
		}
		return trace_collected;
	}

	/**
	 * Callback method for instrumented class constructors.
	 *
	 * @param _qname the qualified name of the method or constructor instrumented, thus, performing the callback
	 * @param _archive_digest the SHA1 digest of the original JAR archive (optional, must be added to the class during static instrumentation)
	 * @param _app_groupid the Maven group Id of the application context (optional, can be added to the class during static instrumentation)
	 * @param _app_artifactid the Maven artifact Id of the application context (optional, see above)
	 * @param _app_version the Maven version of the application context (optional, see above)
	 * @param _class_loader a {@link java.lang.ClassLoader} object.
	 * @param _url a {@link java.net.URL} object.
	 * @param _params a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public static boolean callbackClinit(String _qname, ClassLoader _class_loader, URL _url, String _archive_digest, String _app_groupid, String _app_artifactid, String _app_version, Map<String,Serializable> _params) {
		boolean trace_collected = false;
		if(!ExecutionMonitor.isPaused()) {
			TraceCollector.getInstance().addTrace(JavaId.parseClassInitQName(_qname), _class_loader, _url, _archive_digest, _app_groupid, _app_artifactid, _app_version, _params);
			trace_collected = true;
		}
		return trace_collected;
	}

	//======================================= INSTANCE METHODS

	/**
	 * <p>addTrace.</p>
	 *
	 * @param _id a {@link com.sap.psr.vulas.ConstructId} object.
	 * @param _class_loader a {@link java.lang.ClassLoader} object.
	 * @param _url a {@link java.net.URL} object.
	 * @param _archive_digest a {@link java.lang.String} object.
	 * @param _app_groupid a {@link java.lang.String} object.
	 * @param _app_artifactid a {@link java.lang.String} object.
	 * @param _app_version a {@link java.lang.String} object.
	 * @param _params a {@link java.util.Map} object.
	 */
	public synchronized void addTrace(ConstructId _id, ClassLoader _class_loader, URL _url, String _archive_digest, String _app_groupid, String _app_artifactid, String _app_version, Map<String,Serializable> _params) {

		// Return right away if we already collected >= maxItems traces
		if(CoreConfiguration.isMaxItemsCollected(this.constructUsage.size()))
			return;

		// Type of the traced construct
		if(!(_id instanceof JavaId))
			throw new IllegalArgumentException("Trace collection for type [" + _id.getClass().getSimpleName() + "] not supported");
		final JavaId.Type c_type = ((JavaId)_id).getType();

		final Loader l = (_class_loader == null ? null : this.loaderHierarchy.add(_class_loader));
		final String jar_path = (_url == null ? null : FileUtil.getJARFilePath(_url.toString())); // The complete FS path pointing to the JAR
		final String jar_name = (jar_path == null ? null : FileUtil.getFileName(jar_path));

		// Ignore blacklisted JARs, cf. MONI_BLACKLIST_JARS
		boolean blacklisted_jar = false;

		// Create a new trace
		final int counter = (Integer)_params.get("counter");
		final long now = System.currentTimeMillis();
		if(counter==0) this.getLog().error("Error while reading counter: counter is null");
		final ConstructUsage u = new ConstructUsage(_id, jar_path, l, now, counter);

		// Instrumentation happened in this JVM process
		if(_archive_digest==null) {

			// If construct is part of a JAR, create an analyzer
			if(jar_name!=null) {

				// Unless the JAR is blacklisted
				if(!this.checkedJars.containsKey(jar_name))
					this.checkedJars.put(jar_name, this.jarBlacklist.contains(jar_name, StringList.ComparisonMode.PATTERN, StringList.CaseSensitivity.CASE_INSENSITIVE));
				blacklisted_jar = this.checkedJars.get(jar_name);

				if(!blacklisted_jar && !this.jarFiles.containsKey(jar_path)) {
					try {
						final JarAnalyzer ja = new JarAnalyzer();
						ja.analyze(Paths.get(jar_path).toFile());
						this.jarFiles.put(jar_path, ja);

						// Schedule JAR analysis (and create pool if necessary)
						if(this.pool==null)
							this.pool = Executors.newFixedThreadPool(this.poolSize);
						this.pool.submit(ja);
					}
					catch(FileAnalysisException e) {
						this.getLog().error("Error while reading JAR file from URL [" + jar_path + "]: " + e.getMessage());
					}
				}
			}
		}
		// Instrumentation happened outside of the current JVM process (perfect, saves resources)
		else {
			u.setArchiveDigest(_archive_digest);
			u.setArchiveFileName(jar_name);
			if(_app_groupid!=null && _app_artifactid!=null && _app_version!=null)
				u.setAppContext(new Application(_app_groupid, _app_artifactid, _app_version));
		}

		// Only add the trace if the JAR is not blacklisted
		if(!blacklisted_jar) {
			this.constructUsage.add(u);
			
			// Stats
			switch(c_type) {
				case CONSTRUCTOR: this.constructorTraceCount++; break;
				case METHOD: this.methodTraceCount++; break;
				case CLASSINIT: this.clinitTraceCount++; break;
				default: break; // Should not happen
			}
			
			// Add 1 trace for context and package (more does not seem to make any sense, there could be easily too many)
			final ConstructId ctx_id  = _id.getDefinitionContext();
			final ConstructId pack_id = ((JavaId)_id).getJavaPackageId();
			if(!this.contextConstructs.contains(ctx_id)) {
				final ConstructUsage ctx_u = new ConstructUsage(ctx_id, jar_path, l, now, 1);
				this.contextConstructs.add(ctx_id);
				this.constructUsage.add(ctx_u);
			}
			if(!this.contextConstructs.contains(pack_id)) {
				final ConstructUsage pack_u = new ConstructUsage(pack_id, jar_path, l, now, 1);
				this.contextConstructs.add(pack_id);
				this.constructUsage.add(pack_u);
			}

			// Analyze stacktrace to get path and/or junit information
			if( (Boolean.valueOf((String)_params.get("junit")) || Boolean.valueOf((String)_params.get("path"))) && _params.get("stacktrace")!=null && !c_type.equals(JavaId.Type.CLASSINIT)) {

				//  Build the path in any of the 2 cases
				this.stu = new StackTraceUtil(this.loaderHierarchy, l);
				this.stu.setStopAtJUnit(true);
				final List<PathNode> path = this.stu.transformStackTrace((StackTraceElement[])_params.get("stacktrace"), new PathNode(_id, _archive_digest));

				// Upload path?
				if(Boolean.valueOf((String)_params.get("path"))) {
					constructUsagePaths.add(path);
					this.getLog().info("Path constructed from stacktrace, length [" + path.size() + "]: entry point [" + path.get(0).getConstructId().getQualifiedName() + "], change list element [" + _id.getQualifiedName() + "]");
				}

				// Collect JUnit info?
				if(Boolean.valueOf((String)_params.get("junit"))) {
					final ConstructId junit = this.stu.getJUnitContext(path);
					if(junit!=null) {
						u.addJUnitContext(junit);
					}
				}
			}
		}
		else {
			// Stats
			switch(c_type) {
				case CONSTRUCTOR: this.constructorBlacklistedCount++; break;
				case METHOD: this.methodBlacklistedCount++; break;
				case CLASSINIT: this.clinitBlacklistedCount++; break;
				default: break; // Should not happen
			}
		}
	}

	/**
	 * <p>uploadInformation.</p>
	 *
	 * @param _exe a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
	 * @param batchSize a int.
	 */
	public synchronized void uploadInformation(AbstractGoal _exe, int batchSize) {
		this.exe = _exe;
		if(batchSize > -1){
			this.uploadPaths(10);
			this.uploadTraces(batchSize);
		}
		else{
			this.uploadPaths();
			this.uploadTraces();
		}
	}

	/**
	 * <p>awaitUpload.</p>
	 */
	public void awaitUpload() {
		if(this.pool!=null) {
			this.pool.shutdown();
			try {
				// Once we're all through, let's wait for them to finish
				while (!this.pool.awaitTermination(10, TimeUnit.SECONDS))
					this.getLog().info("Awaiting completion of archive analysis threads");
			} catch (InterruptedException e) {
				this.getLog().error("Got interruped while waiting for the completion of archive analysis threads: " + e.getMessage());
			}
		}
	}

	/**
	 * Uploads all trace information collected during JVM execution to the central collector.
	 */
	private synchronized void uploadTraces() { this.uploadTraces(-1); }

	/**
	 * Uploads trace information collected during JVM execution to the central collector.
	 * If batch size is equal to -1, all traces will be uploaded.
	 */
	private synchronized void uploadTraces(int _batch_size) {
		if(this.constructUsage.isEmpty())
			TraceCollector.getLog().info("No traces collected");
		else {
			try {
				BackendConnector.getInstance().uploadTraces(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), CoreConfiguration.getAppContext(), this.toJSON(_batch_size));
			} catch (Exception e) {
				this.getLog().error("Error while uploaded traces: " + e.getMessage());
			}
		}
	}

	/**
	 * Upload all paths gathered from stacktrace information.
	 * @return
	 */
	private synchronized void uploadPaths() { this.uploadPaths(-1); }

	/**
	 * Upload _batch_size paths gathered from stack trace information (all if _batch_size is negative).
	 *
	 * @param _batch_size a int.
	 */
	public synchronized void uploadPaths(int _batch_size) {
		// No paths collected since last call
		if(this.constructUsagePaths.isEmpty()) {
			TraceCollector.getLog().info("No paths collected");
			return;
		}

		Application app_ctx = null;
		try {
			app_ctx = CoreConfiguration.getAppContext();
		} catch (ConfigurationException e) {
			TraceCollector.getLog().error("Application context could not be determined");
			return;
		}

		TraceCollector.getLog().info(this.constructUsagePaths.size() + " paths collected");
		final StringBuilder json = new StringBuilder();

		List<PathNode> path = null;
		final HashMap<String, List<List<PathNode>>> paths_per_bug = new HashMap<String, List<List<PathNode>>>();

		// Get _batch_size paths and sort them after bugid
		ConstructId cle = null;
		int count=0;
		boolean match = false;
		while(!this.constructUsagePaths.isEmpty() && (_batch_size<0 || count++<_batch_size)) {
			path = this.constructUsagePaths.poll();

			// Get change list element (1st node)
			cle = path.get(path.size()-1).getConstructId();

			// Get the bug id for the change list element
			match = false;
			Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> change_lists = null;
			try {
				change_lists = BackendConnector.getInstance().getAppBugs(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), app_ctx);
			} catch (BackendConnectionException e) {
				TraceCollector.getLog().error("Error while reading app bugs: " + e.getMessage(), e);
				change_lists = new HashMap<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>>();						
			}
			for(Map.Entry<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> e : change_lists.entrySet()) {
				if(e.getValue().contains(ConstructId.toSharedType(cle))) {
					if(!paths_per_bug.containsKey(e.getKey()))
						paths_per_bug.put(e.getKey(), new ArrayList<List<PathNode>>());
					paths_per_bug.get(e.getKey()).add(path);
					match = true;
					TraceCollector.getLog().info("Path for bug [" + e.getKey() + "]: length " + path.size() + ", change list element " + cle);
				}
			}

			// No match? Can happen because we collect traces for all methods of a class
			if(!match)
				TraceCollector.getLog().info("No bug for path: length " + path.size() + ", change list element " + cle);
		}

		URL jar_url = null;
		String jar_path = null;
		JarAnalyzer ja = null;
		ClassPoolUpdater cpu = new ClassPoolUpdater();
		// Upload per bug (as in ReachabilityAnalyzer)
		for(Map.Entry<String, List<List<PathNode>>> e : paths_per_bug.entrySet()) {
			json.delete(0, json.length());
			json.append("[");
			int n=0;

			// Build JSON
			for(List<PathNode> path1: e.getValue()) {
				if ( (n++)>0 ) json.append(",");
				json.append("{");
				json.append("\"app\":").append(JacksonUtil.asJsonString(app_ctx)).append(",");
				json.append("\"bug\":\"").append(e.getKey()).append("\",");
				json.append("\"executionId\":\"").append(exe.getId()).append("\",");
				json.append("\"source\":\"").append(PathSource.X2C).append("\",");
				json.append("\"path\":[");
				int m=0;
				// Path node
				for(PathNode cid: path1) {
					if ( (m++)>0 ) json.append(",");
					json.append("{");
					json.append("\"constructId\":").append(cid.getConstructId().toJSON());

					// If existing, put the SHA1 of the lib from which the construct was loaded
					if(cid.hasSha1()) {
						json.append(",\"lib\":\"").append(cid.getSha1()).append("\"");
					}
					// If not existing, find it
					else {
						// 
						jar_url  = cpu.getJarResourcePath(cid.getConstructId());
						/*if(jar_url==null) {
									try {
										jar_url = new URL(((JavaId)cid.getConstructId()).getJARUrl());
									} catch (MalformedURLException e) {
										this.getLog().warn("Cannot create JAR URL: " + e.getMessage());
									}
								}*/	

						jar_path = (jar_url==null ? null : FileUtil.getJARFilePath(jar_url.toString()));
						if(jar_path!=null && this.jarFiles.containsKey(jar_path)) {
							ja = this.jarFiles.get(jar_path);
							json.append(",\"lib\":\"").append(ja.getSHA1()).append("\"");
						}
						else {
							// Print warning, if the SHA1 of a JAR cannot be determined
							if(jar_path!=null && jar_path.endsWith("jar"))
								this.getLog().warn("Library ignored: Construct " + cid.getConstructId() + " and JAR URL [" + jar_url + "]");
							json.append(",\"lib\":null");
						}
					}
					json.append("}");
				}
				json.append("]}");
			}
			json.append("]");

			// Upload JSON
			TraceCollector.getLog().info("Upload [" + e.getValue().size() + "] path(s) for bug [" + e.getKey() + "]");
			try {
				BackendConnector.getInstance().uploadPaths(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), app_ctx, json.toString());
			} catch (BackendConnectionException bce) {
				TraceCollector.getLog().error("Error while uploading paths: " + bce.getMessage(), bce);
			}
		}
	}

	private String toJSON(int _batch_size) throws ConcurrentModificationException {
		final StringBuilder b = new StringBuilder();

		// Append construct usage by apps
		String jar_path = null;
		JarAnalyzer ja = null;
		int trace_count=0;
		b.append("[");
		ConstructUsage u = null, v = null;
		Application ctx = null;
		
		// The traces to be uploaded (polled one after the other)
		final Map<ConstructUsage,ConstructUsage> traces_to_upload = new HashMap<ConstructUsage,ConstructUsage>();
		while( (_batch_size==-1 || traces_to_upload.size()<_batch_size) && !this.constructUsage.isEmpty()) {

			// Next one from queue
			u = this.constructUsage.poll();
			if(u!=null) {

				// Only upload the trace of accepted class loaders (= not filtered)
				//if(this.loaderFilter==null || this.loaderFilter.accept(u.getLoader())) {

				// Establish the app context
				ctx = u.getAppContext(); // Hard-coded through static instrumentation
				if(ctx==null) {
					try {
						ctx = CoreConfiguration.getAppContext(); // Via configuration
					} catch (ConfigurationException e) {
						log.error(e.getMessage());
					}
				}

				// Continue only if that succeeded
				if(ctx!=null && ctx.isComplete()) {
					u.setAppContext(ctx);
					u.setExecutionId(this.exe.getId());

					// Update counter if it has been prepared already
					if(traces_to_upload.containsKey(u)) {
						v = traces_to_upload.get(u);
						//ExecutionMonitor.log.info("Merge " + v.toString() + " and " + u.toString());
						v.merge(u);
						traces_to_upload.put(u,  v);
					}
					else {
						//ExecutionMonitor.log.info("Add " + u.toString());
						traces_to_upload.put(u,  u);
					}
				}
			}
		}

		// Now prepare the JSON for the selected traces
		for(ConstructUsage usage: traces_to_upload.values()) {

			try {
				// Get the URL of the JAR from which the construct has been loaded (if any)
				jar_path = usage.getResourceURL();

				// It has been loaded from a JAR, now check whether archive digest and file name are already known
				if(jar_path!=null) {

					// Yes, already known
					if(usage.getArchiveDigest()!=null && usage.getArchiveFileName()!=null) {}
					// No, we need to get it from the JAR analyzer (created in method addUsedConstruct) 
					else {

						if(this.jarFiles.containsKey(jar_path)) {
							ja = this.jarFiles.get(jar_path);

							// Yes, but could the SHA1 be computed (which seems to fail sometimes)?
							if(ja.getSHA1()!=null) {
								// Ok, let's update the trace with SHA1 and file name
								usage.setArchiveFileName(this.jarFiles.get(jar_path).getFileName());
								usage.setArchiveDigest(this.jarFiles.get(jar_path).getSHA1());
							}
							// No, SHA1 could not be computed, do add to JSON
							else
								throw new IllegalStateException("SHA1 for construct [" + usage.toString() + "] not known");
						}
						else
							throw new IllegalStateException("JAR analyzer not found for [" + jar_path + "]");
					}
				}

				// Append the trace to the JSON (hopefully all information has been completed)
				if(trace_count++>0) b.append(",");
				b.append(usage.toJSON());
			}
			catch(IllegalStateException e) {
				TraceCollector.getLog().error(e.getMessage());
				//this.constructUsage.add(u);
			}

		}
		b.append("]");

		TraceCollector.getLog().info("[" + trace_count + " traces] prepared for upload, [" + this.constructUsage.size() + " traces] remain in queue");				
		return b.toString();
	}

	/**
	 * <p>getStatistics.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,Long> getStatistics() {
		final Map<String, Long> stats = new HashMap<String,Long>();
		stats.put("archivesAnalyzed", Long.valueOf(this.jarFiles.size()));			
		stats.put("tracesCollectedMethod", this.methodTraceCount);
		stats.put("tracesCollectedConstructor", this.constructorTraceCount);
		stats.put("tracesCollectedClinit", this.clinitTraceCount);
		stats.put("tracesCollectedMethodBlacklisted", this.methodBlacklistedCount);
		stats.put("tracesCollectedConstructorBlacklisted", this.constructorBlacklistedCount);
		stats.put("tracesCollectedClinitBlacklisted", this.clinitBlacklistedCount);
		return stats;
	}
}
