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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;


import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.gson.Gson;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.bytecode.ConstructBytecodeASTManager;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeDeserializer;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.patcheval.representation.ArtifactResult2;
import com.sap.psr.vulas.patcheval2.BugLibManager;
import com.sap.psr.vulas.patcheval.representation.ConstructPathAssessment2;
import com.sap.psr.vulas.patcheval.utils.CSVHelper2;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;


/**
 * <p>ByteCodeComparator class.</p>
 *
 */
public class ByteCodeComparator implements Runnable{ 

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
	
	
	private final ArtifactResult2 ar;
	Map<Class<?>,StdDeserializer<?>> custom_deserializers = new HashMap<Class<?>,StdDeserializer<?>>();
	String bugId;
	
	/**
	 * <p>Constructor for ByteCodeComparator.</p>
	 *
	 * @param ar a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 * @param _b a {@link java.lang.String} object.
	 */
	public ByteCodeComparator(ArtifactResult2 ar, String _b) {
		this.ar = ar;
		this.bugId = _b;
		custom_deserializers.put(ASTSignatureChange.class, new ASTSignatureChangeDeserializer());
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
	
		Integer done_comparisons=0;
		
    	boolean toRewrite = false;
		for(ConstructPathAssessment2 cpa2 :ar.getConstructPathAssessments()){
			done_comparisons=0;
			if(cpa2.getQnameInJar() && BugLibManager.bytecodes.containsKey(cpa2.getConstruct().concat(cpa2.getPath()))){
				//first check that the construct is not in a nested classes otherwise the bytecode comparison does not work 
				// because the decompiler  in use (Procyon) does not keep the outer class in the decompiled inner class 
				// (i.e., the decompiled ClassA$ClassB.class contains public static class ClassB instead of public static class ClassA$ClassB)
				// right now we check if a construct is in a nested class in the most dumb way: if the string contains $
				if(!cpa2.getConstruct().contains("$")){    
					
					ConstructBytecodeASTManager mgr = BugLibManager.bytecodes.get(cpa2.getConstruct().concat(cpa2.getPath()));
				
					//only compare if the number of comparisons tobedone is > than the one performed in the previous run (which was stored in the csv)
					// this is done to avoid that comparisons are done for every run even if no equalities in the bytecode is found
					// the check on the cardinality is an approx, however the number of sources equalities should never decreases thus this check seems adeguate
					Integer comparisonsToBeDone=mgr.getLidsSize();
								 		
					if(cpa2.getConstructType()!=null && (cpa2.getDoneComparisons()==null || !cpa2.getDoneComparisons().equals(comparisonsToBeDone))){
					
						log.info("["+comparisonsToBeDone+"] comparisons to be done for construct path "+ cpa2.getConstruct()+" : "+cpa2.getPath()+" in archive [" +ar.toString()+"]");
						List<LibraryId> vList = new ArrayList<LibraryId>();
						List<LibraryId> fList = new ArrayList<LibraryId>();
						
						// retrieve the bytecode of the currently analyzed library
						String ast_current = BackendConnector.getInstance().getAstForQnameInLib(null,ar.getGroup()+"/"+ar.getArtifact()+"/"+ar.getVersion()+"/"
																		+cpa2.getConstructType().toString()+"/"+cpa2.getConstruct(),false, ProgrammingLanguage.JAVA);	
					    							
					 	if(ast_current!=null){
				 		
					 		//libid whose source was found equal to vuln
					 		for(LibraryId l: mgr.getVulnLids()){
					 		
					 			log.info("Artifact ["+ar.toString()+"] vuln comparison number [" + done_comparisons+"/"+comparisonsToBeDone + "]");
		    					//check if it is equal to bytecode of each c,p in lid found equal to vuln
		    					
	       				    						
    							//retrieve bytecode of the known to be vulnerable library
    						 	String ast_lid = mgr.getVulnAst(l); 
    						 			
    							if (ast_lid != null) {    						 	
	    						 	//check if the ast's diff is empty
	    						 	
	    							String body = "["+ast_lid + "," + ast_current +"]";
	                                String editV = BackendConnector.getInstance().getAstDiff(null,body);
	                                ASTSignatureChange scV = (ASTSignatureChange) JacksonUtil.asObject(editV, custom_deserializers, ASTSignatureChange.class);
	                                /* */
	                                done_comparisons++;
	                                toRewrite = true;
	                                log.info("size to vulnerable lib " +l.toString()+ " is [" + scV.getModifications().size() + "]");
	                                if(scV.getModifications().size()==0){
	                                	
	                                	//check that there isn't also a construct = to vuln
	                                	
	                                	log.info("LID ["+ar.toString()+"] equal to vuln based on AST bytecode comparison with  [" + l.toString() + "]");
	                                	//cpa2.addLibsSameBytecode(l);
	                                	vList.add(l);
	                                	
	                                	
	                                }
    							}
		    					
	    					}
					 		
					 		// libid whose source was found equal to fix
					 		for(LibraryId l: mgr.getFixedLids()){
    					
					 			log.info("Artifact ["+ar.toString()+"] fixed comparison number [" + done_comparisons+"/"+comparisonsToBeDone + "]");						
								
								//retrieve bytecode of the known to be vulnerable library
							 	String ast_lid = mgr.getFixedAst(l);
								
							 	if (ast_lid != null) { 	
									//check if the ast's diff is empty
								 	
									String body = "["+ast_lid + "," + ast_current +"]";
		                            String editV = BackendConnector.getInstance().getAstDiff(null,body);
		                            ASTSignatureChange scV = (ASTSignatureChange) JacksonUtil.asObject(editV, custom_deserializers, ASTSignatureChange.class);
		                            /* */
		                            done_comparisons++;
		                            toRewrite = true;
		                            log.info("size to fixed lib " +l.toString()+ " is [" + scV.getModifications().size() + "]");
		                            if(scV.getModifications().size()==0){
		                            	
		                            
		                            	log.info("LID ["+ar.toString()+"] equal to fix based on AST bytecode comparison with  [" + l.toString() + "]");
		                            //	cpa2.addLibsSameBytecode(l);
		                            	fList.add(l);
		                            
		            				}
							 	}
	                        }
    					
	    					cpa2.setDoneComparisons(done_comparisons);
	    					if(vList.size()>0 && !(fList.size()>0)){
	    						cpa2.setLibsSameBytecode(vList);
	    						cpa2.setdToV(0);
	    					}else if(fList.size()>0 && !(vList.size()>0)){
	    						cpa2.setLibsSameBytecode(fList);
	    						cpa2.setdToF(0);
	    					}else if(vList.size()>0 && fList.size()>0){
	    						log.info("found equalities both to vuln and fixed!");
	    					}else if (vList.size()==0 && fList.size()==0){
	    						log.info("no equalities found");
	    					}
    					}
					}
				}
			}
		}
		if(toRewrite){
			CSVHelper2.rewriteLineInCSV(bugId, ar);
		}
		
	}
}
