package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.rest.ApplicationController;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.StringUtil;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
@Entity
@Table( name="App", uniqueConstraints=@UniqueConstraint( columnNames = { "space", "mvnGroup", "artifact", "version" } ) )
public class Application implements Serializable, Comparable { 

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "space", referencedColumnName = "id")
	@JsonBackReference	 // Required in order to omit the tenant property when de-serializing JSON
	private Space space = null;

	@Column(nullable = false, length = Constants.MAX_LENGTH_GROUP)
	@JsonProperty("group") 
	private String mvnGroup;

	@Column(nullable = false, length = Constants.MAX_LENGTH_ARTIFACT)
	private String artifact;

	@Column(nullable = false, length = Constants.MAX_LENGTH_VERSION)
	private String version;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	@JsonIgnoreProperties(value = { "createdAt" }, allowGetters=true)
	private java.util.Calendar createdAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	@JsonIgnoreProperties(value = { "modifiedAt" }, allowGetters=true)
	private java.util.Calendar modifiedAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	@JsonIgnoreProperties(value = { "lastScan" }, allowGetters=true) //TODO: to remove if the value should be given by the client
	private java.util.Calendar lastScan;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	@JsonIgnoreProperties(value = { "lastVulnChange" }, allowGetters=true)
	private java.util.Calendar lastVulnChange;

	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JsonView(Views.Never.class)
	private Collection<ConstructId> constructs;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "app", orphanRemoval=true)
	@JsonManagedReference//(value="app-deps")
	@JsonView(Views.AppDepDetails.class)
	private Collection<Dependency> dependencies;

	@Transient
	private PackageStatistics packageStats = null;
	
	@Transient
	private Boolean hasVulnerabilities = null;

	/**
	 * Only set when single applications are returned by {@link ApplicationController#getApplication(String, String, String)}.
	 * TODO: Maybe check if they can always bet set (depending on performance and memory).
	 */
	@Transient
	private Collection<Trace> traces;

	/**
	 * Contains collections of traced constructs per application package. 
	 */
	@Transient
	private PackageStatistics tracedPackageStats = null;

	public Application() { super(); }

	public Application(String group, String artifact, String version) {
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

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Space getSpace() { return space; }
	public void setSpace(Space space) { this.space = space; }

	public String getMvnGroup() { return mvnGroup; }
	public void setMvnGroup(String group) {
		if(StringUtil.meetsLengthConstraint(group,  Constants.MAX_LENGTH_GROUP))
			this.mvnGroup = group;
	}

	public String getArtifact() { return artifact; }
	public void setArtifact(String artifact) {
		if(StringUtil.meetsLengthConstraint(artifact,  Constants.MAX_LENGTH_GROUP))
			this.artifact = artifact;
	}

	public String getVersion() { return version; }
	public void setVersion(String version) {
		if(StringUtil.meetsLengthConstraint(version,  Constants.MAX_LENGTH_GROUP))
			this.version = version;
	}

	public Collection<ConstructId> getConstructs() { return constructs; }
	public void setConstructs(Collection<ConstructId> constructs) { this.constructs = constructs; }

	@JsonIgnore
	public Collection<Trace> getTraces() { return traces; }

	@JsonIgnore
	public void setTraces(Collection<Trace> traces) { this.traces = traces; }

	public Dependency getDependency(@NotNull String _digest) {
		if(this.getDependencies()!=null) {
			for(Dependency d: this.getDependencies()) {
				if(d.getLib()!=null && d.getLib().getDigest().equals(_digest)) {
					return d;
				}
			}
		}
		return null;
	}
	public Collection<Dependency> getDependencies() { return dependencies; }
	public void setDependencies(Collection<Dependency> _dependencies) {
		if(this.dependencies==null)
			this.dependencies = _dependencies;
		else{
			// Get around exception: "org.hibernate.HibernateException: A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance:"
			this.dependencies.clear();
			this.dependencies.addAll(_dependencies);
		}
	}

	public void orderDependenciesByDepth(){
		//order dependencies by length of parents
		List<Dependency> ordered_deps = new ArrayList<Dependency>(this.getDependencies());
		Comparator<Dependency> comparator = new Comparator<Dependency>() {
		    @Override
		    public int compare(Dependency left, Dependency right) {
		    	int l=0,r =0;
		    	while(left.getParent()!=null){
		    		l++;
		    		left=left.getParent();
		    	}
		    	while(right.getParent()!=null){
		    		r++;
		    		right=right.getParent();
		    	}
		        return l - r; 
		    }
		};

		Collections.sort(ordered_deps, comparator); 
		this.setDependencies(ordered_deps);
	}
	
	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }
	
	public java.util.Calendar getModifiedAt() { return modifiedAt; }
	public void setModifiedAt(java.util.Calendar modifiedAt) { this.modifiedAt = modifiedAt; }

	public java.util.Calendar getLastScan() { return lastScan; }
	public void setLastScan(java.util.Calendar lastScan) { this.lastScan = lastScan; }
	
	public java.util.Calendar getLastVulnChange() { return lastVulnChange; }
	public void setLastVulnChange(java.util.Calendar lastVulnChange) { this.lastVulnChange = lastVulnChange; }
		
	@JsonProperty(value = "lastChange")
	public java.util.Calendar getLastChange() { return ( this.getLastVulnChange().after(this.getLastScan())?  this.getLastVulnChange() : this.getLastScan()); }
	
	/**
	 * Removes all application {@link ConstructId}s and {@link Dependency}s.
	 */
	public void clean() {
		this.setConstructs(new HashSet<ConstructId>());
		this.setDependencies(new HashSet<Dependency>());
	}

	@JsonProperty(value = "constructCounter")
	@JsonView(Views.CountDetails.class)
	public int countConstructs() { return ( this.getConstructs()==null ? 0 : this.getConstructs().size()); }

	@JsonProperty(value = "constructTypeCounters")
	@JsonView(Views.CountDetails.class)
	public ConstructIdFilter countConstructTypes() { return new ConstructIdFilter(this.getConstructs()); }

	@JsonProperty(value = "countDependencies")
	@JsonView(Views.CountDetails.class)
	public int countDependencies() { return (this.getDependencies()==null ? -1 : this.getDependencies().size()); }

	@JsonProperty(value = "packageCounters")
	@JsonView(Views.CountDetails.class)
	public PackageStatistics countConstructTypesPerPackage() {
		if(this.packageStats==null)
			this.packageStats= new PackageStatistics(this.getConstructs());
		return this.packageStats;
	}

	@JsonProperty(value = "packageTraceCounters")
	@JsonView(Views.CountDetails.class)
	public PackageStatistics countTracedConstructTypesPerPackage() {
		if(this.tracedPackageStats==null && this.getTraces()!=null) {
			final Set<ConstructId> cids = new HashSet<ConstructId>();
			for(Trace t: this.getTraces()) {
				final ConstructId cid = t.getConstructId();
				final ConstructId pid = ConstructId.getPackageOf(cid);
				// Avoid adding up traces of test classes, which are typically in the same Java package than the
				// tested app classes, and which can lead to coverages > 100%
				if(this.getConstructs().contains(pid)) {
					if(this.getConstructs().contains(cid))
						cids.add(t.getConstructId());
				} else {
					cids.add(t.getConstructId());
				}
			}
			this.tracedPackageStats = new PackageStatistics(cids);
		}
		return this.tracedPackageStats;
	}

	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null) {
			this.setCreatedAt(Calendar.getInstance());
		}
		this.setModifiedAt(Calendar.getInstance());
		this.setLastScan(Calendar.getInstance());
		this.setLastVulnChange(Calendar.getInstance());
	}
	
