package com.sap.psr.vulas.backend.component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.repo.GoalExecutionRepository;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringUtil;

@Component(value="csvProducerThread")
@Scope("prototype")
@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
public class ApplicationExporterThread implements Runnable {

	private static Logger log = LoggerFactory.getLogger(ApplicationExporterThread.class);

	private final static DateFormat DATE_FORMAT  = new SimpleDateFormat("dd/MM/yyyy");

	private static final String lb = System.getProperty("line.separator");

	@Autowired
	GoalExecutionRepository gexeRepository;

	@Autowired
	ApplicationRepository appRepository;

	private String separator = ";";

	private String[] includeSpaceProperties = null;

	private String[] includeGoalConfiguration = null;

	private String[] includeGoalSystemInfo = null;

	private String[] bugs = null;

	private StringBuffer buffer = new StringBuffer();
	
	private HashMap<Long, HashMap<String, Boolean>> affectedApps = null;

	private List<Application> apps = null;

	public ApplicationExporterThread setSeparator(String separator) {
		this.separator = separator;
		return this;
	}

	public ApplicationExporterThread setIncludeSpaceProperties(String[] includeSpaceProperties) {
		this.includeSpaceProperties = includeSpaceProperties;
		return this;
	}

	public ApplicationExporterThread setIncludeGoalConfiguration(String[] includeGoalConfiguration) {
		this.includeGoalConfiguration = includeGoalConfiguration;
		return this;
	}

	public ApplicationExporterThread setIncludeGoalSystemInfo(String[] includeGoalSystemInfo) {
		this.includeGoalSystemInfo = includeGoalSystemInfo;
		return this;
	}

	public ApplicationExporterThread setApps(List<Application> apps) {
		this.apps = apps;
		return this;
	}	

	public ApplicationExporterThread setBugs(String[] bugs) {
		this.bugs = bugs;
		return this;
	}
	
	public HashMap<Long, HashMap<String, Boolean>> getAffectedApps() {
		return affectedApps;
	}

	public ApplicationExporterThread setAffectedApps(HashMap<Long, HashMap<String, Boolean>> affectedApps) {
		this.affectedApps = affectedApps;
		return this;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	@Transactional(readOnly=true, propagation=Propagation.REQUIRED) // Needed in order to lazy load properties when called async
	public void run() {
		// Show progress
		final StopWatch sw = new StopWatch("Worker thread: Produce CSV for [" + apps.size() + "] apps");
		sw.setTotal(this.apps.size());
		sw.start();

		for(Application a: this.apps) {
			final StringBuffer entry = new StringBuffer();
			try {
				// Space
				entry.append(a.getSpace().getSpaceToken()).append(separator).append(a.getSpace().getSpaceName()).append(separator).append(a.getSpace().getSpaceOwners()).append(separator);
				if(includeSpaceProperties!=null && includeSpaceProperties.length>0) {
					for(String p: includeSpaceProperties) {
						final String value = a.getSpace().getPropertyValue(p);
						if(value!=null)
							entry.append(value).append(separator);
						else
							entry.append("").append(separator);
					}
				}

				entry.append(a.getId()).append(separator).append(a.getMvnGroup()).append(separator).append(a.getArtifact()).append(separator).append(a.getVersion()).append(separator);
				entry.append(ApplicationExporterThread.DATE_FORMAT.format(a.getCreatedAt().getTime()));
				//entry.append(separator).append(a.countDependencies()).append(separator).append(a.countConstructs());

				// Bugs
				if(!StringUtil.isEmptyOrContainsEmptyString(this.bugs) && this.affectedApps!=null) {
					final HashMap<String, Boolean> affected_app = this.affectedApps.get(a.getId());
					for(String b: this.bugs) {
						Boolean affected = false;
						if(affected_app!=null && affected_app.containsKey(b)) {
							affected = affected_app.get(b); // Can be true or null
						}
						if(affected==null)
							entry.append(separator).append("unconfirmed");
						else if(affected==true)
							entry.append(separator).append("affected");
						else
							entry.append(separator).append("not affected");
					}
				}

				// Stuff from goal execution
				final GoalExecution latest_goal_exe = gexeRepository.findLatestGoalExecution(a, null);
				if(latest_goal_exe!=null) {
					entry.append(separator).append(DATE_FORMAT.format(latest_goal_exe.getCreatedAt().getTime())).append(separator).append(latest_goal_exe.getClientVersion());

					// Goal config
					if(!StringUtil.isEmptyOrContainsEmptyString(this.includeGoalConfiguration)) {
						//log.info("Get goal configuration for " + latest_goal_exe); //TODO: Delete
						for(String p: includeGoalConfiguration) {
							final String prop = latest_goal_exe.getConfiguration(p);
							if(prop!=null)
								entry.append(separator).append(prop);
							else
								entry.append(separator).append("");
						}
					}

					// Sys info
					if(!StringUtil.isEmptyOrContainsEmptyString(includeGoalSystemInfo)) {
						//log.info("Get system info for " + latest_goal_exe); //TODO: Delete
						for(String p: includeGoalSystemInfo) {
							final String prop = latest_goal_exe.getSystemInfo(p);
							if(prop!=null)
								entry.append(separator).append(prop);
							else
								entry.append(separator).append("");
						}
					}
				} else {
					entry.append(separator).append("").append(separator).append("").append(separator).append("");

					// Goal config
					if(!StringUtil.isEmptyOrContainsEmptyString(this.includeGoalConfiguration))
						for(String p: includeGoalConfiguration)
							entry.append(separator).append("");

					// Sys info
					if(!StringUtil.isEmptyOrContainsEmptyString(includeGoalSystemInfo))
						for(String p: includeGoalSystemInfo)
							entry.append(separator).append("");
				}

				entry.append(lb);
				buffer.append(entry);
				sw.progress();
			} catch (Exception e) {
				log.error("[" + e.getClass().getName() + "] while appending data for app " + a + ", entry [" + entry.toString() + "] will not be appended to CSV: " + e.getMessage());
			}
		}
		sw.stop();
	}
}