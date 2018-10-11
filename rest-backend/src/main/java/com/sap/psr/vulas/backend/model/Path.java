package com.sap.psr.vulas.backend.model;

import java.util.List;

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
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.PathSource;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = {"length", "startConstructId", "endConstructId", "lib"}, allowGetters=true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table( name="AppPath", uniqueConstraints=@UniqueConstraint( columnNames = { "app", "bug", "source", "lib", "startConstructId", "endConstructId" } ) )
public class Path {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "app", referencedColumnName = "id")
	@JsonBackReference	 // Required in order to omit the app property when de-serializing JSON
	private Application app;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "bug", referencedColumnName = "bugId")
	private Bug bug;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "lib",  referencedColumnName = "digest")
	private Library lib;
	
	/**
	 * ID of the TEST {@link GoalExecution} during which the path was constructed.
	 * The member is of type {@link String} rather than {@link GoalExecution}, as the
	 * latter will only be uploaded at the very end of the goal execution, hence, a foreign
	 * key relationship could not be satisfied.
	 */
	@Column
	private String executionId;
	
	@Column
	@Enumerated(EnumType.STRING)
	private PathSource source;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name="AppPathPath", joinColumns = @JoinColumn(name = "id"))
    @OrderColumn
	List<PathNode> path;
	
	@ManyToOne(optional = false, cascade = { }, fetch = FetchType.EAGER )
	@JoinColumn(name="startConstructId") // Required for the unique constraint
	private ConstructId startConstructId;
	
	@ManyToOne(optional = false, cascade = { }, fetch = FetchType.EAGER )
	@JoinColumn(name="endConstructId") // Required for the unique constraint
	private ConstructId endConstructId;
	
	public Path() { super(); }
	
	public Path(Application _app, Bug _bug, PathSource _source) {
		super();
		this.app = _app;
		this.bug = _bug;
		this.source = _source;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Application getApp() { return app; }
	public void setApp(Application app) { this.app = app; }

	public Bug getBug() { return bug; }
	public void setBug(Bug bug) { this.bug = bug; }

	public String getExecutionId() { return executionId; }
	public void setExecutionId(String executionId) { this.executionId = executionId; }

	public PathSource getSource() { return source; }
	public void setSource(PathSource source) { this.source = source; }

	public List<PathNode> getPath() { return path; }
	public void setPath(List<PathNode> path) { this.path = path; }
	
	public ConstructId getStartConstructId() { return startConstructId; }
	public void setStartConstructId(ConstructId startNode) { this.startConstructId = startNode; }
	
	public ConstructId getEndConstructId() { return endConstructId; }
	public void setEndConstructId(ConstructId endNode) { this.endConstructId = endNode; }
	
	public Library getLib() { return lib; }
	public void setLib(Library lib) { this.lib = lib; }

	@JsonProperty(value = "length")
	public int countConstructs() { return ( this.getPath()==null ? -1 : this.getPath().size()); }
	
	@Override
	public final String toString() {
		return this.toString(false);
	}
	
	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Path ").append(this.toString(false)).append(System.getProperty("line.separator"));
			builder.append("    App ").append(this.getApp()).append(", Bug ").append(this.getBug()).append(System.getProperty("line.separator"));
			builder.append("    From ").append(this.getPath().get(0).getConstructId()).append(" to ").append(this.getPath().get(this.getPath().size()-1).getConstructId()).append(System.getProperty("line.separator"));
		}
		else {
			builder.append("[").append(this.getId()).append(":").append(this.getSource()).append("]");
		}
		return builder.toString();
	}
	
	public void inferStartConstructId() {
		if(this.getStartConstructId()==null) {
			this.setStartConstructId(this.getPath().get(0).getConstructId());
		}
	}
	
	public void inferEndConstructId() {
		if(this.getEndConstructId()==null) {
			this.setEndConstructId(this.getPath().get(this.getPath().size()-1).getConstructId());
		}
	}
	
	public void inferLibrary() {
		if(this.getSource()==PathSource.A2C || this.getSource()==PathSource.T2C || this.getSource()==PathSource.X2C) {
			this.setLib(this.getPath().get(this.getPath().size()-1).getLib());
		} else if(this.getSource()==PathSource.C2A) {
			this.setLib(this.getPath().get(0).getLib());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((bug == null) ? 0 : bug.hashCode());
		result = prime * result + ((endConstructId == null) ? 0 : endConstructId.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((startConstructId == null) ? 0 : startConstructId.hashCode());
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
		Path other = (Path) obj;
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
		if (endConstructId == null) {
			if (other.endConstructId != null)
				return false;
		} else if (!endConstructId.equals(other.endConstructId))
			return false;
		if (source != other.source)
			return false;
		if (startConstructId == null) {
			if (other.startConstructId != null)
				return false;
		} else if (!startConstructId.equals(other.startConstructId))
			return false;
		return true;
	}
}