package com.sap.psr.vulas.monitor;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.TestGoal;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Uses the {@link ClassVisitor} to dynamically instrument Java constructors and methods, i.e.,
 * during the class loading process. Receives callbacks from instrumented code that is executed
 * during application tests. The collected information is then uploaded to the central Vulas
 * backend using the {@link UploadScheduler}.
 * 
 *
 */
public class ExecutionMonitor {

	// ====================================== STATIC MEMBERS

	private static ExecutionMonitor instance = null;

	private static Log log = null;
	
	private static boolean PAUSE_COLLECTION = false;

	// ====================================== INSTANCE MEMBERS

	private String id = new Double(Math.random()).toString();

	private UploadScheduler shutdownUploader = null;
	private UploadScheduler periodicUploader = null;

	/**
	 * The goal execution related to a trace collection, will be null for all context determination modes except FIXED.
	 * @see ApplicationContextFinder#isFixedMode()
	 */
	private AbstractGoal exe = null;

	public ExecutionMonitor() {
		try {
			final Application app_ctx = CoreConfiguration.getAppContext();
			final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();

			// Always create and register shutdown uploader
			this.shutdownUploader = new UploadScheduler(this);
			Runtime.getRuntime().addShutdownHook(new Thread(this.shutdownUploader, "vulas-shutdown-trace-upload"));

			// Configure uploader: Create and start periodic uploader according to configuration
			if(cfg.getBoolean(CoreConfiguration.MONI_PERIODIC_UPL_ENABLED, true))
				this.enablePeriodicUpload(cfg.getInt(CoreConfiguration.MONI_PERIODIC_UPL_INTERVAL, 300000), cfg.getInt(CoreConfiguration.MONI_PERIODIC_UPL_BATCH_SIZE, 1000));

			// Goal execution
			this.exe = new TestGoal();
			this.exe.setGoalClient(GoalClient.AGENT);
			this.startGoal();
		}
		catch(ConfigurationException ce) {
			ExecutionMonitor.getLog().error(ce.getMessage());
		}
		catch(GoalConfigurationException gce) {
			ExecutionMonitor.getLog().error(gce.getMessage());
		}
	}

	// ====================================== STATIC METHODS

	public synchronized static ExecutionMonitor getInstance() {
		if(ExecutionMonitor.instance==null) ExecutionMonitor.instance = new ExecutionMonitor();
		return ExecutionMonitor.instance;
	}
	
	private static final Log getLog() {
		if(ExecutionMonitor.log==null)
			ExecutionMonitor.log = LogFactory.getLog(ExecutionMonitor.class);
		return ExecutionMonitor.log;
	}
	
	public static boolean isPaused() { return ExecutionMonitor.PAUSE_COLLECTION; }
	public static synchronized void setPaused(boolean _bool) { ExecutionMonitor.PAUSE_COLLECTION = _bool; }

	// ====================================== INSTANCE METHODS

	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("ExecutionMonitor [id=").append(this.id);
		b.append(", periodicUpload=").append(this.isPeriodicUploadEnabled());
		if(this.isPeriodicUploadEnabled()) {
			b.append(", interval=").append(this.getPeriodicUploadInterval());
			b.append(", batchSize=").append(this.getPeriodicUploadBatchSize());
		}
		b.append("]");
		return b.toString();
	}

	public void enablePeriodicUpload(long _interval, int _batch_size) {
		this.periodicUploader = new UploadScheduler(this, _interval, _batch_size);
		final Thread thread = new Thread(this.periodicUploader, "vulas-periodic-trace-upload");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		this.shutdownUploader.addObserver(this.periodicUploader);
	}

	public boolean isPeriodicUploadEnabled() { return this.periodicUploader!=null && this.periodicUploader.isEnabled(); }
	public long getPeriodicUploadInterval() { return (this.periodicUploader==null ? -1 : this.periodicUploader.getInterval()); };
	public int getPeriodicUploadBatchSize() { return (this.periodicUploader==null ? -1 : this.periodicUploader.getBatchSize()); };

	public void startGoal() throws GoalConfigurationException {
		if(this.exe!=null)
			this.exe.start();
	}

	public void stopGoal() {
		if(this.exe!=null) {
			this.exe.stop();

			// Add instrumentation stats (if any)
			exe.addGoalStats("test", InstrumentationControl.getOverallStatistics());

			final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
			final Iterator<IInstrumentor> iter = instrumentorList.iterator();
			while(iter.hasNext()){
				final IInstrumentor i = iter.next();
				exe.addGoalStats("test." + i.getClass().getSimpleName(), i.getStatistics());
			}

			this.exe.upload();
		}
	}

	/**
	 * Iterates over all configured {@link IInstrumentor}s and calls {@link IInstrumentor#awaitUpload()} for each of them.
	 * @param batchSize
	 */
	public void awaitUpload() {
		final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
		final Iterator<IInstrumentor> iter = instrumentorList.iterator();
		while(iter.hasNext()){
			final IInstrumentor i = iter.next();
			i.awaitUpload();
		}
	}

	/**
	 * Calls {@link ExecutionMonitor#uploadInformation(int)} with a batch size of -1.
	 */
	public synchronized void uploadInformation() { this.uploadInformation(-1); }

	/**
	 * Iterates over all configured {@link IInstrumentor}s and calls {@link IInstrumentor#upladInformation(AbstractGoal, int)} for each of them.
	 * @param batchSize
	 */
	public synchronized void uploadInformation(int batchSize) {
		final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
		final Iterator<IInstrumentor> iter = instrumentorList.iterator();
		while(iter.hasNext()){
			final IInstrumentor i = iter.next();
			i.upladInformation(this.exe, batchSize);
		}
	}
}