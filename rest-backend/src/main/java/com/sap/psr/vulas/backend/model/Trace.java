package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

/** Trace class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"filename"},
    allowSetters = true)
@Entity
@Table(
    name = "AppTrace",
    uniqueConstraints = @UniqueConstraint(columnNames = {"app", "lib", "constructId"}))
public class Trace implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "app", referencedColumnName = "id")
  @JsonBackReference // Required in order to omit the app property when de-serializing JSON
  private Application app;

  @ManyToOne(optional = true)
  @JoinColumn(name = "lib", referencedColumnName = "digest")
  private Library lib;

  @Transient private String filename;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar tracedAt;

  // TODO: Change to eager or follow
  // http://stackoverflow.com/questions/24994440/no-serializer-found-for-class-org-hibernate-proxy-pojo-javassist-javassist#24994562
  @ManyToOne(
      optional = false,
      cascade = {},
      fetch = FetchType.LAZY)
  @JoinColumn(name = "constructId") // Required for the unique constraint
  private ConstructId constructId;

  /**
   * ID of the TEST {@link GoalExecution} during which the trace was collected. The member is of
   * type {@link String} rather than {@link GoalExecution}, as the latter will only be uploaded at
   * the very end of the goal execution, hence, a foreign key relationship could not be satisfied.
   */
  @Column private String executionId;

  @Column private int count;

  /** Constructor for Trace. */
  public Trace() {
    super();
  }

  /**
   * Constructor for Trace.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public Trace(Application app, Library lib, ConstructId constructId) {
    super();
    this.app = app;
    this.lib = lib;
    this.constructId = constructId;
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
   * Getter for the field <code>lib</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public Library getLib() {
    return lib;
  }
  /**
   * Setter for the field <code>lib</code>.
   *
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public void setLib(Library lib) {
    this.lib = lib;
  }

  /**
   * Getter for the field <code>tracedAt</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getTracedAt() {
    return tracedAt;
  }
  /**
   * Setter for the field <code>tracedAt</code>.
   *
   * @param tracedAt a {@link java.util.Calendar} object.
   */
  public void setTracedAt(java.util.Calendar tracedAt) {
    this.tracedAt = tracedAt;
  }

  /**
   * Getter for the field <code>constructId</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }
  /**
   * Setter for the field <code>constructId</code>.
   *
   * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public void setConstructId(ConstructId constructId) {
    this.constructId = constructId;
  }

  /**
   * Getter for the field <code>count</code>.
   *
   * @return a int.
   */
  public int getCount() {
    return count;
  }
  /**
   * Setter for the field <code>count</code>.
   *
   * @param count a int.
   */
  public void setCount(int count) {
    this.count = count;
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
   * @param executionId a {@link java.lang.String} object.
   */
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  /**
   * Getter for the field <code>filename</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFilename() {
    return filename;
  }
  /**
   * Setter for the field <code>filename</code>.
   *
   * @param filename a {@link java.lang.String} object.
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
    result = prime * result + ((lib == null) ? 0 : lib.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Trace other = (Trace) obj;
    if (app == null) {
      if (other.app != null) return false;
    } else if (!app.equals(other.app)) return false;
    if (constructId == null) {
      if (other.constructId != null) return false;
    } else if (!constructId.equals(other.constructId)) return false;
    if (lib == null) {
      if (other.lib != null) return false;
    } else if (!lib.equals(other.lib)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return this.toString(false);
  }

  /**
   * toString.
   *
   * @param _deep a boolean.
   * @return a {@link java.lang.String} object.
   */
  public final String toString(boolean _deep) {
    final StringBuilder builder = new StringBuilder();
    if (_deep) {
      builder
          .append("Trace")
          .append(this.toString(false))
          .append(System.getProperty("line.separator"));
      builder.append("    app ").append(this.getApp()).append(System.getProperty("line.separator"));
      builder.append("    lib ").append(this.getLib()).append(System.getProperty("line.separator"));
    } else {
      builder
          .append("[")
          .append(this.getId())
          .append(":")
          .append(this.getConstructId().getQname())
          .append("]");
    }
    return builder.toString();
  }
}
