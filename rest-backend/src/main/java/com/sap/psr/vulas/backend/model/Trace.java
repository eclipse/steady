package com.sap.psr.vulas.backend.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value={"filename"}, allowSetters=true)
@Entity
@Table( name="AppTrace", uniqueConstraints=@UniqueConstraint( columnNames = { "app", "lib", "constructId" } ) )
public class Trace implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "app", referencedColumnName = "id")
	@JsonBackReference	 // Required in order to omit the app property when de-serializing JSON
	private Application app;

	@ManyToOne(optional = true)
	@JoinColumn(name = "lib",  referencedColumnName = "digest")
	private Library lib;
	
	@Transient
	private String filename;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar tracedAt;
	
	//TODO: Change to eager or follow http://stackoverflow.com/questions/24994440/no-serializer-found-for-class-org-hibernate-proxy-pojo-javassist-javassist#24994562
	@ManyToOne(optional = false, cascade = { }, fetch = FetchType.LAZY )
	@JoinColumn(name="constructId") // Required for the unique constraint
	private ConstructId constructId;
	
	/**
	 * ID of the TEST {@link GoalExecution} during which the trace was collected.
	 * The member is of type {@link String} rather than {@link GoalExecution}, as the
	 * latter will only be uploaded at the very end of the goal execution, hence, a foreign
	 * key relationship could not be satisfied.
	 */
	@Column
	private String executionId;
	
	@Column
	private int count;	

	public Trace() { super(); }
	
	public Trace(Application app, Library lib, ConstructId constructId) {
		super();
		this.app = app;
		this.lib = lib;
		this.constructId = constructId;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Application getApp() { return app; }
	public void setApp(Application app) { this.app = app; }

	public Library getLib() { return lib; }
	public void setLib(Library lib) { this.lib = lib; }
	
	public java.util.Calendar getTracedAt() { return tracedAt; }
	public void setTracedAt(java.util.Calendar tracedAt) { this.tracedAt = tracedAt; }

	public ConstructId getConstructId() { return constructId; }
	public void setConstructId(ConstructId constructId) { this.constructId = constructId; }

	public int getCount() { return count; }
	public void setCount(int count) { this.count = count; }
	
	public String getExecutionId() { return executionId; }
	public void setExecutionId(String executionId) { this.executionId = executionId; }
	
	public String getFilename() { return filename; }
	public void setFilename(String filename) { this.filename = filename; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
		result = prime * result + ((lib == null) ? 0 : lib.hashCode());
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
		Trace other = (Trace) obj;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		if (constructId == null) {
			if (other.constructId != null)
				return false;
		} else if (!constructId.equals(other.constructId))
			return false;
		if (lib == null) {
			if (other.lib != null)
				return false;
		} else if (!lib.equals(other.lib))
			return false;
		return true;
	}
	
	@Override
	public final String toString() {
		return this.toString(false);
	}
	
	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Trace").append(this.toString(false)).append(System.getProperty("line.separator"));
			builder.append("    app ").append(this.getApp()).append(System.getProperty("line.separator"));
			builder.append("    lib ").append(this.getLib()).append(System.getProperty("line.separator"));
		}
		else {
			builder.append("[").append(this.getId()).append(":").append(this.getConstructId().getQname()).append("]");
		}
		return builder.toString();
	}
}
