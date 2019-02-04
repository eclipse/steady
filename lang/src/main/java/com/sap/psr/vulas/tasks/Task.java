package com.sap.psr.vulas.tasks;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public interface Task {
	
	// ====================  Setter methods used to passed general context information to the task
	
	/**
	 * Sets the {@link GoalClient} in whose context the task is executed.
	 */
	public void setGoalClient(GoalClient _client);
	
	/**
	 * Sets the {@link Application} in whose context the task is executed.
	 */
	public void setApplication(Application _app);
	
	/**
	 * Sets one or more file systems {@link Path}s that contain application code and/or dependencies.
	 */
	public void setSearchPaths(List<Path> _paths);
	
	/**
	 * Provides the task with application {@link Dependency}s that are already known before task execution.
	 * This information may come from build systems or the like, and typically facilitates the task execution.
	 */
	public void setKnownDependencies(Map<Path, Dependency> _known_dependencies);
	
	// ==================== Configure, execute and clean up
	
	/**
	 * Called prior to {@link Task#execute()}.
	 * @throws GoalConfigurationException
	 */
	public void configure(VulasConfiguration _cfg) throws GoalConfigurationException;
	
	/**
	 * Performs the actual application analysis.
	 * Right after, task-specific methods can be used to retrieve the analysis results. 
	 * @throws GoalExecutionException
	 */
	public void execute() throws GoalExecutionException;
	
	/**
	 * Called after {@link Task#execute()}.
	 */
	public void cleanUp();
	
	// ====================  Getter methods are mostly task-specific and exist in sub-interfaces
	
	/**
	 * The analysis of an application is typically dependent on its {@link ProgrammingLanguage}s.
	 * This method returns the {@link ProgrammingLanguage}s covered by the {@link Task}.
	 * @return
	 */
	public Set<ProgrammingLanguage> getLanguage();
}
