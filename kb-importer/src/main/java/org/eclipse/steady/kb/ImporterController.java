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

	private static Logger log = LoggerFactory.getLogger(ImporterController.class);

	private Thread importerCacheFetch = null;

    final long defaultRefetchAllMs = 
		VulasConfiguration.getGlobal().getConfiguration().getLong("vulas.kb.importer.refetchAllMs", -1);

	private final Manager manager;

	@Autowired
	ImporterController() {
		this.manager = new Manager();
	}

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	public ResponseEntity<Boolean> start(@RequestParam(defaultValue = "false") boolean overwrite, @RequestParam(defaultValue = "false") boolean upload,
		@RequestParam(defaultValue = "false") boolean verbose, @RequestParam(defaultValue = "true") boolean skipClone, @RequestParam(defaultValue = "0") String refetchAllMs) {
		boolean started = false;
		try {
		  if (this.manager.getIsRunningStart()) {
			log.info("Importer already running");
		  } else {
			if (this.importerCacheFetch != null && this.importerCacheFetch.isAlive()) {
				this.importerCacheFetch.interrupt();
			}
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put(Import.OVERWRITE_OPTION, overwrite);
			args.put(Import.UPLOAD_CONSTRUCT_OPTION, upload);
			args.put(Import.VERBOSE_OPTION, verbose);
			args.put(Import.SKIP_CLONE_OPTION, skipClone);
			long timeToWait;
			if (Long.parseLong(refetchAllMs) != 0) {
				timeToWait = Long.parseLong(refetchAllMs);
			} else {
				timeToWait = defaultRefetchAllMs;
			}

			this.importerCacheFetch =
				new Thread(
				new Runnable() {
					public void run() {
						while (true) {
							manager.start("/kb-importer/data/statements", args);

							try {
								log.info("Wait " + Long.toString(timeToWait/1000) + " seconds for next execution");
								Thread.sleep(timeToWait);
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
		  if (this.manager.getIsRunningStart() || (this.importerCacheFetch != null && this.importerCacheFetch.isAlive())) {
			stopped = true;
			this.manager.stop();
			this.importerCacheFetch.interrupt();
			log.info("Importer stopped");
		  } else {
			log.info("Importer not running");
		  }
		  return new ResponseEntity<Boolean>(stopped, HttpStatus.OK);
		} catch (Exception e) {
		  log.error("Exception when starting kb-importer cache refresh: " + e.getMessage(), e);
		  return new ResponseEntity<Boolean>(stopped, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(
		value = "/start/{id}",
		method = RequestMethod.POST)
	public ResponseEntity<Boolean> importSingleVuln(@PathVariable String id, @RequestParam(defaultValue = "false") boolean overwrite, 
			@RequestParam(defaultValue = "false") boolean upload, @RequestParam(defaultValue = "false") boolean verbose, @RequestParam(defaultValue = "true") boolean skipClone) {
		
		try {
			if (this.manager.getIsRunningStart()) {
			  log.info("Importer already running");
			  return new ResponseEntity<Boolean>(false, HttpStatus.SERVICE_UNAVAILABLE);
			} else {
				HashMap<String, Object> args = new HashMap<String, Object>();
				args.put(Import.OVERWRITE_OPTION, overwrite);
				args.put(Import.UPLOAD_CONSTRUCT_OPTION, upload);
				args.put(Import.VERBOSE_OPTION, verbose);
				args.put(Import.SKIP_CLONE_OPTION, skipClone);
				manager.importSingleVuln("/kb-importer/data/statements/" + id, args, id);
				return new ResponseEntity<Boolean>(true, HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error("Exception when importing vulnerability: " + e.getMessage(), e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/status")
	public String status() {
		return manager.status();
	}

	@GetMapping(
		value = "/status/{id}")
	public String statusSingleVuln(@PathVariable String id) {
		String statusStr = manager.getVulnStatus(id).toString();
		if (statusStr == null) { 
			return "Vulnerability not found";
		} else return statusStr;
	}	
}