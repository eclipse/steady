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
package com.sap.psr.vulas.shared.enums;

/**
 * Configures the export of scan results by the export API of the Vulas backend.
 */
public enum ExportConfiguration {

	OFF, // No export
	AGGREGATED, // Scan results of all apps are aggregated before export
	DETAILED; // Scan results of all apps are exported as is

	/**
	 * <p>parse.</p>
	 *
	 * @param _value a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public static ExportConfiguration parse(String _value) throws IllegalArgumentException {
		if(_value==null || _value.equals(""))
			throw new IllegalArgumentException("Cannot parse export configuration: No value specified");
		for (ExportConfiguration t : ExportConfiguration.values())
			if (t.name().equalsIgnoreCase(_value))
				return t;
		throw new IllegalArgumentException("Cannot parse export configuration: Invalid value [" + _value + "]");	
	}
}
