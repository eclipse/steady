package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/** V_AppVulndep class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@IdClass(V_AppVulndep.class)
@Table(
    name = "v_app_vulndep",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"app_id", "dep_id", "lib_id", "digest", "bug"}))
public class V_AppVulndep implements Serializable {

  @Id private Long app_id;

  @Column private String app_group;

  @Column private String app_artifact;

  @Column private String app_version;

  @Id private Long dep_id;

  @Id private String bug;

  @Id private Long lib_id;

  @Id private String digest;

  @Column private String filename;

  @Column private String scope;

  @Column private Boolean transitive;

  @Column private String mvn_group;

  @Column private String artifact;

  @Column private String version;

  @Column private Boolean affected;

  /** Constructor for V_AppVulndep. */
  public V_AppVulndep() {
    super();
  }

  /**
   * Getter for the field <code>app_id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getApp_id() {
    return app_id;
  }

  /**
   * Setter for the field <code>app_id</code>.
   *
   * @param app_id a {@link java.lang.Long} object.
   */
  public void setApp_id(Long app_id) {
    this.app_id = app_id;
  }

  /**
   * Getter for the field <code>dep_id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getDep_id() {
    return dep_id;
  }

  /**
   * Setter for the field <code>dep_id</code>.
   *
   * @param dep_id a {@link java.lang.Long} object.
   */
  public void setDep_id(Long dep_id) {
    this.dep_id = dep_id;
  }

  /**
   * Getter for the field <code>lib_id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getLib_id() {
    return lib_id;
  }

  /**
   * Setter for the field <code>lib_id</code>.
   *
   * @param lib_id a {@link java.lang.Long} object.
   */
  public void setLib_id(Long lib_id) {
    this.lib_id = lib_id;
  }

  /**
   * Setter for the field <code>bug</code>.
   *
   * @param bug a {@link java.lang.String} object.
   */
  public void setBug(String bug) {
    this.bug = bug;
  }

  /**
   * Getter for the field <code>app_group</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getApp_group() {
    return app_group;
  }

  /**
   * Setter for the field <code>app_group</code>.
   *
   * @param app_group a {@link java.lang.String} object.
   */
  public void setApp_group(String app_group) {
    this.app_group = app_group;
  }

  /**
   * Getter for the field <code>app_artifact</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getApp_artifact() {
    return app_artifact;
  }

  /**
   * Setter for the field <code>app_artifact</code>.
   *
   * @param app_artifact a {@link java.lang.String} object.
   */
  public void setApp_artifact(String app_artifact) {
    this.app_artifact = app_artifact;
  }

  /**
   * Getter for the field <code>app_version</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getApp_version() {
    return app_version;
  }

  /**
   * Setter for the field <code>app_version</code>.
   *
   * @param app_version a {@link java.lang.String} object.
   */
  public void setApp_version(String app_version) {
    this.app_version = app_version;
  }

  /**
   * Getter for the field <code>bug</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBug() {
    return bug;
  }

  /**
   * setBugId.
   *
   * @param bug a {@link java.lang.String} object.
   */
  public void setBugId(String bug) {
    this.bug = bug;
  }

  /**
   * Getter for the field <code>digest</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigest() {
    return digest;
  }

  /**
   * Setter for the field <code>digest</code>.
   *
   * @param digest a {@link java.lang.String} object.
   */
  public void setDigest(String digest) {
    this.digest = digest;
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

  /**
   * Getter for the field <code>mvn_group</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMvn_group() {
    return mvn_group;
  }

  /**
   * Setter for the field <code>mvn_group</code>.
   *
   * @param mvn_group a {@link java.lang.String} object.
   */
  public void setMvn_group(String mvn_group) {
    this.mvn_group = mvn_group;
  }

  /**
   * Getter for the field <code>artifact</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArtifact() {
    return artifact;
  }

  /**
   * Setter for the field <code>artifact</code>.
   *
   * @param artifact a {@link java.lang.String} object.
   */
  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  /**
   * Getter for the field <code>version</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Setter for the field <code>version</code>.
   *
   * @param version a {@link java.lang.String} object.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Getter for the field <code>affected</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getAffected() {
    return affected;
  }

  /**
   * Setter for the field <code>affected</code>.
   *
   * @param affected a {@link java.lang.Boolean} object.
   */
  public void setAffected(Boolean affected) {
    this.affected = affected;
  }
}
