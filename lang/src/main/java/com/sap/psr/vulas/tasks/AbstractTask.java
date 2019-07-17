package com.sap.psr.vulas.tasks;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Abstract AbstractTask class.</p>
 *
 */
public abstract class AbstractTask implements Task {
	
	private static final Log log = LogFactory.getLog(AbstractTask.class);

	private GoalClient client = null;
	
	private Application application = null;
	
	private List<Path> searchPaths = null;
	
	private Map<Path, Dependency> knownDependencies = null;
	
	protected VulasConfiguration vulasConfiguration = null;
	
	// ====================  Setter methods used to passed general context information to the task
	
	/**
	 * <p>getSearchPath.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public final List<Path> getSearchPath() { return searchPaths; }

	/**
	 * <p>hasSearchPath.</p>
	 *
	 * @return a boolean.
	 */
	public final boolean hasSearchPath() { return this.searchPaths!=null && !this.searchPaths.isEmpty(); }
	
	/** {@inheritDoc} */
	@Override
	public final void setSearchPaths(List<Path> _paths) { this.searchPaths = _paths; }

	/**
	 * Returns true if the {@link GoalClient} of this task is equal to the given client, false otherwise.
	 *
	 * @param _client a {@link com.sap.psr.vulas.shared.enums.GoalClient} object.
	 * @return a boolean.
	 */
	public final boolean isGoalClient(GoalClient _client) { return this.client==_client; }
	
	/**
	 * <p>isOneOfGoalClients.</p>
	 *
	 * @param clients a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public final boolean isOneOfGoalClients(List<GoalClient> clients) { return clients.contains(this.client); }
		
	/** {@inheritDoc} */
	@Override
	public final void setGoalClient(GoalClient _client) { this.client = _client; }
	
	/**
	 * <p>Getter for the field <code>application</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 */
	public final Application getApplication() { return this.application; }
	
	/** {@inheritDoc} */
	@Override
	public final void setApplication(Application _app) { this.application = _app; }
	
	/**
	 * <p>Getter for the field <code>knownDependencies</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Path, Dependency> getKnownDependencies() { return this.knownDependencies; }
	
	/** {@inheritDoc} */
	@Override
	public void setKnownDependencies(Map<Path, Dependency> _dependencies) { this.knownDependencies = _dependencies; }
	
	// ==================== Configure, execute and clean up
	
	/**
	 * {@inheritDoc}
	 *
	 * Checks the search {@link Path} and {@link GoalClient}.
	 */
	@Override
	public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
		this.vulasConfiguration = _cfg;
		if(!this.hasSearchPath())
			log.warn("Task " + this + ": No search path specified");
		if(this.client==null)
			log.warn("Task " + this + ": No goal client specified");
	}

	/** {@inheritDoc} */
	@Override
	public void cleanUp() {}
}
