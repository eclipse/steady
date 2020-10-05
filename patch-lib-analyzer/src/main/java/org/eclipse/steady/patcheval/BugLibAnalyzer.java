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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.steady.patcheval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.patcheval.representation.ConstructPathLibResult2;
import org.eclipse.steady.patcheval.representation.OrderedCCperConstructPath2;
import org.eclipse.steady.patcheval.representation.OverallConstructChange;
import org.eclipse.steady.patcheval.utils.CSVHelper2;
import org.eclipse.steady.patcheval.utils.PEConfiguration;
import org.eclipse.steady.shared.enums.ConstructChangeType;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.Bug;
import org.eclipse.steady.shared.json.model.ConstructChange;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.json.model.Library;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * Given a bug, this class analyzes all versions (retrieved from Maven central) of JARs containing vulnerable code produces a csv file.
 */
public class BugLibAnalyzer {
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private Bug bug;
  private ExecutorService executorService = null;

  /**
   * <p>Constructor for BugLibAnalyzer.</p>
   */
  public BugLibAnalyzer() {}

  /**
   * <p>Setter for the field <code>bug</code>.</p>
   *
   * @param _b a {@link org.eclipse.steady.shared.json.model.Bug} object.
   */
  public void setBug(Bug _b) {
    this.bug = _b;
  }

