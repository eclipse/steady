package org.eclipse.steady.kb;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.steady.kb.Manager;
import org.eclipse.steady.shared.util.VulasConfiguration;

@RestController
@CrossOrigin("*")
public class ImporterController {

	private static String TIME_REFRESH_ALL = "vulas.kb.importer.refetchAllMs";

	private static Logger log = LoggerFactory.getLogger(ImporterController.class);

	private Thread importerCacheFetch = null;
    // Refresh CVE cache
    final long time_refresh_all = 
		VulasConfiguration.getGlobal().getConfiguration().getLong(TIME_REFRESH_ALL, -1);

	private final Manager manager;

	@Autowired
	ImporterController() {
		this.manager = new Manager();	
		HashMap<String, Object> args = new HashMap<String, Object>();
		this.importerCacheFetch =
			new Thread(
			new Runnable() {
				public void run() {
					while (true) {
						manager.start("/kb-importer/data/statements", args);
		
						long interval = time_refresh_all;
						System.out.println("interval: " + Long.toString(time_refresh_all));
						try {
							Thread.sleep(interval);
						} catch (InterruptedException e) {
							ImporterController.log.error(
								"Interrupted exception: "
									+ e.getMessage());
						}
					}
				}
			},
			"ImporterCacheFetch");
		this.importerCacheFetch.setPriority(Thread.MIN_PRIORITY);	
	}

	//@GetMapping("/start")
	@RequestMapping(value = "/start", method = RequestMethod.POST)
	public ResponseEntity<Boolean> start(@RequestParam(defaultValue = "false") boolean overwrite, @RequestParam(defaultValue = "false") boolean upload,
		@RequestParam(defaultValue = "false") boolean verbose, @RequestParam(defaultValue = "true") boolean skipClone) {
		boolean started = false;
		try {
		  if (this.importerCacheFetch.isAlive()) {
			log.info("Importer already running");
		  } else {
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put(Import.OVERWRITE_OPTION, overwrite);
			args.put(Import.UPLOAD_CONSTRUCT_OPTION, upload);
			args.put(Import.VERBOSE_OPTION, verbose);
			args.put(Import.SKIP_CLONE_OPTION, skipClone);
			this.importerCacheFetch =
				new Thread(
				new Runnable() {
					public void run() {
						while (true) {
							manager.start("/kb-importer/data/statements", args);
			
							long interval = time_refresh_all;
							System.out.println("interval: " + Long.toString(time_refresh_all));
							try {
								Thread.sleep(interval);
							} catch (InterruptedException e) {
								ImporterController.log.error(
									"Interrupted exception: "
										+ e.getMessage());
							}
						}
					}
				},
				"ImporterCacheFetch");
			this.importerCacheFetch.setPriority(Thread.MIN_PRIORITY);

			System.out.println(importerCacheFetch);
			this.importerCacheFetch.start();
			started = true;
			log.info("Importer started");
		  }
		  return new ResponseEntity<Boolean>(started, HttpStatus.OK);
		} catch (Exception e) {
		  log.error("Exception when starting CVE cache refresh: " + e.getMessage(), e);
		  return new ResponseEntity<Boolean>(started, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	public ResponseEntity<Boolean> stop() {
		boolean stopped = false;
		try {
		  if (this.importerCacheFetch.isAlive()) {
			stopped = true;
			this.importerCacheFetch.interrupt();
			log.info("Importer stopped");
		  } else {
			log.info("Importer not running");
		  }
		  return new ResponseEntity<Boolean>(stopped, HttpStatus.OK);
		} catch (Exception e) {
		  log.error("Exception when starting CVE cache refresh: " + e.getMessage(), e);
		  return new ResponseEntity<Boolean>(stopped, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(
		value = "/start/{id}",
		method = RequestMethod.POST)
	public ResponseEntity<Boolean> importSingleVuln(@PathVariable String id) {
		
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}

	@GetMapping("/status")
	public String status() {
		return manager.status();
	}


}