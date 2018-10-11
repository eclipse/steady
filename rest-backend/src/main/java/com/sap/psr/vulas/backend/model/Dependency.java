package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.rest.ApplicationController;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.DependencyOrigin;
import com.sap.psr.vulas.shared.enums.Scope;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value={"traced", "reachableConstructIds", "touchPoints"}, allowGetters=true)
@Entity
@Table( name="AppDependency")
///uniqueConstraints=@UniqueConstraint( columnNames = { "lib" , "app"} )
// Note, the unique constraint is not defined as an annotation any longer as we need more expressiveness than what JPA allows
// The new constraints are defined in the flyway migration V20180828.1730__depParent.sql as partial indexes
public class Dependency implements Serializable{
	
	private static final long serialVersionUID = 1L;
		
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "app", referencedColumnName = "id")
	@JsonBackReference	 // Required in order to omit the app property when de-serializing JSON
	private Application app;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "lib",  referencedColumnName = "digest")
	private Library lib;
	
	@ManyToOne(optional = true, fetch = FetchType.LAZY )//, cascade = { CascadeType.REMOVE }) //we do not need to cascade operations as parents are always stored in the main list of app's dependencies and thus to cascade PERSIST would throw exceptions for saving multiple times the same managed object, similar for the DELETE
	@JoinColumn(name = "parent",  referencedColumnName = "id")
	private Dependency parent;
	
	@Column
	@Enumerated(EnumType.STRING)
	private DependencyOrigin origin;
	
	@Column
	private Boolean declared;
	
	@Column
	private Boolean traced;
		
	@Column
	@Enumerated(EnumType.STRING)
	private Scope scope;
	
	@Column
	private Boolean transitive;
	
	@Column
	private String filename;
	
	@Column(columnDefinition = "text")
	private String path;
	
	@Column(columnDefinition = "text")
	private String relativePath;
	
	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JsonView(Views.DepDetails.class)
	private Set<ConstructId> reachableConstructIds;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name="AppDependencyTouchPoints", joinColumns = @JoinColumn(name = "id"))
	@JsonView(Views.DepDetails.class)
	private Set<TouchPoint> touchPoints;
	
	/**
	 * Only set when single dependencies are returned by {@link ApplicationController#getDependency(String, String, String, String)}.
	 * TODO: Maybe check if they can always bet set (depending on performance and memory).
	 */
	@Transient
	private Collection<Trace> traces;
	
	/**
	 * Contains collections of reachable dependency constructs per {@link ConstructType}.
	 * It MUST be a subset of what can be obtained from the library via {@link Library#countConstructTypes()}.
	 */
	@Transient
	private ConstructIdFilter reachableFilter = null;
	
	/**
	 * Contains collections of traced dependency constructs per {@link ConstructType}.
	 * It MUST be a subset of what can be obtained from the library via {@link Library#countConstructTypes()}.
	 * Depending on the quality of the reachability analysis, it SHOULD be a subset of what can be obtained
	 * via {@link Dependency#countReachableConstructTypes()}. 
	 */
	@Transient
	private ConstructIdFilter tracedFilter = null;

	
	@Transient
	@JsonProperty(value = "tracedExecConstructsCounter")
	@JsonView(Views.Default.class)
	private Integer tracedExecConstructsCounter;
	
	@Transient
	@JsonView(Views.Default.class)
	private Integer reachExecConstructsCounter;
	
	public Dependency() { super(); }

	public Dependency(Application app, Library lib, Scope scope, Boolean transitive, String filename) {
		super();
		this.app = app;
		this.lib = lib;
		this.scope = scope;
		this.transitive = transitive;
		this.filename = filename;
		this.declared = (scope!=null && transitive!=null);
		this.traced = false;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Application getApp() { return app; }
	public void setApp(Application app) { this.app = app; }

	public Library getLib() { return lib; }
	public void setLib(Library lib) { this.lib = lib; }
	
	public Dependency getParent() { return parent; }
	public void setParent(Dependency parent) { this.parent = parent; }

	public DependencyOrigin getOrigin() { return origin; }
	public void setOrigin(DependencyOrigin origin) { this.origin = origin; }
	
	public Scope getScope() { return scope; }
	public void setScope(Scope scope) { this.scope = scope; }
	
	public Boolean getTransitive() { return transitive; }
	public void setTransitive(Boolean transitive) { this.transitive = transitive; }
		
	public String getFilename() { return filename; }
	public void setFilename(String filename) { this.filename = filename; }

	public Boolean getDeclared() { return declared; }
	public void setDeclared(Boolean declared) { this.declared = declared; }

	public String getPath() {return path;}
	public void setPath(String path) {this.path = path;}
	
	public String getRelativePath() {return relativePath;}
	public void setRelativePath(String relativePath) {this.relativePath = relativePath;}


	/**
	 * Returns true if {@link Dependency#traced} is not null and equal to true, false otherwise.
	 * @return
	 */
	public boolean isTraced() {
		return this.traced!=null && this.traced;
	}
	
	/**
	 * Returns the value of the member {@link Dependency#traced}, which can be null.
	 * @return
	 */
	//TODO to check whether to add flags "calls_count" and "reachableArchive" included in old backend
	public Boolean getTraced() { return traced; }
	
	/**
	 * Sets the value of the member {@link Dependency#traced}, which can be null.
	 * @return
	 */
	public void setTraced(Boolean traced) { this.traced = traced; }
	
	public Set<ConstructId> getReachableConstructIds() {
		return reachableConstructIds;
	}
	
	public void setReachableConstructIds(Set<ConstructId> reachableConstructIds) {
		this.reachableConstructIds = reachableConstructIds;
	}
	
	public void addReachableConstructIds(Set<ConstructId> reachableConstructIds) {
		if(this.getReachableConstructIds()==null)
            this.setReachableConstructIds(reachableConstructIds);
        else
            this.getReachableConstructIds().addAll(reachableConstructIds);
	}

	@JsonProperty(value = "reachableConstructTypeCounters")
	@JsonView(Views.DepDetails.class)
	public ConstructIdFilter countReachableConstructTypes() {
		if(this.reachableFilter==null)
			this.reachableFilter = new ConstructIdFilter(this.getReachableConstructIds());
		return this.reachableFilter;
	}
	
	@JsonIgnore
	public Collection<Trace> getTraces() { return traces; }
	
	@JsonIgnore
	public void setTraces(Collection<Trace> traces) { this.traces = traces; }

	@JsonProperty(value = "tracedConstructTypeCounters")
	@JsonView(Views.CountDetails.class)
	public ConstructIdFilter countTracedConstructTypes() {
		if(this.tracedFilter==null && this.getTraces()!=null) {
			final Set<ConstructId> cids = new HashSet<ConstructId>();
			for(Trace t: this.getTraces())
				cids.add(t.getConstructId());
			this.tracedFilter = new ConstructIdFilter(cids);
		}
		return this.tracedFilter;
	}
	
	@JsonIgnore
	public Set<ConstructId> getTracedConstructs() {
		if(this.getTraces()==null)
			return null;
		final Set<ConstructId> traced_cids = new HashSet<ConstructId>();
		for(Trace t: this.getTraces())
			traced_cids.add(t.getConstructId());
		return traced_cids;
	}
	
	public Collection<TouchPoint> getTouchPoints() { return touchPoints; }
	public void setTouchPoints(Set<TouchPoint> touchPoints) { this.touchPoints = touchPoints; }
        public void addTouchPoints(Set<TouchPoint> touchPoints) {
            if(this.getTouchPoints()==null)
                this.setTouchPoints(touchPoints);
            else
                this.getTouchPoints().addAll(touchPoints);
        }
	
	@PrePersist
	public void prePersist() {
		if(this.getTraced()==null) {
			this.setTraced(false);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((lib == null) ? 0 : lib.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((transitive == null) ? 0 : transitive.hashCode());
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
		Dependency other = (Dependency) obj;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (lib == null) {
			if (other.lib != null)
				return false;
		} else if (!lib.equals(other.lib))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		if (transitive == null) {
			if (other.transitive != null)
				return false;
		} else if (!transitive.equals(other.transitive))
			return false;
		return true;
	}
	
	public boolean equalLibParentRelPath(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		
		if (lib == null) {
			if (other.lib != null)
				return false;
		} else if (!lib.equals(other.lib))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equalLibParentRelPath(other.parent))
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		
		return true;
	}
	
	public void setTotalTracedExecConstructCount(Integer countTracesOfConstructorsLibrary) {
		this.tracedExecConstructsCounter  = countTracesOfConstructorsLibrary;
	}
	
	public void setTotalReachExecConstructCount(Integer countReachableExecConstructLibrary) {
		this.reachExecConstructsCounter  = countReachableExecConstructLibrary;
	}
	
}
