package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/** ConstructChange class. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during de-serialization
@Entity
@Table(
    name = "BugConstructChange",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"bug", "repo", "commit", "repoPath", "constructId"}))
public class ConstructChange implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @Column private String repo;

  @Column private String commit;

  @Column private String repoPath;

  @ManyToOne(
      optional = false,
      cascade = {},
      fetch = FetchType.EAGER)
  @JoinColumn(name = "constructId") // Required for the unique constraint
  private ConstructId constructId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "bug", referencedColumnName = "bugId")
  @JsonBackReference // Required in order to omit the bug property when de-serializing JSON
  private Bug bug;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar committedAt;

  @Column(nullable = false, length = 3)
  @Enumerated(EnumType.STRING)
  private ConstructChangeType constructChangeType;

  @Column(columnDefinition = "text")
  // @Lob
  //	@JsonSerialize(using = AstSerializer.class)
  private String buggyBody;

  @Column(columnDefinition = "text")
  // @Lob
  // @JsonSerialize(using = AstSerializer.class)
  private String fixedBody;

  @Column(columnDefinition = "text")
  // @Lob
  private String bodyChange;

  /** Constructor for ConstructChange. */
  public ConstructChange() {
    super();
  }

  /**
   * Constructor for ConstructChange.
   *
   * @param repo a {@link java.lang.String} object.
   * @param commit a {@link java.lang.String} object.
   * @param path a {@link java.lang.String} object.
   * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   * @param committedAt a {@link java.util.Calendar} object.
   * @param changeType a {@link com.sap.psr.vulas.backend.model.ConstructChangeType} object.
   */
  public ConstructChange(
      String repo,
      String commit,
      String path,
      ConstructId constructId,
      Calendar committedAt,
      ConstructChangeType changeType) {
    super();
    this.repo = repo;
    this.commit = commit;
    this.repoPath = path;
    this.constructId = constructId;
    this.committedAt = committedAt;
    this.constructChangeType = changeType;
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
   * Getter for the field <code>repo</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepo() {
    return repo;
  }
  /**
   * Setter for the field <code>repo</code>.
   *
   * @param repo a {@link java.lang.String} object.
   */
  public void setRepo(String repo) {
    this.repo = repo;
  }

  /**
   * Getter for the field <code>commit</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCommit() {
    return commit;
  }
  /**
   * Setter for the field <code>commit</code>.
   *
   * @param commit a {@link java.lang.String} object.
   */
  public void setCommit(String commit) {
    this.commit = commit;
  }

  /**
   * Getter for the field <code>repoPath</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepoPath() {
    return repoPath;
  }
  /**
   * Setter for the field <code>repoPath</code>.
   *
   * @param path a {@link java.lang.String} object.
   */
  public void setRepoPath(String path) {
    this.repoPath = path;
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
   * Getter for the field <code>bug</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Bug} object.
   */
  public Bug getBug() {
    return bug;
  }
  /**
   * Setter for the field <code>bug</code>.
   *
   * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
   */
  public void setBug(Bug bug) {
    this.bug = bug;
  }

  /**
   * Getter for the field <code>committedAt</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getCommittedAt() {
    return committedAt;
  }
  /**
   * Setter for the field <code>committedAt</code>.
   *
   * @param committedAt a {@link java.util.Calendar} object.
   */
  public void setCommittedAt(java.util.Calendar committedAt) {
    this.committedAt = committedAt;
  }

  /**
   * Getter for the field <code>constructChangeType</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructChangeType} object.
   */
  public ConstructChangeType getConstructChangeType() {
    return constructChangeType;
  }
  /**
   * Setter for the field <code>constructChangeType</code>.
   *
   * @param changeType a {@link com.sap.psr.vulas.backend.model.ConstructChangeType} object.
   */
  public void setConstructChangeType(ConstructChangeType changeType) {
    this.constructChangeType = changeType;
  }

  /**
   * Getter for the field <code>buggyBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBuggyBody() {
    return buggyBody;
  }
  /**
   * Setter for the field <code>buggyBody</code>.
   *
   * @param buggyBody a {@link java.lang.String} object.
   */
  public void setBuggyBody(String buggyBody) {
    this.buggyBody = buggyBody;
  }

  /**
   * Getter for the field <code>fixedBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFixedBody() {
    return fixedBody;
  }
  /**
   * Setter for the field <code>fixedBody</code>.
   *
   * @param fixedBody a {@link java.lang.String} object.
   */
  public void setFixedBody(String fixedBody) {
    this.fixedBody = fixedBody;
  }

  /**
   * Getter for the field <code>bodyChange</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBodyChange() {
    return bodyChange;
  }
  /**
   * Setter for the field <code>bodyChange</code>.
   *
   * @param bodyChange a {@link java.lang.String} object.
   */
  public void setBodyChange(String bodyChange) {
    this.bodyChange = bodyChange;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("[").append(this.getId()).append(":").append(this.getCommit()).append("]");
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
    result = prime * result + ((commit == null) ? 0 : commit.hashCode());
    result = prime * result + ((repoPath == null) ? 0 : repoPath.hashCode());
    result = prime * result + ((repo == null) ? 0 : repo.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructChange other = (ConstructChange) obj;
    if (constructId == null) {
      if (other.constructId != null) return false;
    } else if (!constructId.equals(other.constructId)) return false;
    if (commit == null) {
      if (other.commit != null) return false;
    } else if (!commit.equals(other.commit)) return false;
    if (repoPath == null) {
      if (other.repoPath != null) return false;
    } else if (!repoPath.equals(other.repoPath)) return false;
    if (repo == null) {
      if (other.repo != null) return false;
    } else if (!repo.equals(other.repo)) return false;
    return true;
  }
}
