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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.patcheval2;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.bytecode.ConstructBytecodeASTManager;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.patcheval.representation.ArtifactResult2;
import com.sap.psr.vulas.patcheval.representation.Bug;
import com.sap.psr.vulas.patcheval.representation.ConstructPathAssessment2;
import com.sap.psr.vulas.patcheval.representation.Intersection2;
import com.sap.psr.vulas.patcheval.representation.ReleaseTree;
import com.sap.psr.vulas.patcheval.utils.PEConfiguration;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.AffectedLibrary;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.BugChangeList;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.Version;
import com.sap.psr.vulas.shared.json.model.metrics.Counter;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * This class analyzes all bugs (or the configured one) to determine the affected libraries.
 * If it doesn't already exists, it creates a csv file containing the summary of the analysis performed by the BugLibAnalyzer.
 * If a csv for the bug already exists, it checks whether there exist new libraries to be analyzed and adds them (if any).
 * Finally it computes the json of affected libraries.
 */
public class BugLibManager {
	

	
	private static final Log log = LogFactory.getLog(BugLibManager.class);

	// used to serialize cc
	BugChangeList bugChangeList = null; 
    static List<ConstructChange> methsConsCC = null;
    private Set<ArtifactResult2> lids = null;
   
    //data structure to keep all artifact results divided per GA
 //   private List<List<ArtifactResult2>> lidsXGA = new ArrayList<List<ArtifactResult2>>();
    
    //data structure to keep all artifact results divided per GA and minor release
 //   private List<List<ReleaseTree>> lr = new ArrayList<List<ReleaseTree>>();
    
    static HashMap<String,ConstructBytecodeASTManager> bytecodes = null;

	private ExecutorService pool = null;
	