  /**
   * This method retrieves all versions of all libraries to be check for this.bug by using
   * GET /backend/bugs/{bugId}/libraries and  GET /cia/artifacts/{group)/{artifact}
   *
   * @return the list of libraries (having a libraryId) to be analysed for this.bug
   * @throws org.eclipse.steady.backend.BackendConnectionException
   * @throws java.lang.InterruptedException
   */
  public LinkedList<Artifact> getLibToCheck()
      throws BackendConnectionException, InterruptedException {

    LinkedList<LibraryId> libraryIdsToCheck = new LinkedList<>();

    // 1 - get all libraries containing vulnerable code from the api backend/bugs/{bugid}/libraries
    Library[] newApiLibraries = BackendConnector.getInstance().getBugLibraries(bug.getBugId());
    // collect all distinct g,a to be later used to get all versions
    HashMap<String, LibraryId> groupsArtifactsToCheck = new HashMap<>();
    for (Library l : newApiLibraries) {
      // We add to the list of libraries to be analysed only those having a libraryId
      if (l.getLibraryId() != null && !libraryIdsToCheck.contains(l.getLibraryId())) {
        libraryIdsToCheck.add(l.getLibraryId());
        String key = l.getLibraryId().getMvnGroup() + ":" + l.getLibraryId().getArtifact();
        if (!groupsArtifactsToCheck.containsKey(key)) {
          groupsArtifactsToCheck.put(key, l.getLibraryId());
        }
      }
    }
    List<String> excl_gavs =
        Arrays.asList(
            VulasConfiguration.getGlobal()
                .getConfiguration()
                .getStringArray(PEConfiguration.GAV_EXCLUDED));
    List<String> excl_gas =
        Arrays.asList(
            VulasConfiguration.getGlobal()
                .getConfiguration()
                .getStringArray(PEConfiguration.GA_EXCLUDED));
    List<String> excl_gs =
        Arrays.asList(
            VulasConfiguration.getGlobal()
                .getConfiguration()
                .getStringArray(PEConfiguration.GROUP_EXCLUDED));

    HashMap<String, LibraryId> groupsArtifactsToCheck_filtered = new HashMap<String, LibraryId>();
    // if a blacklist is given: apply it
    if (!excl_gavs.isEmpty() || !excl_gas.isEmpty() || !excl_gs.isEmpty()) {
      for (Entry<String, LibraryId> e : groupsArtifactsToCheck.entrySet()) {
        LibraryId value = (LibraryId) e.getValue();
        if ((!excl_gavs.contains(
                value.getMvnGroup() + ":" + value.getArtifact() + ":" + value.getVersion()))
            && (!excl_gas.contains(value.getMvnGroup() + ":" + value.getArtifact()))
            && (!excl_gs.contains(value.getMvnGroup()))) {
          groupsArtifactsToCheck_filtered.put(e.getKey(), e.getValue());
        } else {
          log.info(
              "Blacklisted library: Remove ["
                  + value.toString()
                  + "] from libraries to be checked");
        }
      }
    } else {
      groupsArtifactsToCheck_filtered = groupsArtifactsToCheck;
    }

    LinkedList<Artifact> finalLibrariesList = new LinkedList<>();
    // calling cia/artifacts/{G}/{A} for each g,a
    for (Entry<String, LibraryId> e : groupsArtifactsToCheck_filtered.entrySet()) {
      LibraryId value = (LibraryId) e.getValue();

      Artifact[] artifactsLibraries =
          BackendConnector.getInstance()
              .getAllArtifactsGroupArtifact(value.getMvnGroup(), value.getArtifact());
      boolean found = false;
      if (artifactsLibraries != null) {
        for (Artifact al : artifactsLibraries) {
          // the retrieval of the timestamp has been moved to createCSV(LinkedList<Artifact,File) to
          // avoid all the GETs for artifact that will not need to be processed, e.g., they are
          // already in the existing CSV
          // try to get timestamp if artifact does not have it (it can happen because the getAll for
          // nexus does not include it, whereas the get for a single libId from nexus does)
          //                	 if(al.getTimestamp()==null){
          //                		 Artifact singleArtifact =
          // BackendConnector.getInstance().getArtifact(al.getLibId().getMvnGroup(),
          // al.getLibId().getArtifact(),al.getLibId().getVersion());
          //                		 if(singleArtifact!=null && singleArtifact.getTimestamp()!=null)
          //                			 al.setTimestamp(singleArtifact.getTimestamp());
          //                	 }
          if (al.getLibId().equals(value)) found = true;
          if (!finalLibrariesList.contains(al)) {
            finalLibrariesList.add(al);
          }
        }
      }
      if (!found) {
        log.info(
            "Library Id [" + value.toString() + "] not found during search in Maven, Nexus, Pypi");
        // check if the JAR is available to patch Eval, if not, we do not consider the libid for the
        // analysis
        // check that Jar is available
        //                 boolean jarAvailable =
        // BackendConnector.getInstance().doesArtifactExist(value.getMvnGroup(),value.getArtifact(),value.getVersion(),false);
        //                 if(!jarAvailable){
        //                 	log.info("NO Jar for library Id [" + value.toString() +"] is available
        // in external services, skip patch eval analysis");
        //                 }
        //                 else {
        Artifact a = new Artifact(value.getMvnGroup(), value.getArtifact(), value.getVersion());
        if (!finalLibrariesList.contains(a)) finalLibrariesList.add(a);
        //             }
      }
    }

    LinkedList<Artifact> result = new LinkedList<Artifact>();
    // apply white and blacklists from configuration
    List<String> gavs =
        Arrays.asList(
            VulasConfiguration.getGlobal().getConfiguration().getStringArray(PEConfiguration.GAV));
    List<String> gas =
        Arrays.asList(
            VulasConfiguration.getGlobal().getConfiguration().getStringArray(PEConfiguration.GA));
    List<String> gs =
        Arrays.asList(
            VulasConfiguration.getGlobal()
                .getConfiguration()
                .getStringArray(PEConfiguration.GROUP));

    // if a whitelist is given: apply it
    if (!gavs.isEmpty() || !gas.isEmpty() || !gs.isEmpty()) {
      for (Artifact a : finalLibrariesList) {
        LibraryId lid = a.getLibId();
        if ((gavs.contains(lid.getMvnGroup() + ":" + lid.getArtifact() + ":" + lid.getVersion()))
            || (gas.contains(lid.getMvnGroup() + ":" + lid.getArtifact()))
            || (gs.contains(lid.getMvnGroup()))) {
          result.add(a);
        }
      }
    } else {
      result = finalLibrariesList;
    }
    // if a blacklist is given: apply it
    // TODO: blacklist already applied before, required again?
    //	     if(!excl_gavs.isEmpty() || !excl_gas.isEmpty()  || !excl_gs.isEmpty() ){
    //	    	 for(Artifact a : result){
    //	    		 LibraryId lid = a.getLibId();
    //	        	 if((
    // excl_gavs.contains(lid.getMvnGroup()+":"+lid.getArtifact()+":"+lid.getVersion())) ||
    //	        		( excl_gas.contains(lid.getMvnGroup()+":"+lid.getArtifact()))	||
    //	        		( excl_gs.contains(lid.getMvnGroup()))	){
    //	        		 result.remove(a);
    //	        	 }
    //	    	 }
    //	     }

    return result;
  }

