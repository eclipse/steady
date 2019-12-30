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
package com.sap.psr.vulas.patcheval.representation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.patcheval2.BugLibManager;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.LibraryId;

/**
 * Class used for the results obtained for a construct,path.
 */
public class ConstructPathAssessment2 {

    String construct;
    String path;
    
    ConstructType constructType;
    
    String type;
    
	Boolean qnameInJAR;
   // Boolean sources;
    String ast;
    String vulnAst;
    String fixedAst;
    

	Integer dToV, dToF;
	
	Integer doneComparisons;
	
	List<LibraryId> libsSameBytecode;
	

/**
 * <p>Constructor for ConstructPathAssessment2.</p>
 *
 * @param _construct a {@link java.lang.String} object.
 * @param _path a {@link java.lang.String} object.
 * @param _ctype a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
 * @param _q a {@link java.lang.Boolean} object.
 * @param _ast a {@link java.lang.String} object.
 * @param _dToV a int.
 * @param _dToF a int.
 * @param _t a {@link java.lang.String} object.
 * @param _vast a {@link java.lang.String} object.
 * @param _fast a {@link java.lang.String} object.
 */
public ConstructPathAssessment2(String _construct, String _path, ConstructType _ctype, Boolean _q, String _ast, int _dToV, int _dToF, String _t,String _vast,String _fast) {
        
        
        this.ast = _ast;
        this.vulnAst = _vast;
        this.fixedAst = _fast;
        this.construct = _construct;
        this.qnameInJAR = _q;
        this.path = _path;
        this.constructType = _ctype;
     //   this.sources = _sources;
        this.dToF = _dToF;
        this.dToV = _dToV;
        this.type = _t;
    }
   
   /**
    * <p>Constructor for ConstructPathAssessment2.</p>
    *
    * @param _construct a {@link java.lang.String} object.
    * @param _path a {@link java.lang.String} object.
    * @param _q a {@link java.lang.Boolean} object.
    * @param _t a {@link java.lang.String} object.
    */
   public ConstructPathAssessment2(String _construct, String _path, Boolean _q,String _t) {
       
       
       this.ast = null;
       this.vulnAst = null;
       this.fixedAst = null;
       this.construct = _construct;
       this.qnameInJAR = _q;
       this.path = _path;
    //   this.sources = _sources;
       this.dToF = null;
       this.dToV = null;
       this.type = _t;
   }

    /**
     * <p>Getter for the field <code>construct</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConstruct() {
        return construct;
    }

    /**
     * <p>Setter for the field <code>construct</code>.</p>
     *
     * @param construct a {@link java.lang.String} object.
     */
    public void setConstruct(String construct) {
        this.construct = construct;
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
     * <p>Getter for the field <code>constructType</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
     */
    public ConstructType getConstructType() {
        return constructType;
    }

    /**
     * <p>Setter for the field <code>constructType</code>.</p>
     *
     * @param ct a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
     */
    public void setConstructType(ConstructType ct) {
        this.constructType = ct;
    }
    
    /**
     * <p>getQnameInJar.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getQnameInJar() {
        return qnameInJAR;
    }

    /**
     * <p>Getter for the field <code>vulnAst</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVulnAst() {
		return vulnAst;
	}

	/**
	 * <p>Setter for the field <code>vulnAst</code>.</p>
	 *
	 * @param vulnAst a {@link java.lang.String} object.
	 */
	public void setVulnAst(String vulnAst) {
		this.vulnAst = vulnAst;
	}

	/**
	 * <p>Getter for the field <code>fixedAst</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFixedAst() {
		return fixedAst;
	}

	/**
	 * <p>Setter for the field <code>fixedAst</code>.</p>
	 *
	 * @param fixedAst a {@link java.lang.String} object.
	 */
	public void setFixedAst(String fixedAst) {
		this.fixedAst = fixedAst;
	}

	/**
	 * <p>setQnameInJar.</p>
	 *
	 * @param q a {@link java.lang.Boolean} object.
	 */
	public void setQnameInJar(Boolean q) {
        this.qnameInJAR = q;
    }
    

    /**
     * <p>Getter for the field <code>dToV</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getdToV() {
    	return dToV;
    }

    /**
     * <p>Setter for the field <code>dToV</code>.</p>
     *
     * @param dToV a int.
     */
    public void setdToV(int dToV) {
        this.dToV = dToV;
    }

    /**
     * <p>Getter for the field <code>dToF</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getdToF() {
   		return dToF;
    }

    /**
     * <p>Setter for the field <code>dToF</code>.</p>
     *
     * @param dToF a int.
     */
    public void setdToF(int dToF) {
        this.dToF = dToF;
    }

    /**
     * <p>Getter for the field <code>ast</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAst() {
		return ast;
	}

	/**
	 * <p>Setter for the field <code>ast</code>.</p>
	 *
	 * @param ast a {@link java.lang.String} object.
	 */
	public void setAst(String ast) {
		this.ast = ast;
	}

	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getType() {
		return type;
	}

	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * <p>Getter for the field <code>libsSameBytecode</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<LibraryId> getLibsSameBytecode() {
		return libsSameBytecode;
	}

	/**
	 * <p>setLibsSameBytecodeAsString.</p>
	 *
	 * @param libsSameBytecode an array of {@link java.lang.String} objects.
	 */
	public void setLibsSameBytecodeAsString(String[] libsSameBytecode) {
		if (this.libsSameBytecode == null)
			this.libsSameBytecode= new ArrayList<LibraryId>();
		for(int i=0;i<libsSameBytecode.length;i++){
			String[] l = libsSameBytecode[i].split(":");
			this.libsSameBytecode.add(new LibraryId(l[0],l[1],l[2]));
		}
	}
	
	/**
	 * <p>Setter for the field <code>libsSameBytecode</code>.</p>
	 *
	 * @param libsSameBytecode a {@link java.util.List} object.
	 */
	public void setLibsSameBytecode(List<LibraryId> libsSameBytecode) {
		this.libsSameBytecode=libsSameBytecode;
	}

	
	/**
	 * <p>Getter for the field <code>doneComparisons</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getDoneComparisons() {
		return doneComparisons;
	}

	/**
	 * <p>Setter for the field <code>doneComparisons</code>.</p>
	 *
	 * @param doneComparisons a {@link java.lang.Integer} object.
	 */
	public void setDoneComparisons(Integer doneComparisons) {
		this.doneComparisons = doneComparisons;
	}
}
