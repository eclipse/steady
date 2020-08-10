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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.rest.ApplicationController;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.DependencyOrigin;
import com.sap.psr.vulas.shared.enums.Scope;

/**
 * <p>Dependency class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
        ignoreUnknown = true,
        value = {"traced", "reachableConstructIds", "touchPoints"},
        allowGetters = true)
@Entity
// Note that the unique constraint at DB level (when using PostgreSQL) does not ensure the
// uniqueness of lib,app when parent
// and relativePath are null as null values are considered different by PostgreSQL. Their uniqueness
// must be ensured at Java level
@Table(
        name = "AppDependency",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"lib", "app", "parent", "relativePath"}))
public class Dependency implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "app", referencedColumnName = "id")
    @JsonBackReference // Required in order to omit the app property when de-serializing JSON
    private Application app;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "lib", referencedColumnName = "digest")
    private Library lib;

    @ManyToOne(
            optional = true,
            fetch = FetchType.EAGER) // , cascade = { CascadeType.REMOVE }) //we do not need to
    // cascade operations as parents are always stored in the main
    // list of app's dependencies and thus to cascade PERSIST would
    // throw exceptions for saving multiple times the same managed
    // object, similar for the DELETE
    @JoinColumn(name = "parent", referencedColumnName = "id")
    private Dependency parent;

    @Column
    @Enumerated(EnumType.STRING)
    private DependencyOrigin origin;

    @Column private Boolean declared;

    @Column private Boolean traced;

    @Column
    @Enumerated(EnumType.STRING)
    private Scope scope;

    @Column private Boolean transitive;

    @Column private String filename;

    @Column(columnDefinition = "text")
    private String path;

    @Column(columnDefinition = "text")
    private String relativePath;

    @ManyToMany(
            cascade = {},
            fetch = FetchType.LAZY)
    @JsonView(Views.DepDetails.class)
    private Set<ConstructId> reachableConstructIds;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "AppDependencyTouchPoints", joinColumns = @JoinColumn(name = "id"))
    @JsonView(Views.DepDetails.class)
    private Set<TouchPoint> touchPoints;

    /**
     * Only set when single dependencies are returned by {@link ApplicationController#getDependency(String, String, String, String)}.
     * TODO: Maybe check if they can always bet set (depending on performance and memory).
     */
    @Transient private Collection<Trace> traces;

    /**
     * Contains collections of reachable dependency constructs per {@link ConstructType}.
     * It MUST be a subset of what can be obtained from the library via {@link Library#countConstructTypes()}.
     */
    @Transient private ConstructIdFilter reachableFilter = null;

    /**
     * Contains collections of traced dependency constructs per {@link ConstructType}.
     * It MUST be a subset of what can be obtained from the library via {@link Library#countConstructTypes()}.
     * Depending on the quality of the reachability analysis, it SHOULD be a subset of what can be obtained
     * via {@link Dependency#countReachableConstructTypes()}.
     */
    @Transient private ConstructIdFilter tracedFilter = null;

    @Transient
    @JsonProperty(value = "tracedExecConstructsCounter")
    @JsonView(Views.Default.class)
    private Integer tracedExecConstructsCounter;

    @Transient
    @JsonView(Views.Default.class)
    private Integer reachExecConstructsCounter;

    /**
     * <p>Constructor for Dependency.</p>
     */
    public Dependency() {
        super();
    }

    /**
     * <p>Constructor for Dependency.</p>
     *
     * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
     * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
     * @param scope a {@link com.sap.psr.vulas.shared.enums.Scope} object.
     * @param transitive a {@link java.lang.Boolean} object.
     * @param filename a {@link java.lang.String} object.
     */
    public Dependency(
            Application app, Library lib, Scope scope, Boolean transitive, String filename) {
        super();
        this.app = app;
        this.lib = lib;
        this.scope = scope;
        this.transitive = transitive;
        this.filename = filename;
        this.declared = (scope != null && transitive != null);
        this.traced = false;
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
     * <p>Getter for the field <code>app</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.Application} object.
     */
    public Application getApp() {
        return app;
    }
    /**
     * <p>Setter for the field <code>app</code>.</p>
     *
     * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
     */
    public void setApp(Application app) {
        this.app = app;
    }

    /**
     * <p>Getter for the field <code>lib</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
     */
    public Library getLib() {
        return lib;
    }

    /**
     * <p>setAppRecursively.</p>
     *
     * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
     */
    public void setAppRecursively(Application app) {
        this.app = app;
        if (this.parent != null) this.parent.setAppRecursively(app);
    }

    /**
     * <p>Setter for the field <code>lib</code>.</p>
     *
     * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
     */
    public void setLib(Library lib) {
        this.lib = lib;
    }

    /**
     * <p>Getter for the field <code>parent</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
     */
    public Dependency getParent() {
        return parent;
    }
    /**
     * <p>Setter for the field <code>parent</code>.</p>
     *
     * @param parent a {@link com.sap.psr.vulas.backend.model.Dependency} object.
     */
    public void setParent(Dependency parent) {
        this.parent = parent;
    }

    /**
     * <p>Getter for the field <code>origin</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.enums.DependencyOrigin} object.
     */
    public DependencyOrigin getOrigin() {
        return origin;
    }
    /**
     * <p>Setter for the field <code>origin</code>.</p>
     *
     * @param origin a {@link com.sap.psr.vulas.shared.enums.DependencyOrigin} object.
     */
    public void setOrigin(DependencyOrigin origin) {
        this.origin = origin;
    }

    /**
     * <p>Getter for the field <code>scope</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.enums.Scope} object.
     */
    public Scope getScope() {
        return scope;
    }
    /**
     * <p>Setter for the field <code>scope</code>.</p>
     *
     * @param scope a {@link com.sap.psr.vulas.shared.enums.Scope} object.
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * <p>Getter for the field <code>transitive</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getTransitive() {
        return transitive;
    }
    /**
     * <p>Setter for the field <code>transitive</code>.</p>
     *
     * @param transitive a {@link java.lang.Boolean} object.
     */
    public void setTransitive(Boolean transitive) {
        this.transitive = transitive;
    }

    /**
     * <p>Getter for the field <code>filename</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilename() {
        return filename;
    }
    /**
     * <p>Setter for the field <code>filename</code>.</p>
     *
     * @param filename a {@link java.lang.String} object.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * <p>Getter for the field <code>declared</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getDeclared() {
        return declared;
    }
    /**
     * <p>Setter for the field <code>declared</code>.</p>
     *
     * @param declared a {@link java.lang.Boolean} object.
     */
    public void setDeclared(Boolean declared) {
        this.declared = declared;
    }

    /**
     * <p>Getter for the field <code>path</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPath() {
        return path;
    }
    /**
     * <p>Setter for the field <code>path</code>.</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * <p>Getter for the field <code>relativePath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRelativePath() {
        return relativePath;
    }
    /**
     * <p>Setter for the field <code>relativePath</code>.</p>
     *
     * @param relativePath a {@link java.lang.String} object.
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    /**
     * Returns true if {@link Dependency#traced} is not null and equal to true, false otherwise.
     *
     * @return a boolean.
     */
    public boolean isTraced() {
        return this.traced != null && this.traced;
    }

    /**
     * Returns the value of the member {@link Dependency#traced}, which can be null.
     *
     * @return a {@link java.lang.Boolean} object.
     */
    // TODO to check whether to add flags "calls_count" and "reachableArchive" included in old
    // backend
    public Boolean getTraced() {
        return traced;
    }

    /**
     * Sets the value of the member {@link Dependency#traced}, which can be null.
     *
     * @param traced a {@link java.lang.Boolean} object.
     */
    public void setTraced(Boolean traced) {
        this.traced = traced;
    }

    /**
     * <p>Getter for the field <code>reachableConstructIds</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<ConstructId> getReachableConstructIds() {
        return reachableConstructIds;
    }

    /**
     * <p>Setter for the field <code>reachableConstructIds</code>.</p>
     *
     * @param reachableConstructIds a {@link java.util.Set} object.
     */
    public void setReachableConstructIds(Set<ConstructId> reachableConstructIds) {
        this.reachableConstructIds = reachableConstructIds;
    }

    /**
     * <p>addReachableConstructIds.</p>
     *
     * @param reachableConstructIds a {@link java.util.Set} object.
     */
    public void addReachableConstructIds(Set<ConstructId> reachableConstructIds) {
        if (this.getReachableConstructIds() == null)
            this.setReachableConstructIds(reachableConstructIds);
        else this.getReachableConstructIds().addAll(reachableConstructIds);
    }

    /**
     * <p>countReachableConstructTypes.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.ConstructIdFilter} object.
     */
    @JsonProperty(value = "reachableConstructTypeCounters")
    @JsonView(Views.DepDetails.class)
    public ConstructIdFilter countReachableConstructTypes() {
        if (this.reachableFilter == null)
            this.reachableFilter = new ConstructIdFilter(this.getReachableConstructIds());
        return this.reachableFilter;
    }

    /**
     * <p>Getter for the field <code>traces</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @JsonIgnore
    public Collection<Trace> getTraces() {
        return traces;
    }

    /**
     * <p>Setter for the field <code>traces</code>.</p>
     *
     * @param traces a {@link java.util.Collection} object.
     */
    @JsonIgnore
    public void setTraces(Collection<Trace> traces) {
        this.traces = traces;
    }

    /**
     * <p>countTracedConstructTypes.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.ConstructIdFilter} object.
     */
    @JsonProperty(value = "tracedConstructTypeCounters")
    @JsonView(Views.CountDetails.class)
    public ConstructIdFilter countTracedConstructTypes() {
        if (this.tracedFilter == null && this.getTraces() != null) {
            final Set<ConstructId> cids = new HashSet<ConstructId>();
            for (Trace t : this.getTraces()) cids.add(t.getConstructId());
            this.tracedFilter = new ConstructIdFilter(cids);
        }
        return this.tracedFilter;
    }

    /**
     * <p>getTracedConstructs.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @JsonIgnore
    public Set<ConstructId> getTracedConstructs() {
        if (this.getTraces() == null) return null;
        final Set<ConstructId> traced_cids = new HashSet<ConstructId>();
        for (Trace t : this.getTraces()) traced_cids.add(t.getConstructId());
        return traced_cids;
    }

    /**
     * <p>Getter for the field <code>touchPoints</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<TouchPoint> getTouchPoints() {
        return touchPoints;
    }
    /**
     * <p>Setter for the field <code>touchPoints</code>.</p>
     *
     * @param touchPoints a {@link java.util.Set} object.
     */
    public void setTouchPoints(Set<TouchPoint> touchPoints) {
        this.touchPoints = touchPoints;
    }
    /**
     * <p>addTouchPoints.</p>
     *
     * @param touchPoints a {@link java.util.Set} object.
     */
    public void addTouchPoints(Set<TouchPoint> touchPoints) {
        if (this.getTouchPoints() == null) this.setTouchPoints(touchPoints);
        else this.getTouchPoints().addAll(touchPoints);
    }

    /**
     * <p>prePersist.</p>
     */
    @PrePersist
    public void prePersist() {
        if (this.getTraced() == null) {
            this.setTraced(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((app == null) ? 0 : app.hashCode());
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((lib == null) ? 0 : lib.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((transitive == null) ? 0 : transitive.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Dependency other = (Dependency) obj;
        if (app == null) {
            if (other.app != null) return false;
        } else if (!app.equals(other.app)) return false;
        if (origin == null) {
            if (other.origin != null) return false;
        } else if (!origin.equals(other.origin)) return false;
        if (filename == null) {
            if (other.filename != null) return false;
        } else if (!filename.equals(other.filename)) return false;
        if (path == null) {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        if (lib == null) {
            if (other.lib != null) return false;
        } else if (!lib.equals(other.lib)) return false;
        if (parent == null) {
            if (other.parent != null) return false;
        } else if (!parent.equals(other.parent)) return false;
        if (relativePath == null) {
            if (other.relativePath != null) return false;
        } else if (!relativePath.equals(other.relativePath)) return false;
        if (scope == null) {
            if (other.scope != null) return false;
        } else if (!scope.equals(other.scope)) return false;
        if (transitive == null) {
            if (other.transitive != null) return false;
        } else if (!transitive.equals(other.transitive)) return false;
        return true;
    }

    /**
     * <p>equalLibParentRelPath.</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public boolean equalLibParentRelPath(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Dependency other = (Dependency) obj;

        if (lib == null) {
            if (other.lib != null) return false;
        } else if (!lib.equals(other.lib)) return false;
        if (parent == null) {
            if (other.parent != null) return false;
        } else if (!parent.equalLibParentRelPath(other.parent)) return false;
        if (relativePath == null) {
            if (other.relativePath != null) return false;
        } else if (!relativePath.equals(other.relativePath)) return false;

        return true;
    }

    /**
     * <p>setTotalTracedExecConstructCount.</p>
     *
     * @param countTracesOfConstructorsLibrary a {@link java.lang.Integer} object.
     */
    public void setTotalTracedExecConstructCount(Integer countTracesOfConstructorsLibrary) {
        this.tracedExecConstructsCounter = countTracesOfConstructorsLibrary;
    }

    /**
     * <p>setTotalReachExecConstructCount.</p>
     *
     * @param countReachableExecConstructLibrary a {@link java.lang.Integer} object.
     */
    public void setTotalReachExecConstructCount(Integer countReachableExecConstructLibrary) {
        this.reachExecConstructsCounter = countReachableExecConstructLibrary;
    }

    @Override
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append("[app=")
                .append(this.app.toString())
                .append(", lib=")
                .append(this.lib.toString())
                .append(", filename=")
                .append(this.filename)
                .append(", scope=")
                .append(this.scope)
                .append(", trans=")
                .append(this.getTransitive())
                .append("]");
        return b.toString();
    }
}
