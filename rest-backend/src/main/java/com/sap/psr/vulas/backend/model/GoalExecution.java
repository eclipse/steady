package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.GoalType;
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

/** GoalExecution class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt"},
    allowGetters = true)
@Entity
@Table(
    name = "AppGoalExe",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"app", "goal", "startedAtClient"}),
      @UniqueConstraint(columnNames = {"executionId"})
    })
public class GoalExecution implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "app", referencedColumnName = "id")
  @JsonBackReference // Required in order to omit the app property when de-serializing JSON
  private Application app;

  @Column(nullable = false, length = 9)
  @Enumerated(EnumType.STRING)
  private GoalType goal;

  /** When the entry was created in the backend. */
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  /** When the goal was started on client side. */
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar startedAtClient;

  @Column private long runtimeNano;

  /** Client-generated ID. */
  @Column private String executionId;

  /** Exception that occurred on client side (if any). */
  @Column(columnDefinition = "text")
  @JsonProperty("exception")
  private String exception;

  @Column private long memMax;

  @Column private long memUsedMax;

  @Column private long memUsedAvg;

  @Column private String clientVersion;

  @ManyToMany(
      cascade = {},
      fetch = FetchType.LAZY)
  @JsonView(Views.GoalDetails.class)
  private Collection<Property> configuration;

  // Cache
  @Transient private Map<String, String> configurationMap = null;

  @ManyToMany(
      cascade = {},
      fetch = FetchType.LAZY)
  @JsonView(Views.GoalDetails.class)
  private Collection<Property> systemInfo;

  // Cache
  @Transient private Map<String, String> systemInfoMap = null;

  @ElementCollection
  @CollectionTable(name = "AppGoalExeStatistics")
  @JsonView(Views.GoalDetails.class)
  private Map<String, Long> statistics;

  /** Constructor for GoalExecution. */
  public GoalExecution() {
    super();
  }

  /**
   * Constructor for GoalExecution.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param goal a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
   * @param startedAtClient a {@link java.util.Calendar} object.
   */
  public GoalExecution(Application app, GoalType goal, Calendar startedAtClient) {
    super();
    this.app = app;
    this.goal = goal;
    this.startedAtClient = startedAtClient;
  }

  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * Setter for the field <code>id</code>.
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Getter for the field <code>app</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  public Application getApp() {
    return app;
  }
  /**
   * Setter for the field <code>app</code>.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  public void setApp(Application app) {
    this.app = app;
  }

  /**
   * Getter for the field <code>goal</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
   */
  public GoalType getGoal() {
    return goal;
  }
  /**
   * Setter for the field <code>goal</code>.
   *
   * @param goal a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
   */
  public void setGoal(GoalType goal) {
    this.goal = goal;
  }

  /**
   * Getter for the field <code>createdAt</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getCreatedAt() {
    return createdAt;
  }
  /**
   * Setter for the field <code>createdAt</code>.
   *
   * @param createdAt a {@link java.util.Calendar} object.
   */
  public void setCreatedAt(java.util.Calendar createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Getter for the field <code>startedAtClient</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getStartedAtClient() {
    return startedAtClient;
  }
  /**
   * Setter for the field <code>startedAtClient</code>.
   *
   * @param startedAtClient a {@link java.util.Calendar} object.
   */
  public void setStartedAtClient(java.util.Calendar startedAtClient) {
    this.startedAtClient = startedAtClient;
  }

  /**
   * Getter for the field <code>runtimeNano</code>.
   *
   * @return a long.
   */
  public long getRuntimeNano() {
    return runtimeNano;
  }
  /**
   * Setter for the field <code>runtimeNano</code>.
   *
   * @param runtimeNano a long.
   */
  public void setRuntimeNano(long runtimeNano) {
    this.runtimeNano = runtimeNano;
  }

  /**
   * Getter for the field <code>executionId</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getExecutionId() {
    return executionId;
  }
  /**
   * Setter for the field <code>executionId</code>.
   *
   * @param _id a {@link java.lang.String} object.
   */
  public void setExecutionId(String _id) {
    this.executionId = _id;
  }

  /**
   * getExecutionException.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getExecutionException() {
    return exception;
  }
  /**
   * setExecutionException.
   *
   * @param exception a {@link java.lang.String} object.
   */
  public void setExecutionException(String exception) {
    this.exception = exception;
  }

  /**
   * Getter for the field <code>memMax</code>.
   *
   * @return a long.
   */
  public long getMemMax() {
    return memMax;
  }
  /**
   * Setter for the field <code>memMax</code>.
   *
   * @param memMax a long.
   */
  public void setMemMax(long memMax) {
    this.memMax = memMax;
  }

  /**
   * Getter for the field <code>memUsedMax</code>.
   *
   * @return a long.
   */
  public long getMemUsedMax() {
    return memUsedMax;
  }
  /**
   * Setter for the field <code>memUsedMax</code>.
   *
   * @param memUsedMax a long.
   */
  public void setMemUsedMax(long memUsedMax) {
    this.memUsedMax = memUsedMax;
  }

  /**
   * Getter for the field <code>memUsedAvg</code>.
   *
   * @return a long.
   */
  public long getMemUsedAvg() {
    return memUsedAvg;
  }
  /**
   * Setter for the field <code>memUsedAvg</code>.
   *
   * @param memUsedAvg a long.
   */
  public void setMemUsedAvg(long memUsedAvg) {
    this.memUsedAvg = memUsedAvg;
  }

  /**
   * Getter for the field <code>clientVersion</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getClientVersion() {
    return clientVersion;
  }
  /**
   * Setter for the field <code>clientVersion</code>.
   *
   * @param clientVersion a {@link java.lang.String} object.
   */
  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  /**
   * Getter for the field <code>configuration</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getConfiguration() {
    return configuration;
  }
  /**
   * Setter for the field <code>configuration</code>.
   *
   * @param configuration a {@link java.util.Collection} object.
   */
  public void setConfiguration(Collection<Property> configuration) {
    this.configuration = configuration;
  }

  /**
   * Returns the value of the configuration property with the given name (or null, if no such
   * property exists).
   *
   * @param _name a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  @JsonIgnore
  public String getConfiguration(@NotNull String _name) {
    if (this.configurationMap == null) {
      this.configurationMap = new HashMap<String, String>();
      for (Property p : this.getConfiguration())
        this.configurationMap.put(p.getName(), p.getPropertyValue());
    }
    return this.configurationMap.get(_name);
  }

  /**
   * Getter for the field <code>systemInfo</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getSystemInfo() {
    return systemInfo;
  }
  /**
   * Setter for the field <code>systemInfo</code>.
   *
   * @param systemInfo a {@link java.util.Collection} object.
   */
  public void setSystemInfo(Collection<Property> systemInfo) {
    this.systemInfo = systemInfo;
  }

  /**
   * Returns the value of the system info with the given name (or null, if no such property exists).
   *
   * @param _name a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  @JsonIgnore
  public String getSystemInfo(@NotNull String _name) {
    if (this.systemInfoMap == null) {
      this.systemInfoMap = new HashMap<String, String>();
      for (Property p : this.getSystemInfo())
        this.systemInfoMap.put(p.getName(), p.getPropertyValue());
    }
    return this.systemInfoMap.get(_name);
  }

  /**
   * Getter for the field <code>statistics</code>.
   *
   * @return a {@link java.util.Map} object.
   */
  public Map<String, Long> getStatistics() {
    return statistics;
  }
  /**
   * Setter for the field <code>statistics</code>.
   *
   * @param statistics a {@link java.util.Map} object.
   */
  public void setStatistics(Map<String, Long> statistics) {
    this.statistics = statistics;
  }

  /** Sets {@link GoalExecution#createdAt}. */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) {
      this.setCreatedAt(Calendar.getInstance());
    }
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
    result = prime * result + ((goal == null) ? 0 : goal.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GoalExecution other = (GoalExecution) obj;
    if (app == null) {
      if (other.app != null) return false;
    } else if (!app.equals(other.app)) return false;
    if (executionId == null) {
      if (other.executionId != null) return false;
    } else if (!executionId.equals(other.executionId)) return false;
    if (goal != other.goal) return false;
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
