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
	
	List<ClazzpathUnit> app = new ArrayList<ClazzpathUnit>();
	Set<ClazzpathUnit> deps = new HashSet<ClazzpathUnit>();
	Set<ClazzpathUnit> deps_used = new HashSet<ClazzpathUnit>();
	
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
	    	  deps.add(cp.addClazzpathUnit(p));
	    	  
	      }
	    }
	} catch (IOException e) {
	      // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	log.info("App classpathUnit [" + app.size() +"]");
	for (ConstructId t : traces) {
		if(t.getType().equals(ConstructType.CLAS) || t.getType().equals(ConstructType.INTF) || t.getType().equals(ConstructType.ENUM)) {
			Clazz c = cp.getClazz(t.getQname());
			Set<ClazzpathUnit> units = c.getClazzpathUnits();
			if(units.size()>1) {
				log.warn("Added as entrypoints multiple ClasspathUnits from single class [" + c + "] : [");
				for(ClazzpathUnit u: units) {
					log.warn(u + ",");
				log.warn("]");
				}
			}
			app.addAll(units);
		}
	}
	log.info("App classpathUnit with traces [" + app.size() +"]");
	
	for (Dependency d : reachableConstructIds) {
		if(d.getReachableConstructIds()!=null) {
			for (ConstructId c : d.getReachableConstructIds()) {
				if(c.getType().equals(ConstructType.CLAS) || c.getType().equals(ConstructType.INTF) || c.getType().equals(ConstructType.ENUM)) {
					Clazz cl = cp.getClazz(c.getQname());
					Set<ClazzpathUnit> units = cl.getClazzpathUnits();
					if(units.size()>1) {
						log.warn("Added as entrypoints multiple ClasspathUnits from single class [" + cl + "] : [");
						for(ClazzpathUnit u: units) {
							log.warn(u + ",");
						log.warn("]");
						}
					}
					app.addAll(units);
				}
			}
		}
	}
	log.info("App classpathUnit with reachable constructs [" + app.size() +"]");
	
	log.info("Classpath classpathUnits [" + cp.getUnits().length +"]");
	

	SortedSet<Clazz> missing = new TreeSet<Clazz>();
	for (Clazz c : cp.getMissingClazzes()) {
		missing.add(c);
	}
		
	final SortedSet<Clazz> needed = new TreeSet<Clazz>();
	final Set<Clazz> cp_set = cp.getClazzes();
	for(ClazzpathUnit u: app) {
		cp_set.removeAll(u.getClazzes());
		cp_set.removeAll(u.getTransitiveDependencies());
		needed.addAll(u.getClazzes());
		needed.addAll(u.getDependencies());
		needed.addAll(u.getTransitiveDependencies());
		if(deps.contains(u)) 
			deps_used.add(u);
		for (Clazz c : u.getDependencies()) {
			deps_used.addAll(c.getClazzpathUnits());
		}
		for (Clazz c : u.getTransitiveDependencies()) {
			deps_used.addAll(c.getClazzpathUnits());
		}
	}
	final SortedSet<Clazz> removable = new TreeSet<Clazz>(cp_set); 

	log.info("Needed classes ["+ needed.size()+"] classes");
	log.info("Removable classes ["+ removable.size()+"] classes");
	log.info("Used dependencies ["+ deps_used.size()+"] out of [" + deps.size() + "]");
	
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
		deps.removeAll(deps_used);
		for(ClazzpathUnit c: deps) {
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
