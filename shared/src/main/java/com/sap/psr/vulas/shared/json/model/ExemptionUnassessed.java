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
package com.sap.psr.vulas.shared.json.model;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;


public class ExemptionUnassessed implements IExemption {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ExemptionUnassessed.class);

	/**
	 * Configuration setting <code>REP_EXCL_UNASS="vulas.report.exceptionExcludeUnassessed"</code>.
	 */
	public final static String CFG = "vulas.report.exceptionExcludeUnassessed";
	
	/** Deprecated configuration key in backend. **/
	public static final String DEPRECATED_KEY_BACKEND = "report.exceptionExcludeUnassessed";

	/**
	 * Determines whether unassessed vulnerable dependencies throw a build exception or not.
	 */
	public enum Value { ALL, KNOWN };

	private Value value = null;

	public ExemptionUnassessed(Value _value) { this.value = _value; }

	@Override
	public boolean isExempted(VulnerableDependency _vd) {
		if(this.value.equals(Value.ALL))
			return !_vd.isAffectedVersionConfirmed();
		else if(this.value.equals(Value.KNOWN))
			return !_vd.isAffectedVersionConfirmed() && _vd.getDep().getLib().isWellknownDigest();
		else
			return false;
	}

	@Override
	public String getReason() {
		if(this.value.equals(Value.ALL))
			return "All unassessed findings are exempted according to configuration setting [" + CFG + "]";
		else if(this.value.equals(Value.KNOWN))
			return "Unassessed findings in libraries known to artifact repositories such as Maven Central are exempted according to configuration setting [" + CFG + "]";
		else
			return "Illegal State, check configuration setting [" + CFG + "]";
	}

	/**
	 * Reads the {@link Configuration} setting {@link ExemptionUnassessed#CFG} (if any) in order to create one {@link ExemptionBug}.
	 * 
	 * @param _cfg
	 * @return
	 */
	public static ExemptionSet readFromConfiguration(Configuration _cfg) {
		final ExemptionSet exempts = new ExemptionSet();
		final String setting = _cfg.getString(CFG, null);
		if(setting!=null) {
			if(setting.equalsIgnoreCase(Value.ALL.toString())) {
				exempts.add(new ExemptionUnassessed(Value.ALL));
				ExemptionUnassessed.log.warn("All unassessed vulnerabilities will be ignored");
			}
			else if(setting.equalsIgnoreCase(Value.KNOWN.toString())) {
				exempts.add(new ExemptionUnassessed(Value.KNOWN));
				ExemptionUnassessed.log.warn("All unassessed vulnerabilities in archives with known digests will be ignored");
			}
		}
		return exempts;
	}
	
	/**
	 * Reads the configuration setting {@link ExemptionUnassessed#CFG} (if any) in order to create one {@link ExemptionBug}.
	 * 
	 * @param _map
	 * @return
	 */
	public static ExemptionSet readFromConfiguration(Map<String, String> _map) {
		final ExemptionSet exempts = new ExemptionSet();
		final String setting = _map.get(CFG);
		if(setting!=null && !setting.equals("")) {
			if(setting.equalsIgnoreCase(Value.ALL.toString())) {
				exempts.add(new ExemptionUnassessed(Value.ALL));
				ExemptionUnassessed.log.warn("All unassessed vulnerabilities will be ignored");
			}
			else if(setting.equalsIgnoreCase(Value.KNOWN.toString())) {
				exempts.add(new ExemptionUnassessed(Value.KNOWN));
				ExemptionUnassessed.log.warn("All unassessed vulnerabilities in archives with known digests will be ignored");
			}
		}
		else {
			final String deprecated_key_setting = _map.get(DEPRECATED_KEY_BACKEND);
			if(deprecated_key_setting!=null && !deprecated_key_setting.equals("")) {
				if(deprecated_key_setting.equalsIgnoreCase(Value.ALL.toString())) {
					exempts.add(new ExemptionUnassessed(Value.ALL));
					ExemptionUnassessed.log.warn("All unassessed vulnerabilities will be ignored");
				}
				else if(deprecated_key_setting.equalsIgnoreCase(Value.KNOWN.toString())) {
					exempts.add(new ExemptionUnassessed(Value.KNOWN));
					ExemptionUnassessed.log.warn("All unassessed vulnerabilities in archives with known digests will be ignored");
				}
			}
		}
		
		return exempts;
	}

	@Override
	public String toString() { return "Exemption [unassessed=" + value + "]"; }
	
	public String toShortString() {
		return this.value.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExemptionUnassessed other = (ExemptionUnassessed) obj;
		if (value != other.value)
			return false;
		return true;
	}
}
