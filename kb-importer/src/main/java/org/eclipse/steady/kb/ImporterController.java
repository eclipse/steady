package org.eclipse.steady.kb;

import java.util.HashMap;
import org.eclipse.steady.kb.Manager;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@CrossOrigin("*")
@RequestMapping("/")
class ImporterController {

  @RequestMapping("/start")
  public void start() {

    HashMap<String, Object> mapCommandOptionValues = new HashMap<String, Object>();
    Manager manager = new Manager();
    manager.start("/kb-importer/data/statements", mapCommandOptionValues);
  
    return;
  }
}
