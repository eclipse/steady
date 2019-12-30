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

/**
 * Type of change of a given {@link ConstructId} as part of a bug fix.
 * ADD means that a certain construct, e.g., Java method, has been added, MOD that
 * it's body has been modified, and DEL that it has been deleted.
 *
 * An identical copy exists in vulas-core, package com.sap.psr.vulas.
 */
public enum ConstructChangeType {
	ADD((byte)10), MOD((byte)20), DEL((byte)30);
	private byte value;
	private ConstructChangeType(byte _value) { this.value = _value; }
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		     if(this.value==10) return "ADD";
		else if(this.value==20) return "MOD";
		else if(this.value==30) return "DEL";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid contruct change type");
	}
}
