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
package com.sap.psr.vulas.monitor;

import java.util.Map;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * To be implemented by every instrumentor.
 */
public interface IInstrumentor {
	
	/**
	 * Returns true of a given instrumentor accepts to instrument the given Java class, false otherwise.
	 *
	 * @param _jid a {@link com.sap.psr.vulas.java.JavaId} object.
	 * @param _behavior a {@link javassist.CtBehavior} object.
	 * @param _cv a {@link com.sap.psr.vulas.monitor.ClassVisitor} object.
	 * @return a boolean.
	 */
	public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv);

	/**
	 * Appends Java source code to the given {@link StringBuffer}. After all {@link IInstrumentor}s appended
	 * their respective code, the ensemble will be added to a {@link Construct}, which will then be compiled.
	 *
	 * @param _code a {@link java.lang.StringBuffer} object.
	 * @param _jid a {@link com.sap.psr.vulas.java.JavaId} object.
	 * @param _behavior a {@link javassist.CtBehavior} object.
	 * @param _cv a {@link com.sap.psr.vulas.monitor.ClassVisitor} object.
	 * @throws javassist.CannotCompileException if any.
	 */
	public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) throws CannotCompileException;

	/**
	 * Saves the information that has been collected by the instrumentor
	 * in the context of the given {@link AbstractGoal}.
	 *
	 * @param _exe a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
	 * @param batchSize a int.
	 */
	public void upladInformation(AbstractGoal _exe, int batchSize);

	/**
	 * Make the current thread wait until the upload of the information finished.
	 */
	public void awaitUpload();

	/**
	 * Return instrumentor-specific statistics.
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,Long> getStatistics();
}
