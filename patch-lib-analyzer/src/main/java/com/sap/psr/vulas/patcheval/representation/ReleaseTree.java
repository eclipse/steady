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
package com.sap.psr.vulas.patcheval.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;


/**
 * This class holds all artifact results for the same minor release, e.g., possible roots are 1.0.0, 1.1.0,.. ,2.3.0,..
 * Each node contains its next build and maintenance release (if any), e.g., for node 2.3.0 - build release 2.3.0.1 / - maintenance release 2.3.1
 */
public class ReleaseTree {
	
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ReleaseTree.class);
	
	ReleaseTree maintenance;
	ReleaseTree build;
	
	ArtifactResult2 node;
	
	
	/**
	 * <p>Constructor for ReleaseTree.</p>
	 *
	 * @param n a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 */
	public ReleaseTree(ArtifactResult2 n){
		this.node = n;
		maintenance = null;
		build = null;
	}
	
	
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\n").append(this.node.toString()).append("\n");
		if(this.build!=null)
			sb.append("right children : ").append(this.build.toString());
		if(this.maintenance!=null)
			sb.append("left children : ").append(this.maintenance.toString());
		return sb.toString();
	}
	
	/**
	 * This method adds an ArtifactResult2 r to a release tree.
	 * If the version major.minor.maintenance of r is the same than the current node,
	 * it navigates the build ReleaseTree and inserts r as parent of  the subtree(if any) having a greater major.minor.maintenance.build;
	 * If the version major.minor.maintenance of r is greater then the current node,
	 * it navigates the maintenance ReleaseTree and inserts r as parent of the subtree (if any) having a greater major.minor.maintenance;
	 *
	 * The version comparison is done using the method compareVersion of class ArtifactResult2 which compares ArtifactResult2 based on the following priorities
	 * 1) compare version numbers of the form major.minor[.maintenance[.build]], if only digits are contained
	 * 2) compare timestap (of publication in maven central)
	 * 3) alphanumerical version comparison
	 *
	 * @param r a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 */
	public void add(ArtifactResult2 r){		
		if(this.node.compareVersion(r)>0){
			ReleaseTree root = new ReleaseTree(r);
			if(this.node.getMaintenanceRelease().equals(r.getMaintenanceRelease())){
				root.setBuild(this);
			}
			else
				root.setMaintainance(this);
		}
			
		else if(this.node.getMaintenanceRelease().equals(r.getMaintenanceRelease())){
			if(this.build==null)
				this.build = new ReleaseTree(r);
			else if(this.build.getNode().compareVersion(r)>0){
				ReleaseTree subtree = new ReleaseTree(r);
				subtree.setBuild(this.build);
				this.build = subtree;
			}
			else
				this.build.add(r);
		}
		else if(this.maintenance==null)
			this.maintenance = new ReleaseTree(r);
		
		else if (this.maintenance.getNode().compareVersion(r)>0){
			ReleaseTree subtree = new ReleaseTree(r);
			subtree.setMaintainance(this.maintenance);
			this.maintenance = subtree;
		}
		else
			this.maintenance.add(r);
		
	}
	

	
	


	/**
	 * <p>getMaintainance.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.patcheval.representation.ReleaseTree} object.
	 */
	public ReleaseTree getMaintainance() {
		return maintenance;
	}

	/**
	 * <p>setMaintainance.</p>
	 *
	 * @param maintainance a {@link com.sap.psr.vulas.patcheval.representation.ReleaseTree} object.
	 */
	public void setMaintainance(ReleaseTree maintainance) {
		this.maintenance = maintainance;
	}

	/**
	 * <p>Getter for the field <code>build</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.patcheval.representation.ReleaseTree} object.
	 */
	public ReleaseTree getBuild() {
		return build;
	}

	/**
	 * <p>Setter for the field <code>build</code>.</p>
	 *
	 * @param build a {@link com.sap.psr.vulas.patcheval.representation.ReleaseTree} object.
	 */
	public void setBuild(ReleaseTree build) {
		this.build = build;
	}

	/**
	 * <p>Getter for the field <code>node</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 */
	public ArtifactResult2 getNode() {
		return node;
	}


	/**
	 * <p>Setter for the field <code>node</code>.</p>
	 *
	 * @param node a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 */
	public void setNode(ArtifactResult2 node) {
		this.node = node;
	}
	
	
	/**
	 * This method returns the last vulnerable artifact. If both build and maintenance release exist, then LV is searched
	 * first among the build releases within the same maintenance (that means in root.build); then among the maintenance releases. 
	 * 
	 * @return the last vulnerable ArtifactResult2; null if it doesn't exist 
	 */
	private ArtifactResult2 getLV(){
		
		ArtifactResult2 lastVuln = null;
        
		if(this.build!=null){
			lastVuln = this.build.getLV();
			if(lastVuln!=null)
				return lastVuln;
		}
		
       if(this.maintenance!=null){
        	lastVuln = this.maintenance.getLVInMaintainance();
			if(lastVuln!=null)
				return lastVuln;
        }
        return this.isLV();
	}
	
	private ArtifactResult2 getLVInMaintainance(){
		ArtifactResult2 lastVuln = null;
		if(this.maintenance!=null){
        	lastVuln = this.maintenance.getLVInMaintainance();
		}
		if (lastVuln==null)
			lastVuln = this.isLV() ;
		return lastVuln;
	}
	
	private ArtifactResult2 isLV(){
		if(this.node.getSourceAvailable()&& this.node.isEqualToV()){
    		ReleaseTree.log.debug("LID ["+this.node.toString()+"] equal to vuln with confidence [" + this.node.getVConfidence() + "]");
    		return this.node;
		}
		return null;
		
	}

	
	        
	/**
	 * This method returns the first fixed artifact.
	 * It starts from the root, and navigates the maintenance releases until it finds the build releases 
	 * if it finds a before ff, then it returns null (a is not fixed) 
	 * 
	 * @return the first fixed ArtifactResult2; null if it doesn't exist 
	 */
	private ArtifactResult2 getFFinBuild(){    
		
		ArtifactResult2 firstFixed = this.isFF();
        
		if(firstFixed!= null)
			return firstFixed;
		
		if(this.build!=null){
			firstFixed = this.build.getFFinBuild();
			if(firstFixed!=null)
				return firstFixed;
		}
		
//       if(this.maintenance!=null){
//    	   firstFixed = this.maintenance.getFFInMaintainance();
//			if(firstFixed!=null)
//				return firstFixed;
//        }
       return firstFixed;
	}
	
	private ArtifactResult2 getFFInMaintainance(){
		ArtifactResult2 firstFixed = this.isFF();
        
		if(firstFixed!= null)
			return firstFixed;
	
		if(this.maintenance!=null){
			return this.maintenance.getFFInMaintainance();
		}
		return firstFixed;
	}
        
	

	
