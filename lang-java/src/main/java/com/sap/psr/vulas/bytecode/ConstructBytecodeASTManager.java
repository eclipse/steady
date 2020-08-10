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
package com.sap.psr.vulas.bytecode;

import java.util.HashMap;
import java.util.Set;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.LibraryId;

/**
 * <p>ConstructBytecodeASTManager class.</p>
 *
 */
public class ConstructBytecodeASTManager {

    String construct;
    String path;

    ConstructType type;

    private GoalContext goalContext = null;

    private static final String BYTECODE_NOT_FOUND = "none";

    HashMap<LibraryId, String> lidVulnBytecodeAST;
    HashMap<LibraryId, String> lidFixedBytecodeAST;

    /**
     * <p>Constructor for ConstructBytecodeASTManager.</p>
     * @param _gc TODO
     * @param _c a {@link java.lang.String} object.
     * @param _p a {@link java.lang.String} object.
     * @param _t a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
     */
    public ConstructBytecodeASTManager(GoalContext _gc, String _c, String _p, ConstructType _t) {
        this.goalContext = _gc;
        this.construct = _c;
        this.path = _p;
        this.type = _t;
        this.lidVulnBytecodeAST = new HashMap<LibraryId, String>();
        this.lidFixedBytecodeAST = new HashMap<LibraryId, String>();
    }

    /**
     * <p>addVulnLid.</p>
     *
     * @param l a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     */
    public void addVulnLid(LibraryId l) {
        this.lidVulnBytecodeAST.put(l, null);
    }

    /**
     * <p>addFixedLid.</p>
     *
     * @param l a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     */
    public void addFixedLid(LibraryId l) {
        this.lidFixedBytecodeAST.put(l, null);
    }

    /**
     * <p>getVulnLids.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<LibraryId> getVulnLids() {
        return this.lidVulnBytecodeAST.keySet();
    }

    /**
     * <p>getVulnAst.</p>
     *
     * @param l a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     * @return a {@link java.lang.String} object.
     */
    public synchronized String getVulnAst(LibraryId l) {
        if (this.lidVulnBytecodeAST.get(l) == null) {
            String ast_lid =
                    BackendConnector.getInstance()
                            .getAstForQnameInLib(
                                    this.goalContext,
                                    l.getMvnGroup()
                                            + "/"
                                            + l.getArtifact()
                                            + "/"
                                            + l.getVersion()
                                            + "/"
                                            + type.toString()
                                            + "/"
                                            + construct,
                                    false,
                                    ProgrammingLanguage.JAVA);
            // the file is found
            if (ast_lid != null) {
                this.lidVulnBytecodeAST.put(l, ast_lid);
            } else { // the file is not found and cannot be used as a comparison basis, we set it to
                     // BytecodeNotFound to avoid further requests
                this.lidVulnBytecodeAST.put(l, BYTECODE_NOT_FOUND);
            }
        }
        String res = this.lidVulnBytecodeAST.get(l);
        if (res == BYTECODE_NOT_FOUND) {
            return null;
        } else {
            return res;
        }
    }

    /**
     * <p>getFixedLids.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<LibraryId> getFixedLids() {
        return this.lidFixedBytecodeAST.keySet();
    }

    /**
     * <p>getFixedAst.</p>
     *
     * @param l a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     * @return a {@link java.lang.String} object.
     */
    public synchronized String getFixedAst(LibraryId l) {
        if (this.lidFixedBytecodeAST.get(l) == null) {
            String ast_lid =
                    BackendConnector.getInstance()
                            .getAstForQnameInLib(
                                    this.goalContext,
                                    l.getMvnGroup()
                                            + "/"
                                            + l.getArtifact()
                                            + "/"
                                            + l.getVersion()
                                            + "/"
                                            + type.toString()
                                            + "/"
                                            + construct,
                                    false,
                                    ProgrammingLanguage.JAVA);
            // the file is found
            if (ast_lid != null) {
                this.lidFixedBytecodeAST.put(l, ast_lid);
            } else { // the file is not found and cannot be used as a comparison basis, we set it to
                     // BytecodeNotFound to avoid furthur requests
                this.lidFixedBytecodeAST.put(l, BYTECODE_NOT_FOUND);
            }
        }

        String res = this.lidFixedBytecodeAST.get(l);
        if (res == BYTECODE_NOT_FOUND) {
            return null;
        } else {
            return res;
        }
    }

    /**
     * <p>getLidsSize.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public synchronized Integer getLidsSize() {
        return this.lidVulnBytecodeAST.size() + this.lidFixedBytecodeAST.size();
    }
}
