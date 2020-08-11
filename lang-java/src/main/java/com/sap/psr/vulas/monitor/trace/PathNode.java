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
package com.sap.psr.vulas.monitor.trace;

import com.sap.psr.vulas.ConstructId;

/**
 * <p>PathNode class.</p>
 *
 */
public class PathNode {

	private ConstructId constructId = null;
	
	private String sha1 = null;
	
	/**
	 * <p>Constructor for PathNode.</p>
	 *
	 * @param _cid a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public PathNode(ConstructId _cid) {
		this(_cid, null);
	}
	
	/**
	 * <p>Constructor for PathNode.</p>
	 *
	 * @param _cid a {@link com.sap.psr.vulas.ConstructId} object.
	 * @param _sha1 a {@link java.lang.String} object.
	 */
	public PathNode(ConstructId _cid, String _sha1) {
		this.constructId = _cid;
		this.sha1 = _sha1;
	}

	/**
	 * <p>Getter for the field <code>constructId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public ConstructId getConstructId() {
		return constructId;
	}

	/**
	 * <p>Getter for the field <code>sha1</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSha1() {
		return sha1;
	}
	
	/**
	 * <p>Setter for the field <code>sha1</code>.</p>
	 *
	 * @param _sha1 a {@link java.lang.String} object.
	 */
	public void setSha1(String _sha1) {
		this.sha1= _sha1;
	}
	
	/**
	 * <p>hasSha1.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasSha1() { return this.sha1!=null; }
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[qname=");
		b.append(this.constructId.getQualifiedName());
		if(this.sha1!=null) {
			b.append(", libsha1=").append(this.sha1);
		}
		b.append("]");
		return b.toString();
	}
}