//	private ArtifactResult2 getFF(ReleaseTree a, ArtifactResult2 ff){    
//		ArtifactResult2 firstFixed = a.isFF(ff);
//		//if we navigated a until this.node and FF was not found -> it's not among this.node parents
//		if(this.node.equals(a))
//			return firstFixed;
//		if(firstFixed!=null)
//			return firstFixed;
//		//we navigate a until the maintainance release of this
//		if(!this.node.getMaintenanceRelease().equals(a.node.getMaintenanceRelease()) && a.maintenance!=null){
//			return this.getFF(a.maintenance,firstFixed);
//		}
//		else if(this.node.getMaintenanceRelease().equals(a.node.getMaintenanceRelease()) && a.build!=null){
//			return this.getFF(a.build,firstFixed);
//		}
//		return firstFixed;
//	}
        
	//private ArtifactResult2 isFF(ArtifactResult2 firstFixed){
	private ArtifactResult2 isFF(){
		if(this.node.getSourceAvailable()&& this.node.isEqualToF() ){ //&& firstFixed==null){
			ReleaseTree.log.debug("LID ["+this.node.toString()+"] equal to fixed with confidence [" + this.node.getFConfidence() + "]");
			return this.node;
		}
		else
			return null;
	}
	
	
	/**
	 * This method looks for an intersection on the build or maintenance releases of the current node
	 * 
	 * @return an intersection on the build or maintenance (if any)
	 */
	private Intersection2 getIS(){
		Intersection2 is = null;
		if(this.build!=null){
			ArtifactResult2 lastV = this.getLV();
			ArtifactResult2 firstF = this.getFFinBuild();
			if(firstF==null)
				firstF = this.getFFInMaintainance();
			is = this.getBuildIS(lastV,firstF);
		}
		if(this.maintenance!=null && is==null){
			ArtifactResult2 lastV = this.getLV();
			ArtifactResult2 firstF = this.getFFinBuild();
			if(firstF==null)
				firstF = this.getFFInMaintainance();
			is = this.getMaintenanceIS(lastV,firstF);
		}
		return is;
	}
	

	private Intersection2 getBuildIS(ArtifactResult2 lv, ArtifactResult2 ff){
		Intersection2 is = null;
		if(this.build!=null){
			is = getIS(this.node, this.build.getNode(), lv,ff);
			if(is!=null && this.build.getBuildIS(lv,ff)==null)
				return is;
			return this.build.getBuildIS(lv,ff);
		}
		return is;
	}
	
	private Intersection2 getMaintenanceIS(ArtifactResult2 lv, ArtifactResult2 ff){
		Intersection2 is = null;
		if(this.maintenance!=null){
			is = getIS(this.node, this.maintenance.getNode(), lv, ff);
			if(is!=null && this.maintenance.getMaintenanceIS(lv,ff)==null)
				return is;
			else
				return this.maintenance.getMaintenanceIS(lv,ff);
		}
		return is;
	}
	
