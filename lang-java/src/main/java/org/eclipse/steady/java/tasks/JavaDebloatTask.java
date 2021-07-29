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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.json.model.Dependency;
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

  private Set<ConstructId> traces = null;
  
  private Set<Dependency> reachableConstructIds = null;
  
  private static final List<GoalClient> pluginGoalClients =
      Arrays.asList(GoalClient.MAVEN_PLUGIN, GoalClient.GRADLE_PLUGIN);

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getLanguage() {
    return new HashSet<ProgrammingLanguage>(
        Arrays.asList(new ProgrammingLanguage[] {ProgrammingLanguage.JAVA}));
  }


  /** {@inheritDoc} */
  @Override
  public void execute() throws GoalExecutionException {

	final Clazzpath cp = new Clazzpath();
	
	// list of classpathunits to be used as entrypoints
	List<ClazzpathUnit> app = new ArrayList<ClazzpathUnit>();
	// list of classes to be used as entrypoints (from traces and reachable constructs)
	List<Clazz> used_classes = new ArrayList<Clazz>();
	// classpathunits for the application dependencies (as identified by maven)
	Set<ClazzpathUnit> maven_deps = new HashSet<ClazzpathUnit>();
	// classpathunits considered used according to traces, reachable constructs and jdependency analysis
	Set<ClazzpathUnit> deps_used = new HashSet<ClazzpathUnit>();
	
	try {
		//1) Add application paths (to be then used as entrypoints) 
		if (this.hasSearchPath()) {
		  for (Path p : this.getSearchPath()) {
		    log.info(
		        "Add app path ["
		            + p
		            + "] to classpath");
		    app.add(cp.addClazzpathUnit(p));
	      }
		}
		//2) Add dependencies to jdependency classpath object and populate set of dependencies
	    if (this.getKnownDependencies() != null) {
	      for (Path p : this.getKnownDependencies().keySet()) {
	    	  log.info(
	  		        "Add dep path ["
	  		            + p
	  		            + "] to classpath");
	    	  maven_deps.add(cp.addClazzpathUnit(p));
	    	  
	      }
	    }
	} catch (IOException e) {
	      // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	log.info("[" + app.size() +"] ClasspathUnit for the application to be used as entrypoints ");
	
	// retrieve traces to be used as Clazz entrypoints (1 class may be part of multiple classpathUnits) 
	for (ConstructId t : traces) {
		if(t.getType().equals(ConstructType.CLAS) || t.getType().equals(ConstructType.INTF) || t.getType().equals(ConstructType.ENUM)) {
			Clazz c = cp.getClazz(t.getQname());
			used_classes.add(c);
			Set<ClazzpathUnit> units = c.getClazzpathUnits();
			if(units.size()>1) {
				log.warn("Added as entrypoints multiple ClasspathUnits from single class [" + c + "] : [");
				for(ClazzpathUnit u: units) {
					log.warn(u + ",");
				log.warn("]");
				}
			}
			deps_used.addAll(units);
		}
	}
	log.info("[" + used_classes.size() +"] Clazz as entrypoints from traces");
	
	// retrieve reachable constructs to be used as Clazz entrypoints (1 class may be part of multiple classpathUnits)
	//TODO: deserialization not working (method getAppDependencies in BackendConnector:707)
	for (Dependency d : reachableConstructIds) {
		if(d.getReachableConstructIds()!=null) {
			for (ConstructId c : d.getReachableConstructIds()) {
				if(c.getType().equals(ConstructType.CLAS) || c.getType().equals(ConstructType.INTF) || c.getType().equals(ConstructType.ENUM)) {
					Clazz cl = cp.getClazz(c.getQname());
					used_classes.add(cl);
					Set<ClazzpathUnit> units = cl.getClazzpathUnits();
					if(units.size()>1) {
						log.warn("Added as entrypoints multiple ClasspathUnits from single class [" + cl + "] : [");
						for(ClazzpathUnit u: units) {
							log.warn(u + ",");
						log.warn("]");
						}
					}
					deps_used.addAll(units);
				}
			}
		}
	}
	log.info("[" + used_classes.size() +"]  Clazz as entrypoints from traces and reachable constructs");
	
	log.info("[" + cp.getUnits().length +"] classpathUnits in jdependency classpath object");
	

	// also collect "missing" classes to be able to check their relation with jre objects considered used
	// to be removed from final version
	SortedSet<Clazz> missing = new TreeSet<Clazz>();
	for (Clazz c : cp.getMissingClazzes()) {
		missing.add(c);
	}
	
	// classes considered used 
	final SortedSet<Clazz> needed = new TreeSet<Clazz>();
	final Set<Clazz> classes = cp.getClazzes();
	//loop over classpathunits (representing the application) marked as entrypoints to find needed classes
	for(ClazzpathUnit u: app) {
		classes.removeAll(u.getClazzes());
		//TODO: check if getDependencies is also needed or getTransitiveDependencies sufficies
		classes.removeAll(u.getDependencies());  
		classes.removeAll(u.getTransitiveDependencies());
		needed.addAll(u.getClazzes());
		needed.addAll(u.getDependencies());
		needed.addAll(u.getTransitiveDependencies());

		//loop over dependent classes to add their units among the used dependencies
		for (Clazz c : u.getDependencies()) {
			deps_used.addAll(c.getClazzpathUnits());
		}
		for (Clazz c : u.getTransitiveDependencies()) {
			deps_used.addAll(c.getClazzpathUnits());
		}
	}
	
	
	// loop over class (representing traces and reachable constructs) marked as entrypoints to find needed classes
	for(Clazz c: used_classes) {
		classes.remove(c);
		classes.removeAll(c.getDependencies());
		classes.removeAll(c.getTransitiveDependencies());
		needed.add(c);
		needed.addAll(c.getDependencies());
		needed.addAll(c.getTransitiveDependencies());
		
		//loop over dependent classes to add their units among the used dependencies
		for (Clazz cc : c.getDependencies()) {
			deps_used.addAll(cc.getClazzpathUnits());
		}
		for (Clazz cc : c.getTransitiveDependencies()) {
			deps_used.addAll(cc.getClazzpathUnits());
		}
	}
	final SortedSet<Clazz> removable = new TreeSet<Clazz>(classes); 

	log.info("Needed classes ["+ needed.size()+"] classes");
	log.info("Removable classes ["+ removable.size()+"] classes");
	log.info("Used dependencies ["+ deps_used.size()+"] out of [" + maven_deps.size() + "]");
	
	//TODO: write to target/vulas/...
	try {
		FileWriter writer=new FileWriter("removable.txt");
		for(Clazz clazz : removable) {
			writer.write(clazz + System.lineSeparator());
		}
		writer.close();
	
		writer=new FileWriter("needed.txt");
		for(Clazz c: needed) {
			writer.write(c + System.lineSeparator());
		}
		writer.close();
	
		writer=new FileWriter("missing.txt");
		for(Clazz c: missing) {
			writer.write(c + System.lineSeparator());
		}
		writer.close();
		
		writer=new FileWriter("removable_deps.txt");
		maven_deps.removeAll(deps_used);
		for(ClazzpathUnit c: maven_deps) {
			writer.write(c + System.lineSeparator());
		}
		writer.close();
	} catch (IOException e){// TODO Auto-generated catch block
		e.printStackTrace();
	} 
  }

  /** {@inheritDoc} */
  @Override 
  public void setTraces(Set<ConstructId> _traces){
	  this.traces = _traces;
  }

  /** {@inheritDoc} */
  @Override 
  public void setReachableConstructIds(Set<Dependency> _deps){
	  this.reachableConstructIds = _deps;
  }

}