//	@PreUpdate
//	public void preUpdate() {
//		this.setModifiedAt(Calendar.getInstance());
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((mvnGroup == null) ? 0 : mvnGroup.hashCode());
		result = prime * result + ((space == null) ? 0 : space.hashCode());
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
		if (space == null) {
			if (other.space != null)
				return false;
		} else if (!space.equals(other.space))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	/**
	 * Compares using an {@link Application}'s space, group, artifact and version.
	 */
	@Override
	public int compareTo(Object _other) {
		if(_other==null || !(_other instanceof Application))
			throw new IllegalArgumentException();
		int v = this.getSpace().getSpaceToken().compareTo(((Application)_other).getSpace().getSpaceToken());
		if(v==0) v = this.getMvnGroup().compareTo(((Application)_other).getMvnGroup());
		if(v==0) v = this.getArtifact().compareTo(((Application)_other).getArtifact());
		if(v==0) v = this.getVersion().compareTo(((Application)_other).getVersion());
		return v;
	}

	@Override
	public final String toString() { return this.toString(false); }

	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Application ").append(this.toString(false)).append(System.getProperty("line.separator"));
			for(ConstructId cid: this.getConstructs()) {
				builder.append("    ConstructId     ").append(cid).append(System.getProperty("line.separator"));
			}
		}
		else {
			builder.append("[").append(this.getSpace().getSpaceName()).append(":");
			builder.append(this.getMvnGroup()).append(":").append(this.getArtifact()).append(":").append(this.getVersion()).append("]");
		}
		return builder.toString();
	}

	public Boolean getHasVulnerabilities() {
		return hasVulnerabilities;
	}

	public void setHasVulnerabilities(Boolean hasVulnerabilities) {
		this.hasVulnerabilities = hasVulnerabilities;
	}
	
	public boolean equalsIgnoreSpace(Object obj) {
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
}
