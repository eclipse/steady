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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>ConstructChangeInDependency class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructChangeInDependency implements Serializable {

	private ConstructChange cc;
	
	//"inarchive": false,
	private Trace trace;
	private Boolean traced;
	/*"reachabilityGraph": null or  {
"sourceDescription": "APP",
"shortestpathlength": "5",
"shortestpathEPcid": "1B06F030C13DA3348473B006824D973F336BB7152BC783C564E415FEE61C8459",
"id": "16832",
"shortestpathEP": {
"lang": "JAVA",
"type": "METH",
"qname": "com.acme.ArchivePrinter.compressArchive()"
}
}*/
	private Boolean reachable;
	/*"versionCheck": {
	"fixed_version": false,
	
	"class_in_archive": false,
	"overall_change_type": "ADD"
	}*/
	
	//TODO
	private Boolean inArchive;
	//fake
	private String reachabilityGraph;
	
	
	private Boolean affected;
	
	private Boolean classInArchive;
	
	private Boolean equalChangeType;
	

	private com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType  overall_change;
	
	/**
	 * <p>Constructor for ConstructChangeInDependency.</p>
	 */
	public ConstructChangeInDependency(){super();}
	
	/**
	 * <p>Constructor for ConstructChangeInDependency.</p>
	 *
	 * @param cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
	 */
	public ConstructChangeInDependency(ConstructChange cc){
		super();
		this.cc = cc;
		this.reachabilityGraph=null;
	}
	
	/**
	 * <p>getConstructChange.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
	 */
	public ConstructChange getConstructChange() { return cc; }
	/**
	 * <p>setConstructChange.</p>
	 *
	 * @param _cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
	 */
	public void setConstructChange(ConstructChange _cc) { this.cc = _cc; }
	
	/**
	 * <p>Getter for the field <code>trace</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Trace} object.
	 */
	public Trace getTrace() { return trace; }
	/**
	 * <p>Setter for the field <code>trace</code>.</p>
	 *
	 * @param trace a {@link com.sap.psr.vulas.backend.model.Trace} object.
	 */
	public void setTrace(Trace trace) { this.trace = trace; }
	
	/**
	 * <p>Getter for the field <code>traced</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getTraced() { return traced; }
	/**
	 * <p>Setter for the field <code>traced</code>.</p>
	 *
	 * @param traced a {@link java.lang.Boolean} object.
	 */
	public void setTraced(Boolean traced) { this.traced = traced; }
	
	/**
	 * <p>Getter for the field <code>affected</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getAffected() { return affected; }
	/**
	 * <p>Setter for the field <code>affected</code>.</p>
	 *
	 * @param a a {@link java.lang.Boolean} object.
	 */
	public void setAffected(Boolean a) { this.affected = a; }
	
	/**
	 * <p>Getter for the field <code>inArchive</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getInArchive() { return inArchive; }
	/**
	 * <p>Setter for the field <code>inArchive</code>.</p>
	 *
	 * @param inArchive a {@link java.lang.Boolean} object.
	 */
	public void setInArchive(Boolean inArchive) { this.inArchive = inArchive; }
	
	/**
	 * <p>Getter for the field <code>classInArchive</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getClassInArchive() { return classInArchive; }
	/**
	 * <p>Setter for the field <code>classInArchive</code>.</p>
	 *
	 * @param classinArchive a {@link java.lang.Boolean} object.
	 */
	public void setClassInArchive(Boolean classinArchive) { this.classInArchive = classinArchive; }
	
	/**
	 * <p>Getter for the field <code>equalChangeType</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getEqualChangeType() { return equalChangeType; }
	/**
	 * <p>Setter for the field <code>equalChangeType</code>.</p>
	 *
	 * @param e a {@link java.lang.Boolean} object.
	 */
	public void setEqualChangeType(Boolean e) { this.equalChangeType = e; }
	
	/**
	 * <p>Getter for the field <code>overall_change</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
	 */
	public com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType  getOverall_change() { return overall_change; }
	/**
	 * <p>Setter for the field <code>overall_change</code>.</p>
	 *
	 * @param changeType a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
	 */
	public void setOverall_change(com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType changeType) { this.overall_change = changeType; }
	
	/**
	 * <p>Getter for the field <code>reachable</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getReachable() { return reachable; }
	/**
	 * <p>Setter for the field <code>reachable</code>.</p>
	 *
	 * @param reachable a {@link java.lang.Boolean} object.
	 */
	public void setReachable(Boolean reachable) { this.reachable = reachable; }

}
