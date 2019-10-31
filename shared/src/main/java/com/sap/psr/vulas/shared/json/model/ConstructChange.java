package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/** ConstructChange class. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during de-serialization
public class ConstructChange implements Serializable, Comparable<ConstructChange> {

  private static final long serialVersionUID = 1L;

  @JsonIgnore private Long id;

  private String repo;

  private String commit;

  private String repoPath;

  private ConstructId constructId;

  @JsonBackReference // Required in order to omit the bug property when de-serializing JSON
  private Bug bug;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar committedAt;

  private ConstructChangeType constructChangeType;

  private String buggyBody;

  private String fixedBody;

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
   * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @param committedAt a {@link java.util.Calendar} object.
   * @param changeType a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
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
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }
  /**
   * Setter for the field <code>constructId</code>.
   *
   * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void setConstructId(ConstructId constructId) {
    this.constructId = constructId;
  }

  /**
   * Getter for the field <code>bug</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
   */
  public Bug getBug() {
    return bug;
  }
  /**
   * Setter for the field <code>bug</code>.
   *
   * @param bug a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
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
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getConstructChangeType() {
    return constructChangeType;
  }
  /**
   * Setter for the field <code>constructChangeType</code>.
   *
   * @param changeType a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
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

  /**
   * {@inheritDoc}
   *
   * <p>Order ChangeListConstructs using their timeStamp
   */
  @Override
  public int compareTo(ConstructChange o) {
    if (this.committedAt.compareTo(o.committedAt) == 0) {
      return this.constructId.getQname().compareTo(o.getConstructId().getQname());
    } else {
      return this.committedAt.compareTo(o.committedAt);
    }
  }

  /**
   * Splits the given {@link ConstructChange}s into groups of changes that apply to different
   * releases, typically trunk and branch.
   *
   * <p>This split is done using different heuristics: First, by checking all the paths that precede
   * the src directory. If that fails, by finding paths that contain the key words trunk and branch.
   *
   * <p>If none of them work, the method returns "" so that all construct changes will be put into
   * one group.
   *
   * <p>Example (CVE-2014-3529): The construct
   * "org.apache.poi.openxml4j.opc.internal.ContentTypeManager.parseContentTypesFile(InputStream)"
   * has been changed both in trunk and branch:
   * /poi/trunk/src/ooxml/java/org/apache/poi/openxml4j/opc/internal/ContentTypeManager.java
   * /poi/branches/REL_3_10_BRANCH/src/ooxml/java/org/apache/poi/openxml4j/opc/internal/ContentTypeManager.java
   *
   * <p>In that case, the method will return both /poi/trunk and /poi/branches/REL_3_10_BRANCH, so
   * that the archive check can be done one after the other.
   *
   * @param _changes a {@link java.util.Set} object.
   * @return a {@link java.util.Set} object.
   */
  @JsonIgnore
  public static Set<String> getRepoPathsToSrc(Set<ConstructChange> _changes) {
    final Set<String> paths = new HashSet<String>();
    int idx;

    // Check whether every path contains /src/
    boolean all_contain_src = true;
    for (ConstructChange change : _changes)
      all_contain_src =
          all_contain_src
              && change.getRepoPath() != null
              && change.getRepoPath().indexOf("/src/") != -1;

    // Yes, get all the different paths preceding /src/
    if (all_contain_src) {
      for (ConstructChange change : _changes) {
        idx = change.getRepoPath().indexOf("/src/");
        if (idx != -1) {
          paths.add(change.getRepoPath().substring(0, idx));
        }
      }
    }

    // No, search for trunk and branches
    else {
      boolean all_contain_branches_or_trunk = true;
      for (ConstructChange change : _changes)
        all_contain_branches_or_trunk =
            all_contain_branches_or_trunk
                && change.getRepoPath() != null
                && (change.getRepoPath().indexOf("/trunk/") != -1
                    || change.getRepoPath().indexOf("/branches/") != -1);

      // Yes, get all the different paths (until and including /trunk/ and including the different
      // branch)
      int idx_trunk, idx_branches, idx_slash;
      if (all_contain_branches_or_trunk) {
        for (ConstructChange change : _changes) {
          idx_trunk = change.getRepoPath().indexOf("/trunk/");
          idx_branches = change.getRepoPath().indexOf("/branches/");
          if (idx_trunk != -1 && idx_branches == -1) {
            paths.add(change.getRepoPath().substring(0, idx_trunk + 7));
          } else if (idx_trunk == -1 && idx_branches != -1) {
            idx_slash = change.getRepoPath().indexOf("/", idx_branches + 10);
            if (idx_slash != -1) {
              paths.add(change.getRepoPath().substring(0, idx_slash));
            }
          }
        }
      }

      // No
      else {
        paths.add("");
      }
    }

    return paths;
  }

  /**
   * Returns a new set with all changes that match to the given path.
   *
   * @param _changes a {@link java.util.Set} object.
   * @param _path a {@link java.lang.String} object.
   * @return a {@link java.util.Set} object.
   */
  @JsonIgnore
  public static Set<ConstructChange> filter(Set<ConstructChange> _changes, String _path) {
    final Set<ConstructChange> changes = new HashSet<ConstructChange>();
    for (ConstructChange change : _changes) {
      if (_path.equals("")
          || change.getRepoPath() == null
          || change.getRepoPath().startsWith(_path)) {
        changes.add(change);
      }
    }
    return changes;
  }
}
