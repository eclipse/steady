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
package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;

/**
 * <p>JavaClassInit class.</p>
 *
 */
public class JavaClassInit extends JavaId {

	/** Constant <code>NAME="&lt;clinit&gt;"</code> */
	public static final String NAME = "<clinit>";
	private JavaClassId classContext = null;
	
	/**
	 * <p>Constructor for JavaClassInit.</p>
	 *
	 * @param _c a {@link com.sap.psr.vulas.java.JavaClassId} object.
	 */
	protected JavaClassInit(JavaClassId _c) {
		super(JavaId.Type.CLASSINIT);
		this.classContext = _c;
	}

	/**
	 * <p>getJavaClassContext.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaClassId} object.
	 */
	public JavaClassId getJavaClassContext() { return this.classContext; }
	
	/**
	 * <p>getQualifiedName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQualifiedName() { return classContext.getQualifiedName() + "." + JavaClassInit.NAME; }

	/**
	 * Returns &lt;clinit&gt;.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() { return JavaClassInit.NAME; }
	
	/**
	 * Returns &lt;clinit&gt;.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSimpleName() { return JavaClassInit.NAME; }
	
	/**
	 * <p>getDefinitionContext.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public ConstructId getDefinitionContext() { return this.classContext; }
	
	/**
	 * <p>getJavaPackageId.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaPackageId} object.
	 */
	public JavaPackageId getJavaPackageId() { return this.classContext.getJavaPackageId(); }
}
