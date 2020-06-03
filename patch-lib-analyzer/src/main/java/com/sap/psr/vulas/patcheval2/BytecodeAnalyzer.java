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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.backend.EntityNotFoundInBackendException;
import com.sap.psr.vulas.bytecode.BytecodeComparator;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.patcheval.utils.PEConfiguration;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.AffectedLibrary;
import com.sap.psr.vulas.shared.json.model.Bug;
import com.sap.psr.vulas.shared.json.model.BugChangeList;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.Property;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>DigestAnalyzer class.</p>
 *
 */
public class BytecodeAnalyzer {
	
	private static final Log log = LogFactory.getLog(BytecodeAnalyzer.class);
	
	String digest;

	
	/**
	 * <p>Constructor for DigestAnalyzer.</p>
	 *
	 */
	public BytecodeAnalyzer() {}

	/**
	 * <p>analyze.</p>
	 * @throws IOException 
	 */
	public void analyze() throws IOException{
		//necessary read_write as we use GET PUT POST
		VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_WRITE.toString());
//		final Gson gson = GsonHelper.getCustomGsonBuilder().create();
//		
//		
//		if(!bytecode) {
//			try {
//				//check that the 'Implementation-version' is available among the properties		
//				String lib;
//				
//					lib = BackendConnector.getInstance().getLibrary(digest);
//				
//				Library l = (Library) JacksonUtil.asObject(lib, Library.class);
//		
//				boolean canAnalyze = false;
//				for(Property p1: l.getProperties()){
//					if(p1.getName().equals("Implementation-Version")){
//						canAnalyze = true;
//						version = p1.getValue();
//						break;
//					}
//				}
//			
//				if(canAnalyze){
//					
//					//get all bugs for the given digest
//					List<Bug> bugsToAnalyze = new ArrayList<Bug>();
//					
//					String allbugs = BackendConnector.getInstance().getBugsForLib(digest);
//					bugsToAnalyze = Arrays.asList(gson.fromJson(allbugs, Bug[].class));
//					//bugsToAnalyze= Arrays.asList((Bug[]) JacksonUtil.asObject(allbugs, Bug[].class));
//					
//					log.info("["+bugsToAnalyze.size()+"] bugs to analyze");
//					
//					bugLoop:
//					for (Bug b: bugsToAnalyze){
//						Bug b1 = BackendConnector.getInstance().getBug(null, b.getBugId());
//						
//						HashMap<AffectedVersionSource, AffectedLibrary[]> existingxSource = new HashMap<AffectedVersionSource, AffectedLibrary[]>();
//						
//						//retrieve existing affectedVersions
//						for(AffectedVersionSource s : AffectedVersionSource.values()){
//				    		AffectedLibrary[] al = BackendConnector.getInstance().getBugAffectedLibraries(null, b.getBugId(),s.toString(),true);
//				    		existingxSource.put(s, al);
//				    		log.info("Existing [" + al.length + "] affected libraries in backend for source [" +s.toString()+"]");
//				    	}
//								
//						for(AffectedLibrary a: existingxSource.get(AffectedVersionSource.MANUAL)){
//							if(a.getLib()!=null && a.getLib().getDigest().equals(digest)){
//								continue bugLoop;
//							}
//						}
//						for(AffectedLibrary a: existingxSource.get(AffectedVersionSource.PROPAGATE_MANUAL)){
//							if(a.getLib()!=null && a.getLib().getDigest().equals(digest)){
//								continue bugLoop;
//							}
//						}
//						existingxSource.remove(AffectedVersionSource.TO_REVIEW);
//						
//						//skip analysis if libid already assessed with some strategy
//						for(AffectedLibrary[] array: existingxSource.values()){
//							for(AffectedLibrary a: array)
//								if(a.getLibraryId()!=null && a.getLibraryId().equals(l.getLibraryId())){
//									continue bugLoop;
//								}
//						}
//						
//						
//						
//						Boolean affected = null;
//						boolean toUpload = false;
//						Set<LibraryId> list = new HashSet<LibraryId>();
//						
//					
//						//loop over affected versions, if the same version for org.apache.tomcat was assessed consistently to VULN/FIXED, propagate it to SHA1 (meant to be used for tomcat >6 right now)	
//						for(AffectedLibrary[] array: existingxSource.values()){
//							for(AffectedLibrary a : array){
//								if(
//									//tomcat JAR without GAV
//									(a.getLibraryId()!=null && l.getLibraryId()==null && a.getLibraryId().getMvnGroup().equals("org.apache.tomcat") && a.getLibraryId().getVersion().equals(version)) ||
//									//tomcat JAR with GAV for OSGI eclipse bundle, works for version > 7.0.x	
//									(a.getLibraryId()!=null && l.getLibraryId()!=null 
//										&& a.getLibraryId().getMvnGroup().equals("org.apache.tomcat") &&  (a.getLibraryId().getArtifact().startsWith("tomcat-") || a.getLibraryId().getArtifact().startsWith("tomcat7-"))
//										&& l.getLibraryId().getMvnGroup().equals("p2.eclipse-plugin")
//										&& (l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("org.apache")
//												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("org.apache.tomcat")
//												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("org.apache.catalina")
//												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("com.springsource.org.apache")
//												|| l.getLibraryId().getArtifact().substring(0,l.getLibraryId().getArtifact().lastIndexOf(".")).equals("com.springsource.org.apache.tomcat"))
//										&& l.getLibraryId().getArtifact().substring(l.getLibraryId().getArtifact().lastIndexOf(".")+1, l.getLibraryId().getArtifact().length()).equals(a.getLibraryId().getArtifact().substring(a.getLibraryId().getArtifact().lastIndexOf("-")+1,a.getLibraryId().getArtifact().length()))
//										&& a.getLibraryId().getVersion().equals(version)) ||
//									//springframework for OSGI eclipse bundle having same artifactId
//									(a.getLibraryId()!=null && l.getLibraryId()!=null && a.getLibraryId().getMvnGroup().equals("org.springframework") &&  a.getLibraryId().getArtifact().startsWith("org.springframework.")
//											&& l.getLibraryId().getMvnGroup().equals("p2.eclipse-plugin"))
//										&& a.getLibraryId().getArtifact().equals(l.getLibraryId().getArtifact())
//										&& a.getLibraryId().getVersion().equals(version)
//									){
//									if(a.getAffected()!=null){
//										if(affected == null){
//											affected = a.getAffected();
//											toUpload = true;
//										} else if (!affected.equals(a.getAffected())){
//											toUpload = false;
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			} catch (EntityNotFoundInBackendException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (BackendConnectionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else {
//	
			BytecodeComparator comparator = new BytecodeComparator();
			Iterator<File> it = FileUtils.iterateFiles(new File(PEConfiguration.getBaseFolder().toString()+File.separator), null, false);
			//Path p = Paths.get(PEConfiguration.getBaseFolder().toString()+File.separator+l.getDigest()+".jar").normalize();
			while(it.hasNext()){
          		File j = (File) it.next();
	            Path p = j.toPath();
		        if(p==null || !p.toFile().exists()) {
		        	BytecodeAnalyzer.log.info("Archive file [" + p +"] does not exists");
		        	break;
		        }

	        	this.digest= FileUtil.getSHA1(j).toUpperCase();
	        
	        	log.info("Digest to be analyzed: " + this.digest);
	    		String lib;
				try {
					lib = BackendConnector.getInstance().getLibrary(digest);
					
					Library l = (Library) JacksonUtil.asObject(lib, Library.class);
			//		Library l = gson.fromJson(lib, Library.class);
					
					List<Bug> bugsToAnalyze = new ArrayList<Bug>();
					
					String allbugs = BackendConnector.getInstance().getBugsForLib(digest);
					//bugsToAnalyze = Arrays.asList(gson.fromJson(allbugs, Bug[].class));
					bugsToAnalyze= Arrays.asList((Bug[]) JacksonUtil.asObject(allbugs, Bug[].class));
					
					log.info("["+bugsToAnalyze.size()+"] bugs to analyze");
					
					for (Bug b: bugsToAnalyze){
						comparator.compareLibForBug(l, b.getBugId(), p);
					}
				} catch (EntityNotFoundInBackendException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BackendConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	//	}
	}
	
	
		/**
		 * <p>main.</p>
		 *
		 * @param _args an array of {@link java.lang.String} objects.
		 * @throws IOException 
		 */
		public static void main(String[] _args) throws IOException{
			// Prepare parsing of cmd line arguments
			final Options options = new Options();
			
		//	options.addOption("digest", "digest", false, "");
		//	options.addOption("bytecode", "bytecode", false, "Compare bytecode with assessed Jars");
			
						 
			try {
				final CommandLineParser parser = new DefaultParser();
			    CommandLine cmd = parser.parse(options, _args);
				
//			    Boolean bytecode = false;
//			    if(cmd.hasOption("bytecode"))
//			    	bytecode = Boolean.valueOf(true);
//					log.info("Assess all bugs of Jars found at basefolder, all other options will be ignored.");
//				if(cmd.hasOption("digest")){
//					String digest = cmd.getOptionValue("digest");
//					log.info("Assess all bugs of digest["+digest+"] using metadata, all other options will be ignored.");
//					DigestAnalyzer d = new DigestAnalyzer(digest, bytecode);
//					d.analyze();
//				}
				BytecodeAnalyzer d = new BytecodeAnalyzer();
				d.analyze();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
}