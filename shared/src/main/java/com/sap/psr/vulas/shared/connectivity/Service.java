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
package com.sap.psr.vulas.shared.connectivity;

/**
 * RESTful service that clients can connect to.
 *
 * Example services comprise, for instance, the backend service where analysis results are stored,
 * or the cia service that provides information on Java archives, classes and single methods.
 */
public enum Service {
	BACKEND((byte)10), CIA((byte)20), CVE((byte)30), JIRA((byte)40);
	private byte value;
	private Service(byte _value) { this.value = _value; }
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		     if(this.value==10) return "backend";
		else if(this.value==20) return "cia";
		else if(this.value==30) return "cve";
		else if(this.value==40) return "jira";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid service");
	}
}
