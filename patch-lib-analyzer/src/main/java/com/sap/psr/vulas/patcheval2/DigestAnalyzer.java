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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.backend.EntityNotFoundInBackendException;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.patcheval.representation.ArtifactResult2;
import com.sap.psr.vulas.patcheval.representation.Bug;
import com.sap.psr.vulas.patcheval.utils.ConstructBytecodeASTManager;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.AffectedLibrary;
import com.sap.psr.vulas.shared.json.model.BugChangeList;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.Property;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>DigestAnalyzer class.</p>
 *
 */
public class DigestAnalyzer {
	
	private static final Log log = LogFactory.getLog(DigestAnalyzer.class);
	
	String digest;
	String version;
	Boolean bytecode;

	/**
	 * <p>Constructor for DigestAnalyzer.</p>
	 *
	 * @param _digestl a {@link java.lang.String} object.
	 */
	public DigestAnalyzer(String _digestl, Boolean _bytecode) {
		super();
		this.digest = _digestl;
		this.bytecode = _bytecode;
	}

	/**
	 * <p>analyze.</p>
	 */
	public void analyze(){
		//necessary read_write as we use GET PUT POST
		VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_WRITE.toString());
		final Gson gson = GsonHelper.getCustomGsonBuilder().create();
		
		try {
		//check that the 'Implementation-version' is available among the properties
		String lib;
		
			lib = BackendConnector.getInstance().getLibrary(digest);
			Library l = (Library) JacksonUtil.asObject(lib, Library.class);
	//		Library l = gson.fromJson(lib, Library.class);
	
			boolean canAnalyze = false;
			if(bytecode) {
				canAnalyze = true;
			}else {
				for(Property p: l.getProperties()){
					if(p.getName().equals("Implementation-Version")){
						canAnalyze = true;
						version = p.getValue();
						break;
					}
				}
			}
			
			if(canAnalyze){
				
				//get all bugs for the given digest
				List<Bug> bugsToAnalyze = new ArrayList<Bug>();
				
				String allbugs;
				
				allbugs = BackendConnector.getInstance().getBugsForLib(digest);
				bugsToAnalyze = Arrays.asList(gson.fromJson(allbugs, Bug[].class));
				//bugsToAnalyze= Arrays.asList((Bug[]) JacksonUtil.asObject(allbugs, Bug[].class));
				
				log.info("["+bugsToAnalyze.size()+"] bugs to analyze");
				
				bugLoop:
				for (Bug b: bugsToAnalyze){
					BugChangeList b1 = BackendConnector.getInstance().getBug(b.getBugId());
					
					HashMap<AffectedVersionSource, AffectedLibrary[]> existingxSource = new HashMap<AffectedVersionSource, AffectedLibrary[]>();
					
					//retrieve existing affectedVersions
					for(AffectedVersionSource s : AffectedVersionSource.values()){
			    		AffectedLibrary[] al = BackendConnector.getInstance().getBugAffectedLibraries(b.getBugId(),s.toString());
			    		existingxSource.put(s, al);
			    		log.info("Existing [" + al.length + "] affected libraries in backend for source [" +s.toString()+"]");
			    	}
							
					for(AffectedLibrary a: existingxSource.get(AffectedVersionSource.MANUAL)){
						if(a.getLib()!=null && a.getLib().getDigest().equals(digest)){
							continue bugLoop;
						}
					}
					for(AffectedLibrary a: existingxSource.get(AffectedVersionSource.PROPAGATE_MANUAL)){
						if(a.getLib()!=null && a.getLib().getDigest().equals(digest)){
							continue bugLoop;
						}
					}
					existingxSource.remove(AffectedVersionSource.TO_REVIEW);
					
					//skip analysis if libid already assessed with some strategy
					for(AffectedLibrary[] array: existingxSource.values()){
						for(AffectedLibrary a: array)
							if(a.getLibraryId()!=null && a.getLibraryId().equals(l.getLibraryId())){
								continue bugLoop;
							}
					}
					
					
					
					Boolean affected = null;
					boolean toUpload = false;
					Set<LibraryId> list = new HashSet<LibraryId>();
					
					if(!bytecode) {
						//loop over affected versions, if the same version for org.apache.tomcat was assessed consistently to VULN/FIXED, propagate it to SHA1 (meant to be used for tomcat >6 right now)	
						for(AffectedLibrary[] array: existingxSource.values()){
							for(AffectedLibrary a : array){
								if(
									//tomcat JAR without GAV
									(a.getLibraryId()!=null && l.getLibraryId()==null && a.getLibraryId().getMvnGroup().equals("org.apache.tomcat") && a.getLibraryId().getVersion().equals(version)) ||
									//tomcat JAR with GAV for OSGI eclipse bundle, works for version > 7.0.x	
									(a.getLibraryId()!=null && l.getLibraryId()!=null 
										&& a.getLibraryId().getMvnGroup().equals("org.apache.tomcat") &&  (a.getLibraryId().getArtifact().startsWith("tomcat-") || a.getLibraryId().getArtifact().startsWith("tomcat7-"))
										&& l.getLibraryId().getMvnGroup().equals("p2.eclipse-plugin")
										&& (l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("org.apache")
												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("org.apache.tomcat")
												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("org.apache.catalina")
												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("com.springsource.org.apache")
												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("com.springsource.org.apache.tomcat"))
										&& l.getLibraryId().getArtifact().substring(l.getLibraryId().getArtifact().lastIndexOf(".")+1, l.getLibraryId().getArtifact().length()).equals(a.getLibraryId().getArtifact().substring(a.getLibraryId().getArtifact().lastIndexOf("-")+1,a.getLibraryId().getArtifact().length()))
										&& a.getLibraryId().getVersion().equals(version)) ||
									//springframework for OSGI eclipse bundle having same artifactId
									(a.getLibraryId()!=null && l.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals("org.springframework") &&  a.getLibraryId().getArtifact().startsWith("org.springframework.")
											&& l.getLibraryId().getMvnGroup().equals("p2.eclipse-plugin"))
										&& a.getLibraryId().getArtifact().equals(l.getLibraryId().getArtifact())
										&& a.getLibraryId().getVersion().equals(version)
									){
									if(a.getAffected()!=null){
										if(affected == null){
											affected = a.getAffected();
											toUpload = true;
										} else if (!affected.equals(a.getAffected())){
											toUpload = false;
										}
									}
								}
							}
						}
					} 
					//establish affectedness based on bytecode (for now only if digest has lidid)
					else if(l.getLibraryId()!=null) {
						
						LibraryId toAssess = l.getLibraryId();
						
						boolean vuln = false;
						boolean fixed = false;
						
						//check for each construct change
						for(ConstructChange cc: b1.getConstructChanges()) {
							//only compare for type MOD
							if(cc.getConstructChangeType().equals(ConstructChangeType.MOD) && 
									(cc.getConstructId().getType().equals(ConstructType.CONS) || cc.getConstructId().getType().equals(ConstructType.METH))) {
								
								
								// retrieve the bytecode of the currently analyzed library
								String ast_current = BackendConnector.getInstance().getAstForQnameInLib(toAssess.getMvnGroup()+"/"+toAssess.getArtifact()+"/"+toAssess.getVersion()+"/"
														+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname(),false,ProgrammingLanguage.JAVA);	
				
								if(ast_current!=null){
									
									ConstructBytecodeASTManager astMgr = new ConstructBytecodeASTManager(cc.getConstructId().getQname(),cc.getRepoPath(),cc.getConstructId().getType());
									
									for(AffectedLibrary[] array: existingxSource.values()){
										for(AffectedLibrary a : array){
											if(a.getAffected() && a.getLibraryId()!=null)
												astMgr.addVulnLid(a.getLibraryId());
											else if(!a.getAffected() && a.getLibraryId()!=null)
												astMgr.addFixedLid(a.getLibraryId());
										}
									}
									
							 		
							 		//retrieve and compare source whose libid was assessed as vuln
							 		for(LibraryId v: astMgr.getVulnLids()){
							 		   	log.info(v.toString());
		    							//retrieve bytecode of the known to be vulnerable library
		    						 	String ast_lid = astMgr.getVulnAst(v); 
		    						 			
		    							if (ast_lid != null) {    						 	
			    						 	//check if the ast's diff is empty
			    						 	
			    							String body = "["+ast_lid + "," + ast_current +"]";
			                                String editV = BackendConnector.getInstance().getAstDiff(body);
			                                ASTSignatureChange scV = gson.fromJson(editV, ASTSignatureChange.class);
			                                /* */

			                                log.debug("size to vulnerable lib " +v.toString()+ " is [" + scV.getModifications().size() + "]");
			                                if(scV.getModifications().size()==0){
			                                	
			                                	//check that there isn't also a construct = to vuln
			                                	
			                                	log.info("LID ["+toAssess.toString()+"] equal to vuln based on AST bytecode comparison with  [" + v.toString() + "]");
			                                	vuln=true;
			                                	list.add(v);
			                                	break;
			                                }
		    							}
				    					
			    					}
							 		
							 		//retrieve and compare source whose libid was assessed as fixed
							 		for(LibraryId f: astMgr.getFixedLids()){
		    					
							 			//retrieve bytecode of the known to be vulnerable library
									 	String ast_lid = astMgr.getFixedAst(f);
										
									 	if (ast_lid != null) { 	
											//check if the ast's diff is empty
										 	
											String body = "["+ast_lid + "," + ast_current +"]";
				                            String editV = BackendConnector.getInstance().getAstDiff(body);
				                            ASTSignatureChange scV = gson.fromJson(editV, ASTSignatureChange.class);
				                            /* */

				                            log.debug("size to fixed lib " +l.toString()+ " is [" + scV.getModifications().size() + "]");
				                            if(scV.getModifications().size()==0){
				                            	
				                            
				                            	log.info("LID ["+toAssess.toString()+"] equal to fix based on AST bytecode comparison with  [" + f.toString() + "]");
				                            //	cpa2.addLibsSameBytecode(l);
				                            	fixed=true;
				                            	list.add(f);
				                            	break;
				            				}
									 	}
			                        }
								}
							}
							// cia does not serve code for type class
//							if(cc.getConstructChangeType().equals(ConstructChangeType.MOD) && cc.getConstructId().getType().equals(ConstructType.CLAS)) {
//								// retrieve the bytecode of the currently analyzed library
//								String cls_current = BackendConnector.getInstance().getSourcesForQnameInLib(toAssess.getMvnGroup()+"/"+toAssess.getArtifact()
//										+"/"+toAssess.getVersion()+"/"+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname());
//				
//								if(cls_current!=null){
//									for(AffectedLibrary[] array: existingxSource.values()){
//										for(AffectedLibrary a : array){
//											if(a.getLibraryId()!=null) {
//												String cls_known = BackendConnector.getInstance().getSourcesForQnameInLib(a.getLibraryId().getMvnGroup()+"/"+a.getLibraryId().getArtifact()
//												+"/"+a.getLibraryId().getVersion()+"/"+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname());
//											
//												if(cls_known!=null && cls_current.equals(cls_known)) {
//													list.add(a.getLibraryId());
//													if(a.getAffected()) 
//														vuln=true;
//													else if(!a.getAffected()) 
//														fixed=true;
//												}
//											}
//										}
//									}
//								}
//							}
						}
						if(vuln && ! fixed) {
							affected = true;
							toUpload = true;
						}
						else if(!vuln && fixed) {
							affected = false;
							toUpload = true;
						}
					}
					if(toUpload){
						log.info("Creating Json for PROPAGATE_MANUAL for digest [" + digest +"] and bug [" + b.getBugId() +"]");
						JsonObject result = createJsonResult(l, affected,list);

						JsonArray sourceResult = new JsonArray();
						
						sourceResult.add(result);
						
						BackendConnector.getInstance().uploadPatchEvalResults(b.getBugId(),sourceResult.toString(), "PROPAGATE_MANUAL");
					}
					
					
					
				}
				
				
			}
		} catch (EntityNotFoundInBackendException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (BackendConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 private JsonObject createJsonResult(Library _lib, Boolean _affected, Set<LibraryId> _list){
			JsonObject result = new JsonObject();
			result.addProperty("source", "PROPAGATE_MANUAL");
			if(_list == null)
				result.addProperty("explanation", "Generated automatically by DigestAnalyzer on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			else
				result.addProperty("explanation", "Same bytecode found in library(ies) ["+_list.toString()+"] by DigestAnalyzer on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			if(_affected!=null)
				result.addProperty("affected", _affected);
				if(_lib.getLibraryId()!=null){
					JsonObject lib = new JsonObject();
					lib.addProperty("group", _lib.getLibraryId().getMvnGroup());
					lib.addProperty("artifact", _lib.getLibraryId().getArtifact());
					lib.addProperty("version", _lib.getLibraryId().getVersion());
					result.add("libraryId", lib);
			}
			else{
				JsonObject lib = new JsonObject();
				lib.addProperty("digest", _lib.getDigest());
				result.add("lib", lib);
			}
			return result;
	    }
	 
		/**
		 * <p>main.</p>
		 *
		 * @param _args an array of {@link java.lang.String} objects.
		 */
		public static void main(String[] _args){
			// Prepare parsing of cmd line arguments
			final Options options = new Options();
			
			options.addOption("digest", "digest", true, "Delete all existing results before upload; otherwise only upload results for AffectedLibraries not already existing in the backend");
			options.addOption("bytecode", "bytecode", false, "Compare bytecode with assessed Jars");
			
						 
			try {
				final CommandLineParser parser = new DefaultParser();
			    CommandLine cmd = parser.parse(options, _args);
				
			    Boolean bytecode = false;
			    if(cmd.hasOption("bytecode"))
			    	bytecode = Boolean.valueOf(true);
				if(cmd.hasOption("digest")){
					String digest = cmd.getOptionValue("digest");
					log.info("Running patcheval to assess all bugs of digest["+digest+"], all other options (excluding -bytecode-) will be ignored.");
					DigestAnalyzer d = new DigestAnalyzer(digest, bytecode);
					d.analyze();
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

}
