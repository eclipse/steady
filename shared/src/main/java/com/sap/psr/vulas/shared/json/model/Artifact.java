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
package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Artifact class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact implements Serializable, Comparable<Object> {

    private static Logger log = org.apache.logging.log4j.LogManager.getLogger();

    static final Pattern VERSION_PATTERN = Pattern.compile("([\\d\\.]*)(.*)", Pattern.DOTALL);

    private LibraryId libId;

    private String classifier;

    private String packaging;

    private ProgrammingLanguage lang;

    /** Release timestamp (null if unknown). */
    private Long timestamp;

    private String repository;

    /**
     * <p>Constructor for Artifact.</p>
     */
    public Artifact() {
        super();
    }

    /**
     * <p>Constructor for Artifact.</p>
     *
     * @param _g a {@link java.lang.String} object.
     * @param _a a {@link java.lang.String} object.
     * @param _v a {@link java.lang.String} object.
     */
    public Artifact(String _g, String _a, String _v) {
        super();
        libId = new LibraryId(_g, _a, _v);
    }

    // this method does not reuse the compareTo of LibID as the timestamp is used before the version
    // for comparing artifacts
    /** {@inheritDoc} */
    @Override
    public int compareTo(Object _other) {
        if (_other instanceof Artifact) {
            final Artifact other_libid = (Artifact) _other;
            int result =
                    this.getLibId()
                            .getMvnGroup()
                            .compareToIgnoreCase(other_libid.getLibId().getMvnGroup());
            if (result == 0)
                result =
                        this.getLibId()
                                .getArtifact()
                                .compareToIgnoreCase(other_libid.getLibId().getArtifact());
            if (result == 0 && this.getTimestamp() != null && other_libid.getTimestamp() != null)
                result = this.getTimestamp().compareTo(other_libid.getTimestamp());
            if (result == 0) {
                Version v = new Version(this.getLibId().getVersion());
                result = v.compareTo(new Version(other_libid.getLibId().getVersion()));
            }
            //	result = this.getLibId().getVersion().compareToIgnoreCase(((Artifact)
            // _other).getLibId().getVersion());
            return result;
        } else {
            throw new IllegalArgumentException(
                    "Expected object of type Artifact, got [" + _other.getClass().getName() + "]");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("[").append(libId.toString());
        if (this.classifier != null && !this.classifier.equals(""))
            b.append(":").append(this.getClassifier());
        if (this.packaging != null && !this.packaging.equals("")) b.append(":").append(packaging);
        b.append(":" + this.getTimestamp()).append("]");
        return b.toString();
    }

    /**
     * <p>Getter for the field <code>libId</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     */
    public LibraryId getLibId() {
        return libId;
    }

    /**
     * <p>Setter for the field <code>libId</code>.</p>
     *
     * @param libId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     */
    public void setLibId(LibraryId libId) {
        this.libId = libId;
    }

    /**
     * <p>Getter for the field <code>timestamp</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * <p>Setter for the field <code>timestamp</code>.</p>
     *
     * @param timestamp a {@link java.lang.Long} object.
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * <p>Getter for the field <code>repository</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepository() {
        return repository;
    }

    /**
     * <p>Setter for the field <code>repository</code>.</p>
     *
     * @param repository a {@link java.lang.String} object.
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * <p>Getter for the field <code>classifier</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * <p>Setter for the field <code>classifier</code>.</p>
     *
     * @param classifier a {@link java.lang.String} object.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * <p>Getter for the field <code>packaging</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     * <p>Setter for the field <code>packaging</code>.</p>
     *
     * @param packaging a {@link java.lang.String} object.
     */
    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    /**
     * <p>getProgrammingLanguage.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
     */
    public ProgrammingLanguage getProgrammingLanguage() {
        return this.lang;
    }

    /**
     * <p>setProgrammingLanguage.</p>
     *
     * @param _lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
     */
    public void setProgrammingLanguage(ProgrammingLanguage _lang) {
        this.lang = _lang;
    }

    /**
     * <p>getAbsM2Path.</p>
     *
     * @return a {@link java.nio.file.Path} object.
     */
    @JsonIgnore
    public Path getAbsM2Path() {
        return Paths.get(
                        VulasConfiguration.getGlobal().getLocalM2Repository().toString(),
                        this.getRelM2Path().toString())
                .normalize();
    }

    /**
     * http://search.maven.org/remotecontent?filepath=com/jolira/guice/3.0.0/guice-3.0.0.pom
     * @return
     */
    @JsonIgnore
    private Path getRelM2Path() {
        return Paths.get(this.getRelM2Dir().toString(), this.getM2Filename());
    }

    /**
     * Returns the path of the directory where the artifact is stored, relative to the local M2 repository.
     * E.g, com/jolira/guice/3.0.0.
     * @return
     */
    @JsonIgnore
    private Path getRelM2Dir() {
        final StringBuilder b = new StringBuilder();
        b.append(this.getLibId().getMvnGroup().replace('.', '/')).append("/");
        b.append(this.getLibId().getArtifact()).append("/");
        b.append(this.getLibId().getVersion());
        return Paths.get(b.toString());
    }

    /**
     * <p>isCached.</p>
     *
     * @return a boolean.
     * @throws java.lang.IllegalStateException if any.
     */
    @JsonIgnore
    public boolean isCached() throws IllegalStateException {
        return this.getAbsM2Path().toFile().exists();
    }

    /**
     * <p>getContentType.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @JsonIgnore
    public String getContentType() {
        if (this.getPackaging() != null) {
            if (this.getPackaging().equals("jar") || this.getPackaging().equals("war"))
                return "application/java-archive";
            else if (this.getPackaging().equals("pom")) return "text/xml; charset=\"utf-8\"";
            else if (this.getPackaging().equals("sdist")) return "application/octet-stream";
            else if (this.getPackaging().equals("bdist_wheel")) return "binary/octet-stream";
        }
        throw new IllegalArgumentException(this + " has invalid packaging");
    }

    /**
     * Returns the artifact's filename root, e.g., guice-3.0.0
     * To be completed with one of the available postfix in this.ec
     *
     * @return a {@link java.lang.String} object.
     */
    @JsonIgnore
    public String getM2Filename() {
        final StringBuilder b = new StringBuilder();
        b.append(this.getLibId().getArtifact()).append("-").append(this.getLibId().getVersion());
        if (this.classifier != null && !this.classifier.equals(""))
            b.append("-").append(this.getClassifier());
        if (this.lang != null && this.lang == ProgrammingLanguage.PY) {
            if (this.getPackaging().equals("sdist")) {
                b.append(".sdist");
            } else if (this.getPackaging().equals("bdist_wheel")) {
                b.append(".whl");
            }
        } else {
            b.append(".").append(this.getPackaging());
        }
        return b.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((libId == null) ? 0 : libId.hashCode());
        result = prime * result + ((packaging == null) ? 0 : packaging.hashCode());
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + ((repository == null) ? 0 : repository.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Artifact other = (Artifact) obj;
        if (libId.getArtifact() == null) {
            if (other.getLibId().getArtifact() != null) return false;
        } else if (!libId.getArtifact().equals(other.getLibId().getArtifact())) return false;
        if (libId.getMvnGroup() == null) {
            if (other.getLibId().getMvnGroup() != null) return false;
        } else if (!libId.getMvnGroup().equals(other.getLibId().getMvnGroup())) return false;
        if (libId.getVersion() == null) {
            if (other.getLibId().getVersion() != null) return false;
        } else if (!libId.getVersion().equals(other.getLibId().getVersion())) return false;
        if (classifier == null) {
            if (other.getClassifier() != null) return false;
        } else if (other.getClassifier() != null && !classifier.equals(other.getClassifier()))
            return false;
        if (packaging == null) {
            if (other.getPackaging() != null) return false;
        } else if (other.getPackaging() != null && !packaging.equals(other.getPackaging()))
            return false;
        if (timestamp == null) {
            if (other.getTimestamp() != null) return false;
        } else if (other.getTimestamp() != null && !timestamp.equals(other.getTimestamp()))
            return false;
        if (lang == null) {
            if (other.getProgrammingLanguage() != null) return false;
        } else if (other.getProgrammingLanguage() != null
                && !lang.equals(other.getProgrammingLanguage())) return false;
        if (repository == null) {
            if (other.getRepository() != null) return false;
        } else if (other.getRepository() != null && !repository.equals(other.getRepository()))
            return false;
        return true;
    }

    /**
     * [OUT-OF-DATE]
     * Returns true if group, artifact, version are specified,
     * classifier is one of 'sources', 'javadoc', 'test-sources', and 'tests', and
     * packaging is one of 'pom', 'jar' and 'war'.
     * Without all this information, a download is not possible.
     *
     * @return a boolean.
     */
    public boolean isReadyForDownload() {
        return this.getLibId().getMvnGroup() != null
                && !this.getLibId().getMvnGroup().equals("")
                && this.getLibId().getArtifact() != null
                && !this.getLibId().getArtifact().equals("")
                && this.getLibId().getVersion() != null
                && !this.getLibId().getVersion().equals("")
                && (this.getClassifier() == null
                        || this.getClassifier().equals("javadoc")
                        || this.getClassifier().equals("sources")
                        || this.getClassifier().equals("test-sources")
                        || this.getClassifier().equals("tests")
                        || this.getClassifier().equals("site")
                        || this.getClassifier().equals("jar"))
                && this.getPackaging() != null
                && (this.getPackaging().equalsIgnoreCase("pom")
                        || this.getPackaging().equalsIgnoreCase("jar")
                        || this.getPackaging().equalsIgnoreCase("war")
                        || this.getPackaging().equalsIgnoreCase("xml")
                        || this.getPackaging().equalsIgnoreCase("sdist")
                        || this.getPackaging().equalsIgnoreCase("bdist_wheel"));
    }
}