  /**
   *
   * this method creates a CSV for all versions of all libraries related to this.bug and returns the csv as string
   *
   * @return the csv file containing the analysis results as string
   * @throws java.lang.InterruptedException
   * @throws org.eclipse.steady.backend.BackendConnectionException if any.
   */
  public String createCSV() throws BackendConnectionException, InterruptedException {
    LinkedList<Artifact> finalLibrariesList = this.getLibToCheck();
    if (finalLibrariesList.size() > 0) return this.createCSV(finalLibrariesList, null);
    else return "";
  }

  /**
   * this method analyzes the libraries provided as first argument and appends the result to the file provided as second arguments if it exists or creates a new one otherwise.
   *
   * @param finalLibrariesList the list of libraries to be analyzed
   * @param existing the file where to append the result (it will be created if null)
   * @return the csv file containing the analysis results as string
   * @throws org.eclipse.steady.backend.BackendConnectionException
   */
  public String createCSV(LinkedList<Artifact> finalLibrariesList, File existing)
      throws BackendConnectionException {

    // create list of overall construct changes (when the same construct,path is modified over
    // several commits)
    List<OrderedCCperConstructPath2> orderedQnamePerCC =
        new ArrayList<OrderedCCperConstructPath2>();
    Collection<ConstructChange> constructChanges = bug.getConstructChanges();

    Boolean hasJava = false;
    for (ConstructChange cc : constructChanges) {
      if (cc.getConstructId().getLang() == ProgrammingLanguage.JAVA) hasJava = true;
      // skip tests
      if (!isBelowTestDir(cc.getConstructId().getQname())
          && !isInTestClass(cc.getConstructId())
          && (cc.getConstructId().getType().equals(ConstructType.METH)
              || cc.getConstructId().getType().equals(ConstructType.CONS)
              || cc.getConstructId().getType().equals(ConstructType.FUNC)
              || cc.getConstructId().getType().equals(ConstructType.MODU))) {
        OrderedCCperConstructPath2 a =
            new OrderedCCperConstructPath2(cc.getConstructId(), cc.getRepoPath());
        if (orderedQnamePerCC.contains(a)) {
          orderedQnamePerCC.get(orderedQnamePerCC.indexOf(a)).addConstructChange(cc);
        } else {
          a.addConstructChange(cc);
          orderedQnamePerCC.add(a);
        }
      }
    }
    ProgrammingLanguage lang = null;
    if (hasJava) lang = ProgrammingLanguage.JAVA;
    else lang = ProgrammingLanguage.PY;

    LinkedList<OverallConstructChange> methsConsMOD = new LinkedList<OverallConstructChange>();
    LinkedList<OverallConstructChange> methsConsAD = new LinkedList<OverallConstructChange>();

    for (OrderedCCperConstructPath2 o : orderedQnamePerCC) {
      if (o.getOverallChangeType().equals(ConstructChangeType.MOD))
        methsConsMOD.add(o.getOverallCC());
      else methsConsAD.add(o.getOverallCC());
    }

    List<ConstructPathLibResult2> results = new ArrayList<ConstructPathLibResult2>();

    int libtoAnalize = finalLibrariesList.size();
    int returnedFromThread = 0;
    int modcount = methsConsMOD.size();
    int addcount = methsConsAD.size();

    // TODO: try to comment out the copies below! they seems useless!
    LinkedList<OverallConstructChange> mod = new LinkedList<OverallConstructChange>();
    for (OverallConstructChange a : methsConsMOD) {
      OverallConstructChange b =
          new OverallConstructChange(
              a.getFixedBody(),
              a.getBuggyBody(),
              a.getChangeType(),
              a.getRepoPath(),
              a.getConstructId());
      mod.add(b);
    }
    LinkedList<OverallConstructChange> ad = new LinkedList<OverallConstructChange>();
    for (OverallConstructChange c : methsConsAD) {
      OverallConstructChange d =
          new OverallConstructChange(
              c.getFixedBody(),
              c.getBuggyBody(),
              c.getChangeType(),
              c.getRepoPath(),
              c.getConstructId());
      ad.add(d);
    }

    executorService = Executors.newFixedThreadPool(4);
    Future<List<ConstructPathLibResult2>> f = null;
    Set<Future<List<ConstructPathLibResult2>>> set =
        new HashSet<Future<List<ConstructPathLibResult2>>>();
    log.info("[" + finalLibrariesList.size() + "] libraries to be analyzed");
    for (int t = 0; t < finalLibrariesList.size(); t++) {

      // retrieve libid timestamp if not available
      if (finalLibrariesList.get(t).getTimestamp() == null) {
        Artifact singleArtifact =
            BackendConnector.getInstance()
                .getArtifact(
                    finalLibrariesList.get(t).getLibId().getMvnGroup(),
                    finalLibrariesList.get(t).getLibId().getArtifact(),
                    finalLibrariesList.get(t).getLibId().getVersion());
        if (singleArtifact != null && singleArtifact.getTimestamp() != null)
          finalLibrariesList.get(t).setTimestamp(singleArtifact.getTimestamp());
      }
      Callable<List<ConstructPathLibResult2>> thread =
          new LibraryAnalyzerThread2(t, mod, ad, finalLibrariesList.get(t), lang);
      f = executorService.submit(thread);
      set.add(f);
    }

    executorService.shutdown();

    try {
      for (Future<List<ConstructPathLibResult2>> future : set) {
        List<ConstructPathLibResult2> res = future.get();
        returnedFromThread += res.size();
        results.addAll(res);
      }
      log.info(
          "Sent ["
              + libtoAnalize
              + "] libs to be analyzed for ["
              + modcount
              + "]MOD and ["
              + addcount
              + "] ADD, ["
              + returnedFromThread
              + "] returned.");

    } catch (InterruptedException | ExecutionException e1) {
      e1.printStackTrace();
    }

    log.info(
        "++++++++++++++++++++++++++++++++++++++++++++++++++ALL Thread executions"
            + " finished++++++++++++++++++++++++++++++++++++++++++++++");
    log.info(
        "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

    //  }

    File dir = new File(PEConfiguration.getBaseFolder().toString());

    if (!dir.exists()) {
      try {
        Files.createDirectories(dir.toPath());
      } catch (IOException ex) {
        log.error(ex);
      }
    }

    String file = "";
    if (results.size() > 0) {
      if (existing == null) file = CSVHelper2.writeResultsToFile(bug.getBugId(), results);
      else file = CSVHelper2.appendResultsToFile(bug.getBugId(), existing, results);
    }

    return file;
  }

  private static boolean isInTestClass(ConstructId _cid) {
    return (_cid.getQname().indexOf("test") != -1 || _cid.getQname().indexOf("Test") != -1);
  }

  private static boolean isBelowTestDir(String _p) {
    return _p != null && (_p.indexOf("/testcases/") != -1 || _p.indexOf("src/test/") != -1);
  }
}