//	/**
//	 * This method looks for an intersection among the build releases of a or maintenance
//	 * 
//	 * @return the Intersection if it's unique per build or per maintenance; null if it doesn't exist 
//	 */
//	public Intersection2 getPostIS(ArtifactResult2 a){
//		ReleaseTree rt = getTree(a);
//		ArtifactResult2 lastV = rt.getLV();
//		ArtifactResult2 firstF = this.getFF(a);
//		Intersection2 is = null;//, is1 = null;
//	
//		//find IS along build release of a
//		if(this.build!=null && this.node.getMaintenanceRelease().equals(a.getMaintenanceRelease()))
//			is = this.getPostIS(lastV,firstF);
//		return is;
////		if(this.maintenance!=null)
////			is1 = this.getPostISMaintenance(lastV,firstF);
////		
////		if(is!=null && is1==null)
////			return is;
////		else if (is==null && is1!=null)
////			return is1;
////		else
////			return null;
//	}
			
	
	
	private Intersection2 getIS(ArtifactResult2 a,ArtifactResult2 b,ArtifactResult2 lv, ArtifactResult2 ff ){
		
		int ixc = 0;
		int tot=0;
		Intersection2 is = null;
//		to be good the intersection has to happen AFTER lastVulnerable and BEFORE firstFixed
		if((lv==null || a.compareTo(lv)>=0) && (ff==null || b.compareTo(ff)<=0) ){
			for (ConstructPathAssessment2 cpa : a.getConstructPathAssessments()) {
				if (cpa.getQnameInJar() && cpa.getType().equals("MOD") && cpa.getdToF()>-1) {
					tot++;
					for (ConstructPathAssessment2 cpa2 : b.getConstructPathAssessments()) {
						if (cpa.getConstruct().equals(cpa2.getConstruct())								
								&& cpa2.getType().equals("MOD")
								&& cpa.getPath().equals(cpa2.getPath()) && cpa2.getQnameInJar() && cpa2.getdToF()>-1) {
							int distanceB = cpa.getdToF() - cpa.getdToV();
							int distanceA = cpa2.getdToF() - cpa2.getdToV();
							if (distanceB > 0 && distanceA < 0) {
								ixc++;
							}
						}
					}
				}
			}
			if(tot==0)
 				ReleaseTree.log.debug("None of the MOD constructs are part of archive ["+ a.toString() +"]" );
			else if(ixc==0)
				ReleaseTree.log.debug("["+tot+"] constructs analyzed but no intersection found between archives ["+ a.toString() +"] and [" + b.toString() +"]");
			else if(ixc!=0 && is ==null){
        			Double confidence = ixc / (double) tot;
        			is=new Intersection2(a, b,ixc,confidence);
        			ReleaseTree.log.debug("Found intersection between archives ["+ a.toString() +"] and [" + b.toString() +"] with confidence [" +is.getConfidence()+ "]");
        			return is;
        		}
		}
		return null;
 	}
        
	/**
	 * This method computes all artifactResults2 of a releaseTree which are before an intersection (considering both maintenance or build branches)
	 *
	 * @return pairs of artifact results and the intersection they preceed
	 */
	public HashMap<ArtifactResult2,Intersection2> getBeforeIntersection(){
		HashMap<ArtifactResult2,Intersection2> arbeforeIS = new HashMap<ArtifactResult2,Intersection2>();
		this.getBeforeIntersection(arbeforeIS);
		return arbeforeIS;
	}
	
	// all nodes before an intersection must include all maintenance and builds encountered
	// TODO: to check whether it works correctly
	private HashMap<ArtifactResult2,Intersection2>  getPreIs(Intersection2 i){
		HashMap<ArtifactResult2,Intersection2> a = new HashMap<ArtifactResult2,Intersection2>();
		//add node if it's a minor maintenance release or in the same maintenance (which means all its build releases are affected as they do not contain an intersection)
	//	if(this.node.compareVersion(i.getFrom())<=0 || (this.node.getMaintenanceRelease().equals(i.getFrom().getMaintenanceRelease())&& !this.node.getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease())))
			
		if(this.node.compareVersion(i.getFrom())<=0 &&
			((i.getFrom().getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease()) && this.node.getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease()) ) ||
					!(i.getFrom().getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease()))))
			a.put(this.node,i);
		if(this.build!=null)
			a.putAll(this.build.getPreIs(i));
		if (this.maintenance!=null)
			a.putAll(this.maintenance.getPreIs(i));
		return a;
	}
	
	/**
	 * This method populates the hashmap passed as argument with all the pairs artifactresult and intersection they preceed.
	 * The method is called recoursively, at each iteration it looks for the intersection for the root node either in the 
	 * maintenance or build branch and adds it to the hashmap (if any)
	 * 
	 * @param arbeforeIS: the hashmap to be populated with all the artifactresult and intersection they preceed found
	 */
	private void getBeforeIntersection(HashMap<ArtifactResult2,Intersection2> arbeforeIS){
		//get the intersection on the current root, i.e., on it's build releases or maintenance
		Intersection2 is = this.getIS();
		if(is!=null){
			arbeforeIS.putAll(this.getPreIs(is));
		}
		//no need to navigate the build as it's not branching on its own and the intersection(if any) was already found in this.getIS()
//		if(this.build!=null)
//			this.build.getBeforeIntersection(arbeforeIS);
		
		
		if(this.maintenance!=null)
			this.maintenance.getBeforeIntersection(arbeforeIS);   
	
	}
	  
	
	/**
	 * <p>getPostIntersection.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<ArtifactResult2,Intersection2> getPostIntersection(){
		HashMap<ArtifactResult2,Intersection2> arpostIS = new HashMap<ArtifactResult2,Intersection2>();
		this.getPostIntersection(arpostIS);
		return arpostIS;
	}
	
	private HashMap<ArtifactResult2,Intersection2>  getPostIs(Intersection2 i){
		HashMap<ArtifactResult2,Intersection2> a = new HashMap<ArtifactResult2,Intersection2>();
		if(this.node.compareVersion(i.getTo())>=0 &&
				((i.getFrom().getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease()) && this.node.getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease()) ) ||
						!(i.getFrom().getMaintenanceRelease().equals(i.getTo().getMaintenanceRelease()))))
			a.put(this.node,i);
		if(this.build!=null)
			a.putAll(this.build.getPostIs(i));
		if (this.maintenance!=null)
			a.putAll(this.maintenance.getPostIs(i));
		return a;
	}
	
	private void getPostIntersection(HashMap<ArtifactResult2,Intersection2> arpostIS){
		Intersection2 is = this.getIS();//this);
		if(is!=null){
			arpostIS.putAll(this.getPostIs(is));
		}
		if(this.build!=null)
			this.build.getPostIntersection(arpostIS);
		if(this.maintenance!=null)
			this.maintenance.getPostIntersection(arpostIS);   
	
	}
//	/**
//	 * This method looks for an intersection among the build releases of a or maintenance
//	 * 
//	 * @return the Intersection if it's unique per build or per maintenance; null if it doesn't exist 
//	 */
//	public Intersection2 getPreIS(ArtifactResult2 a){
//		this.getPreIS(a,this);
//	}
//		
//	public Intersection2 getPreIS(ArtifactResult2 a, ReleaseTree t){
//		Intersection2 is = null, is1=null;
//		if(t.node.equals(a))
//			return is;
//		
//		if(t.node.getMaintenanceRelease().equals(a.getMaintenanceRelease()) && t.build!=null){
//			is1 = this.getPostIS(t.node);
//			if(is1.getTo().compareTo(a)<0)
//				is=is1;
//		}
//		else if(t.maintenance!=null){
//			is = this.getPreIS(a, t.maintenance);
//		}
//		if(is==null){
//			is=this.getPostISMaintenance(lv, ff)
//		}
//		return is;
//	}
//		
//		
//		
//	
//	private Intersection2 getPreIS(ArtifactResult2 a, Intersection2 ff){    
//		ArtifactResult2 firstFixed = this.isFF(ff);
//		//if we encounter a we have finished the search
//		if(this.node.equals(a))
//			return firstFixed;
//		
//		//if the requested artifact a is in the build releases
//		if(this.node.getMaintenanceRelease().equals(a.getMaintenanceRelease()) && this.build!=null){
//			return this.build.getFF(a,firstFixed);
//		}
//		else if(this.maintenance!=null){
//			return this.maintenance.getFF(a,firstFixed);
//		}
//		return null;
//	}
//    
	        
