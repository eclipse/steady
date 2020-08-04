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

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;


import com.sap.psr.vulas.shared.json.model.Application;

/**
 * Goal types for a given {@link Application} or workspace.
 */
public enum ExportFormat {

	CSV, JSON;

	/** Constant <code>log</code> */
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger();
	
	/** Constant <code>TXT_CSV="text/csv;charset=UTF-8"</code> */
	public static final String TXT_CSV   = "text/csv;charset=UTF-8";
	/** Constant <code>APP_JSON="application/json;charset=UTF-8"</code> */
	public static final String APP_JSON  = "application/json;charset=UTF-8";
	/** Constant <code>TXT_PLAIN="text/plain;charset=UTF-8"</code> */
	public static final String TXT_PLAIN = "text/plain;charset=UTF-8";

	/**
	 * <p>parseFormat.</p>
	 *
	 * @param _format a {@link java.lang.String} object.
	 * @param _default a {@link com.sap.psr.vulas.shared.enums.ExportFormat} object.
	 * @return a {@link com.sap.psr.vulas.shared.enums.ExportFormat} object.
	 */
	public static ExportFormat parseFormat(String _format, @NotNull ExportFormat _default) {
		if(_format==null || _format.equals("")) {
			log.warn("Invalid format [" + _format + "], returning the default [" + _default + "]");
			return _default;
		}
		for (ExportFormat t : ExportFormat.values())
			if (t.name().equalsIgnoreCase(_format))
				return t;
		log.warn("Invalid format [" + _format + "], returning the default [" + _default + "]");
		return _default;
	}

	/**
	 * Returns the Http content type for the given {@link ExportFormat}, either "text/csv;charset=UTF-8" or "application/json;charset=UTF-8".
	 *
	 * @param _f a {@link com.sap.psr.vulas.shared.enums.ExportFormat} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getHttpContentType(@NotNull ExportFormat _f) {
		switch(_f) {
			case CSV: return TXT_CSV;
			case JSON: return APP_JSON;
			default: return TXT_PLAIN;
		}
	}
}
