package org.eclipse.steady.kb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import org.eclipse.steady.kb.Manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin("*")
public class ImporterController {

	private static Logger log = LoggerFactory.getLogger(ImporterController.class);

	private Thread importerCacheFetch = null;
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
	
				  long interval = 3600*24*1000;
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

	@GetMapping("/start")
	public String start() {
		System.out.println("ImportController.start()");
		this.importerCacheFetch.start();
		return "Started importing vulnerabilities";
	}

}