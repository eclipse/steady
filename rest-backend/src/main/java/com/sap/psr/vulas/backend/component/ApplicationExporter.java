package com.sap.psr.vulas.backend.component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.util.Message;
import com.sap.psr.vulas.backend.util.SmtpClient;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.ThreadUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

@Component
public class ApplicationExporter {

	private static Logger log = LoggerFactory.getLogger(ApplicationExporter.class);

	private static final String CSV_PREFIX = "vulas_all_apps-";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	final int no_threads = ThreadUtil.getNoThreads();

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ApplicationRepository appRepository;

	public synchronized void produceCsvAsync(final Tenant _tenant, final String separator, final String[] includeSpaceProperties, final String[] includeGoalConfiguration, final String[] includeGoalSystemInfo, final String[] _bugs, final Message _msg) {
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					final java.nio.file.Path csv_file = produceCsv(_tenant, separator, includeSpaceProperties, includeGoalConfiguration, includeGoalSystemInfo, _bugs);
					_msg.setAttachment(csv_file);
					final SmtpClient c = new SmtpClient();
					c.send(_msg);
				} catch (IOException e) {
					log.error("Error while reading all tenant apps (as CSV): " + e.getMessage(), e);
				} catch (MessagingException e) {
					log.error("Error while sending all tenant apps (as CSV) per email: " + e.getMessage(), e);
				}
			}
		}, "AllAppsAsCsv-" + StringUtil.getRandonString(6));
		thread.setPriority(Thread.MIN_PRIORITY); 
		thread.start();
	}

	/**
	 * 
	 * @param _tenant
	 * @param separator
	 * @param includeSpaceProperties
	 * @param includeGoalConfiguration
	 * @param includeGoalSystemInfo
	 * @param _msg
	 */
	@Transactional
	public synchronized Path produceCsv(Tenant _tenant, String separator, String[] includeSpaceProperties, String[] includeGoalConfiguration, String[] includeGoalSystemInfo, String[] _bugs) throws IOException {
		// To be returned
		Path csv = null;

		// Get all apps
		final ArrayList<Application> apps = this.appRepository.findAllApps(_tenant);

		// Show progress
		final StopWatch sw = new StopWatch("Produce CSV for [" + apps.size() + "] apps");
		sw.setTotal(apps.size());
		sw.start();
		
		// Get applications affected by given bugs
		HashMap<Long, HashMap<String, Boolean>> affected_apps = null;
		if(_bugs!=null && _bugs.length>0) {
			affected_apps = this.appRepository.findAffectedApps(_bugs);
			sw.lap("Completed search for apps affected by [" + StringUtil.join(_bugs, ", ") + "]", true);
		}

		// Partition size
		final int size = new Double(Math.ceil( (double)apps.size() / (double)no_threads)).intValue();

		// Create parallel threads
		final ExecutorService pool = Executors.newFixedThreadPool(no_threads);
		final Set<ApplicationExporterThread> searches = new HashSet<ApplicationExporterThread>();
		if(no_threads > apps.size()) {
			final ApplicationExporterThread search = (ApplicationExporterThread)this.applicationContext.getBean("csvProducerThread");
			search.setSeparator(separator)
			.setApps(apps)
			.setIncludeGoalConfiguration(includeGoalConfiguration)
			.setIncludeGoalSystemInfo(includeGoalSystemInfo)
			.setIncludeSpaceProperties(includeSpaceProperties)
			.setBugs(_bugs)
			.setAffectedApps(affected_apps);
			searches.add(search);
			pool.execute(search);
		}
		else {
			for(int i=0; i<no_threads; i++) {
				int min = i * size;
				int max = Math.min((i + 1) * size, apps.size());
				final ApplicationExporterThread search = (ApplicationExporterThread)this.applicationContext.getBean("csvProducerThread");
				search.setSeparator(separator)
				.setApps(apps.subList(min, max))
				.setIncludeGoalConfiguration(includeGoalConfiguration)
				.setIncludeGoalSystemInfo(includeGoalSystemInfo)
				.setIncludeSpaceProperties(includeSpaceProperties)
				.setBugs(_bugs)
				.setAffectedApps(affected_apps);
				searches.add(search);
				pool.execute(search);
			}
		}

		try {
			// Temporary file
			final java.nio.file.Path dir = VulasConfiguration.getGlobal().getTmpDir();
			final String prefix = CSV_PREFIX + DATE_FORMAT.format(Calendar.getInstance().getTime()) + "-";
			final File f = File.createTempFile(prefix, ".csv", dir.toFile());
			csv = f.toPath();
	
			// Produce CSV header
			final StringBuffer header = new StringBuffer();
	
			header.append("Space Token").append(separator).append("Space Name").append(separator).append("Space Owners").append(separator);
			if(includeSpaceProperties!=null && includeSpaceProperties.length>0)
				for(String p: includeSpaceProperties)
					header.append(p).append(separator);
	
			header.append("App ID").append(separator).append("Group").append(separator).append("Artifact").append(separator).append("Version").append(separator);
			header.append("Created At").append(separator);
			//header.append("Count Dependencies").append(separator).append("Count Constructs").append(separator);
	
			// Bugs
			if(_bugs!=null)
				for(String b: _bugs)
					header.append(b).append(separator);
	
			// Most recent goal execution
			header.append("Last Goal Execution").append(separator).append("Vulas Version");
			if(includeGoalConfiguration!=null && includeGoalConfiguration.length>0)
				for(String p: includeGoalConfiguration)
					header.append(separator).append(p);
			if(includeGoalSystemInfo!=null && includeGoalSystemInfo.length>0)
				for(String p: includeGoalSystemInfo)
					header.append(separator).append(p);
	
			PrintWriter writer = null;
		
			writer = new PrintWriter(f);
			writer.print(header.toString());
			writer.println();

			// Write content from threads
			try {
				// Wait for the thread pool to finish the work
				pool.shutdown();
				while (!pool.awaitTermination(60, TimeUnit.SECONDS))
					log.info("Wait for the completion of CSV producers ...");

				// Join reachable constructs and touch points
				for(ApplicationExporterThread search: searches) {
					writer.write(search.getBuffer().toString());
					writer.flush();
				}
			} catch (InterruptedException e) {
				log.error("Interrupt exception", e);
			}			

			sw.stop();
		} catch (IOException e) {
			sw.stop(e);
			log.error("Exception while writing the apps: " + e.getMessage());
		}

		return csv;
	}
}
