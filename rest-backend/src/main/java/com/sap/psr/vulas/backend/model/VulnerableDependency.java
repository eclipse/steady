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

	public VulnerableDependency() { super(); }

	public VulnerableDependency(Dependency d, Bug b){
		super();
		this.dep = d;
		this.bug = b;
	//	this.evalAffectedVersion();
	}

	public Dependency getDep() { return dep; }
	public void setDep(Dependency dep) { this.dep = dep; }

	@JsonView(Views.Never.class)
	public Long getBugId() { return bug.getId(); }
	public Bug getBug() { return bug; }
	public void setBug(Bug bug) { this.bug = bug; }

	public List<ConstructChangeInDependency> getConstructList() { return constructList; }
	public void setConstructList(List<ConstructChangeInDependency> ccd) { this.constructList = ccd; }

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

	public boolean isAffectedVersionConfirmed() { return this.getAffectedVersionConfirmed()==1; }
	public int getAffectedVersionConfirmed() { return this.affectedVersionConfirmed; }
	public void setAffectedVersionConfirmed(int i) { this.affectedVersionConfirmed = i; }

	public int getAffectedVersion() { return this.affectedVersion; }
	public void setAffectedVersion(int i) { this.affectedVersion= i; }
	
	public AffectedVersionSource getAffectedVersionSource() { return this.affectedVersionSource; }
	public void setAffectedVersionSource(AffectedVersionSource s) { this.affectedVersionSource = s; }
	
	public boolean isReachable() { return this.getReachable()==1; }
	public int getReachable() { return this.reachable; }
	
	/**
	 * Called by {@link ApplicationRepositoryCustom#updateFlags(VulnerableDependency)}.
	 */
	public void setReachable(int i) { this.reachable = i; }

	public boolean isReachableConfirmed() { return this.getReachableConfirmed() == 1; }

	@JsonProperty(value = "reachable_confirmed")
	public int getReachableConfirmed() {
		return ((this.dep.getReachableConstructIds()!=null && this.dep.getReachableConstructIds().size()>0)||this.reachable==1) ? 1 : 0;
	}

	public boolean isTraced() { return this.getTraced()==1; }
	public int getTraced() { return this.traced; }
	
	/**
	 * Called by {@link ApplicationRepositoryCustom#updateFlags(VulnerableDependency)}.
	 */
	public void setTraced(int i) { this.traced = i; }

	/**
	 * Returns true if at least one construct of the {@link Dependency} has been traced, false otherwise.
	 * @see {@link VulnerableDependency#getTracedConfirmed()}
	 */
	public boolean isTracedConfirmed() { return this.getTracedConfirmed() == 1; }
	
	/**
	 * Returns 1 if at least one construct of the {@link Dependency} has been traced, 0 otherwise.
	 */
	@JsonProperty(value = "traced_confirmed")
	public int getTracedConfirmed() { return this.dep.getTraced()!=null && this.dep.getTraced() ? 1 : 0; }
	
	public Excemption getExcemption() {
		return excemption;
	}

	public void setExcemption(Excemption excemption) {
		this.excemption = excemption;
	}
	
	public LibraryId getBundledLibId() {
		return bundledLibId;
	}

	public void setBundledLibId(LibraryId bundledLibId) {
		this.bundledLibId = bundledLibId;
	}
	
	public Library getBundledLib() {
		return bundledLib;
	}

	public void setBundledLib (Library bundledLib) {
		this.bundledLib = bundledLib;
	}

	public VulnDepOrigin getVulnDepOrigin() {
		return origin;
	}

	public void setVulnDepOrigin(VulnDepOrigin origin) {
		this.origin = origin;
	}

	/**
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
