package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import javax.persistence.PrePersist;
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PropertySource;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value={ "createdAt"}, allowGetters=true)
@Entity
@Table( name="AppGoalExe", uniqueConstraints=@UniqueConstraint( columnNames = { "app", "goal", "startedAtClient"} ) )
public class GoalExecution implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "app", referencedColumnName = "id")
	@JsonBackReference	 // Required in order to omit the app property when de-serializing JSON
	private Application app;
	
	@Column(nullable = false, length = 9)
	@Enumerated(EnumType.STRING)
	private GoalType goal;
	
	/**
	 * When the entry was created in the backend.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	/**
	 * When the goal was started on client side.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar startedAtClient;
	
	@Column
	private long runtimeNano;
	
	/**
	 * Client-generated ID.
	 */
	@Column
	private String executionId;
	
	/**
	 * Exception that occurred on client side (if any).
	 */
	@Column(columnDefinition = "text")
	@JsonProperty("exception") 
	private String exception;
	
	@Column
	private long memMax;
	
	@Column
	private long memUsedMax;
	
	@Column
	private long memUsedAvg;
	
	@Column
	private String clientVersion;
	
	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JsonView(Views.GoalDetails.class)
	private Collection<Property> configuration;
	
	// Cache
	@Transient
	private Map<String, String> configurationMap = null;
	
	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JsonView(Views.GoalDetails.class)
	private Collection<Property> systemInfo;
	
	// Cache
	@Transient
	private Map<String, String> systemInfoMap = null;
	
	@ElementCollection
	@CollectionTable(name="AppGoalExeStatistics")
	@JsonView(Views.GoalDetails.class)
	private Map<String,Long> statistics;

	public GoalExecution() { super(); }
	
	public GoalExecution(Application app, GoalType goal, Calendar startedAtClient) {
		super();
		this.app = app;
		this.goal = goal;
		this.startedAtClient = startedAtClient;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Application getApp() { return app; }
	public void setApp(Application app) { this.app = app; }

	public GoalType getGoal() { return goal; }
	public void setGoal(GoalType goal) { this.goal = goal; }

	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public java.util.Calendar getStartedAtClient() { return startedAtClient; }
	public void setStartedAtClient(java.util.Calendar startedAtClient) { this.startedAtClient = startedAtClient; }

	public long getRuntimeNano() { return runtimeNano; }
	public void setRuntimeNano(long runtimeNano) { this.runtimeNano = runtimeNano; }

	public String getExecutionId() { return executionId; }
	public void setExecutionId(String _id) { this.executionId = _id; }

	public String getExecutionException() { return exception; }
	public void setExecutionException(String exception) { this.exception = exception; }

	public long getMemMax() { return memMax; }
	public void setMemMax(long memMax) { this.memMax = memMax; }

	public long getMemUsedMax() { return memUsedMax; }
	public void setMemUsedMax(long memUsedMax) { this.memUsedMax = memUsedMax; }

	public long getMemUsedAvg() { return memUsedAvg; }
	public void setMemUsedAvg(long memUsedAvg) { this.memUsedAvg = memUsedAvg; }

	public String getClientVersion() { return clientVersion; }
	public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }

	public Collection<Property> getConfiguration() { return configuration; }
	public void setConfiguration(Collection<Property> configuration) { this.configuration = configuration; }

	/**
	 * Returns the value of the configuration property with the given name (or null, if no such property exists).
	 * @param _name
	 * @return
	 */
	@JsonIgnore
	public String getConfiguration(@NotNull String _name) {
		if(this.configurationMap==null) {
			this.configurationMap = new HashMap<String, String>();
			for(Property p: this.getConfiguration())
				this.configurationMap.put(p.getName(), p.getPropertyValue());
		}
		return this.configurationMap.get(_name);
	}
	
	public Collection<Property> getSystemInfo() { return systemInfo; }
	public void setSystemInfo(Collection<Property> systemInfo) { this.systemInfo = systemInfo; }

	/**
	 * Returns the value of the system info with the given name (or null, if no such property exists).
	 * @param _name
	 * @return
	 */
	@JsonIgnore
	public String getSystemInfo(@NotNull String _name) {
		if(this.systemInfoMap==null) {
			this.systemInfoMap = new HashMap<String, String>();
			for(Property p: this.getSystemInfo())
				this.systemInfoMap.put(p.getName(), p.getPropertyValue());
		}
		return this.systemInfoMap.get(_name);
	}
	
	public Map<String, Long> getStatistics() { return statistics; }
	public void setStatistics(Map<String, Long> statistics) { this.statistics = statistics; }
	
	/**
	 * Sets {@link GoalExecution#createdAt}.
	 */
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null) {
			this.setCreatedAt(Calendar.getInstance());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
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
		GoalExecution other = (GoalExecution) obj;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		if (executionId == null) {
			if (other.executionId != null)
				return false;
		} else if (!executionId.equals(other.executionId))
			return false;
		if (goal != other.goal)
			return false;
		return true;
	}

	/*@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		result = prime * result + ((startedAtClient == null) ? 0 : startedAtClient.hashCode());
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
		GoalExecution other = (GoalExecution) obj;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		if (goal != other.goal)
			return false;
		if (startedAtClient == null) {
			if (other.startedAtClient != null)
				return false;
		} else if (!startedAtClient.equals(other.startedAtClient))
			return false;
		return true;
	}*/
	
	
}
