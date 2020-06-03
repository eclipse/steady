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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval2;

import com.google.gson.Gson;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.python.sign.PythonConstructDigest;
import com.sap.psr.vulas.patcheval.representation.ConstructPathLibResult2;
import com.sap.psr.vulas.patcheval.representation.LidResult2;
import com.sap.psr.vulas.patcheval.representation.OverallConstructChange;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.LibraryId;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread to analyze for the set of received libraries using CIA service.
 */
public class LibraryAnalyzerThread2 implements Callable<List<ConstructPathLibResult2>>{
    private static final Log log = LogFactory.getLog(LibraryAnalyzerThread2.class);
    private int tid;
     
  //  private Gson gson;
    private LinkedList<OverallConstructChange> singleMethsConsCC;
    private LinkedList<OverallConstructChange> addedDelMethsConsCC;
    private LibraryId libId;
    private Long timestamp;
    private ProgrammingLanguage lang;
      
    /**
     * <p>Constructor for LibraryAnalyzerThread2.</p>
     *
     * @param _id a int.
     * @param methsConsMOD list of MOD constructs in the overall change list (i.e., consolidating construct,path changed over multiple commits) for current bug
     * @param methsConsAD list of ADD/DEL constructs in the overall change list
     * @param a a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
     * @param l a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
     */
    public LibraryAnalyzerThread2(int _id, LinkedList<OverallConstructChange> methsConsMOD, 
    		LinkedList<OverallConstructChange> methsConsAD, Artifact a , ProgrammingLanguage l){
        this.tid=_id;

        this.singleMethsConsCC = methsConsMOD;    
        this.addedDelMethsConsCC = methsConsAD;
        this.libId = a.getLibId();
        this.timestamp = a.getTimestamp();
        this.lang = l;
      //  gson = GsonHelper.getCustomGsonBuilder().create();
    }
    

   
    /** {@inheritDoc} */
    @Override
    public List<ConstructPathLibResult2> call() throws Exception {
    	List<ConstructPathLibResult2> results = new ArrayList<ConstructPathLibResult2>();
       // for (int libLoop=0;libLoop<libIds.size();libLoop++){
        	LibraryId l = libId;
        	log.info("Analysis of lib ["+ l.toString() + "] with tid [" +tid+"]");
        	
        	  //TODO: now only going on for Java, extend to PYTHON
        //    if (lang==ProgrammingLanguage.JAVA) {
            	        	
                
            	boolean binAvailable = true;
            	// are these comments still valid?
            	//doesArtifactExist works for both Java and Python as the called api (/artifacts/g/a/v) does not use the package information for Python
            	// doesArtifactExist sets the package to Jar; as a result it checks that a Jar exists for java and that an artifact for the given gav exists for python (no matter which packaging)

            	binAvailable = BackendConnector.getInstance().doesArtifactExist(l.getMvnGroup(), l.getArtifact(), l.getVersion(), false, (lang==ProgrammingLanguage.JAVA)?"jar":"sdist");
                if(!binAvailable){
                	log.warn("No artifact available for library Id [" + l.toString() +"], it will not be included in the csv");
                }
                else{
              	
	                //check whether the sources for the current version are available
                	boolean sourcesAvailable = false;
                	if(lang==ProgrammingLanguage.JAVA){
                		sourcesAvailable = BackendConnector.getInstance().doesArtifactExist(l.getMvnGroup(), l.getArtifact(), l.getVersion(), true, "jar");
                	}

	                String ast_lid = null;
	                int changesToV = -1;
	                int changesToF = -1;
	                
	                String qString = l.getMvnGroup()+"/"+l.getArtifact()+"/"+l.getVersion();
	                
	                List<ConstructId> c = new ArrayList<ConstructId>();
	                for (int j=0;j<singleMethsConsCC.size();j++){
	                	c.add(singleMethsConsCC.get(j).getConstructId());
	                }
	                
	              
		            if(c.size()>0){
		
		                //intersect change list with artifact constructs 
		                List<ConstructId> cids = null;
		                ConstructId[] cids_array =null;
		                cids_array=BackendConnector.getInstance().getArtifactBugConstructsIntersection(qString,c,(lang==ProgrammingLanguage.JAVA)?"jar":"sdist",lang);
		                if(cids_array ==null){
		                	log.warn("The intersection returned null (thus something went wrong in cia); the Jar for library Id [" + l.toString() +"] will not be included in the csv for MOD constructs");
		                } else {
		                	cids = Arrays.asList(cids_array);
			                
			             
			                boolean qnameInBin = false;
			                for ( OverallConstructChange mcCC : singleMethsConsCC  ) {
			
			                	ast_lid = null;
			                	changesToV = -1;
			                    changesToF = -1;
			                    qnameInBin = false;
			      
				                if(cids!=null && cids.contains(mcCC.getConstructId())){
				                	log.info("cids contains ["+mcCC.getConstructId()+"], change type [" +mcCC.getChangeType()+"]");
				                	qnameInBin=true;
				                }
	
				                LidResult2 lr = new LidResult2(l, timestamp,qnameInBin);
		                		lr.setSourcesAvailable(sourcesAvailable);
		                		
				                // if sourcesAvailable is true, it also implies that lang==ProgrammingLanguage.JAVA
					            if ( qnameInBin && sourcesAvailable){
				                    
					            	//if the construct is in the JAR and the sources are available
					            	//we can perform the ast comparison in case 
					            	// (a) changes are of type MOD
					            	// (b) we have the ast for the buggy and fixed versions
					            	// In particular,
					            	// for (a):  it should always be MOD at this point of the execution, since add and del are pre filtered out
					            	// for (b) even if the overall change is a mod, we may not have the buggy and fixed
			                    	//asts: e.g., the fix includes 2 commit and the constructs is first DELETED and the 
			                    	//ADDED, this means that overall is a MOD but we do not have asts (see CVE-2015-5254 
			                    	//as an example)
					            	
					            	 if ( mcCC.getChangeType().equals(ConstructChangeType.MOD) &&
					            			 (mcCC.getBuggyBody()!=null ||mcCC.getFixedBody()!=null)){
					            	
						                    boolean sourcesQnameLib = false;
						                    if(BackendConnector.getInstance().getSourcesForQnameInLib(qString+"/"+mcCC.getConstructId().getType().toString()+"/"+mcCC.getConstructId().getQname())!=null)
						                    	sourcesQnameLib = true;
						                
						                
						                    if (sourcesQnameLib) {
						                    	// GET /constructs/{mvnGroup}/{artifact}/{version}/{type}/{qname}/  : AST_LID
						                    	ast_lid = BackendConnector.getInstance().getAstForQnameInLib(qString+"/"+mcCC.getConstructId().getType().toString()+"/"+mcCC.getConstructId().getQname(),true,ProgrammingLanguage.JAVA);
						                    	Gson gson = GsonHelper.getCustomGsonBuilder().create();
			
						                    	if ( ast_lid != null ) {
						                    		if ( ast_lid.compareTo("")==0 ){
						                    			//throw exception as this should never happen as we get the ast only if the sources contain the qname!
						                    			throw new Exception("Ast lid for qname ["+mcCC.getConstructId().getQname()+"]is empty");
						                    		} else {
						                    			if(mcCC.getBuggyBody()!=null){
					
							                            	String lidToVulnBody = "["+mcCC.getBuggyBody() + "," + ast_lid +"]";
							                            	  String editV = BackendConnector.getInstance().getAstDiff(null,lidToVulnBody);
							                            	  ASTSignatureChange scV = gson.fromJson(editV, ASTSignatureChange.class);
			
							                             //   ASTSignatureChange scV = (ASTSignatureChange)BackendConnector.getInstance().getAstDiff(lidToVulnBody);
							                                /* */
							                                changesToV = scV.getModifications().size();
						                    			}
						                    			if(mcCC.getFixedBody()!=null){
							                                String lidToFixedBody = "["+ast_lid + "," + mcCC.getFixedBody()+"]";
							                                String editF = BackendConnector.getInstance().getAstDiff(null,lidToFixedBody);
							                                ASTSignatureChange scF = gson.fromJson(editF, ASTSignatureChange.class);
			
							                              //  ASTSignatureChange scF = (ASTSignatureChange)BackendConnector.getInstance().getAstDiff(lidToFixedBody);
							                                /**/      
							                                changesToF = scF.getModifications().size();
						                    			}
						                    		}
						                    	
						                    	} else {
						                    		// it should never happen that ast not available if we ask for it
						                    		log.error("Got null for ast "+qString+"/"+mcCC.getConstructId().getType().toString()+"/"+mcCC.getConstructId().getQname()+ " for library [" + l + "] where sourcesQnameLib is " + sourcesQnameLib + "qnameinjar and sources " + qnameInBin + " " + sourcesAvailable );
						                    		throw new Exception("AST_LID not available"); // happens when 500
						                    	}
						                    }
						             }
					            	 
				                }else if(qnameInBin && lang==ProgrammingLanguage.PY){
				                //TODO: implement PY comparison
				                	 if ( mcCC.getChangeType().equals(ConstructChangeType.MOD) &&
					            			 (mcCC.getBuggyBody()!=null ||mcCC.getFixedBody()!=null)){
				                		//in this case the ast_lid field will contain the digest and body
			                			ast_lid = BackendConnector.getInstance().getAstForQnameInLib(qString+"/"+mcCC.getConstructId().getType().toString()+"/"+mcCC.getConstructId().getQname(),true,ProgrammingLanguage.PY);
				                    	Gson gson = GsonHelper.getCustomGsonBuilder().create();
				                    	PythonConstructDigest pythonConstructDigest = gson.fromJson(ast_lid, PythonConstructDigest.class);
				                    	if(pythonConstructDigest!=null){
					                    	PythonConstructDigest vulnConstructDigest = gson.fromJson(mcCC.getBuggyBody(), PythonConstructDigest.class);
					                    	PythonConstructDigest fixedConstructDigest = gson.fromJson(mcCC.getFixedBody(), PythonConstructDigest.class);
					                		if(pythonConstructDigest.getDigest()!=null && vulnConstructDigest.getDigest()!=null && pythonConstructDigest.getDigest().equals(vulnConstructDigest.getDigest())){
					                			changesToV = 0;
					                		}
					                		if(pythonConstructDigest.getDigest()!=null && fixedConstructDigest.getDigest()!=null && pythonConstructDigest.getDigest().equals(fixedConstructDigest.getDigest())){
					                			changesToF = 0;
					                		}
				                    	}
				                	 }
				                }else{
				                    	 log.info("Qname ["+mcCC.getConstructId().getQname()+"] not in sources of [" + l.toString()+"]");
				                }
					            lr.setAst_lid(ast_lid);
				            	lr.setChangesToF(changesToF);
				            	lr.setChangesToV(changesToV);
					          //  LidResult2 lr = new LidResult2(l, timestamp, qnameInBin, sourcesAvailable, changesToV, changesToF, ast_lid);
				                ConstructPathLibResult2 cplr = new ConstructPathLibResult2(mcCC.getConstructId().getQname(), mcCC.getRepoPath(), mcCC.getChangeType(), lr, mcCC.getBuggyBody() ,mcCC.getFixedBody());
				                results.add(cplr);

			                }
		                }
		            }
	                    	
	             
	                
	                List<ConstructId> ad = new ArrayList<ConstructId>();
	                for (int j=0;j<addedDelMethsConsCC.size();j++){
	                	ad.add(addedDelMethsConsCC.get(j).getConstructId());
	                }
	                
	                if(ad.size()>0){
		                //intersect change list add/del with artifact constructs
		                List<ConstructId> adcids = null;
		                ConstructId[] adcids_array = BackendConnector.getInstance().getArtifactBugConstructsIntersection(qString,ad,(lang==ProgrammingLanguage.JAVA)?"jar":"sdist",lang);
		                if(adcids_array ==null){
		                	log.warn("The intersection returned null (thus something went wrong in cia); the artifact for library Id [" + l.toString() +"] will not be included in the csv for ADD/DEL constructs");
		                } else {
			               
			                     	
			                boolean qnameInJar=false;
			            	for ( OverallConstructChange adCc : addedDelMethsConsCC ){
			        			qnameInJar=false;
			 		      
			                    if(adCc!=null && adcids!=null && adcids.contains(adCc.getConstructId())){
			   	                	log.info("adcids contains ["+adCc.getConstructId());
			   	                	qnameInJar=true;
			   	                }
  			       
			   	               	LidResult2 lr = new LidResult2(l, timestamp, qnameInJar);
					
					            ConstructPathLibResult2 cplr = new ConstructPathLibResult2(adCc.getConstructId().getQname(), adCc.getRepoPath(), adCc.getChangeType(), lr,null,null);
					            results.add(cplr);
			            	}
		                }
		            }
                }
  //          }
        log.info("++++++++" + "Thread " + (tid) + " for library id " + libId.toString() +" finished+++++++++");
        return results;
    }

}
