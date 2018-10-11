package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VulnerableDependency  implements Serializable, Comparable {

	private Dependency dep;

	private Bug bug;

	
	private List<ConstructChangeInDependency> constructList;

	@JsonProperty(value = "affected_version_confirmed")
	int affectedVersionConfirmed;
	
	@JsonProperty(value = "affected_version")
	int affectedVersion;

	AffectedVersionSource affectedVersionSource;
	
	int reachable;
	
	@JsonProperty(value = "reachable_confirmed")
	int reachableConfirmed;

	int traced;

	public VulnerableDependency() { super(); }

	public VulnerableDependency(Dependency d, Bug b){
		super();
		this.dep = d;
		this.bug = b;
		this.evalAffectedVersion();
	}

	public Dependency getDep() { return dep; }
	public void setDep(Dependency dep) { this.dep = dep; }

	
	public Long getBugId() { return bug.getId(); }
	public Bug getBug() { return bug; }
	public void setBug(Bug bug) { this.bug = bug; }


	public List<ConstructChangeInDependency> getConstructList() { return constructList; }
	public void setConstructList(List<ConstructChangeInDependency> ccd) { this.constructList = ccd; }

	public boolean isAffectedVersion() { return this.getAffectedVersion() == 1; }
	
	public void evalAffectedVersion() { 
		Boolean pre_commit_pom=null, line_add = null, check_version=null, manual_libId=null, equal=null;
		AffectedVersionSource source=null;
		if(bug.getAffectedVersions()!=null) {
			for( AffectedLibrary a : bug.getAffectedVersions()) {
				
				
				if(a.getLibraryId()!=null && dep.getLib()!=null && dep.getLib().getLibraryId()!=null) {
					if(a.getLibraryId().equals(dep.getLib().getLibraryId())) {
					
						if(a.getSource() == AffectedVersionSource.MANUAL){
							manual_libId = a.getAffected();
						}
						else if(a.getSource() == AffectedVersionSource.AST_EQUALITY || a.getSource() == AffectedVersionSource.MAJOR_EQUALITY || a.getSource() == AffectedVersionSource.MINOR_EQUALITY || a.getSource() == AffectedVersionSource.INTERSECTION || a.getSource() == AffectedVersionSource.GREATER_RELEASE){
							equal = a.getAffected();
							source = a.getSource();
						}
						else if(a.getSource() == AffectedVersionSource.PRE_COMMIT_POM)
							pre_commit_pom = a.getAffected();
						else if(a.getSource() == AffectedVersionSource.LINE_ADD)
							line_add = a.getAffected();
					}
				}
				else if(a.getLib()!=null && a.getLib()==dep.getLib()){
					if(a.getSource() == AffectedVersionSource.MANUAL){
						this.setAffectedVersion((a.getAffected())?1:0);
						this.setAffectedVersionConfirmed(1);
						this.setAffectedVersionSource(AffectedVersionSource.MANUAL);
						return;
					}
					else if (a.getSource() == AffectedVersionSource.CHECK_VERSION)
						check_version = a.getAffected();
				}
				else if (a.getLib()!=null && a.getLibraryId()!=null){
					System.out.println("affectedLib with both LIB: " + a.getLib() + " , and LIBID: " + dep.getLib());
				}
			}
			if(manual_libId!=null){
				this.setAffectedVersion((manual_libId)?1:0);
				this.setAffectedVersionConfirmed(1);
				this.setAffectedVersionSource(AffectedVersionSource.MANUAL);
				return;
			}
			if(equal!=null){
				this.setAffectedVersion((equal)?1:0);
				this.setAffectedVersionConfirmed(1);
				this.setAffectedVersionSource(source);
				return;
			}				
			if(check_version!=null){
					this.setAffectedVersion((check_version)?1:0);
					this.setAffectedVersionConfirmed(1);
					this.setAffectedVersionSource(AffectedVersionSource.CHECK_VERSION);
					return;
				}
			if(pre_commit_pom != null){
				this.setAffectedVersion(pre_commit_pom?1:0);
				this.setAffectedVersionConfirmed(1);
				this.setAffectedVersionSource(AffectedVersionSource.PRE_COMMIT_POM);
				return;
			}
			else if (line_add != null){
				this.setAffectedVersion(line_add?1:0);
				this.setAffectedVersionConfirmed(1);
				this.setAffectedVersionSource(AffectedVersionSource.LINE_ADD);
				return;
			}
		}
		this.setAffectedVersionConfirmed(0);
		this.setAffectedVersion(1); // when the confirmed flag is 0, the value of affected-version is irrelevant but we set it to 1 so that the UI doesn't filter it out when filtering out historical vulnerabilities
	}
	
	public boolean isAffectedVersionConfirmed() { return this.getAffectedVersionConfirmed()==1; }
	public int getAffectedVersionConfirmed() { return this.affectedVersionConfirmed; }
	public void setAffectedVersionConfirmed(int i) { this.affectedVersionConfirmed = i; }

	public int getAffectedVersion() { return this.affectedVersion; }
	public void setAffectedVersion(int i) { this.affectedVersion= i; }
	
	public AffectedVersionSource getAffectedVersionSource() { return this.affectedVersionSource; }
	public void setAffectedVersionSource(AffectedVersionSource s) { this.affectedVersionSource = s; }
	
	public boolean isReachable() { return this.getReachable()==1; }
	public int getReachable() { return this.reachable; }
	public void setReachable(int i) { this.reachable = i; }

	public boolean isReachableConfirmed() { return this.getReachableConfirmed() == 1; }
	public int getReachableConfirmed() {
		// Need to be calculated differently than in backend model, since the collection of reachable constructs is not serialized
		//return (this.dep.getReachableConstructIds()==null || this.dep.getReachableConstructIds().size()==0) ? 0 : 1;
		return this.reachableConfirmed;
	}
	public void setReachableConfirmed(int i) { this.reachableConfirmed = i; }
	
	public boolean isTraced() { return this.getTraced()==1; }
	public int getTraced() { return this.traced; }
	public void setTraced(int i) { this.traced = i; }

	public boolean isTracedConfirmed() { return this.getTracedConfirmed() == 1; }
	@JsonProperty(value = "traced_confirmed")
	public int getTracedConfirmed() { return this.dep.getTraced()!=null && this.dep.getTraced() ? 1 : 0; }

	// =================== Added on top of the original class from vulas-backend

	public boolean isNotReachable() { return !isReachable() && isReachableConfirmed(); }
	public boolean isNotTraced() { return !isTraced() && isTracedConfirmed(); }

	private Boolean is_blacklisted;
	public void setBlacklisted(boolean _b) { this.is_blacklisted = Boolean.valueOf(_b); }
	public Boolean isBlacklisted() { return is_blacklisted; }

	public boolean isNoneAffectedVersion() { return this.getAffectedVersion()==0 && this.getAffectedVersionConfirmed()==1; }

	private Boolean above_threshold;
	public void setAboveThreshold(boolean _b) { this.above_threshold = Boolean.valueOf(_b); }
	public Boolean isAboveThreshold() { return above_threshold; }

	public Boolean isThrowsException() { return this.isAboveThreshold() && !this.isBlacklisted(); }
	public Boolean isThrowsExceptionExcluded() { return this.isAboveThreshold() && this.isBlacklisted(); }

	public Application app = null;
	public Application getApp() { return this.app; }
	public void setApp(Application _app) { this.app = _app; }
	
	public String getAffectedVersionSourceAsString() {
		if(this.affectedVersionSource==null)
			return "";
		else if(AffectedVersionSource.MANUAL.equals(this.affectedVersionSource))
			return "M";
		else if(AffectedVersionSource.PRE_COMMIT_POM.equals(this.affectedVersionSource))
			return "P";
		else if(AffectedVersionSource.CHECK_VERSION.equals(this.affectedVersionSource))
			return "C";
		else if(AffectedVersionSource.AST_EQUALITY.equals(this.affectedVersionSource))
			return "E";
		else if(AffectedVersionSource.MAJOR_EQUALITY.equals(this.affectedVersionSource))
			return "ME";
		else if(AffectedVersionSource.MINOR_EQUALITY.equals(this.affectedVersionSource))
			return "mE";
		else if(AffectedVersionSource.INTERSECTION.equals(this.affectedVersionSource))
			return "IE";
		else if(AffectedVersionSource.GREATER_RELEASE.equals(this.affectedVersionSource))
			return "GE";
		else
			return String.valueOf(this.affectedVersionSource);
	}

	@Override
	public int compareTo(Object _o) {
		VulnerableDependency other = null;
		if(_o instanceof VulnerableDependency)
			other = (VulnerableDependency)_o;
		else
			throw new IllegalArgumentException();

		final int filename_comparison = this.getDep().getFilename().compareTo(other.getDep().getFilename());
		final int bugid_comparison = this.getBug().getBugId().compareTo(other.getBug().getBugId());

		if(filename_comparison!=0)
			return filename_comparison;
		else
			return bugid_comparison;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((bug == null) ? 0 : bug.hashCode());
		result = prime * result + ((dep == null) ? 0 : dep.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VulnerableDependency other = (VulnerableDependency) obj;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		if (bug == null) {
			if (other.bug != null)
				return false;
		} else if (!bug.equals(other.bug))
			return false;
		if (dep == null) {
			if (other.dep != null)
				return false;
		} else if (!dep.equals(other.dep))
			return false;
		return true;
	}
}
