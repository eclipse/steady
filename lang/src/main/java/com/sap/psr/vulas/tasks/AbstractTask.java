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

public abstract class AbstractTask implements Task {
	
	private static final Log log = LogFactory.getLog(AbstractTask.class);

	private GoalClient client = null;
	
	private Application application = null;
	
	private List<Path> searchPaths = null;
	
	private Map<Path, Dependency> knownDependencies = null;
	
	protected VulasConfiguration vulasConfiguration = null;
	
	// ====================  Setter methods used to passed general context information to the task
	
	public final List<Path> getSearchPath() { return searchPaths; }

	public final boolean hasSearchPath() { return this.searchPaths!=null && !this.searchPaths.isEmpty(); }
	
	@Override
	public final void setSearchPaths(List<Path> _paths) { this.searchPaths = _paths; }

	/**
	 * Returns true if the {@link GoalClient} of this task is equal to the given client, false otherwise.
	 * @param _client
	 * @return
	 */
	public final boolean isGoalClient(GoalClient _client) { return this.client==_client; }
	
	public final boolean isOneOfGoalClients(List<GoalClient> clients) { return clients.contains(this.client); }
		
	@Override
	public final void setGoalClient(GoalClient _client) { this.client = _client; }
	
	public final Application getApplication() { return this.application; }
	
	@Override
	public final void setApplication(Application _app) { this.application = _app; }
	
	public Map<Path, Dependency> getKnownDependencies() { return this.knownDependencies; }
	
	@Override
	public void setKnownDependencies(Map<Path, Dependency> _dependencies) { this.knownDependencies = _dependencies; }
	
	// ==================== Configure, execute and clean up
	
	/**
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

	@Override
	public void cleanUp() {}
}
