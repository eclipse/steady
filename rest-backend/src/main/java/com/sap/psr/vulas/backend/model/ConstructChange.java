package com.sap.psr.vulas.backend.model;


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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during de-serialization
@Entity
@Table( name="BugConstructChange", uniqueConstraints=@UniqueConstraint( columnNames = { "bug", "repo", "commit", "repoPath", "constructId" } ) )
public class ConstructChange implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;
	
	@Column
	private String repo;
	
	@Column
	private String commit;
	
	@Column
	private String repoPath;
	
	@ManyToOne(optional = false, cascade = { }, fetch = FetchType.EAGER )
	@JoinColumn(name="constructId") // Required for the unique constraint
	private ConstructId constructId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "bug", referencedColumnName = "bugId")
	@JsonBackReference // Required in order to omit the bug property when de-serializing JSON
	private Bug bug;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar committedAt;
	
	@Column(nullable = false, length=3)
	@Enumerated(EnumType.STRING)
	private ConstructChangeType constructChangeType;
	
	@Column(columnDefinition = "text")
	//@Lob
//	@JsonSerialize(using = AstSerializer.class)
	private String buggyBody;
	
	@Column(columnDefinition = "text")
	//@Lob
	//@JsonSerialize(using = AstSerializer.class)
	private String fixedBody;
	
	@Column(columnDefinition = "text")
	//@Lob
	private String bodyChange;
	
	public ConstructChange() { super(); }
	
	public ConstructChange(String repo, String commit, String path, ConstructId constructId, Calendar committedAt, ConstructChangeType changeType) {
		super();
		this.repo = repo;
		this.commit = commit;
		this.repoPath = path;
		this.constructId = constructId;
		this.committedAt = committedAt;
		this.constructChangeType = changeType;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public String getRepo() { return repo; }
	public void setRepo(String repo) { this.repo = repo; }
	
	public String getCommit() {	return commit; }
	public void setCommit(String commit) { this.commit = commit; }

	public String getRepoPath() { return repoPath; }
	public void setRepoPath(String path) { this.repoPath = path; }
 
	public ConstructId getConstructId() { return constructId; }
	public void setConstructId(ConstructId constructId) { this.constructId = constructId; }

	public Bug getBug() { return bug; }
	public void setBug(Bug bug) { this.bug = bug; }

	public java.util.Calendar getCommittedAt() { return committedAt; }
	public void setCommittedAt(java.util.Calendar committedAt) { this.committedAt = committedAt; }

	public ConstructChangeType getConstructChangeType() { return constructChangeType; }
	public void setConstructChangeType(ConstructChangeType changeType) { this.constructChangeType = changeType; }

	public String getBuggyBody() { return buggyBody; }
	public void setBuggyBody(String buggyBody) { this.buggyBody = buggyBody; }

	public String getFixedBody() { return fixedBody; }
	public void setFixedBody(String fixedBody) { this.fixedBody = fixedBody; }

	public String getBodyChange() { return bodyChange; }
	public void setBodyChange(String bodyChange) { this.bodyChange = bodyChange; }
	
	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[").append(this.getId()).append(":").append(this.getCommit()).append("]");
		return builder.toString();
	} 

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConstructChange other = (ConstructChange) obj;
		if (constructId == null) {
			if (other.constructId != null)
				return false;
		} else if (!constructId.equals(other.constructId))
			return false;
		if (commit == null) {
			if (other.commit != null)
				return false;
		} else if (!commit.equals(other.commit))
			return false;
		if (repoPath == null) {
			if (other.repoPath != null)
				return false;
		} else if (!repoPath.equals(other.repoPath))
			return false;
		if (repo == null) {
			if (other.repo != null)
				return false;
		} else if (!repo.equals(other.repo))
			return false;
		return true;
	}
}