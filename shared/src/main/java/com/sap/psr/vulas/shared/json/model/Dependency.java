package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.DependencyOrigin;
import com.sap.psr.vulas.shared.enums.Scope;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value={"reachableConstructIds", "touchPoints"}, allowGetters=true)
public class Dependency implements Serializable, Comparable<Dependency> {
	
	private static final long serialVersionUID = 1L;
		
	@JsonIgnore
	private Long id;

	@JsonBackReference	 // Required in order to omit the app property when de-serializing JSON
	private Application app;

	private Library lib;
	
	private Boolean declared;
	
	private Boolean traced;
		
	private Scope scope;
	
	private Boolean transitive;
	
	private String filename;
	
	private String path;
	
	private Collection<ConstructId> reachableConstructIds;
	
	List<PathNode> touchPoints;
	
	private Dependency parent;
	
	private DependencyOrigin origin;
	
	private String relativePath;
	
	public Dependency() { super(); }

	public Dependency(Application app, Library lib, Scope scope, Boolean transitive, String filename, String path) {
		super();
		this.app = app;
		this.lib = lib;
		this.scope = scope;
		this.transitive = transitive;
		this.filename = filename;
		this.path = path;
		this.declared = (scope!=null && transitive!=null);
		this.traced = false;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Application getApp() { return app; }
	public void setApp(Application app) { this.app = app; }
	
	public void setAppRecursively(Application app) { 
		this.app = app;
		if(this.parent!=null)
			this.parent.setAppRecursively(app);
	}

	public Library getLib() { return lib; }
	public void setLib(Library lib) { this.lib = lib; }

	public Dependency getParent() { return parent; }
	public void setParent(Dependency parent) { this.parent = parent; }
	public boolean isParent(Dependency _dep) {
		if(this.parent==null)
			return false;
		else if(this.parent.equals(_dep))
			return true;
		else
			return this.parent.isParent(_dep);
	}

	public DependencyOrigin getOrigin() { return origin; }
	public void setOrigin(DependencyOrigin origin) { this.origin = origin; }
	
	public String getRelativePath() {return relativePath;}
	public void setRelativePath(String relativePath) {this.relativePath = relativePath;}
	
	public Scope getScope() { return scope; }
	public void setScope(Scope scope) { this.scope = scope; }
	
	public Boolean getTransitive() { return transitive; }
	public void setTransitive(Boolean transitive) { this.transitive = transitive; }
		
	public String getFilename() { return filename; }
	public void setFilename(String filename) { this.filename = filename; }
	
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }

	public Boolean getDeclared() { return declared; }
	public void setDeclared(Boolean declared) { this.declared = declared; }

	//TODO to check whether to add flags "calls_count" and "reachableArchive" included in old backend
	public Boolean getTraced() { return traced; }
	public void setTraced(Boolean traced) { this.traced = traced; }
	
	public Collection<ConstructId> getReachableConstructIds() { return reachableConstructIds; }
	public void setReachableConstructIds(Collection<ConstructId> reachableConstructIds) { this.reachableConstructIds = reachableConstructIds; }

	public List<PathNode> getTouchPoints() { return touchPoints; }
	public void setTouchPoints(List<PathNode> touchPoints) { this.touchPoints = touchPoints; }
	
	/**
	 * Compares this dependency with the given dependency by looking at parent-child relationships,
	 * library identifiers and filenames.
	 */
	@Override
	public int compareTo(Dependency _other) {		
		if(_other.isParent(this))
			return -1;
		else if(this.isParent(_other))
			return +1;
		else {
			if(this.getFilename()!=null && _other.getFilename()!=null) {
				return this.getFilename().compareTo(_other.getFilename());
			} else if(this.getLib().getDigest()!=null && _other.getLib().getDigest()!=null) {
				return this.getLib().getDigest().compareTo(_other.getLib().getDigest());
			} else if(this.getLib().getLibraryId()!=null && _other.getLib().getLibraryId()!=null) {
				return this.getLib().getLibraryId().compareTo(_other.getLib().getLibraryId());
			} else {
				throw new IllegalStateException("Cannot compare " + this + " with " + _other);
			}
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
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((transitive == null) ? 0 : transitive.hashCode());
		return result;
	}

	/**
	 * Considers the application, the library, the path and filename, the scope and transitivity to compare the objects.
	 */
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
	
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[app=").append(this.app.toString()).append(", lib=").append(this.lib.toString()).append(", filename=").append(this.filename).append(", scope=").append(this.scope).append(", trans=").append(this.getTransitive()).append("]");
		return b.toString();
	}
}
