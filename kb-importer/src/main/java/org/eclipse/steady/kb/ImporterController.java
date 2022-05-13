package org.eclipse.steady.kb;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
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
		HashMap<String, Object> mapCommandOptionValues = new HashMap<String, Object>();
		manager = new Manager();

		System.out.println("ImportController Constructor");
	
		this.importerCacheFetch =
		  new Thread(
			new Runnable() {
			  public void run() {
				while (true) {
				  manager.start("/kb-importer/data/statements", mapCommandOptionValues);
	
				  long interval = time_refresh_all;
				  System.out.println("interval: "+Long.toString(time_refresh_all));
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
	public ResponseEntity<Boolean> start() {
		System.out.println("ImportController.start()");

		boolean started = false;
		try {
		  if (this.importerCacheFetch.isAlive()) {
			log.info("Importer already running");
		  } else {
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

	@GetMapping("/status")
	public String status() {
		return manager.status();
	}


}