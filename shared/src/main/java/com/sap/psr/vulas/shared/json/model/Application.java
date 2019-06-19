package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.StringUtil;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = { "createdAt" }, allowGetters=true)
public class Application implements Serializable, Comparable<Application> { 
		
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Long id;

	@JsonProperty("group") 
	protected String mvnGroup;
	
	protected String artifact;
	
	protected String version;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar modifiedAt;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar lastScan;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar lastVulnChange;

	private Collection<ConstructId> constructs = new HashSet<ConstructId>();
	
	@JsonManagedReference//(value="app-deps")
	private Set<Dependency> dependencies = new TreeSet<Dependency>();
	
	public Application() { super(); }

	public Application(String group, String artifact, String version) throws IllegalArgumentException {
		super();
		try {
			this.setMvnGroup(group);
			this.setArtifact(artifact);
			this.setVersion(version);
		}
		catch(IllegalArgumentException iae) {
			throw new IllegalArgumentException("Arguments provided cannot be used to create an application identifier. Group and artifact must be specified and cannot exceed " + Constants.MAX_LENGTH_GROUP + " characters, version must be specified and cannot exceed " + Constants.MAX_LENGTH_VERSION + " characters");
		}
	}
	
	public static boolean canBuildApplication(String group, String artifact, String version) {
		boolean r = false;
		try {
			r = StringUtil.meetsLengthConstraint(group,  Constants.MAX_LENGTH_GROUP) &&
					StringUtil.meetsLengthConstraint(artifact,  Constants.MAX_LENGTH_ARTIFACT) &&
					StringUtil.meetsLengthConstraint(version,  Constants.MAX_LENGTH_VERSION);
		} catch (IllegalArgumentException e) {
			r = false;
		}
		return r;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public String getMvnGroup() { return this.mvnGroup; }
	public void setMvnGroup(@NotNull String _string) throws IllegalArgumentException {
		if(StringUtil.meetsLengthConstraint(_string,  Constants.MAX_LENGTH_GROUP))
			this.mvnGroup = _string;
	}
	
	public String getArtifact() { return this.artifact; }
	public void setArtifact(@NotNull String _string) throws IllegalArgumentException {
		if(StringUtil.meetsLengthConstraint(_string,  Constants.MAX_LENGTH_ARTIFACT))
			this.artifact = _string;
	}
	
	public String getVersion() { return this.version; }
	public void setVersion(@NotNull String _string) throws IllegalArgumentException {
		if(StringUtil.meetsLengthConstraint(_string,  Constants.MAX_LENGTH_VERSION))
			this.version = _string;
	}
	
	public boolean isComplete() {
		return this.mvnGroup!=null && !this.mvnGroup.equals("") &&
				this.artifact!=null && !this.artifact.equals("") &&
				this.version!=null && !this.version.equals("");
	}
	
	public Collection<ConstructId> getConstructs() { return constructs; }
	public void setConstructs(Collection<ConstructId> constructs) { this.constructs = constructs; }
	public void addConstructs(Collection<ConstructId> constructs) {
		this.constructs.addAll(constructs);
	}
	
	public Dependency getDependencyForPath(@NotNull String _path) {
		for(Dependency d: this.getDependencies()) {
			if(d.getPath()!=null && d.getPath().equals(_path)) {
				return d;
			}
		}
		return null;
	}
	
	public Dependency getDependency(@NotNull String _sha1) {
			for(Dependency d: this.getDependencies()) {
				if(d.getLib()!=null && d.getLib().getDigest().equals(_sha1)) {
					return d;
				}
			}
		return null;
	}
	
	public Collection<Dependency> getDependencies() { return dependencies; }
	
	/**
	 * Adds the given dependency to the dependencies of this application.
	 * @param _dependency
	 */
	public void addDependency(Dependency _dependency) {
		this.dependencies.add(_dependency);
	}
	
	/**
	 * Adds the given dependencies to the dependencies of this application.
	 * @param _dependency
	 */
	public void addDependencies(Set<Dependency> _dependencies) {
		for(Dependency d: _dependencies)
			this.dependencies.add(d);
	}
	
	public void setDependencies(Collection<Dependency> _dependencies) {
			// Get around exception: "org.hibernate.HibernateException: A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance:"
			this.dependencies.clear();
			this.dependencies.addAll(_dependencies);
	}
	
	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }
	
	public java.util.Calendar getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(java.util.Calendar modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public java.util.Calendar getLastScan() {
		return lastScan;
	}

	public void setLastScan(java.util.Calendar lastScan) {
		this.lastScan = lastScan;
	}

	public java.util.Calendar getLastVulnChange() {
		return lastVulnChange;
	}

	public void setLastVulnChange(java.util.Calendar lastVulnChange) {
		this.lastVulnChange = lastVulnChange;
	}

	/**
	 * Removes all {@link ConstructId}s and {@link Dependency}s of the application.
	 */
	public void clean() {
		this.setConstructs(new HashSet<ConstructId>());
		this.setDependencies(new HashSet<Dependency>());
	}
	
	/**
	 * Returns true if the application has {@link ConstructId}s and no {@link Dependency}s, false otherwise.
	 * The invocation of {@link Application#isEmpty()} right after {@link Application#clear()} must return true. 
	 * @return
	 */
	public Boolean isEmpty() {
		return (this.getConstructs()==null || this.getConstructs().isEmpty()) && (this.getDependencies()==null || this.getDependencies().isEmpty());
	}
	
	@JsonProperty(value = "constructCounter")
	public int countConstructs() { return ( this.getConstructs()==null ? 0 : this.getConstructs().size()); }
	
	@JsonProperty(value = "constructTypeCounters")
	public ConstructIdFilter countConstructTypes() { return new ConstructIdFilter(this.getConstructs()); }
	
	@JsonProperty(value = "countDependencies")
	public int countDependencies() { return (this.getDependencies()==null ? -1 : this.getDependencies().size()); }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((mvnGroup == null) ? 0 : mvnGroup.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		Application other = (Application) obj;
		if (artifact == null) {
			if (other.artifact != null)
				return false;
		} else if (!artifact.equals(other.artifact))
			return false;
		if (mvnGroup == null) {
			if (other.mvnGroup != null)
				return false;
		} else if (!mvnGroup.equals(other.mvnGroup))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(Application _other) {
		int v = this.getMvnGroup().compareTo((_other).getMvnGroup());
		if(v==0) v = this.getArtifact().compareTo((_other).getArtifact());
		if(v==0) v = this.getVersion().compareTo((_other).getVersion());
		return v;
	}

	@Override
	public String toString() { return this.toString(false); }
	
	public String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Application ").append(this.toString(false)).append(System.getProperty("line.separator"));
			if(this.getConstructs()!=null)
				for(ConstructId cid: this.getConstructs())
					builder.append("    ConstructId     ").append(cid).append(System.getProperty("line.separator"));
		}
		else {
			builder.append("[").append(this.getMvnGroup()).append(":").append(this.getArtifact()).append(":").append(this.getVersion()).append("]");
		}
		return builder.toString();
	}
}
