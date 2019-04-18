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
import java.util.List;
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
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.util.Message;
import com.sap.psr.vulas.backend.util.SmtpClient;
import com.sap.psr.vulas.shared.enums.ExportFormat;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.ThreadUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

@Component
public class ApplicationExporter {

	private static Logger log = LoggerFactory.getLogger(ApplicationExporter.class);

	private static final String EXPORT_FILE_PREFIX = "vulas_all_apps-";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	final int no_threads = ThreadUtil.getNoThreads();

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ApplicationRepository appRepository;

	public synchronized void produceExportAsync(final Tenant _tenant, final Space _space, final String separator, final String[] includeSpaceProperties, final String[] includeGoalConfiguration, final String[] includeGoalSystemInfo, final String[] _selected_bugs, final boolean _incl_all_bugs, final boolean _incl_exemptions, final ExportFormat _format, final Message _msg) {
		// Check whether SMTP is properly configured (throws ISE if not)
		SmtpClient.getSmtpProperties(VulasConfiguration.getGlobal().getConfiguration());
		
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					final java.nio.file.Path csv_file = produceExport(_tenant, _space, separator, includeSpaceProperties, includeGoalConfiguration, includeGoalSystemInfo, _selected_bugs, _incl_all_bugs, _incl_exemptions, _format);
					_msg.setAttachment(csv_file);
					final SmtpClient c = new SmtpClient();
					c.send(_msg);
				} catch (IOException e) {
					log.error("Error while reading all tenant apps as [" + _format + "]: " + e.getMessage(), e);
				} catch (MessagingException e) {
					log.error("Error while sending all tenant apps as [" + _format + "] per email: " + e.getMessage(), e);
				}
			}
		}, "ExportAllApps-" + StringUtil.getRandonString(6));
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
	public synchronized Path produceExport(Tenant _tenant, Space _space, String separator, String[] includeSpaceProperties, String[] includeGoalConfiguration, String[] includeGoalSystemInfo, String[] _selected_bugs, boolean _incl_all_bugs, boolean _incl_exemptions, ExportFormat _format) throws IOException {
		// To be returned
		Path export_file = null;

		// Get the apps to be exported
		ArrayList<Application> apps = null;
		if(_space!=null)
			apps = this.appRepository.findAllApps(_tenant, _space.getSpaceToken());
		else
			apps = this.appRepository.findAllApps(_tenant);

		// Show progress
		final StopWatch sw = new StopWatch("Produce [" + _format + "] export for [" + apps.size() + "] app(s) of " + _tenant + " and " + _space);
		sw.setTotal(apps.size());
		sw.start();

		// Get applications affected by given bugs
		HashMap<Long, HashMap<String, Boolean>> affected_apps = null;
		if(_selected_bugs!=null && _selected_bugs.length>0) {
			affected_apps = this.appRepository.findAffectedApps(_selected_bugs);
			sw.lap("Completed search for apps affected by [" + StringUtil.join(_selected_bugs, ", ") + "]", true);
		}
		
		// Create parallel threads
		final ExecutorService pool = Executors.newFixedThreadPool(no_threads);
		final Set<ApplicationExporterThread> searches = new HashSet<ApplicationExporterThread>();
		
		final Set<List<Application>> parts = ApplicationExporter.partition(apps, (apps.size()<=no_threads ? 1 : no_threads) );
		for(List<Application> part: parts) {
			final ApplicationExporterThread search = (ApplicationExporterThread)this.applicationContext.getBean("csvProducerThread");
			search.setSeparator(separator)
			.setApps(part)
			.setIncludeGoalConfiguration(includeGoalConfiguration)
			.setIncludeGoalSystemInfo(includeGoalSystemInfo)
			.setIncludeSpaceProperties(includeSpaceProperties)
			.setBugs(_selected_bugs)
			.setAffectedApps(affected_apps)
			.setIncludeAllBugs(_incl_all_bugs)
			.setIncludeExemptions(_incl_exemptions)
			.setFormat(_format);
			searches.add(search);
			pool.execute(search);
		}

		try {
			// Temporary file
			final java.nio.file.Path dir = VulasConfiguration.getGlobal().getTmpDir();
			final String prefix = EXPORT_FILE_PREFIX + DATE_FORMAT.format(Calendar.getInstance().getTime()) + "-";
			final File f = File.createTempFile(prefix, "." + _format.toString().toLowerCase(), dir.toFile());
			export_file = f.toPath();
			
			final PrintWriter writer = new PrintWriter(f);

			// Create CSV header
			if(ExportFormat.CSV.equals(_format)) {
				final StringBuffer header = new StringBuffer();
	
				header.append("Space Token").append(separator).append("Space Name").append(separator).append("Space Owners").append(separator);
				if(includeSpaceProperties!=null && includeSpaceProperties.length>0)
					for(String p: includeSpaceProperties)
						header.append(p).append(separator);
	
				header.append("App ID").append(separator).append("Group").append(separator).append("Artifact").append(separator).append("Version").append(separator);
				header.append("Created At").append(separator);
				//header.append("Count Dependencies").append(separator).append("Count Constructs").append(separator);
			
			
				// Bugs
				if(_selected_bugs!=null)
					for(String b: _selected_bugs)
						header.append(b).append(separator);
	
				// Most recent goal execution
				header.append("Last Goal Execution").append(separator).append("Vulas Version");
				if(includeGoalConfiguration!=null && includeGoalConfiguration.length>0)
					for(String p: includeGoalConfiguration)
						header.append(separator).append(p);
				if(includeGoalSystemInfo!=null && includeGoalSystemInfo.length>0)
					for(String p: includeGoalSystemInfo)
						header.append(separator).append(p);
				
				writer.print(header.toString());
				writer.println();
			}

			// Write content from threads
			try {
				// Wait for the thread pool to finish the work
				pool.shutdown();
				while (!pool.awaitTermination(60, TimeUnit.SECONDS))
					log.info("Wait for the completion of [" + _format + "] producers ...");

				// Open JSON array
				if(ExportFormat.JSON.equals(_format))
					writer.write("[");
				
				// Join reachable constructs and touch points
				int i = 0;
				for(ApplicationExporterThread search: searches) {
					// Separate JSON objects
					if(ExportFormat.JSON.equals(_format) && i++>0)
						writer.write(",");
					
					writer.write(search.getBuffer().toString());
					writer.flush();
				}
				
				// Close JSON array
				if(ExportFormat.JSON.equals(_format)) {
					writer.write("]");
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

		return export_file;
	}
	
	/**
	 * Creates a set with the given number of sublists of the given list.
	 * 
	 * If the given list is null, the method returns an empty set.
	 * If the given list is empty or its size is smaller than the given number, the method returns a set that just contains the given list as its only element.
	 * 
	 * @param _list
	 * @param _num
	 * @return
	 */
	public static <T> Set<List<T>> partition(List<T> _list, int _num) throws IllegalArgumentException {
		final Set<List<T>> parts = new HashSet<List<T>>();
		
		// Number of parts must be > 0
		if(_num<1)
			throw new IllegalArgumentException("Number of partitions must be greater than 0 but is [" + _num + "]");
		
		// No list
		else if(_list==null)
			;
		
		// Return 1 part only 
		else if(_list.size()<_num)
			parts.add(_list);
		
		// Create sublists
		else {
			final int size = new Double(Math.floor((double)_list.size()/(double)_num)).intValue();
			int i, min, max;
			
			// Create _partition_no -1 parts
			for(i=0; i<_num-1; i++) {
				min = i * size;
				max = Math.min((i + 1) * size, _list.size());
				if(max>=min) {
					parts.add(_list.subList(min, max));
				}
			}
			
			// Create last part with remaining elements
			min = i * size;
			max = Math.min((_num + 1) * size, _list.size());
			if(max>=min) {
				parts.add(_list.subList(min, max));
			}
		}
		return parts;
	}
}
