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
package com.sap.psr.vulas;

import com.sap.psr.vulas.core.util.CoreConfiguration;
// import com.sap.psr.vulas.java.JavaConstructorId;
// import com.sap.psr.vulas.java.JavaMethodId;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;
import com.sap.psr.vulas.sign.SignatureFactory;

/**
 * <p>ConstructChange class.</p>
 *
 */
public class ConstructChange implements Comparable<ConstructChange> {

    private Construct def = null, fix = null;
    private Signature defSignature = null, fixSignature = null;

    private SignatureChange signatureChange = null;

    private String commit = null;

    private String committedAt = null;

    private String repo = null;

    /**
     * The file in which the construct is defined (relative to the repository's root).
     */
    private String repoPath = null;

    /**
     * <p>Constructor for ConstructChange.</p>
     *
     * @param _repo a {@link java.lang.String} object.
     * @param _repo_path a {@link java.lang.String} object.
     * @param _def a {@link com.sap.psr.vulas.Construct} object.
     * @param _fix a {@link com.sap.psr.vulas.Construct} object.
     * @param _rev a {@link java.lang.String} object.
     * @param _time_stamp a {@link java.lang.String} object.
     */
    public ConstructChange(
            String _repo,
            String _repo_path,
            Construct _def,
            Construct _fix,
            String _rev,
            String _time_stamp) {
        if (_def == null && _fix == null)
            throw new IllegalArgumentException(
                    "At least one construct must be provided (defective, fixed or both)");
        this.repo = _repo;
        this.repoPath = _repo_path;
        this.def = _def;
        this.fix = _fix;
        this.commit =
                (_rev != null && _rev.contains(":"))
                        ? _rev.substring(0, _rev.indexOf(":") - 1)
                        : _rev;
        this.committedAt = _time_stamp;

        // Signatures of the defective and fixed constructs (if any, can be null) and the so-called
        // signature change
        final SignatureFactory factory =
                CoreConfiguration.getSignatureFactory(
                        com.sap.psr.vulas.ConstructId.toSharedType(
                                (_def != null ? _def.getId() : _fix.getId())));
        if (factory != null) {
            defSignature = factory.createSignature(def);
            fixSignature = factory.createSignature(fix);
            signatureChange = factory.computeChange(def, fix);
        }
    }

    /**
     * <p>Getter for the field <code>repo</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepo() {
        return this.repo;
    }

    /**
     * Returns the file in which the construct is defined.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepoPath() {
        return this.repoPath;
    }

    /**
     * <p>Getter for the field <code>committedAt</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommittedAt() {
        return committedAt;
    }

    /**
     * Returns true if a {@link SignatureChange} was computed and the list of changes is non-empty, false otherwise.
     *
     * @return a boolean.
     */
    public boolean hasSignatureChange() {
        return this.signatureChange != null && !signatureChange.isEmpty();
    }

    /**
     * <p>getType.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
     */
    public ConstructChangeType getType() {
        if (this.def == null) return ConstructChangeType.ADD;
        else if (this.fix == null) return ConstructChangeType.DEL;
        else return ConstructChangeType.MOD;
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(this.getType().toString());
        buffer.append(" ");
        buffer.append(this.getConstruct());
        buffer.append(System.getProperty("line.separator"));

        // If there is a signature for the construct
        // Note : It will not be generated for these constructs (CLASS, CLASSINIT, PACKAGE) but
        // these types are collected as a construct
        if (fixSignature != null) {
            buffer.append("F_AST :" + fixSignature.toJson() + System.getProperty("line.separator"));
            buffer.append(System.getProperty("line.separator"));
        }

        if (defSignature != null) {
            // buffer.append("D_AST :" + defSignature.toJson());
            buffer.append("D_AST :" + this.defSignature.toJson());
            buffer.append(System.getProperty("line.separator"));
        }

        if (signatureChange != null) {
            buffer.append(
                    System.getProperty("line.separator")
                            + "AST_Diff_json  : "
                            + signatureChange.toJSON());
            buffer.append(System.getProperty("line.separator"));
        }

        return buffer.toString();
    }

    /**
     * <p>toJSON.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toJSON() {
        final StringBuilder b = new StringBuilder();

        // Key information
        b.append("{");
        b.append(" \"repo\" : \"").append(this.repo).append("\",");
        if (this.repoPath != null)
            b.append(" \"repoPath\" : \"").append(this.repoPath).append("\",");
        b.append(" \"constructId\" : ").append(this.getConstruct().getId().toJSON()).append(",");
        if (this.commit != null) b.append(" \"commit\" : \"").append(this.commit).append("\",");

        // Other information
        b.append(" \"constructChangeType\":\"").append(this.getType().toString()).append("\",");
        if (this.committedAt != null)
            b.append(" \"committedAt\" : \"")
                    .append(StringUtil.formatDate(Long.parseLong(this.getCommittedAt())))
                    .append("\"");

        // Buggy method body, fixed one and applied changes
        if (this.getType() == ConstructChangeType.MOD) {
            if (this.fixSignature != null)
                // b.append(" \"description\" :
                // ").append(JsonBuilder.escape(this.bugDescription)).append(", ");
                b.append(",\"fixedBody\":")
                        .append(JsonBuilder.escape(this.fixSignature.toJson().toString()));
            // b.append(getFixConstructASTSignature().toJson().toString()).append(" ,");
            if (this.defSignature != null)
                b.append(",\"buggyBody\":")
                        .append(JsonBuilder.escape(this.defSignature.toJson().toString()));
            // b.append(getDefConstructASTSignature().toJson().toString()).append(" ,");
            if (signatureChange != null)
                // b.append(" \"signatureChange\" : ").append(astDiff.toJSON().toString()).append("
                // ,");
                // b.append("\"sigchg  \"
                // :[").append(JsonBuilder.escape(astDiff.toJSON().toString())).append("]").append("
                // ,");
                b.append(",\"bodyChange\":")
                        .append(JsonBuilder.escape(signatureChange.toJSON().toString()));
            // b.append(astDiff.toJSON().toString()).append(" ,");
        }
        b.append("}");
        return b.toString();
    }

    /**
     * Returns the construct that is subject to change.
     *
     * @return a {@link com.sap.psr.vulas.Construct} object.
     */
    public Construct getConstruct() {
        if (this.getType() == ConstructChangeType.DEL) return this.def;
        else return this.fix;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((def != null) ? def.hashCode() : fix.hashCode());
        result = prime * result + (commit.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if construct id and revision are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final ConstructChange other = (ConstructChange) obj;
        final ConstructId tc =
                (this.def != null
                        ? com.sap.psr.vulas.ConstructId.toSharedType(this.def.getId())
                        : com.sap.psr.vulas.ConstructId.toSharedType(this.fix.getId()));
        final ConstructId oc =
                (other.def != null
                        ? com.sap.psr.vulas.ConstructId.toSharedType(other.def.getId())
                        : com.sap.psr.vulas.ConstructId.toSharedType(other.fix.getId()));
        return (tc.equals(oc) && this.commit.equals(other.commit));
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(ConstructChange _cc) {
        final int id_comparison_result =
                this.getConstruct().getId().compareTo(_cc.getConstruct().getId());
        if (id_comparison_result == 0 && this.commit != null)
            return this.commit.compareTo(_cc.commit);
        else return id_comparison_result;
    }
}
