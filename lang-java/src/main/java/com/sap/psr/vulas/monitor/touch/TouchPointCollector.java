package com.sap.psr.vulas.monitor.touch;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.monitor.ExecutionMonitor;
import com.sap.psr.vulas.monitor.Loader;
import com.sap.psr.vulas.monitor.LoaderHierarchy;
import com.sap.psr.vulas.monitor.trace.StackTraceUtil;
import com.sap.psr.vulas.shared.util.FileUtil;

import javassist.CtBehavior;

/**
 * <p>TouchPointCollector class.</p>
 *
 */
public class TouchPointCollector {

	// STATIC MEMBERS

	private static Log log = null;
	private static final Log getLog() {
		if(TouchPointCollector.log==null)
			TouchPointCollector.log = LogFactory.getLog(TouchPointCollector.class);
		return TouchPointCollector.log;
	}

	private static TouchPointCollector instance = null;

	private static LoaderHierarchy loaderHierarchy = null;

	//private static boolean PAUSE_COLLECTION = false;

	// INSTANCE MEMBERS

	private Set<TouchPoint> touchPoints = new HashSet<TouchPoint>();

	private Map<String,JarAnalyzer> jarAnalyzerCache = new HashMap<String,JarAnalyzer>();

	private TouchPointCollector() {
		this.loaderHierarchy = new LoaderHierarchy();
	}

	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.monitor.touch.TouchPointCollector} object.
	 */
	public synchronized static TouchPointCollector getInstance() {
		if(TouchPointCollector.instance==null) {
			// Disable trace collection during the instantiation process. As we use a couple of OSS components
			// ourselves, we may end up in an endless loop and StackOverflow exceptions otherwise
			ExecutionMonitor.setPaused(true);//TouchPointCollector.PAUSE_COLLECTION  = true;

			TouchPointCollector.instance = new TouchPointCollector();

			// Trigger the creation of the execution monitor singleton
			ExecutionMonitor.getInstance();

			// Create instances of StackTraceUtil and ConstructIdUtil, which are both called in the instrumented code
			new StackTraceUtil();
			ConstructIdUtil.getInstance();

			BackendConnector.getInstance();

			ClassVisitor.removePackageContext("a.b.c");

			TouchPointCollector.getLog().info("Completed instantiation of touch point collector");

			// Now that the instance has been created, we enable trace collection again
			ExecutionMonitor.setPaused(false); //TouchPointCollector.PAUSE_COLLECTION = false;
		}
		return TouchPointCollector.instance;
	}

	/**
	 * Adds the given touch point to the list of touch points collected during the tests. This method is called by the
	 * callback method {@link TouchPointCollector#callback(Direction, String, String, URL, ConstructId, URL, String, String, String, Map)}.
	 *
	 * @param _tp a {@link com.sap.psr.vulas.monitor.touch.TouchPointCollector.TouchPoint} object.
	 */
	public void addTouchPoint(TouchPoint _tp) { this.touchPoints.add(_tp); }

	/**
	 * <p>Getter for the field <code>touchPoints</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<TouchPoint> getTouchPoints() { return this.touchPoints; }

	/**
	 * <p>getJarAnalyzerByPath.</p>
	 *
	 * @param _jar_path a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JarAnalyzer} object.
	 */
	public JarAnalyzer getJarAnalyzerByPath(String _jar_path){
		if(!this.jarAnalyzerCache.containsKey(_jar_path)){
			try {
				final JarAnalyzer ja = new JarAnalyzer();
				ja.analyze(Paths.get(_jar_path).toFile());
				this.jarAnalyzerCache.put(_jar_path, ja);
			}
			catch(FileAnalysisException e) {
				TouchPointCollector.getLog().error("Error while reading JAR file from URL [" + _jar_path + "]: " + e.getMessage());
			}
		}
		return this.jarAnalyzerCache.get(_jar_path);
	}

	/**
	 * <p>callback.</p>
	 *
	 * @param _callee_type a {@link java.lang.String} object.
	 * @param _callee_qname a {@link java.lang.String} object.
	 * @param _class_loader a {@link java.lang.ClassLoader} object.
	 * @param _callee_url a {@link java.net.URL} object.
	 * @param callee_in_app a boolean.
	 * @param _stacktrace an array of {@link java.lang.StackTraceElement} objects.
	 * @param _app_groupid a {@link java.lang.String} object.
	 * @param _app_artifactid a {@link java.lang.String} object.
	 * @param _app_version a {@link java.lang.String} object.
	 * @param _callee_params a {@link java.util.Map} object.
	 */
	public static void callback(String _callee_type, String _callee_qname, ClassLoader _class_loader, URL _callee_url, boolean callee_in_app, StackTraceElement[] _stacktrace, String _app_groupid, String _app_artifactid, String _app_version, Map<String,Serializable> _callee_params){
		if(!ExecutionMonitor.isPaused()) {

			final TouchPointCollector tpi = TouchPointCollector.getInstance();

			// Return right away if the max number of touch points has been collected already
			if(CoreConfiguration.isMaxItemsCollected(tpi.getTouchPoints().size()))
				return;

			final ConstructIdUtil cidu = ConstructIdUtil.getInstance();

			// Extract caller from stacktrace
			final Loader l = (_class_loader == null ? null : loaderHierarchy.add(_class_loader));
			final ConstructId caller = new StackTraceUtil(loaderHierarchy, l).getPredecessorConstruct(_stacktrace);
			if(caller!=null) {

				// Get the direction of the touch point call (if any)
				TouchPointCollector.Direction tp_direction = null;
				if(cidu.isAppConstruct(caller) && !callee_in_app)
					tp_direction = TouchPointCollector.Direction.A2L;
				else if(cidu.isLibConstruct(caller) && callee_in_app)
					tp_direction = TouchPointCollector.Direction.L2A;

				if(tp_direction!=null) {
					final java.net.URL caller_url = ((com.sap.psr.vulas.java.JavaId)caller).getJarUrl();

					// Build from method args
					final ConstructId callee = ConstructIdUtil.getInstance().getConstructidFromQName(_callee_qname, _callee_type);

					// Caller and callee should be methods, constructors or clinits
					if(!ConstructIdUtil.isOfInstrumentableType(caller) || !ConstructIdUtil.isOfInstrumentableType(callee)) {
						TouchPointCollector.getLog().warn("Expected <CLINIT>, method or constructor for caller and callee, but got " + caller + " and " + callee);
					}

					// Create and collect the touch point
					else {
						final String callee_jar_path = (_callee_url == null ? null : FileUtil.getJARFilePath(_callee_url.toString()));
						JarAnalyzer callee_ja = null;
						if(callee_jar_path!=null ) callee_ja = tpi.getJarAnalyzerByPath(callee_jar_path);

						final String caller_jar_path = (caller_url == null ? null : FileUtil.getJARFilePath(caller_url.toString()));
						JarAnalyzer caller_ja = null;
						if(caller_jar_path!=null ) caller_ja = tpi.getJarAnalyzerByPath(caller_jar_path);

						final TouchPoint tp = new TouchPoint(tp_direction, caller, caller_ja, callee, _callee_params, callee_ja);

						tpi.addTouchPoint(tp);
					}
				}
			}
		}
	}

	/**
	 * The instrumentation code created by {@link TouchPointInstrumentor#instrument(JavaId, CtBehavior, ClassVisitor)} will
	 * call this callback method if it is possible to determine the caller (through stack trace analysis).
	 *
	 * @param _exe a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
	 * @param _batch_size a int.
	 */
	/*public static void callback(Direction _direction, String _callee_type, String _callee_qname, URL _callee_url, ConstructId _caller, URL _caller_url, String _app_groupid, String _app_artifactid, String _app_version, Map<String,Serializable> _callee_params){
		if(!TouchPointCollector.PAUSE_COLLECTION) {

			// Build from method args
			final ConstructId callee = ConstructIdUtil.getInstance().getConstructidFromQName(_callee_qname, _callee_type);

			// Caller and callee should be methods, constructors or clinits
			if(!ConstructIdUtil.isOfInstrumentableType(_caller) || !ConstructIdUtil.isOfInstrumentableType(callee)) {
				TouchPointCollector.getLog().warn("Expected <CLINIT, method or constructor for caller and callee, but got " + _caller + " and " + callee);
			}

			// Create and collect the touch point
			else {
				final String callee_jar_path = (_callee_url == null ? null : FileUtil.getJARFilePath(_callee_url.toString()));
				JarAnalyzer callee_ja = null;
				if(callee_jar_path!=null ) callee_ja = TouchPointCollector.getInstance().getJarAnalyzerByPath(callee_jar_path);

				final String caller_jar_path = (_caller_url == null ? null : FileUtil.getJARFilePath(_caller_url.toString()));
				JarAnalyzer caller_ja = null;
				if(caller_jar_path!=null ) caller_ja = TouchPointCollector.getInstance().getJarAnalyzerByPath(caller_jar_path);

				final TouchPoint tp = new TouchPoint(_direction, _caller, caller_ja, callee, _callee_params, callee_ja);

				TouchPointCollector.getInstance().addTouchPoint(tp);
			}
		}
	}*/
	public void uploadInformation(AbstractGoal _exe, int _batch_size) {
		if(this.touchPoints.isEmpty()) {
			TouchPointCollector.getLog().info("No touch points collected");
			return;
		} else {
			TouchPointCollector.getLog().info("Preparing a total of [" + this.touchPoints.size() + "] touch points for upload");
		}

		// Touch points per archive
		final HashMap<String, List<TouchPoint>> touchPointByArchiveId = new HashMap<String, List<TouchPoint>>();

		// Loop and group by archive
		final Iterator<TouchPoint> iter = this.touchPoints.iterator();
		while(iter.hasNext()){
			TouchPoint ase = iter.next();
			if(ase.getJarAnalyzerOfOss()!=null) {
				String archiveId = ase.getJarAnalyzerOfOss().getSHA1();
				if(!touchPointByArchiveId.containsKey(archiveId)){
					touchPointByArchiveId.put(archiveId, new LinkedList<TouchPoint>());
				}
				touchPointByArchiveId.get(archiveId).add(ase);
			}
			else {
				TouchPointCollector.getLog().warn("No JAR analyzer found for touch point " + ase);
			}
		}

		// Upload touch points for each archive
		for (Map.Entry<String, List<TouchPoint>> entry : touchPointByArchiveId.entrySet()) {
			TouchPointCollector.getLog().info("Uploading [" + entry.getValue().size() + "] touch points for archive [" + entry.getKey() + "]");

			// build attacksurface json
			JsonArray myArray = new JsonArray();
			for(TouchPoint ase : entry.getValue()) {
				myArray.add(ase.toJSON());
			}

			// Upload touch points for archiveid
			try {
				BackendConnector.getInstance().uploadTouchPoints(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), CoreConfiguration.getAppContext(), entry.getKey(), myArray.toString());
			} catch (ConfigurationException e) {
				TouchPointCollector.getLog().error(e.getMessage(), e);
			} catch (Exception e) {
				TouchPointCollector.getLog().error("Error during upload: " + e.getMessage(), e);
			}
		}
	}

	public static enum Direction { A2L, L2A };

	private static class TouchPoint {

		private Direction direction = null;

		private ConstructId caller = null;
		private ConstructId callee = null;

		private Map<String, Serializable> calleeArgs = null;

		private JarAnalyzer callerJa = null;        
		private JarAnalyzer calleeJa = null;

		private TouchPoint(Direction _direction, ConstructId _caller, JarAnalyzer _caller_ja, ConstructId _callee, Map<String, Serializable> _callee_args, JarAnalyzer _callee_ja) {
			this.direction = _direction;
			this.caller = _caller;
			this.callee = _callee;
			this.calleeArgs = _callee_args;
			this.callerJa = _caller_ja;
			this.calleeJa = _callee_ja;   
		}

		/**
		 * Returns the {@link JarAnalyzer} of the OSS that is part of the {@link TouchPoint}. Depending on the direction of the touch point,
		 * this can be the analyzer of the caller or callee.
		 * @return
		 */
		public JarAnalyzer getJarAnalyzerOfOss(){
			if(this.direction.equals(Direction.A2L)) return calleeJa;
			else return callerJa;
		}

		public JsonObject toJSON(){
			final JsonObject rootObj = new JsonObject();

			rootObj.addProperty("direction", this.direction.toString());
			rootObj.add("from", this.caller.toGSON());
			rootObj.add("to", this.callee.toGSON());

			// Add the archive SHA1 of the OSS (not needed for the application archive, if any)
			if(this.getJarAnalyzerOfOss()!=null)
				rootObj.addProperty("archiveId", this.getJarAnalyzerOfOss().getSHA1());

			// Callee arguments
			/*if(this.calleeArgs != null && this.calleeArgs.size()>0){
                final JsonArray myArray = new JsonArray();
                for(Entry<String, Serializable> entry : this.calleeArgs.entrySet()){
                    if (entry.getKey().contains("arg_value_")) {
                        String argNumber = entry.getKey().substring(entry.getKey().lastIndexOf('_')+1);
                        JsonObject jo = new JsonObject();
                        jo.addProperty(this.calleeArgs.get("arg_type_"+argNumber), entry.getValue().toString());
                        myArray.add(jo);
                    }

                }
                rootObj.add("endPointArguments", myArray);
            }*/
			rootObj.addProperty("source", "X2C");
			return rootObj;
		}

		@Override
		public String toString() {
			return "[" + this.caller.getQualifiedName() + " --> " + this.callee.getQualifiedName() + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((callee == null) ? 0 : callee.hashCode());
			result = prime * result + ((calleeJa == null) ? 0 : calleeJa.hashCode());
			result = prime * result + ((caller == null) ? 0 : caller.hashCode());
			result = prime * result + ((callerJa == null) ? 0 : callerJa.hashCode());
			result = prime * result + ((direction == null) ? 0 : direction.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TouchPoint other = (TouchPoint) obj;
			if (callee == null) {
				if (other.callee != null)
					return false;
			} else if (!callee.equals(other.callee))
				return false;
			if (calleeJa == null) {
				if (other.calleeJa != null)
					return false;
			} else if (!calleeJa.equals(other.calleeJa))
				return false;
			if (caller == null) {
				if (other.caller != null)
					return false;
			} else if (!caller.equals(other.caller))
				return false;
			if (callerJa == null) {
				if (other.callerJa != null)
					return false;
			} else if (!callerJa.equals(other.callerJa))
				return false;
			if (direction != other.direction)
				return false;
			return true;
		}
	}
}
