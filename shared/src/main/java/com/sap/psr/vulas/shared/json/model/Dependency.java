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

/**
 * <p>Dependency class.</p>
 *
 */
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
	
	/**
	 * <p>Constructor for Dependency.</p>
	 */
	public Dependency() { super(); }

	/**
	 * <p>Constructor for Dependency.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 * @param scope a {@link com.sap.psr.vulas.shared.enums.Scope} object.
	 * @param transitive a {@link java.lang.Boolean} object.
	 * @param filename a {@link java.lang.String} object.
	 * @param path a {@link java.lang.String} object.
	 */
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
	
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getId() { return id; }
	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.Long} object.
	 */
	public void setId(Long id) { this.id = id; }
	
	/**
	 * <p>Getter for the field <code>app</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 */
	public Application getApp() { return app; }
	/**
	 * <p>Setter for the field <code>app</code>.</p>
	 *
	 * @param app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 */
	public void setApp(Application app) { this.app = app; }
	
	public void setAppRecursively(Application app) { 
		this.app = app;
		if(this.parent!=null)
			this.parent.setAppRecursively(app);
	}

	/**
	 * <p>Getter for the field <code>lib</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 */
	public Library getLib() { return lib; }
	/**
	 * <p>Setter for the field <code>lib</code>.</p>
	 *
	 * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 */
	public void setLib(Library lib) { this.lib = lib; }

	/**
	 * <p>Getter for the field <code>parent</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Dependency} object.
	 */
	public Dependency getParent() { return parent; }
	/**
	 * <p>Setter for the field <code>parent</code>.</p>
	 *
	 * @param parent a {@link com.sap.psr.vulas.shared.json.model.Dependency} object.
	 */
	public void setParent(Dependency parent) { this.parent = parent; }
	/**
	 * <p>isParent.</p>
	 *
	 * @param _dep a {@link com.sap.psr.vulas.shared.json.model.Dependency} object.
	 * @return a boolean.
	 */
	public boolean isParent(Dependency _dep) {
		if(this.parent==null)
			return false;
		else if(this.parent.equals(_dep))
			return true;
		else
			return this.parent.isParent(_dep);
	}

	/**
	 * <p>Getter for the field <code>origin</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.DependencyOrigin} object.
	 */
	public DependencyOrigin getOrigin() { return origin; }
	/**
	 * <p>Setter for the field <code>origin</code>.</p>
	 *
	 * @param origin a {@link com.sap.psr.vulas.shared.enums.DependencyOrigin} object.
	 */
	public void setOrigin(DependencyOrigin origin) { this.origin = origin; }
	
	/**
	 * <p>Getter for the field <code>relativePath</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRelativePath() {return relativePath;}
	/**
	 * <p>Setter for the field <code>relativePath</code>.</p>
	 *
	 * @param relativePath a {@link java.lang.String} object.
	 */
	public void setRelativePath(String relativePath) {this.relativePath = relativePath;}
	
	/**
	 * <p>Getter for the field <code>scope</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.Scope} object.
	 */
	public Scope getScope() { return scope; }
	/**
	 * <p>Setter for the field <code>scope</code>.</p>
	 *
	 * @param scope a {@link com.sap.psr.vulas.shared.enums.Scope} object.
	 */
	public void setScope(Scope scope) { this.scope = scope; }
	
	/**
	 * <p>Getter for the field <code>transitive</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getTransitive() { return transitive; }
	/**
	 * <p>Setter for the field <code>transitive</code>.</p>
	 *
	 * @param transitive a {@link java.lang.Boolean} object.
	 */
	public void setTransitive(Boolean transitive) { this.transitive = transitive; }
		
	/**
	 * <p>Getter for the field <code>filename</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilename() { return filename; }
	/**
	 * <p>Setter for the field <code>filename</code>.</p>
	 *
	 * @param filename a {@link java.lang.String} object.
	 */
	public void setFilename(String filename) { this.filename = filename; }
	
	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPath() { return path; }
	/**
	 * <p>Setter for the field <code>path</code>.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public void setPath(String path) { this.path = path; }

	/**
	 * <p>Getter for the field <code>declared</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getDeclared() { return declared; }
	/**
	 * <p>Setter for the field <code>declared</code>.</p>
	 *
	 * @param declared a {@link java.lang.Boolean} object.
	 */
	public void setDeclared(Boolean declared) { this.declared = declared; }

	//TODO to check whether to add flags "calls_count" and "reachableArchive" included in old backend
	/**
	 * <p>Getter for the field <code>traced</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getTraced() { return traced; }
	/**
	 * <p>Setter for the field <code>traced</code>.</p>
	 *
	 * @param traced a {@link java.lang.Boolean} object.
	 */
	public void setTraced(Boolean traced) { this.traced = traced; }
	
	/**
	 * <p>Getter for the field <code>reachableConstructIds</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<ConstructId> getReachableConstructIds() { return reachableConstructIds; }
	/**
	 * <p>Setter for the field <code>reachableConstructIds</code>.</p>
	 *
	 * @param reachableConstructIds a {@link java.util.Collection} object.
	 */
	public void setReachableConstructIds(Collection<ConstructId> reachableConstructIds) { this.reachableConstructIds = reachableConstructIds; }

	/**
	 * <p>Getter for the field <code>touchPoints</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<PathNode> getTouchPoints() { return touchPoints; }
	/**
	 * <p>Setter for the field <code>touchPoints</code>.</p>
	 *
	 * @param touchPoints a {@link java.util.List} object.
	 */
	public void setTouchPoints(List<PathNode> touchPoints) { this.touchPoints = touchPoints; }
	
	/**
	 * {@inheritDoc}
	 *
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
	
	/** {@inheritDoc} */
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
	 * {@inheritDoc}
	 *
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
	
	/**
	 * <p>equalLibParentRelPath.</p>
	 *
	 * @param obj a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
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
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[app=").append(this.app.toString()).append(", lib=").append(this.lib.toString()).append(", filename=").append(this.filename).append(", scope=").append(this.scope).append(", trans=").append(this.getTransitive()).append("]");
		return b.toString();
	}
}
