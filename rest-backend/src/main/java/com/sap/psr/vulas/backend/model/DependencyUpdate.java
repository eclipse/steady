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

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;

/**
 * Describes the update of a {@link Dependency} of an {@link Application} from one version of
 * a {@link Library} to another one. To that end, it contains a list of calls that require to be
 * modified because certain constructs are not available in the target {@link Library}.
 * Moreover, diverse {@link Metrics} quantify the update effort.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyUpdate {
	
	LibraryId fromLibraryId;
	
	LibraryId toLibraryId;
	
	Metrics metrics;
	
	Set<TouchPoint> callsToModify ;

	/**
	 * <p>Constructor for DependencyUpdate.</p>
	 *
	 * @param f a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 * @param t a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public DependencyUpdate(LibraryId f, LibraryId t){
		this.fromLibraryId=f;
		this.toLibraryId=t;
	}
	
	/**
	 * <p>Getter for the field <code>callsToModify</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<TouchPoint> getCallsToModify(){ return callsToModify;}
	/**
	 * <p>Setter for the field <code>callsToModify</code>.</p>
	 *
	 * @param c a {@link java.util.Set} object.
	 */
	public void setCallsToModify(Set<TouchPoint> c){this.callsToModify=c;}
	
	/**
	 * <p>Getter for the field <code>fromLibraryId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public LibraryId getFromLibraryId() { return fromLibraryId; }
	/**
	 * <p>Setter for the field <code>fromLibraryId</code>.</p>
	 *
	 * @param f a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public void setFromLibraryId(LibraryId f) { this.fromLibraryId = f; }

	/**
	 * <p>Getter for the field <code>toLibraryId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public LibraryId getToLibraryId() { return toLibraryId; }
	/**
	 * <p>Setter for the field <code>toLibraryId</code>.</p>
	 *
	 * @param t a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public void setToLibraryId(LibraryId t) { this.toLibraryId = t; }

	/**
	 * <p>Getter for the field <code>metrics</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
	 */
	public Metrics getMetrics() { return metrics; }
	/**
	 * <p>Setter for the field <code>metrics</code>.</p>
	 *
	 * @param m a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
	 */
	public void setMetrics(Metrics m) { this.metrics = m; }
}
