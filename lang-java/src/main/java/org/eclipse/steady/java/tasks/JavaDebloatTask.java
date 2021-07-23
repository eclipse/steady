/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java.tasks;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.Construct;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalConfigurationException;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.java.JarAnalyzer;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.tasks.AbstractTask;
import org.eclipse.steady.tasks.DebloatTask;
import org.vafer.jdependency.Clazz;
import org.vafer.jdependency.Clazzpath;
import org.vafer.jdependency.ClazzpathUnit;

/**
 * <p>JavaBomTask class.</p>
 */
public class JavaDebloatTask extends AbstractTask implements DebloatTask {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final String[] EXT_FILTER = new String[] {"jar", "war", "class", "java", "aar"};

  private String[] appPrefixes = null;

  private StringList appJarNames = null;

  private static final List<GoalClient> pluginGoalClients =
      Arrays.asList(GoalClient.MAVEN_PLUGIN, GoalClient.GRADLE_PLUGIN);

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getLanguage() {
    return new HashSet<ProgrammingLanguage>(
        Arrays.asList(new ProgrammingLanguage[] {ProgrammingLanguage.JAVA}));
  }

  /**
   * Returns true if the configuration setting {@link CoreConfiguration#APP_PREFIXES} shall be considered, false otherwise.
   */
  private final boolean useAppPrefixes() {
    return this.appPrefixes != null && !this.isOneOfGoalClients(pluginGoalClients);
  }

  /**
   * Returns true if the configuration setting {@link CoreConfiguration#APP_PREFIXES} shall be considered, false otherwise.
   */
  private final boolean useAppJarNames() {
    return this.appJarNames != null && !this.isOneOfGoalClients(pluginGoalClients);
  }

  /** {@inheritDoc} */
  @Override
  public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
    super.configure(_cfg);

    // App constructs identified using package prefixes
    this.appPrefixes = _cfg.getStringArray(CoreConfiguration.APP_PREFIXES, null);

    // Print warning message in case the setting is used as part of the Maven plugin
    if (this.appPrefixes != null && this.isOneOfGoalClients(pluginGoalClients)) {
      log.warn(
          "Configuration setting ["
              + CoreConfiguration.APP_PREFIXES
              + "] ignored when running the goal as Maven plugin");
      this.appPrefixes = null;
    }

    // App constructs identified using JAR file name patterns (regex)
    final String[] app_jar_names = _cfg.getStringArray(CoreConfiguration.APP_JAR_NAMES, null);
    if (app_jar_names != null) {
      // Print warning message in case the setting is used as part of the Maven plugin
      if (this.isOneOfGoalClients(pluginGoalClients)) {
        log.warn(
            "Configuration setting ["
                + CoreConfiguration.APP_JAR_NAMES
                + "] ignored when running the goal as Maven plugin");
        this.appJarNames = null;
      } else {
        this.appJarNames = new StringList();
        this.appJarNames.addAll(app_jar_names);
      }
    }

    // CLI: Only one of appPrefixes and appJarNames can be used
    if (!this.isOneOfGoalClients(pluginGoalClients)) {
      if (this.appPrefixes != null && this.appJarNames != null) {
        throw new GoalConfigurationException(
            "Exactly one of the configuration settings ["
                + CoreConfiguration.APP_PREFIXES
                + "] and ["
                + CoreConfiguration.APP_JAR_NAMES
                + "] must be set");
      } else if (this.appPrefixes == null && this.appJarNames == null) {
        throw new GoalConfigurationException(
            "Exactly one of the configuration settings ["
                + CoreConfiguration.APP_PREFIXES
                + "] and ["
                + CoreConfiguration.APP_JAR_NAMES
                + "] must be set");
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void execute() throws GoalExecutionException {

	final Clazzpath cp = new Clazzpath();
	
	List<ClazzpathUnit> app = new ArrayList<ClazzpathUnit>();
	try {
		//1) Add app paths 
		if (this.hasSearchPath()) {
		  for (Path p : this.getSearchPath()) {
		    log.info(
		        "Add app path ["
		            + p
		            + "] to classpath");
		    app.add(cp.addClazzpathUnit(p));
	      }
		}
		//2) Add dependencies to classpath
	    if (this.getKnownDependencies() != null) {
	      for (Path p : this.getKnownDependencies().keySet()) {
	    	  log.info(
	  		        "Add dep path ["
	  		            + p
	  		            + "] to classpath");
	    	  cp.addClazzpathUnit(p);
	      }
	    }
	} catch (IOException e) {
	      // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	final Set<Clazz> needed = new HashSet<Clazz>();
	final Set<Clazz> removable = cp.getClazzes();
	for(ClazzpathUnit u: app) {
		removable.removeAll(u.getClazzes());
		removable.removeAll(u.getTransitiveDependencies());
		needed.addAll(u.getClazzes());
		needed.addAll(u.getTransitiveDependencies());
	}

	log.info("Needed ["+ needed.size()+"] classes");
	log.info("Removable ["+ removable.size()+"] classes");
	for(Clazz clazz : removable) {
	  System.out.println("class " + clazz + " is not required");
	}
	try {
		FileWriter writer=new FileWriter("needed.txt");
		for(Clazz c: needed) {
			writer.write(c + System.lineSeparator());
		}
		writer.close();
	} catch (IOException e){// TODO Auto-generated catch block
		e.printStackTrace();} 
  }

}
