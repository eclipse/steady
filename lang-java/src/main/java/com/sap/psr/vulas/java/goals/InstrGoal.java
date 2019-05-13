package com.sap.psr.vulas.java.goals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractAppGoal;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.java.JarAnalysisManager;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.FileSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.ThreadUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class InstrGoal extends AbstractAppGoal {

	private static final Log log = LogFactory.getLog(InstrGoal.class);

	private Path libPath = null;

	private Path inclPath = null;

	private Path targetPath = null;
	
	private Set<Path> instrPaths = new HashSet<Path>();

	public InstrGoal() { super(GoalType.INSTR); }

	public Path getLibPath() { return this.libPath; }

	public void setLibPath(Path _p) throws IllegalArgumentException {
		if(FileUtil.isAccessibleDirectory(_p) || FileUtil.isAccessibleFile(_p))
			this.libPath = _p;
	}

	public boolean hasLibPath() { return this.getLibPath()!=null; }

	public Path getInclPath() { return this.inclPath; }

	public void setInclPath(Path _p) throws IllegalArgumentException {
		if(FileUtil.isAccessibleDirectory(_p) || FileUtil.isAccessibleFile(_p))
			this.inclPath = _p;
	}

	public boolean hasInclPath() { return this.getInclPath()!=null; }

	public Path getTargetPath() { return this.targetPath; }

	public void setTargetPath(Path _p) throws IllegalArgumentException {
		this.targetPath = _p;
	}

	public boolean hasTargetPath() { return this.getTargetPath()!=null; }
	
	public Set<Path> getInstrPaths() { return this.instrPaths; }
	
	public void addInstrPath(Path _p) throws IllegalArgumentException {
		if(!FileUtil.isAccessibleDirectory(_p) && !FileUtil.isAccessibleFile(_p))
			log.warn("[" + _p + "] is not an accessible file or directory");
		else if(this.getInstrPaths().contains(_p))
			log.debug("[" + _p + "] is already part of intrumentation paths, and will not be added another time");
		else
			this.instrPaths.add(_p);

	}

	public void addInstrPaths(Set<Path> _paths) throws IllegalArgumentException {
		for(Path p: _paths)
			this.addInstrPath(p);
	}

	public boolean hasInstrPaths() { return this.getInstrPaths()!=null && !this.getInstrPaths().isEmpty(); }

	/**
	 * Checks whether one or more {@link Path}s with application constructs, and one or more {@link Path}s
	 * with dependencies are available.
	 */
	@Override
	protected void prepareExecution() throws GoalConfigurationException {

		super.prepareExecution();

		try {
			// Lib path
			this.setLibPath(FileUtil.getPath(this.getConfiguration().getConfiguration().getString(CoreConfiguration.INSTR_LIB_DIR, null)));

			// Warn if there's no lib path
			if(!this.hasLibPath())
				log.warn("No library path");

			// Include path
			this.setInclPath(FileUtil.getPath(this.getConfiguration().getConfiguration().getString(CoreConfiguration.INSTR_INCLUDE_DIR, null)));

			// Warn if there's no include path
			if(!this.hasInclPath())
				log.warn("No path with to-be-included JAR files");

			// Where the instrumented archives will be written to
			this.setTargetPath(FileUtil.getPath(this.getConfiguration().getConfiguration().getString(CoreConfiguration.INSTR_TARGET_DIR, null), true));

			// Warn if there's no target path
			if(!this.hasTargetPath()) {
				this.setTargetPath(FileUtil.createTmpDir("instr"));
				log.warn("No target path specified, using [" + this.getTargetPath() + "]");
			}
			
			// Instrumentation paths?
			this.addInstrPaths(FileUtil.getPaths(this.getConfiguration().getStringArray(CoreConfiguration.INSTR_SRC_DIR, null)));

			// Warn if there's no app path
			if(!this.hasInstrPaths()) {
				log.warn("No path(s) with instrumentation sources, take application paths instead");
				final Set<Path> paths = new HashSet<Path>();
				paths.addAll(this.getAppPaths());
				this.addInstrPaths(paths);
			}			
		}
		// Thrown by all methods related to updating/adding paths
		catch (IllegalArgumentException e) {
			throw new GoalConfigurationException(e.getMessage());
		}
		catch (IOException ioe) {
			throw new GoalConfigurationException(ioe.getMessage());
		}
	}

	@Override
	protected void executeTasks() throws Exception {
		final Application app = this.getApplicationContext();

		//TODO: Check how to use packaging information from the Maven plugin

		final long timeout   = this.getConfiguration().getConfiguration().getLong(CoreConfiguration.JAR_TIMEOUT, -1);
		final int no_threads = ThreadUtil.getNoThreads(this.getConfiguration(), 2);
		
		final JarAnalysisManager mgr = new JarAnalysisManager(no_threads, timeout, true, app);
		mgr.setRename(true);

		// Set the lib, include and work directories (if any)
		//if(this.inclPathsFileUtil.isAccessibleDirectory(includeDir) && this.app.getPackaging().toLowerCase().equals("war"))
		if(this.hasInclPath())
			mgr.setIncludeDir(this.inclPath);

		if(this.hasLibPath())
			mgr.setLibDir(this.getLibPath());

		mgr.setWorkDir(this.getTargetPath(), true);

		// Search source archives and go
		final FileSearch vis = new FileSearch(AbstractGoal.JAR_WAR_EXT);
		final int search_depth = this.getConfiguration().getConfiguration().getBoolean(CoreConfiguration.INSTR_SEARCH_RECURSIVE, false) ? Integer.MAX_VALUE : 1;
		mgr.startAnalysis(vis.search(getInstrPaths(), search_depth), null);

		// Add goal stats
		//this.addGoalStats("instr.archivesAnalyzed", mgr.countArchivesAnalyzed());
		//this.addGoalStats("instr.archivesOriginalFileSizeTotal", mgr.getFileSize());
		//this.addGoalStats("instr.archivesInstrumentedFileSizeTotal", mgr.getInstrumentedFileSize());
		// The following probably makes no sense, since we always instrument one JAR or WAR in the context of this Maven goal
		//			this.addGoalStats("instr.archivesFileSizeMax", ...);
		//			this.addGoalStats("instr.archivesFileSizeAvg", ...);

		// Number of classes in the JAR or WAR. For WARs, also the libs in WB/INF/lib are considered. 
		//this.addGoalStats("instr.classesInstrumentedTotal", mgr.countClassesTotal());
		//this.addGoalStats("instr.classesInstrumentedAlready", mgr.countClassesInstrumentedAlready());
		//this.addGoalStats("instr.classesInstrumentedSuccess", mgr.countClassesInstrumentedSuccess());
		//this.addGoalStats("instr.classesInstrumentedFailure", mgr.countClassesInstrumentedFailure());

		//TODO: Keep track of single constructs instrumented
		//exe.addGoalStats("instr.noDepConstructs", mgr.countConstructsIdentified());*/
	}
}