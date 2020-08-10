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
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>AffectedConstructChange class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AffectedConstructChange implements Serializable {

    @JsonIgnore private Long id;

    private ConstructChange cc;

    private String testedBody;

    private String vulnBody;

    private String fixedBody;

    private AffectedLibrary affectedLib;

    private Boolean affected;

    private String pathGroup;

    private Boolean qnameInJar;

    private Boolean inArchive;

    private Boolean classInArchive;

    private Integer dtv;

    private Integer dtf;

    private Boolean equalChangeType;

    private Collection<LibraryId> sameBytecodeLids;

    enum ChangeType {
        ADD,
        DEL,
        MOD,
        NUL
    };

    private ChangeType overall_chg;

    /**
     * <p>Constructor for AffectedConstructChange.</p>
     */
    public AffectedConstructChange() {
        super();
    }

    /**
     * <p>Constructor for AffectedConstructChange.</p>
     *
     * @param _cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
     * @param _af a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
     * @param _affected a {@link java.lang.Boolean} object.
     * @param _inArch a {@link java.lang.Boolean} object.
     * @param _classinArch a {@link java.lang.Boolean} object.
     * @param _testedBody a {@link java.lang.String} object.
     */
    public AffectedConstructChange(
            ConstructChange _cc,
            AffectedLibrary _af,
            Boolean _affected,
            Boolean _inArch,
            Boolean _classinArch,
            String _testedBody) {
        super();
        this.cc = _cc;
        this.affectedLib = _af;
        this.affected = _affected;
        this.inArchive = _inArch;
        this.classInArchive = _classinArch;
        this.testedBody = _testedBody;
    }

    /**
     * <p>Getter for the field <code>pathGroup</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPathGroup() {
        return pathGroup;
    }

    /**
     * <p>Setter for the field <code>pathGroup</code>.</p>
     *
     * @param pathGroup a {@link java.lang.String} object.
     */
    public void setPathGroup(String pathGroup) {
        this.pathGroup = pathGroup;
    }

    /**
     * <p>Getter for the field <code>qnameInJar</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getQnameInJar() {
        return qnameInJar;
    }

    /**
     * <p>Setter for the field <code>qnameInJar</code>.</p>
     *
     * @param qnameInJar a {@link java.lang.Boolean} object.
     */
    public void setQnameInJar(Boolean qnameInJar) {
        this.qnameInJar = qnameInJar;
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
     * <p>Getter for the field <code>cc</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
     */
    public ConstructChange getCc() {
        return cc;
    }
    /**
     * <p>Setter for the field <code>cc</code>.</p>
     *
     * @param _cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
     */
    public void setCc(ConstructChange _cc) {
        this.cc = _cc;
    }

    /**
     * <p>Getter for the field <code>affectedLib</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
     */
    public AffectedLibrary getAffectedLib() {
        return affectedLib;
    }
    /**
     * <p>Setter for the field <code>affectedLib</code>.</p>
     *
     * @param _al a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
     */
    public void setAffectedLib(AffectedLibrary _al) {
        this.affectedLib = _al;
    }

    /**
     * <p>Getter for the field <code>affected</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getAffected() {
        return affected;
    }
    /**
     * <p>Setter for the field <code>affected</code>.</p>
     *
     * @param affected a {@link java.lang.Boolean} object.
     */
    public void setAffected(Boolean affected) {
        this.affected = affected;
    }

    /**
     * <p>Getter for the field <code>inArchive</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getInArchive() {
        return inArchive;
    }
    /**
     * <p>Setter for the field <code>inArchive</code>.</p>
     *
     * @param _b a {@link java.lang.Boolean} object.
     */
    public void setInArchive(Boolean _b) {
        this.inArchive = _b;
    }

    //	public String getAstEqual() { return this.astEqual; }
    //	public void setAstEqual(String _b) { this.astEqual = _b; }

    /**
     * <p>Getter for the field <code>dtv</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getDtv() {
        return this.dtv;
    }
    /**
     * <p>Setter for the field <code>dtv</code>.</p>
     *
     * @param _b a {@link java.lang.Integer} object.
     */
    public void setDtv(Integer _b) {
        this.dtv = _b;
    }

    /**
     * <p>Getter for the field <code>dtf</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getDtf() {
        return this.dtf;
    }
    /**
     * <p>Setter for the field <code>dtf</code>.</p>
     *
     * @param _b a {@link java.lang.Integer} object.
     */
    public void setDtf(Integer _b) {
        this.dtf = _b;
    }

    /**
     * <p>Getter for the field <code>classInArchive</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getClassInArchive() {
        return classInArchive;
    }
    /**
     * <p>Setter for the field <code>classInArchive</code>.</p>
     *
     * @param _b a {@link java.lang.Boolean} object.
     */
    public void setClassInArchive(Boolean _b) {
        this.classInArchive = _b;
    }

    /**
     * <p>Getter for the field <code>equalChangeType</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getEqualChangeType() {
        return equalChangeType;
    }
    /**
     * <p>Setter for the field <code>equalChangeType</code>.</p>
     *
     * @param _b a {@link java.lang.Boolean} object.
     */
    public void setEqualChangeType(Boolean _b) {
        this.equalChangeType = _b;
    }

    /**
     * <p>Getter for the field <code>overall_chg</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
     */
    public ChangeType getOverall_chg() {
        return overall_chg;
    }
    /**
     * <p>Setter for the field <code>overall_chg</code>.</p>
     *
     * @param _c a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
     */
    public void setOverall_chg(ChangeType _c) {
        this.overall_chg = _c;
    }

    /**
     * <p>Setter for the field <code>overall_chg</code>.</p>
     *
     * @param _c a {@link java.lang.String} object.
     */
    public void setOverall_chg(String _c) {
        this.overall_chg = ChangeType.valueOf(_c);
    }

    /**
     * <p>Getter for the field <code>testedBody</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTestedBody() {
        return testedBody;
    }
    /**
     * <p>Setter for the field <code>testedBody</code>.</p>
     *
     * @param testedBody a {@link java.lang.String} object.
     */
    public void setTestedBody(String testedBody) {
        this.testedBody = testedBody;
    }

    /**
     * <p>Getter for the field <code>vulnBody</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVulnBody() {
        return vulnBody;
    }

    /**
     * <p>Setter for the field <code>vulnBody</code>.</p>
     *
     * @param vulnBody a {@link java.lang.String} object.
     */
    public void setVulnBody(String vulnBody) {
        this.vulnBody = vulnBody;
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
     * <p>Getter for the field <code>sameBytecodeLids</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<LibraryId> getSameBytecodeLids() {
        return sameBytecodeLids;
    }

    /**
     * <p>Setter for the field <code>sameBytecodeLids</code>.</p>
     *
     * @param sameBytecodeLids a {@link java.util.Collection} object.
     */
    public void setSameBytecodeLids(Collection<LibraryId> sameBytecodeLids) {
        this.sameBytecodeLids = sameBytecodeLids;
    }
}
