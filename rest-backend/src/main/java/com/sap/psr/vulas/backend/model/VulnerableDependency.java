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
package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.repo.ApplicationRepositoryCustom;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.VulnDepOrigin;

/**
 * <p>VulnerableDependency class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VulnerableDependency  implements Serializable, Comparable<VulnerableDependency> {

	private Dependency dep;

	private Bug bug;

	@JsonView(Views.VulnDepDetails.class)
	private List<ConstructChangeInDependency> constructList;

	@JsonProperty(value = "affected_version_confirmed")
	int affectedVersionConfirmed;
	
	@JsonProperty(value = "affected_version")
	int affectedVersion;
	
	AffectedVersionSource affectedVersionSource;

	int reachable;

	int traced;
	
	LibraryId bundledLibId;
	
	Library bundledLib; 
	
	VulnDepOrigin origin; 
	
	private Excemption excemption = null;

	/**
	 * <p>Constructor for VulnerableDependency.</p>
	 */
	public VulnerableDependency() { super(); }

	/**
	 * <p>Constructor for VulnerableDependency.</p>
	 *
	 * @param d a {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 * @param b a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 */
	public VulnerableDependency(Dependency d, Bug b){
		super();
		this.dep = d;
		this.bug = b;
	//	this.evalAffectedVersion();
	}

	/**
	 * <p>Getter for the field <code>dep</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 */
	public Dependency getDep() { return dep; }
	/**
	 * <p>Setter for the field <code>dep</code>.</p>
	 *
	 * @param dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 */
	public void setDep(Dependency dep) { this.dep = dep; }

	/**
	 * <p>getBugId.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	@JsonView(Views.Never.class)
	public Long getBugId() { return bug.getId(); }
	/**
	 * <p>Getter for the field <code>bug</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 */
	public Bug getBug() { return bug; }
	/**
	 * <p>Setter for the field <code>bug</code>.</p>
	 *
	 * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 */
	public void setBug(Bug bug) { this.bug = bug; }

	/**
	 * <p>Getter for the field <code>constructList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ConstructChangeInDependency> getConstructList() { return constructList; }
	/**
	 * <p>Setter for the field <code>constructList</code>.</p>
	 *
	 * @param ccd a {@link java.util.List} object.
	 */
	public void setConstructList(List<ConstructChangeInDependency> ccd) { this.constructList = ccd; }

	/**
	 * <p>isAffectedVersion.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAffectedVersion() { return this.getAffectedVersion()==1; }
	
//	public void evalAffectedVersion() { 
//		Boolean pre_commit_pom=null, line_add = null, check_version=null, manual_libId=null, equal=null;
//		AffectedVersionSource source=null;
//		if(bug.getAffectedVersions()!=null) {
//			for( AffectedLibrary a : bug.getAffectedVersions()) {
//				
//				
//				if(a.getLibraryId()!=null && dep.getLib()!=null && dep.getLib().getLibraryId()!=null) {
//					if(a.getLibraryId().equals(dep.getLib().getLibraryId())) {
//					
//						if(a.getSource() == AffectedVersionSource.MANUAL){
//							manual_libId = a.getAffected();
//						}
//						else if(a.getSource() == AffectedVersionSource.AST_EQUALITY || a.getSource() == AffectedVersionSource.MAJOR_EQUALITY || a.getSource() == AffectedVersionSource.MINOR_EQUALITY || a.getSource() == AffectedVersionSource.INTERSECTION || a.getSource() == AffectedVersionSource.GREATER_RELEASE){
//							equal = a.getAffected();
//							source = a.getSource();
//						}
//						else if(a.getSource() == AffectedVersionSource.PRE_COMMIT_POM)
//							pre_commit_pom = a.getAffected();
//						else if(a.getSource() == AffectedVersionSource.LINE_ADD)
//							line_add = a.getAffected();
//					}
//				}
//				else if(a.getLib()!=null && a.getLib()==dep.getLib()){
//					if(a.getSource() == AffectedVersionSource.MANUAL){
//						this.setAffectedVersion((a.getAffected())?1:0);
//						this.setAffectedVersionConfirmed(1);
//						this.setAffectedVersionSource(AffectedVersionSource.MANUAL);
//						return;
//					}
//					//Until the checkver problems are resolved, its results should not be considered anymore for computing the affected version assessment.
////					else if (a.getSource() == AffectedVersionSource.CHECK_VERSION)
////						check_version = a.getAffected();
//				}
//				else if (a.getLib()!=null && a.getLibraryId()!=null){
//					System.out.println("affectedLib with both LIB: " + a.getLib() + " , and LIBID: " + dep.getLib());
//				}
//			}
//			if(manual_libId!=null){
//				this.setAffectedVersion((manual_libId)?1:0);
//				this.setAffectedVersionConfirmed(1);
//				this.setAffectedVersionSource(AffectedVersionSource.MANUAL);
//				return;
//			}
//			if(equal!=null){
//				this.setAffectedVersion((equal)?1:0);
//				this.setAffectedVersionConfirmed(1);
//				this.setAffectedVersionSource(source);
//				return;
//			}				
//			if(check_version!=null){
//					this.setAffectedVersion((check_version)?1:0);
//					this.setAffectedVersionConfirmed(1);
//					this.setAffectedVersionSource(AffectedVersionSource.CHECK_VERSION);
//					return;
//				}
//			if(pre_commit_pom != null){
//				this.setAffectedVersion(pre_commit_pom?1:0);
//				this.setAffectedVersionConfirmed(1);
//				this.setAffectedVersionSource(AffectedVersionSource.PRE_COMMIT_POM);
//				return;
//			}
//			else if (line_add != null){
//				this.setAffectedVersion(line_add?1:0);
//				this.setAffectedVersionConfirmed(1);
//				this.setAffectedVersionSource(AffectedVersionSource.LINE_ADD);
//				return;
//			}
//		}
//		this.setAffectedVersionConfirmed(0);
//		this.setAffectedVersion(1); // when the confirmed flag is 0, the value of affected-version is irrelevant but we set it to 1 so that the UI doesn't filter it out when filtering out historical vulnerabilities
//	}

	/**
	 * <p>isAffectedVersionConfirmed.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAffectedVersionConfirmed() { return this.getAffectedVersionConfirmed()==1; }
	/**
	 * <p>Getter for the field <code>affectedVersionConfirmed</code>.</p>
	 *
	 * @return a int.
	 */
	public int getAffectedVersionConfirmed() { return this.affectedVersionConfirmed; }
	/**
	 * <p>Setter for the field <code>affectedVersionConfirmed</code>.</p>
	 *
	 * @param i a int.
	 */
	public void setAffectedVersionConfirmed(int i) { this.affectedVersionConfirmed = i; }

	/**
	 * <p>Getter for the field <code>affectedVersion</code>.</p>
	 *
	 * @return a int.
	 */
	public int getAffectedVersion() { return this.affectedVersion; }
	/**
	 * <p>Setter for the field <code>affectedVersion</code>.</p>
	 *
	 * @param i a int.
	 */
	public void setAffectedVersion(int i) { this.affectedVersion= i; }
	
	/**
	 * <p>Getter for the field <code>affectedVersionSource</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
	 */
	public AffectedVersionSource getAffectedVersionSource() { return this.affectedVersionSource; }
	/**
	 * <p>Setter for the field <code>affectedVersionSource</code>.</p>
	 *
	 * @param s a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
	 */
	public void setAffectedVersionSource(AffectedVersionSource s) { this.affectedVersionSource = s; }
	
	/**
	 * <p>isReachable.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReachable() { return this.getReachable()==1; }
	/**
	 * <p>Getter for the field <code>reachable</code>.</p>
	 *
	 * @return a int.
	 */
	public int getReachable() { return this.reachable; }
	
	/**
	 * Called by {@link ApplicationRepositoryCustom#updateFlags(VulnerableDependency)}.
	 *
	 * @param i a int.
	 */
	public void setReachable(int i) { this.reachable = i; }

	/**
	 * <p>isReachableConfirmed.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReachableConfirmed() { return this.getReachableConfirmed() == 1; }

	/**
	 * <p>getReachableConfirmed.</p>
	 *
	 * @return a int.
	 */
	@JsonProperty(value = "reachable_confirmed")
	public int getReachableConfirmed() {
		return ((this.dep.getReachableConstructIds()!=null && this.dep.getReachableConstructIds().size()>0)||this.reachable==1) ? 1 : 0;
	}

	/**
	 * <p>isTraced.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isTraced() { return this.getTraced()==1; }
	/**
	 * <p>Getter for the field <code>traced</code>.</p>
	 *
	 * @return a int.
	 */
	public int getTraced() { return this.traced; }
	
	/**
	 * Called by {@link ApplicationRepositoryCustom#updateFlags(VulnerableDependency)}.
	 *
	 * @param i a int.
	 */
	public void setTraced(int i) { this.traced = i; }

	/**
	 * Returns true if at least one construct of the {@link Dependency} has been traced, false otherwise.
	 *
	 * @see {@link VulnerableDependency#getTracedConfirmed()}
	 * @return a boolean.
	 */
	public boolean isTracedConfirmed() { return this.getTracedConfirmed() == 1; }
	
	/**
	 * Returns 1 if at least one construct of the {@link Dependency} has been traced, 0 otherwise.
	 *
	 * @return a int.
	 */
	@JsonProperty(value = "traced_confirmed")
	public int getTracedConfirmed() { return this.dep.getTraced()!=null && this.dep.getTraced() ? 1 : 0; }
	
	/**
	 * <p>Getter for the field <code>excemption</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Excemption} object.
	 */
	public Excemption getExcemption() {
		return excemption;
	}

	/**
	 * <p>Setter for the field <code>excemption</code>.</p>
	 *
	 * @param excemption a {@link com.sap.psr.vulas.backend.model.Excemption} object.
	 */
	public void setExcemption(Excemption excemption) {
		this.excemption = excemption;
	}
	
	/**
	 * <p>Getter for the field <code>bundledLibId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public LibraryId getBundledLibId() {
		return bundledLibId;
	}

	/**
	 * <p>Setter for the field <code>bundledLibId</code>.</p>
	 *
	 * @param bundledLibId a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public void setBundledLibId(LibraryId bundledLibId) {
		this.bundledLibId = bundledLibId;
	}
	
	/**
	 * <p>Getter for the field <code>bundledLib</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public Library getBundledLib() {
		return bundledLib;
	}

	/**
	 * <p>Setter for the field <code>bundledLib</code>.</p>
	 *
	 * @param bundledLib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public void setBundledLib (Library bundledLib) {
		this.bundledLib = bundledLib;
	}

	/**
	 * <p>getVulnDepOrigin.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
	 */
	public VulnDepOrigin getVulnDepOrigin() {
		return origin;
	}

	/**
	 * <p>setVulnDepOrigin.</p>
	 *
	 * @param origin a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
	 */
	public void setVulnDepOrigin(VulnDepOrigin origin) {
		this.origin = origin;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Orders {@link VulnerableDependency}s after filename and bug identifier.
	 */
	@Override
	public int compareTo(VulnerableDependency _other) {
		int compare = 0;
		// The deps should always have a filename, but check nevertheless
		if(this.getDep().getFilename()!=null && _other.getDep().getFilename()!=null)
			compare = this.getDep().getFilename().compareTo(_other.getDep().getFilename());
		// In the unlikely case that two deps have the same filename, compare their digest
		if(compare==0)
			compare = this.getDep().getLib().getDigest().compareTo(_other.getDep().getLib().getDigest());
		if(compare==0)
			compare = this.getBug().getBugId().compareTo(_other.getBug().getBugId());
		return compare;
	}
}
