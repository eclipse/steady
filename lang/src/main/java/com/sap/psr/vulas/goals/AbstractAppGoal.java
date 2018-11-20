package com.sap.psr.vulas.goals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Represents an analysis goal executed in the context of a given {@link Application}.
 * It deals particularly with application code and dependencies.
 *
 */
public abstract class AbstractAppGoal extends AbstractGoal {

	private static final Log log = LogFactory.getLog(AbstractAppGoal.class);
	
	/**
	 * Maps file system paths to {@link Dependency}s.
	 */
	private Map<Path, Dependency> knownDependencies = new HashMap<Path, Dependency>();

	private List<Path> searchPaths = new ArrayList<Path>();

	protected AbstractAppGoal(GoalType _type) { super(_type); }
	
	protected Application getApplicationContext() {
		return this.getGoalContext().getApplication();
	}
	
	/**
	 * Returns known {@link Dependency}s.
	 * 
	 * @see {@link AbstractAppGoal#setKnownDependencies(Map)}
	 * @param _paths
	 */
	public Map<Path, Dependency> getKnownDependencies() {
		return this.knownDependencies;
	}
	
	/**
	 * Sets known {@link Dependency}s.
	 * 
	 * Typically called by Vulas plugins for build tools (e.g., the Vulas Maven plugin),
	 * where app dependencies are described by project meta information (e.g., the pom.xml file).
	 * 
	 * @param _paths
	 */
	public void setKnownDependencies(Map<Path, Dependency> _paths) {
		this.knownDependencies = _paths;
	}
	
	/**
	 * Checks whether one or more {@link Path}s with application constructs, and one or more {@link Path}s
	 * with dependencies are available.
	 */
	@Override
	protected void prepareExecution() throws GoalConfigurationException {

		super.prepareExecution();

		// Ensure presence of application in goal context
		if(this.getApplicationContext()==null)
			throw new GoalConfigurationException("Application context is required to execute goal [" + this.getGoalType().toString() + "]");
			
		try {
			// Check path(s) with app constructs
			this.addAppPaths(FileUtil.getPaths(this.getConfiguration().getStringArray(CoreConfiguration.APP_DIRS, null)));

			// Warn if there's no app path
			if(!this.hasAppPaths())
				log.warn("No search path(s) provided");

			// Extract single WAR file
			//if(this.appPaths.size()==1 && FileUtil.hasFileExtension(this.appPaths.get(0), WAR_EXT)) {
			//this.handleAppWars();
			//}
			
			// Upload before actual analysis
			final boolean created = this.upload();
			if(!created)
				throw new GoalConfigurationException("Upload of goal execution failed, aborting the goal execution...");
		}
		// Thrown by all methods related to updating/adding paths
		catch (IllegalArgumentException e) {
			throw new GoalConfigurationException(e.getMessage());
		}
	}

	public List<Path> getAppPaths() { return this.searchPaths; }

	public void addAppPath(Path _p) throws IllegalArgumentException {
		if(!FileUtil.isAccessibleDirectory(_p) && !FileUtil.isAccessibleFile(_p))
			log.warn("[" + _p + "] is not an accessible file or directory");
		else if(this.getAppPaths().contains(_p))
			log.debug("[" + _p + "] is already part of application paths, and will not be added another time");
		else
			this.searchPaths.add(_p);

	}

	public void addAppPaths(Set<Path> _paths) throws IllegalArgumentException {
		for(Path p: _paths)
			this.addAppPath(p);
	}

	public boolean hasAppPaths() { return this.getAppPaths()!=null && !this.getAppPaths().isEmpty(); }

	/**
	 * Loops over all app paths, extracts all WARs (if any), and adds their content
	 * to the app and dep paths.
	 */
	/*private void handleAppWars() {
		for(int i=0; i<this.appPaths.size(); i++) {
			final Path p = this.appPaths.get(i);
			if(FileUtil.hasFileExtension(p, WAR_EXT)) {
				try {
					this.extractAppWar(p);
					this.appPaths.remove(i); // Remove only if extraction was successful
				} catch (IOException e) {
					log.error("Error while extracting WAR [" + p + "]: " + e.getMessage(), e);
				}
			}
		}
	}*/
}