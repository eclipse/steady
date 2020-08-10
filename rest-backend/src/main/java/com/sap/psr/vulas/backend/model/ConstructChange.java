/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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

/**
 * <p>ConstructChange class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown properties during de-serialization
@Entity
@Table(
        name = "BugConstructChange",
        uniqueConstraints =
                @UniqueConstraint(
                        columnNames = {"bug", "repo", "commit", "repoPath", "constructId"}))
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

    /**
     * <p>Constructor for ConstructChange.</p>
     */
    public ConstructChange() {
        super();
    }

    /**
     * <p>Constructor for ConstructChange.</p>
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
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getId() {
        return id;
    }
    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.Long} object.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>Getter for the field <code>repo</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepo() {
        return repo;
    }
    /**
     * <p>Setter for the field <code>repo</code>.</p>
     *
     * @param repo a {@link java.lang.String} object.
     */
    public void setRepo(String repo) {
        this.repo = repo;
    }

    /**
     * <p>Getter for the field <code>commit</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommit() {
        return commit;
    }
    /**
     * <p>Setter for the field <code>commit</code>.</p>
     *
     * @param commit a {@link java.lang.String} object.
     */
    public void setCommit(String commit) {
        this.commit = commit;
    }

    /**
     * <p>Getter for the field <code>repoPath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepoPath() {
        return repoPath;
    }
    /**
     * <p>Setter for the field <code>repoPath</code>.</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public void setRepoPath(String path) {
        this.repoPath = path;
    }

    /**
     * <p>Getter for the field <code>constructId</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
     */
    public ConstructId getConstructId() {
        return constructId;
    }
    /**
     * <p>Setter for the field <code>constructId</code>.</p>
     *
     * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
     */
    public void setConstructId(ConstructId constructId) {
        this.constructId = constructId;
    }

    /**
     * <p>Getter for the field <code>bug</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.Bug} object.
     */
    public Bug getBug() {
        return bug;
    }
    /**
     * <p>Setter for the field <code>bug</code>.</p>
     *
     * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
     */
    public void setBug(Bug bug) {
        this.bug = bug;
    }

    /**
     * <p>Getter for the field <code>committedAt</code>.</p>
     *
     * @return a {@link java.util.Calendar} object.
     */
    public java.util.Calendar getCommittedAt() {
        return committedAt;
    }
    /**
     * <p>Setter for the field <code>committedAt</code>.</p>
     *
     * @param committedAt a {@link java.util.Calendar} object.
     */
    public void setCommittedAt(java.util.Calendar committedAt) {
        this.committedAt = committedAt;
    }

    /**
     * <p>Getter for the field <code>constructChangeType</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.ConstructChangeType} object.
     */
    public ConstructChangeType getConstructChangeType() {
        return constructChangeType;
    }
    /**
     * <p>Setter for the field <code>constructChangeType</code>.</p>
     *
     * @param changeType a {@link com.sap.psr.vulas.backend.model.ConstructChangeType} object.
     */
    public void setConstructChangeType(ConstructChangeType changeType) {
        this.constructChangeType = changeType;
    }

    /**
     * <p>Getter for the field <code>buggyBody</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBuggyBody() {
        return buggyBody;
    }
    /**
     * <p>Setter for the field <code>buggyBody</code>.</p>
     *
     * @param buggyBody a {@link java.lang.String} object.
     */
    public void setBuggyBody(String buggyBody) {
        this.buggyBody = buggyBody;
    }

    /**
     * <p>Getter for the field <code>fixedBody</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFixedBody() {
        return fixedBody;
    }
    /**
     * <p>Setter for the field <code>fixedBody</code>.</p>
     *
     * @param fixedBody a {@link java.lang.String} object.
     */
    public void setFixedBody(String fixedBody) {
        this.fixedBody = fixedBody;
    }

    /**
     * <p>Getter for the field <code>bodyChange</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBodyChange() {
        return bodyChange;
    }
    /**
     * <p>Setter for the field <code>bodyChange</code>.</p>
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