//	public int countNodes(){
//		int i = 0;
//		if(this.build!=null){
//			i+=this.build.countNodes();
//		}
//		if(this.maintenance!=null)
//			i+=this.maintenance.countNodes();
//		return i++;
//	}
	
	//looks an intersection in the maintenance release
	/**
	 * <p>getISLastFirstFixed.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 */
	public ArtifactResult2 getISLastFirstFixed(){
		ArtifactResult2 lastV = this.getLV();
		ArtifactResult2 firstF = this.getFFinBuild();
		if(firstF==null)
			firstF = this.getFFInMaintainance();
		Intersection2 is = this.getMaintenanceIS(lastV,firstF);
		if(is!=null)
			return is.getTo();
		else return null;
	}

	/**
	 * <p>getLastFirstFixed.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
	 */
	public ArtifactResult2 getLastFirstFixed(){
		if(this.isFF()!=null && (this.build==null || this.build.getLV()==null) && (this.maintenance==null || this.maintenance.getLVInMaintainance()==null))
			return this.node;
		
		ArtifactResult2 a = null, b=null, lff=null;
		if(this.build!=null && (this.maintenance==null || this.maintenance.getLVInMaintainance()==null)){
			a=this.build.getFFinBuild();
			return a;
		}
		if(this.maintenance!=null)
			return this.maintenance.getLastFirstFixed();
		else{
			return null;
		}
	//	if()
		
//		if(this.build!=null)
//			a = this.build.getLastFirstFixed();
//		if(this.maintenance!=null)
//			b = this.maintenance.getLastFirstFixed();
//		
//		if(this.isFF()!=null && a!=null && this.node.getTimestamp()<a.getTimestamp())
//			lff = a;
//		else if(a!=null)
//			lff = a;
//		else if(this.isFF()!=null)
//			lff = this.node;
//		
//		if (lff!=null && b!=null && lff.getTimestamp()<b.getTimestamp())
//			lff = b;
//		else if(b!=null)
//			lff = b;
//		return lff;
			
	}
	
	/**
	 * <p>getEqualsV.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ArtifactResult2> getEqualsV(){
	    List<ArtifactResult2> listOfEqualsV = new ArrayList<ArtifactResult2>();
	    this.getEqualsV(listOfEqualsV);
		return listOfEqualsV;
	}

	private void getEqualsV(List<ArtifactResult2> listOfEqualsV) {
		if (this.node.isEqualToV()) {
			ReleaseTree.log.debug("LID ["+this.node.toString()+"] equal to vuln with confidence [" + this.node.getVConfidence() + "]");
			listOfEqualsV.add(node);
		}
		if(this.build!=null)
			this.build.getEqualsV(listOfEqualsV);
		if(this.maintenance!=null)
			this.maintenance.getEqualsV(listOfEqualsV);   
		
	}
	
	/**
	 * <p>getEqualsF.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ArtifactResult2> getEqualsF(){
	    List<ArtifactResult2> listOfEqualsF = new ArrayList<ArtifactResult2>();
	    this.getEqualsF(listOfEqualsF);
		return listOfEqualsF;
	}

	private void getEqualsF(List<ArtifactResult2> listOfEqualsF) {
		if (this.node.isEqualToF()) {
			ReleaseTree.log.debug("LID ["+this.node.toString()+"] equal to fixed with confidence [" + this.node.getFConfidence() + "]");
			listOfEqualsF.add(node);
		}
		if(this.build!=null)
			this.build.getEqualsF(listOfEqualsF);
		if(this.maintenance!=null)
			this.maintenance.getEqualsF(listOfEqualsF);   
		
	}
	
	/**
	 * <p>getMinorV.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<ArtifactResult2,ArtifactResult2> getMinorV(){
		HashMap<ArtifactResult2,ArtifactResult2> arwithLV = new HashMap<ArtifactResult2,ArtifactResult2>();
		this.getMinorV(arwithLV);
		return arwithLV;
	}
	
	private void getMinorV(HashMap<ArtifactResult2,ArtifactResult2> arwithLV){
		ArtifactResult2 a = this.getLV();
		if(a!=null && this.node!=a && !this.node.isEqualToV() && this.node.compareTo(a)<0){
			ReleaseTree.log.debug("LID ["+this.node.toString()+"] is minor than [" +a.toString() + "] which is vuln with confidence [" + a.getVConfidence() + "]");
			arwithLV.put(this.node, a);
		}
		if(this.build!=null)
			this.build.getMinorV(arwithLV);
		if(this.maintenance!=null)
			this.maintenance.getMinorV(arwithLV);   
	}
	
	/**
	 * <p>getMajorF.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<ArtifactResult2,ArtifactResult2> getMajorF(){
		HashMap<ArtifactResult2,ArtifactResult2> arwithFF = new HashMap<ArtifactResult2,ArtifactResult2>();
		this.getMajorF(arwithFF, null);
		return arwithFF;
	}
	
	private void getMajorF(HashMap<ArtifactResult2,ArtifactResult2> arwithFF, ArtifactResult2 ff){
		ArtifactResult2 a = null;
		ArtifactResult2 b = null;
		if(ff==null){
			a = this.getFFinBuild();
			b = this.getFFInMaintainance();
		}
		else{
			if(ff.getMaintenanceRelease().equals(this.node.getMaintenanceRelease())){
				a = ff;
				b = this.getFFInMaintainance();
			}
			else {
				b = ff;
				a = this.getFFinBuild();
			}
		}
			
		if(a!=null && this.node!=a && !this.node.isEqualToF() && this.node.compareTo(a)>0){
			ReleaseTree.log.debug("LID ["+this.node.toString()+"] is major than [" +a.toString() + "] which is fixed in build with confidence [" + a.getFConfidence() + "]");
			arwithFF.put(this.node, a);
		}
		else if(b!=null && this.node!=b && !this.node.isEqualToF() && this.node.compareTo(b)>0){
			ReleaseTree.log.debug("LID ["+this.node.toString()+"] is major than [" +b.toString() + "which is fixed in maintenance with confidence" + b.getFConfidence() + "]");
			arwithFF.put(this.node, b);
		}
		if(this.build!=null){
			if(a!=null)
				this.build.getMajorF(arwithFF,a);
			else
				this.build.getMajorF(arwithFF,b);
		}
		if(this.maintenance!=null)
			this.maintenance.getMajorF(arwithFF,b);   
	}
	
	
	/**
	 * <p>getNodes.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ArtifactResult2> getNodes(){
		List<ArtifactResult2> list = new ArrayList<ArtifactResult2>();
		list.add(this.node);
		if(this.build!=null)
			list.addAll(this.build.getNodes());
		if(this.maintenance!=null)
			list.addAll(this.maintenance.getNodes());
		return list;
	}
}