	/**
	 * <p>Constructor for BugLibManager.</p>
	 */
	public BugLibManager(){
		this.lids = new TreeSet<ArtifactResult2>();
		BugLibManager.bytecodes  = new HashMap<String,ConstructBytecodeASTManager>();
    }
    
	
	/**
	 * <p>resetToBug.</p>
	 *
	 * @param b a {@link com.sap.psr.vulas.shared.json.model.BugChangeList} object.
	 */
	public void resetToBug(BugChangeList b){
		this.bugChangeList = b;
    	this.setChangeList();
		this.lids = new TreeSet<ArtifactResult2>();
		BugLibManager.bytecodes  = new HashMap<String,ConstructBytecodeASTManager>();
	}
    /**
     * This methods reads a CSV file f containing the results of the BugLibAnalyzer and stores the data into the set this.lids
     * Each line of the csv is an instance of class ArtifactResult2.
     *
     * @param f a {@link java.io.File} object.
     * @throws java.io.IOException
     */
    public void readFile(File f) throws IOException {
  		/* for csv scan */
		//BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try (BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(f.getAbsolutePath()), FileUtil.getCharset()) )){
			//br = new BufferedReader(new FileReader(f));

			// READ CSV as ArtifactResults2 into a TreeSet to order them by GA, timestamp, version
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(cvsSplitBy);
				processLine(splitLine);
				
			}
			if(lids.size()==0){
				BugLibManager.log.warn("File [" + f.getName()+"] is empty!");
			}
			BugLibManager.log.info("Read ["+lids.size()+"] artifactResults from file [" + f.getName()+"]");
			br.close();
		}
	}
    
	/**
	 * This method reads the string representation of the CSV computed by the BugLibAnalyzer
	 *
	 * @param f a {@link java.lang.String} object.
	 * @throws java.io.IOException
	 */
	public void readString(String f) throws IOException {
		/* for csv scan */
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		br = new BufferedReader(new StringReader(f));

		// READ CSV as ArtifactResults2 into a TreeSet to order them by GA,
		// timestamp, version

		while ((line = br.readLine()) != null) {
			// use comma as separator
			String[] splitLine = line.split(cvsSplitBy);
			processLine(splitLine);

		}
		if (lids.size() == 0) {
			BugLibManager.log.warn("File [" + bugChangeList.getBugId() + ".csv] is empty!");
		}
		br.close();

	}
        
	/**
	 * This method fills the data structures lidsXGA and lr based on the lids.
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<List<ReleaseTree>> computeGA() {

		// divide results per GA
		List<List<ArtifactResult2>> lidsXGA = new ArrayList<List<ArtifactResult2>>();
		ArtifactResult2[] ll = this.lids.toArray(new ArtifactResult2[lids.size()]);
		String g = null, a = null;
		List<ArtifactResult2> ga = new ArrayList<ArtifactResult2>();
		for (int i = 0; i < ll.length; i++) {
			// if we encounter a new library ga, find its releases and set them
			if (g != null && (!ll[i].getGroup().equals(g) || !ll[i].getArtifact().equals(a))) {

				BugLibManager.log.info("[" + ga.size() + "] versions for group artifact [" + ga.get(0).getGa() + "]");
				lidsXGA.add(new ArrayList<ArtifactResult2>(ga));
				ga.clear();
			}
			g = ll[i].getGroup();
			a = ll[i].getArtifact();
			ga.add(ll[i]);

			if (i == ll.length - 1) {
				BugLibManager.log.info("[" + ga.size() + "] versions for group artifact [" + ga.get(0).getGa() + "]");
				lidsXGA.add(new ArrayList<ArtifactResult2>(ga));
			}

		}

		// divide results per minor releases (a tree for 1.0, a tree for 1.1,
		// .... , a tree for 2.2, ..)

		List<List<ReleaseTree>> lr = new ArrayList<List<ReleaseTree>>();
		for (int lga = 0; lga < lidsXGA.size(); lga++) {
			List<ArtifactResult2> gaList = lidsXGA.get(lga);

			// create a list with all minor releases for GA
			List<ReleaseTree> gaRelList = new ArrayList<ReleaseTree>();
			for (int l = 0; l < gaList.size(); l++) {

				boolean added = false;
				// check whether a tree for the current minor release already
				// exists
				for (int i = 0; i < gaRelList.size(); i++) {
					if (gaRelList.get(i).getNode().getMinorRelease().equals(gaList.get(l).getMinorRelease())) {
						gaRelList.get(i).add(gaList.get(l));
						added = true;
					}
				}
				if (!added) {
					ReleaseTree rt = new ReleaseTree(gaList.get(l));
					gaRelList.add(rt);
				}

			}
			log.info("Created [" + gaRelList.size()+"] trees for ga [" + gaRelList.get(0).getNode().getGa().toString()+"]" );
//			for(ReleaseTree t: gaRelList){
//				log.info(t.toString());
//			}
			lr.add(gaRelList);

		}
		
		return lr;

	}
            	
            
    /**
     * 
     * This methods computes the results for each source (AST_EQUALITY, etc.) and creates the JSON to be uploaded or saved to file
     * @throws BackendConnectionException 
     * 
     */
    private void computeAndUploadResults() throws BackendConnectionException{
    	HashMap<String, JsonArray> gaResxSource = new HashMap<String, JsonArray>();
    	int overall_ast_equality_v = 0, overall_ast_equality_f = 0, overall_minor = 0, overall_major = 0, overall_intersection = 0, overall_greater = 0,
    			overall_toreview = 0;
    	int upload_ast_equality_v = 0, upload_ast_equality_f = 0, upload_minor = 0, upload_major = 0, upload_intersection = 0, upload_greater = 0,
    			upload_toreview = 0 ,  upload_propagate = 0;
    	
    	//true when only affectedlibraries not already existing in backend should be uploaded
    	boolean addnew = VulasConfiguration.getGlobal().getConfiguration().getBoolean(PEConfiguration.ADD_RESULTS);
    	HashMap<AffectedVersionSource, AffectedLibrary[]> existingxSource = new HashMap<AffectedVersionSource, AffectedLibrary[]>();
    	//if(addnew){
    	
    	// we always get all the existing affected versions (even if onlyAddNew is false)
    	// as we always need the results for MANUAL and PROGATE_MANUAL in order to further propagate
    	for(AffectedVersionSource s : AffectedVersionSource.values()){
    		AffectedLibrary[] al = BackendConnector.getInstance().getBugAffectedLibraries(bugChangeList.getBugId(),s.toString(),false);
    		existingxSource.put(s, al);
    		BugLibManager.log.info("Existing [" + al.length + "] affected libraries in backend for source [" +s.toString()+"]");
    	}
    	//}
    	
    	List<List<ReleaseTree>> lr = computeGA();
    	
		for (int ga = 0; ga < lr.size(); ga++) {
			BugLibManager.log.info("Collecting results for group artifact [" + lr.get(ga).get(0).getNode().getGa() + "]");

			ArtifactResult2 lastFirstFixed = null;
			List<ReleaseTree> relsxGA = lr.get(ga);

			int ast_equality_v = 0, ast_equality_f = 0, minor = 0, major = 0, intersection = 0, greater = 0,
					toreview = 0, propagate = 0;
			int new_ast_equality_v = 0, new_ast_equality_f = 0, new_minor = 0, new_major = 0, new_intersection = 0, new_greater = 0,
					new_toreview = 0;

			for (int r = 0; r < relsxGA.size(); r++) {
	
				String source = null;
				List<String> libAssessed = new ArrayList<String>();

				ReleaseTree tree = relsxGA.get(r);
				BugLibManager.log.info("Collecting results for minor release [" + tree.getNode().getMinorRelease() + "]");

				// find lastFirstFixed by equality
				if (lastFirstFixed == null || (tree.getLastFirstFixed() != null
						&& lastFirstFixed.getTimestamp()!=null && tree.getLastFirstFixed().getTimestamp()!=null
						&& lastFirstFixed.getTimestamp() < tree.getLastFirstFixed().getTimestamp())){
					lastFirstFixed = tree.getLastFirstFixed();
					if(lastFirstFixed!=null)
						BugLibManager.log.info("Last first fixed is [" + lastFirstFixed.toString() +"] based on AST equality to fixed");
				}
				// find lastFirstFixed by intersection
				if (lastFirstFixed == null || (tree.getISLastFirstFixed() != null
						&& lastFirstFixed.getTimestamp()!=null && tree.getISLastFirstFixed().getTimestamp()!=null
						&& lastFirstFixed.getTimestamp() < tree.getISLastFirstFixed().getTimestamp())){
					lastFirstFixed = tree.getISLastFirstFixed();
					if(lastFirstFixed!=null)
						BugLibManager.log.info("Last first fixed is [" + lastFirstFixed.toString() +"] based on Intersection");
				}
			
				boolean toAdd = true;

				List<ArtifactResult2> v = tree.getEqualsV();
				for (int i = 0; i < v.size(); i++) {
					if (!libAssessed.contains(v.get(i).getGroup().concat(v.get(i).getArtifact().concat(v.get(i).getVersion())))) {
						libAssessed.add(v.get(i).getGroup().concat(v.get(i).getArtifact().concat(v.get(i).getVersion())));
						ast_equality_v++;
						source = "AST_EQUALITY";
						
						toAdd = true;
						if(addnew){
							for(AffectedLibrary a: existingxSource.get(AffectedVersionSource.AST_EQUALITY)){
								if(a.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals(v.get(i).getGroup()) 
										&& a.getLibraryId().getArtifact().equals(v.get(i).getArtifact())
										&& a.getLibraryId().getVersion().equals(v.get(i).getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_ast_equality_v++;
							JsonObject result = createJsonResult(v.get(i), source, true);
							result.addProperty("overallConfidence", v.get(i).getVConfidence());
							result.addProperty("pathConfidence", v.get(i).getVPathConfidence());
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}
					}
				}
				List<ArtifactResult2> f = tree.getEqualsF();
				for (int i = 0; i < f.size(); i++) {
					if (!libAssessed.contains(f.get(i).getGroup().concat(f.get(i).getArtifact().concat(f.get(i).getVersion())))) {
						libAssessed.add(f.get(i).getGroup().concat(f.get(i).getArtifact().concat(f.get(i).getVersion())));
						ast_equality_f++;
						source = "AST_EQUALITY";
						
						toAdd = true;
						if(addnew){
							for(int j=0;j<existingxSource.get(AffectedVersionSource.AST_EQUALITY).length;j++){
								AffectedLibrary a = existingxSource.get(AffectedVersionSource.AST_EQUALITY)[j];
								if(a.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals(f.get(i).getGroup()) 
										&& a.getLibraryId().getArtifact().equals(f.get(i).getArtifact())
										&& a.getLibraryId().getVersion().equals(f.get(i).getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_ast_equality_f++;
							JsonObject result = createJsonResult(f.get(i), source, false);
							result.addProperty("overallConfidence", f.get(i).getFConfidence());
							result.addProperty("pathConfidence", f.get(i).getFPathConfidence());
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}
					}
				}
				
				
				HashMap<ArtifactResult2, ArtifactResult2> mv = tree.getMinorV();
				for (Entry<ArtifactResult2, ArtifactResult2> el : mv.entrySet()) {
					if (!libAssessed.contains(el.getKey().getGroup()
							.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())))) {
						libAssessed.add(el.getKey().getGroup()
								.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())));
						source = "MINOR_EQUALITY";
						minor++;
						toAdd = true;
						if(addnew){
							for(int j=0;j<existingxSource.get(AffectedVersionSource.MINOR_EQUALITY).length;j++){
								AffectedLibrary a = existingxSource.get(AffectedVersionSource.MINOR_EQUALITY)[j];
								if(a.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals(el.getKey().getGroup()) 
										&& a.getLibraryId().getArtifact().equals(el.getKey().getArtifact())
										&& a.getLibraryId().getVersion().equals(el.getKey().getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_minor++;
							JsonObject result = createJsonResult(el.getKey(), source, true);
	
							result.addProperty("lastVulnerable", el.getValue().toString());
	
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}

					}
				}
     		
			
				HashMap<ArtifactResult2, ArtifactResult2> mf = tree.getMajorF();

				for (Entry<ArtifactResult2, ArtifactResult2> el : mf.entrySet()) {
					if (!libAssessed.contains(el.getKey().getGroup()
							.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())))) {
						libAssessed.add(el.getKey().getGroup()
								.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())));

						source = "MAJOR_EQUALITY";
						major++;
						toAdd = true;
						if(addnew){
							for(int j=0;j<existingxSource.get(AffectedVersionSource.MAJOR_EQUALITY).length;j++){
								AffectedLibrary a = existingxSource.get(AffectedVersionSource.MAJOR_EQUALITY)[j];
								if(a.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals(el.getKey().getGroup()) 
										&& a.getLibraryId().getArtifact().equals(el.getKey().getArtifact())
										&& a.getLibraryId().getVersion().equals(el.getKey().getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_major++;
							JsonObject result = createJsonResult(el.getKey(), source, false);
							result.addProperty("source", "MAJOR_EQUALITY");
	
							result.addProperty("firstFixed", el.getValue().toString());
	
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}
					}
				}
				
				
				HashMap<ArtifactResult2, Intersection2> posti = tree.getPostIntersection();

				for (Entry<ArtifactResult2, Intersection2> el : posti.entrySet()) {
					if (!libAssessed.contains(el.getKey().getGroup()
							.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())))) {
						libAssessed.add(el.getKey().getGroup()
								.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())));

						source = "INTERSECTION";
						intersection++;
						toAdd = true;
						if(addnew){
							for(int j=0;j<existingxSource.get(AffectedVersionSource.INTERSECTION).length;j++){
								AffectedLibrary a = existingxSource.get(AffectedVersionSource.INTERSECTION)[j];
							
								if(a.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals(el.getKey().getGroup()) 
										&& a.getLibraryId().getArtifact().equals(el.getKey().getArtifact())
										&& a.getLibraryId().getVersion().equals(el.getKey().getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_intersection++;
							JsonObject result = createJsonResult(el.getKey(), source, false);
	
							result.addProperty("fromIntersection", el.getValue().getFrom().toString());
							result.addProperty("toIntersection", el.getValue().getTo().toString());
	
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}
					}

				}
        			
				HashMap<ArtifactResult2, Intersection2> prei = tree.getBeforeIntersection();

				for (Entry<ArtifactResult2, Intersection2> el : prei.entrySet()) {
					if (!libAssessed.contains(el.getKey().getGroup()
							.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())))) {
						libAssessed.add(el.getKey().getGroup()
								.concat(el.getKey().getArtifact().concat(el.getKey().getVersion())));

						source = "INTERSECTION";
						intersection++;
						
						toAdd = true;
						if(addnew){
							for(int j=0;j<existingxSource.get(AffectedVersionSource.INTERSECTION).length;j++){
								AffectedLibrary a = existingxSource.get(AffectedVersionSource.INTERSECTION)[j];
							
								if(a.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals(el.getKey().getGroup()) 
										&& a.getLibraryId().getArtifact().equals(el.getKey().getArtifact())
										&& a.getLibraryId().getVersion().equals(el.getKey().getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_intersection++;
							JsonObject result = createJsonResult(el.getKey(), source, true);
	
							result.addProperty("fromIntersection", el.getValue().getFrom().toString());
							result.addProperty("toIntersection", el.getValue().getTo().toString());
							result.addProperty("overallConfidence", el.getValue().getConfidence());
	
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}
					}
				}
				
			
				// check greater release
				if (libAssessed.size() == 0 && lastFirstFixed != null && tree.getNode().getTimestamp()!=null && lastFirstFixed.getTimestamp()!=null
						&& tree.getNode().getTimestamp() > lastFirstFixed.getTimestamp()) {
					for (ArtifactResult2 a : tree.getNodes()) {
						greater++;

						source = "GREATER_RELEASE";
						toAdd = true;
						if(addnew){
							for(int j=0;j<existingxSource.get(AffectedVersionSource.GREATER_RELEASE).length;j++){
								AffectedLibrary a1 = existingxSource.get(AffectedVersionSource.GREATER_RELEASE)[j];
							
								if(a1.getLibraryId()!=null && a1.getLibraryId().getMvnGroup().equals(a.getGroup()) 
										&& a1.getLibraryId().getArtifact().equals(a.getArtifact())
										&& a1.getLibraryId().getVersion().equals(a.getVersion())){
									toAdd = false;
									break;
								}
							}
						}
						if(toAdd){
							new_greater++;
							JsonObject result = createJsonResult(a, source, false);
	
							JsonArray sourceResult = null;
							if (gaResxSource.containsKey(source)) {
								sourceResult = gaResxSource.get(source);
							} else {
								sourceResult = new JsonArray();
							}
							sourceResult.add(result);
							gaResxSource.put(source, sourceResult);
						}
					}
				}
				
				// if it's not a greater release, upload the available
				// information for the libraries on which no conclusion was
				// taken
				// and check whether we can propagate existing manual assessments 
				else {
					
					//retrieve Manual assessment
					List<AffectedLibrary> manual = new ArrayList<AffectedLibrary>();
					for(int j=0;j<existingxSource.get(AffectedVersionSource.MANUAL).length;j++){
						AffectedLibrary a1 = existingxSource.get(AffectedVersionSource.MANUAL)[j];
						if(a1.getLibraryId()!=null && a1.getLibraryId().getMvnGroup().equals(tree.getNode().getGroup()) 
								&& a1.getLibraryId().getArtifact().equals(tree.getNode().getArtifact())
								&& a1.getLibraryId().getVersion().startsWith(tree.getNode().getMinorRelease())){
							manual.add(a1);
						}
					}
					//retrieve existing propagate_manual
					for(int j=0;j<existingxSource.get(AffectedVersionSource.PROPAGATE_MANUAL).length;j++){
						AffectedLibrary a1 = existingxSource.get(AffectedVersionSource.PROPAGATE_MANUAL)[j];
						if(a1.getLibraryId()!=null && a1.getLibraryId().getMvnGroup().equals(tree.getNode().getGroup()) 
								&& a1.getLibraryId().getArtifact().equals(tree.getNode().getArtifact())
								&& a1.getLibraryId().getVersion().startsWith(tree.getNode().getMinorRelease())){
							manual.add(a1);
						}
					}
					
					for (ArtifactResult2 a : tree.getNodes()) {
						if (!libAssessed.contains(a.getGroup().concat(a.getArtifact().concat(a.getVersion())))) {
							
							//add artifactResult to to_review
							toreview++;

							source = "TO_REVIEW";
							toAdd = true;
							if(addnew){
								for(int j=0;j<existingxSource.get(AffectedVersionSource.TO_REVIEW).length;j++){
									AffectedLibrary a1 = existingxSource.get(AffectedVersionSource.TO_REVIEW)[j];
									if(a1.getLibraryId()!=null && a1.getLibraryId().getMvnGroup().equals(a.getGroup()) 
											&& a1.getLibraryId().getArtifact().equals(a.getArtifact())
											&& a1.getLibraryId().getVersion().equals(a.getVersion())){
										toAdd = false;
										break;
									}
								}
							}
							if(toAdd){
								new_toreview++;
								JsonObject result = createJsonResult(a, source, null);
	
								JsonArray sourceResult = null;
								if (gaResxSource.containsKey(source)) {
									sourceResult = gaResxSource.get(source);
								} else {
									sourceResult = new JsonArray();
								}
								sourceResult.add(result);
								gaResxSource.put(source, sourceResult);
							}
							
							//add artifactResult to propagate_manual
							source = "PROPAGATE_MANUAL";
							boolean isGreater=false,isSmaller=false;
							for(AffectedLibrary i :manual){
								if(i.getLibraryId()!=null && i.getLibraryId().getMvnGroup().equals(a.getGroup()) && 
										i.getLibraryId().getArtifact().equals(a.getArtifact()) ){
									Version toCompare = new Version(i.getLibraryId().getVersion());
									
									if(a.getVersionObject().getMaintenanceRelease().equals(toCompare.getMaintenanceRelease()) ||
											toCompare.isMaintenanceRelease()){
										if(a.getVersionObject().compareTo(toCompare)==0){
											isGreater=false;
											break;
										}
										else if (!i.getAffected() && a.getVersionObject().compareTo(toCompare)>0){
											isGreater=true;
										}
										else if (i.getAffected() && a.getVersionObject().compareTo(toCompare)<0){
											isSmaller=true;
										}
									}
								}
							}
							if(isGreater&&!isSmaller){
								propagate++;
								log.info("Creating Json for PROPAGATE_MANUAL for artifact [" + a.toString()+"]");
								JsonObject result = createJsonResult(a, source, false);
	
								JsonArray sourceResult = null;
								if (gaResxSource.containsKey(source)) {
									sourceResult = gaResxSource.get(source);
								} else {
									sourceResult = new JsonArray();
								}
								sourceResult.add(result);
								gaResxSource.put(source, sourceResult);
							}
							
							
						}
					}
				}
        				
        		
        		
        		
				// 
				// if next round is new GA or list is finished, log stats//&& !gaResxSource.isEmpty()
				if (relsxGA.size() == r + 1 ) { 
						log.info("Computed results for GA [" + lr.get(ga).get(0).getNode().getGa() + "]");
						log.info("[" + ast_equality_v + "] results for source AST_EQUALITY to vulnerable, [" +new_ast_equality_v+ "] new to upload");
						log.info("[" + ast_equality_f + "] results for source AST_EQUALITY to fixed, [" +new_ast_equality_f+ "] new to upload");
						log.info("[" + minor + "] results for source minor_EQUALITY, [" +new_minor+ "] new to upload");
						log.info("[" + major + "] results for source major_EQUALITY, [" +new_major+ "] new to upload");
						log.info("[" + intersection + "] results for source INTERSECTION, [" +new_intersection+ "] new to upload");
						log.info("[" + greater + "] results for source GREATER_RELEASE, [" +new_greater+ "] new to upload");
						log.info("[" + toreview + "] results for source TO_REVIEW, [" +new_toreview+ "] new to upload");
						log.info("[" + propagate + "] results for source PROPAGATE, all to upload");
						overall_ast_equality_v += ast_equality_v; ast_equality_v=0;
						upload_ast_equality_v += new_ast_equality_v; new_ast_equality_v=0;
						overall_ast_equality_f += ast_equality_f; ast_equality_f = 0;
						upload_ast_equality_f += new_ast_equality_f; new_ast_equality_f = 0;
						overall_minor += minor; minor = 0;
						upload_minor += new_minor; new_minor = 0;
						overall_major += major; major = 0;
						upload_major += new_major; new_major = 0;
						overall_intersection += intersection; intersection=0;
						upload_intersection += new_intersection; new_intersection=0;
						overall_greater += greater; greater= 0;
						upload_greater += new_greater; new_greater= 0;
						overall_toreview += toreview; toreview = 0;
						upload_toreview += new_toreview; new_toreview = 0;
						upload_propagate += propagate; propagate = 0;
					
				}
			}
		}
		log.info("Overall results");
		log.info("[" + overall_ast_equality_v + "] results for source AST_EQUALITY to vulnerable [" +upload_ast_equality_v +"] new to upload");
		log.info("[" + overall_ast_equality_f + "] results for source AST_EQUALITY to fixed [" +upload_ast_equality_f+"] new to upload");
		log.info("[" + overall_minor + "] results for source minor_EQUALITY [" +upload_minor+"] new to upload");
		log.info("[" + overall_major + "] results for source major_EQUALITY [" +upload_major+"] new to upload");
		log.info("[" + overall_intersection + "] results for source INTERSECTION [" +upload_intersection+"] new to upload");
		log.info("[" + overall_greater + "] results for source GREATER_RELEASE [" +upload_greater+"] new to upload");
		log.info("[" + overall_toreview + "] results for source TO_REVIEW [" +upload_toreview+"] new to upload");
		log.info("[" + upload_propagate + "] results for source PROPAGATE");
		 
		// save to file or upload results per GA
			if (!gaResxSource.isEmpty()) {
				
				if (VulasConfiguration.getGlobal().getConfiguration().getBoolean(PEConfiguration.UPLOAD_RESULTS) == true) {
					try {
						
						Collection<Counter> existingAffLib = bugChangeList.getCountAffLibIds().getCounters();
						
						//delete old results for sources that did not lead any result
						if(overall_ast_equality_v==0 && overall_ast_equality_f==0)
							BackendConnector.getInstance().deletePatchEvalResults(bugChangeList.getBugId(), "AST_EQUALITY");
						if(overall_minor==0)
							BackendConnector.getInstance().deletePatchEvalResults(bugChangeList.getBugId(), "MINOR_EQUALITY");
						if(overall_major==0)
							BackendConnector.getInstance().deletePatchEvalResults(bugChangeList.getBugId(), "MAJOR_EQUALITY");
						if(overall_intersection==0)
							BackendConnector.getInstance().deletePatchEvalResults(bugChangeList.getBugId(), "INTERSECTION");
						if(overall_greater==0)
							BackendConnector.getInstance().deletePatchEvalResults(bugChangeList.getBugId(), "GREATER_RELEASE");
						//upload results if they are more or less than the already existing ones 
					
						for (Entry<String,JsonArray> e : gaResxSource.entrySet()) {
							log.info("Uploading results for source " + e.getKey());
							
							boolean toUpload = true;
							if(!addnew){
								for(Counter i : existingAffLib){
									if(i.getName().equals(e.getKey()) && i.getCount()==e.getValue().size()){
										toUpload=false;
										BugLibManager.log.info("Patch Eval concluded for ["+gaResxSource.get(e.getKey()).size() + "] libs for source ["+e.getKey()+"], results already existing [" +i.getCount()+ "]: skip upload" );
									}
										
								}
							}
							
							
							if(toUpload){
								if(!addnew){
									// delete all previous results and upload the new ones
									BackendConnector.getInstance().deletePatchEvalResults(bugChangeList.getBugId(), e.getKey());
									
								}
								BackendConnector.getInstance().uploadPatchEvalResults(bugChangeList.getBugId(),gaResxSource.get(e.getKey()).toString(), e.getKey());
							}
						
						}
					} catch (BackendConnectionException ex) {
						log.error(ex);
					}
				} else {
					// save to file
					for (Entry<String,JsonArray> e : gaResxSource.entrySet()) {
						final File json_file = Paths.get(PEConfiguration.getBaseFolder().toString() + File.separator+ bugChangeList.getBugId() + "_" + e.getKey() + "_" + ".json").toFile();
						try {
							FileUtil.writeToFile(json_file, gaResxSource.get(e.getKey()).toString());
						} catch (IOException exception) {
							exception.printStackTrace();
						}
						log.info("Results for source " + e.getKey() + " written to [" + json_file + "]");

					}
				}

			}		 
        	
        }
         
        	 
        
        
  
    /**
     * Checks for the consistency of the line, cannot be both equal to vulnerable and fixed
     * @param String eV
     * @param String eF
     * @return boolean
     */
    private static boolean isLineConsistent(String eV, String eF){
        // no equalV==True && equalF==True
        if ( eV.equals("0") && eF.equals("0") ) {
            BugLibManager.log.error("############LINE CONFLICT: found equality ON V and F");
            return false;
        }
        return true;
    }
    
    /**
     * <p>analyze.</p>
     *
     * @param bugsToAnalyze a {@link java.util.List} object.
     * @throws java.lang.InterruptedException if any.
     */
    public static void analyze(List<Bug> bugsToAnalyze) throws InterruptedException{
        
    	//necessary read_write as the ast_diff is done using POST
		VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_WRITE.toString());
    	BugLibAnalyzer bla = new BugLibAnalyzer();
    	BugLibManager  bm = new BugLibManager();
        int count=0;	        
        for ( Bug bug : bugsToAnalyze ){
        	try {
	        	BugChangeList b = BackendConnector.getInstance().getBug(bug.getBugId());
	    		
	        	if(b==null){ 
					BugLibManager.log.error("Error getting bug; the bug [" + bug.getBugId() + "] does not exist in the backend");
	        	}
	        	else{
	        		boolean mod_exists = false;
	        		boolean no_cc = false;
	        	
	        		if(b.getConstructChanges()!=null && b.getConstructChanges().isEmpty()){
	        			no_cc=true;
	        			BugLibManager.log.info("Bug ["+b.getBugId()+"] does not have any construct change, we still continue to propagate the MANUAL results.");
	        		}
	        		//the following loop and subsequent if on mod_exists are not really needed any longer as, not matter the result, we still proceed with the analysis. For now they are still present to provide the logging info
	        		for(ConstructChange c: b.getConstructChanges()){
	        			if((c.getConstructId().getType().equals(ConstructType.CONS)||c.getConstructId().getType().equals(ConstructType.METH))&&c.getConstructChangeType().equals(ConstructChangeType.MOD)){
	        				mod_exists=true;
	        				BugLibManager.log.info("At least one MOD constructor/method for bug ["+b.getBugId()+"] exists.");
	        				break;
	        			}
	        		}
	        		if(!mod_exists){
	        			BugLibManager.log.info("No-MOD constructor/method for bug ["+b.getBugId()+"] exists, we still continue to propagate the MANUAL results.");
	        		}
	        		
	        		bm.resetToBug(b);
			        File f = bm.getCsv();
			        bla.setBug(b);
	        		
	        		if(!no_cc){				        	
				        
				      //if CSV does not exist, create it
				        if (f==null){
				            bm.readString(bla.createCSV());
				        }
				        else{
			        		bm.readFile(f);
							//check whether existing csv contains all libraries that would be analyzed if patch Eval is run
							LinkedList<Artifact> latestList = bla.getLibToCheck();
							BugLibManager.log.info("["+latestList.size()+"] libraries to analyze returned from backend+cia");
							LinkedList<Artifact> newlibs = new LinkedList<Artifact>();
							//retrieve all libraries contained in the CSV
							List<Artifact> old = new ArrayList<Artifact>();
							for(ArtifactResult2 a : bm.lids){
								Artifact old_artifact = new Artifact(a.getGroup(), a.getArtifact(), a.getVersion());
								if(a.getTimestamp()!=null)
									old_artifact.setTimestamp(a.getTimestamp());
								old.add(old_artifact);
							}
							//check whether there are some new ones among the latest
							for(Artifact l:latestList){
								//if(!old.contains(l))
								boolean found=false;
								for(Artifact o: old){
									if(o.getLibId().equals(l.getLibId())){
										found=true;
										break;
									}
								}
								if(!found)
									newlibs.add(l);
							}
							if(newlibs.size()>0){
								BugLibManager.log.info("["+newlibs.size()+"] new releases exists! Going to analyze and add them to the CSV");
								bm.readString(bla.createCSV(newlibs,f));
							}
				        
						
				        }
			       
				        //try to compare bitecode
				        bm.compareBytecode();
				        
				    //    bm.computeGA();
				      	bm.computeAndUploadResults();
		        		
	        		}
	        		else{
	        			bm.computeAndUploadPropagateResults();    	     
	        		}
	        	}
	           
	        } catch (FileNotFoundException e) {
				BugLibManager.log.error("CSV file not found : " + e);
			} catch (IOException e) {
				BugLibManager.log.error("Error reasding CSV file" + e);
			
			} catch (BackendConnectionException bce){
				if(bce.getHttpResponseStatus()==503)
					log.error("Service still unavailable (503) after 1h, could not analyze bugs");
				else
					BugLibManager.log.error("Cannot analyze bug [" +bug.getBugId()+ "], exception occurred. Cause : " + bce.getCause() );
				Thread.sleep(10000);
			}
        	log.info("###################################################################");
            log.info("*******************************************************************");
            log.info("BUG [" + bug.getBugId() + "] completed.");
            log.info("Status: " + (count+1)+ "/" + bugsToAnalyze.size());
            log.info("*******************************************************************");
            log.info("###################################################################");
		    count++;
        }

        
    }
    
    //this method is used for bugs without construct changes to propagate manual (and already propagated) assessments (affected-false) to newer versions within the same major release
    private void computeAndUploadPropagateResults() throws BackendConnectionException{
    	List<AffectedLibrary> existingManual = new ArrayList<AffectedLibrary>();
		existingManual.addAll(Arrays.asList(BackendConnector.getInstance().getBugAffectedLibraries(bugChangeList.getBugId(),AffectedVersionSource.MANUAL.toString(),false)));
		existingManual.addAll(Arrays.asList(BackendConnector.getInstance().getBugAffectedLibraries(bugChangeList.getBugId(),AffectedVersionSource.PROPAGATE_MANUAL.toString(),false)));
    	
		BugLibManager.log.info("Existing [" + existingManual.size() + "] affected libraries in backend for sources MANUAL and PROPAGATE_MANUAL.");
		
        List<String> groupsArtifactsToCheck = new ArrayList<>();
		List<Artifact> gavToBeAssessed = new ArrayList<Artifact>();
		List<Artifact> gavAssessed = new ArrayList<Artifact>();
        for (AffectedLibrary al : existingManual){
            if ( al.getLibraryId() != null ){
            	String ga = al.getLibraryId().getMvnGroup() + ":" + al.getLibraryId().getArtifact();
            	gavAssessed.add(new Artifact(al.getLibraryId().getMvnGroup(),al.getLibraryId().getArtifact(),al.getLibraryId().getVersion()));
            	if(!groupsArtifactsToCheck.contains(ga) ){
            		groupsArtifactsToCheck.add(ga);
            		Artifact[] artifactsLibraries = BackendConnector.getInstance().getAllArtifactsGroupArtifact(al.getLibraryId().getMvnGroup(), al.getLibraryId().getArtifact());
            		if ( artifactsLibraries != null ){
                        for ( Artifact a : artifactsLibraries){
                        	if ( !gavToBeAssessed.contains(a)){
                        		gavToBeAssessed.add(a);
	                   	 	}
                        }
            		}
            	}
            }
        }
		
		
		//add artifactResult to propagate_manual
		String source = "PROPAGATE_MANUAL";
		boolean isGreater=false,isSmaller=false;
		int propagate=0;
		JsonArray sourceResult = new JsonArray();
		
		for(Artifact a : gavToBeAssessed){
			if(!gavAssessed.contains(a)){
				isGreater=false;
				isSmaller=false;
				for(AffectedLibrary i :existingManual){
					if(i.getLibraryId()!=null && i.getLibraryId().getMvnGroup().equals(a.getLibId().getMvnGroup()) && 
							i.getLibraryId().getArtifact().equals(a.getLibId().getArtifact()) ){
						Version toCompare = new Version(i.getLibraryId().getVersion());
						Version current = new Version(a.getLibId().getVersion());
						
						if(current.getMajorRelease().equals(toCompare.getMajorRelease()) &&
								Integer.parseInt((current.getMinorRelease().split("\\."))[1])> Integer.parseInt((toCompare.getMinorRelease().split("\\."))[1]) ){
							
							if(current.getMaintenanceRelease().equals(toCompare.getMaintenanceRelease()) ||
									toCompare.isMaintenanceRelease()){
								if(current.compareTo(toCompare)==0){
									isGreater=false;
									break;
								}
								else if (!i.getAffected() && current.compareTo(toCompare)>0){
									isGreater=true;
								}
								else if (i.getAffected() && current.compareTo(toCompare)<0){
									isSmaller=true;
								}
							}
						}
					}
				}
				
				if(isGreater&&!isSmaller){
					propagate++;
					log.info("Creating Json for PROPAGATE_MANUAL for artifact [" + a.toString()+"]");
					JsonObject result = createJsonResult(a, source, false);
					
					sourceResult.add(result);
				}
			}			
		}
		BugLibManager.log.info("Propagated results for [" + propagate + "] artifacts.");
		if(propagate>0){
			if (VulasConfiguration.getGlobal().getConfiguration().getBoolean(PEConfiguration.UPLOAD_RESULTS) == true) {
				BackendConnector.getInstance().uploadPatchEvalResults(bugChangeList.getBugId(),sourceResult.toString(), source);
			} else {
				// save to file
	
				final File json_file = Paths.get(PEConfiguration.getBaseFolder().toString() + File.separator+ bugChangeList.getBugId() + "_" + source + "_" + ".json").toFile();
				try {
					FileUtil.writeToFile(json_file, sourceResult.toString());
				} catch (IOException exception) {
					exception.printStackTrace();
				}
				log.info("Results for source PROPAGATE_MANUAL written to [" + json_file + "]");
			}
		}

    }
    
    private void compareBytecode() {
    	this.pool = Executors.newFixedThreadPool(4); 
    	
    	int count=0;

    	for(ArtifactResult2 ar:this.lids){
    		//log.info("Compare Bytecode for library [" + ar.toString()+ "]");
    		//check that none of the construct was compared
    		boolean toCompare=true;
    		for(ConstructPathAssessment2 cpa :ar.getConstructPathAssessments()){
    			//the check on doneComparisons avoids that we re-analize libs for which the bytecode was compared (for some constructs) but no equalities were found
    			//if((cpa.getType().equals("MOD") && cpa.getQnameInJar() && (cpa.getdToF()!=-1 || cpa.getdToV()!=-1 || cpa.getDoneComparisons()!=null))
    			// ||(cpa.getType().equals("ADD")&&cpa.getDoneComparisons()!=null)){
    			//TODO check condition (add required?)
    			if((cpa.getType().equals("MOD") && cpa.getQnameInJar() && (cpa.getdToF()!=-1 || cpa.getdToV()!=-1 ))
    		    			){	
    				toCompare=false;
    				break;
    			}
    		}
    		if(toCompare){    			
    			// Create a ByteCodeComparator for all archive to compare, and ask the thread pool to start them
    			ByteCodeComparator b = new ByteCodeComparator(ar,bugChangeList.getBugId());
    		
    			this.pool.execute(b);
    			count++;
    		}	
    			
    		
    	}
    	this.pool.shutdown();
    	try {
    		this.pool.awaitTermination(2, TimeUnit.HOURS);
			//while (!this.pool.awaitTermination(10, TimeUnit.SECONDS))
				//log.info("Wait for the completion of Bytecode comparison...");
		} catch (InterruptedException e) {
			log.error("Interrupt execution of bytecode comparison (timeout of 2H)");
		}
		
		log.info("ByteCodeComparison: a total of [" + count + "] archives compared for [" + bytecodes.size()+"] construct paths.");
		//CSVHelper2.rewriteCSV(bugChangeList.getBugId(), lids);
				
	}
    
	private File getCsv(){
    	String baseFolder = PEConfiguration.getBaseFolder().toString();
        File containingFolder = new File(baseFolder);
        if ( !containingFolder.exists() ) {
            try {
                Files.createDirectories(containingFolder.toPath());
            } catch (IOException ex) {
            	log.error(ex);
            }
        }
        String filePath = baseFolder+File.separator+this.bugChangeList.getBugId()+".csv";
        File f = new File(filePath);
        if ( f.exists() ){
        	BugLibManager.log.info("Found file [" + f.getName()+"]");
        	return f;
        }
        else 
        	return null;
    }
    
    
    private void processLine(String[] splitLine){
    	String baseFolder = PEConfiguration.getBaseFolder().toString()+File.separator;
    	if (splitLine.length>0&&splitLine[0].compareTo("") != 0 && splitLine[2].compareTo("Group") != 0) {
			if (isLineConsistent(splitLine[8], splitLine[9])) {
				ConstructPathAssessment2 cpa = null ;
				if(splitLine[5].equals("MOD")){
					String vulnAst = null, fixedAst=null,testedAst=null;
					
					//retrieve vulnAst
					if(splitLine[10]!=null && !splitLine[10].equals("")){
						try {
							if(splitLine[10].contains("\\")){
								splitLine[10] = splitLine[10].replace('\\', File.separator.charAt(0));
							}else if(splitLine[10].contains("/")){
								splitLine[10] = splitLine[10].replace('/', File.separator.charAt(0));
							}
							vulnAst = FileUtil.readFile(baseFolder+splitLine[10]);
						} catch (FileNotFoundException e) {
							BugLibManager.log.error("Couldn't find file [" +splitLine[10] + "]");
						} catch (IOException e) {
							BugLibManager.log.error("Couldn't read file [" +splitLine[10] + "]");
						}
					}
					//retrieve fixedAst
					if(splitLine[11]!=null && !splitLine[11].equals("")){
						try {
							if(splitLine[11].contains("\\")){
								splitLine[11] = splitLine[11].replace('\\', File.separator.charAt(0));
							}else if(splitLine[11].contains("/")){
								splitLine[11] = splitLine[11].replace('/', File.separator.charAt(0));
							}
							fixedAst = FileUtil.readFile(baseFolder+splitLine[11]);
						} catch (FileNotFoundException e) {
							BugLibManager.log.error("Couldn't find file [" +splitLine[11] + "]");
						} catch (IOException e) {
							BugLibManager.log.error("Couldn't read file [" +splitLine[11] + "]");
						}
					}
					//retrieve testedAst
					if(splitLine[12]!=null && !splitLine[12].equals("")){
						try {
							if(splitLine[12].contains("\\")){
								splitLine[12] = splitLine[12].replace('\\', File.separator.charAt(0));
							}else if(splitLine[12].contains("/")){
								splitLine[12] = splitLine[12].replace('/', File.separator.charAt(0));
							}
							if(!splitLine[12].contains("_testedAst_") && splitLine[12].contains("_vulnAst_")){
								BugLibManager.log.error("Tested-AST affected by previous bug: vuln AST was saved as tested one for bug[" +bugChangeList.getBugId() + "]");
							}
							testedAst = FileUtil.readFile(baseFolder+splitLine[12]);
						} catch (FileNotFoundException e) {
							BugLibManager.log.error("Couldn't find file [" +splitLine[12] + "]");
						} catch (IOException e) {
							BugLibManager.log.error("Couldn't read file [" +splitLine[12] + "]");
						}
					}
					
					//************ use constructchange list to find the type of the construct because is needed by the ast api but we
					// don't have it in the CSV!
					ConstructType type = null;
					for(ConstructChange c:methsConsCC){
						if(c.getConstructId().getQname().equals(splitLine[0]))
							type=c.getConstructId().getType();
					}
					
					cpa = new ConstructPathAssessment2(splitLine[0], splitLine[1],type,
							Boolean.valueOf(splitLine[6]), 
							testedAst, Integer.parseInt(splitLine[8]),
							Integer.parseInt(splitLine[9]), splitLine[5],vulnAst,fixedAst);
					
					//collect vuln and fixed bytecodes (only for matches that did not originated from bytecode)
					//and only if the type of the constuct is not null
					if(Integer.parseInt(splitLine[8])==0 && !(Integer.parseInt(splitLine[9])==-1)){
						if(type!=null){
						
							LibraryId current = new LibraryId(splitLine[2], splitLine[3], splitLine[4]);
							if(bytecodes.containsKey(splitLine[0].concat(splitLine[1]))){
								ConstructBytecodeASTManager mgr = bytecodes.get(splitLine[0].concat(splitLine[1]));
								Set<LibraryId> existing = mgr.getVulnLids();
								if(!existing.contains(current)){
									mgr.addVulnLid(current);
									bytecodes.put(splitLine[0].concat(splitLine[1]),mgr);
								}
							}
							else{
								//add new entry
								ConstructBytecodeASTManager mgr  = new ConstructBytecodeASTManager(splitLine[0], splitLine[1], type);
								mgr.addVulnLid(current);
								bytecodes.put(splitLine[0].concat(splitLine[1]), mgr );
							}
						}
					}
					if(Integer.parseInt(splitLine[9])==0 && !(Integer.parseInt(splitLine[8])==-1)){
						if(type!=null){
							LibraryId current = new LibraryId(splitLine[2], splitLine[3], splitLine[4]);
							if(bytecodes.containsKey(splitLine[0].concat(splitLine[1]))){
								ConstructBytecodeASTManager mgr = bytecodes.get(splitLine[0].concat(splitLine[1]));
								Set<LibraryId> existing = mgr.getFixedLids();
								if(!existing.contains(current)){
									mgr.addFixedLid(current);
									bytecodes.put(splitLine[0].concat(splitLine[1]),mgr);
								}
							}
							else{
								//add new entry
								ConstructBytecodeASTManager mgr  = new ConstructBytecodeASTManager(splitLine[0], splitLine[1], type);
								mgr.addFixedLid(current);
								bytecodes.put(splitLine[0].concat(splitLine[1]), mgr );
							}
						}
					}
					
					//collect lib w/ same bytecode (computed in previous runs)
					if(splitLine.length>14){
						String[] libraries = splitLine[14].split("\\|");
						if(libraries.length>0&&libraries[0].contains(":"))
							cpa.setLibsSameBytecodeAsString(libraries);
					}
					//collect lib w/ same bytecode (computed in previous runs)
					if(splitLine.length>15){
						cpa.setDoneComparisons(Integer.valueOf(splitLine[15]));
					}			
					
				}else{
					cpa = new ConstructPathAssessment2(splitLine[0], splitLine[1],
							Boolean.valueOf(splitLine[6]),splitLine[5]);
				}
				
				boolean added = false;
				for (ArtifactResult2 a : lids) {
					if (a.equalGAV(splitLine[2], splitLine[3], splitLine[4])) {
						a.addConstructPathAssessment(cpa);
						added = true;
						break;
						
					}
				}
				if (!added) {
					ArtifactResult2 ar = null;
					if(splitLine[13].equals("null"))
						ar = new ArtifactResult2(splitLine[2], splitLine[3], splitLine[4],
								Boolean.valueOf(splitLine[7]));
					else
						ar = new ArtifactResult2(splitLine[2], splitLine[3], splitLine[4],
								Boolean.valueOf(splitLine[7]),Long.valueOf(splitLine[13]));
					ar.addConstructPathAssessment(cpa);
					lids.add(ar);

				}
			}
		}
    }
    
    private void setChangeList(){
		Collection<ConstructChange> constructChanges = bugChangeList.getConstructChanges();
		this.methsConsCC = new ArrayList<>();
		for (ConstructChange cc : constructChanges) {
			if (cc.getConstructId().getType().equals(ConstructType.METH)
					|| cc.getConstructId().getType().equals(ConstructType.CONS))
				this.methsConsCC.add(cc);
		}
    }
    
    private JsonObject createJsonResult(ArtifactResult2 a, String s, Boolean affected){
		JsonObject result = new JsonObject();
		result.addProperty("source", s);
		if(affected!=null)
			result.addProperty("affected", affected);
		JsonObject libraryId = new JsonObject();
		libraryId.addProperty("group", a.getGroup());
		libraryId.addProperty("artifact", a.getArtifact());
		libraryId.addProperty("version", a.getVersion());
		result.add("libraryId", libraryId);
		result.addProperty("sourcesAvailable", a.getSourceAvailable());
		if (a.containsAD()) {
			result.addProperty("adfixed", a.isADFixed());
			result.addProperty("adpathfixed", a.isPathADFixed());
		}
		result.add("affectedcc", a.getAffectedcc(methsConsCC));
		return result;
    }
    
    private JsonObject createJsonResult(Artifact a, String s, Boolean affected){
		JsonObject result = new JsonObject();
		result.addProperty("source", s);
		if(affected!=null)
			result.addProperty("affected", affected);
		JsonObject libraryId = new JsonObject();
		libraryId.addProperty("group", a.getLibId().getMvnGroup());
		libraryId.addProperty("artifact", a.getLibId().getArtifact());
		libraryId.addProperty("version", a.getLibId().getVersion());
		result.add("libraryId", libraryId);
		return result;
    }


}
