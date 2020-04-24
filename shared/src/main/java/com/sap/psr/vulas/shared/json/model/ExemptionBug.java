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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.StringUtil;


/**
 * Exemptions are used to prevent that {@link VulnerableDependency}s result in build exceptions during the execution of the report goal.
 * 
 * Exemptions can be created for bug identifiers and libraries (by specifying their digests) using the following
 * format: vulas.report.exempt.&lt;bugId&gt;.&lt;digest&gt; = &lt;reason&gt;
 * 
 * The wildcard * can be used to indicate that all bugs for a given digest are exempted (or vice-versa).
 */
public class ExemptionBug implements IExemption {
	
	private static final Log log = LogFactory.getLog(ExemptionBug.class);
	
	private static final String ALL = "*";
	
	/** Deprecated configuration prefix. **/
	public static final String DEPRECATED_CFG_PREFIX = "vulas.report.exceptionExcludeBugs";
	
	/** Deprecated configuration key in backend. **/
	public static final String DEPRECATED_KEY_BACKEND = "report.exceptionExcludeBugs";
	
	/** New configuration prefix. **/
	public static final String CFG_PREFIX = "vulas.report.exemptBug";
	
	/** The identifier of a bug, or star (*), which means that the exemption applies to all bugs. */ 
	private String bugId = null;

	/** The digest of a library or star (*), which means that the exemption applies to all libraries. */
	private String digest = null;

	private String reason = null;

	/**
	 * Creates a new exemption, whereby parameters equal to null will be interpreted as star (*).
	 * 
	 * @param _bug_id
	 * @param _digest
	 * @param _reason
	 */
	public ExemptionBug(String _bug_id, String _digest, String _reason) {
		this.bugId  = (_bug_id==null ? ALL : _bug_id);
		this.digest = (_digest==null ? ALL : _digest);
		this.reason = _reason;
	}

	public String getBugId() { return bugId; }

	public String getDigest() { return digest; }	

	@Override
	public String getReason() { return reason; }

	@Override
	public boolean isExempted(VulnerableDependency _vd) {
		// Bug ID
		boolean is_exempted = ALL.equals(this.bugId)  || this.bugId.equalsIgnoreCase(_vd.getBug().getBugId());
		
		// Archive
		if(is_exempted) {
			// All
			if(ALL.equals(this.digest)) { ; }

			// Digest
			else if(this.digest.startsWith("dig:")) {
				is_exempted = is_exempted && this.digest.substring(4).equals(_vd.getDep().getLib().getDigest());
			}
			
			//TODO: Support purl format to exempt findings: https://github.com/package-url/purl-spec
			else if(this.digest.startsWith("pkg:")) {
				log.warn("Purl format not yet supported");
				is_exempted = false;
			}
		}
				 
		return is_exempted;
	}

	/**
	 * Reads all {@link Configuration} settings starting with {@link ExemptionBug#CFG_PREFIX} in order to create {@link ExemptionBug}s.
	 * Also considers the deprecated settings {@link ExemptionBug#DEPRECATED_CFG_PREFIX} and {@link ExemptionBug#CFG_PREFIX_EXEMPTED_SCOPES} for backward compatibility. 
	 * 
	 * @param _cfg
	 * @return
	 */
	public static ExemptionSet readFromConfiguration(Configuration _cfg) {
		final ExemptionSet exempts = new ExemptionSet();
		
		// New format
		Iterator<String> iter = _cfg.subset(CFG_PREFIX).getKeys();
		while(iter.hasNext()) {
			final String k = iter.next();
			if(!k.equals("")) {
				final String[] key_elements = k.split("\\.");
				final int l = key_elements.length; 
				if(l<2) {
					log.error("Exemption with key [" + CFG_PREFIX + "." + k + "] has less than 2 elements");
				}
				else {
					exempts.add(new ExemptionBug(StringUtil.join(Arrays.copyOfRange(key_elements, 0, l-1), "."), key_elements[l-1], _cfg.getString(CFG_PREFIX + "." + k)));
				}
			}
		}
		
		// Deprecated format
		final String[] bugs = _cfg.getStringArray(DEPRECATED_CFG_PREFIX);
		if(bugs!=null && bugs.length>0) {
			log.warn("Exemption with key [" + DEPRECATED_CFG_PREFIX + "] is deprecated, switch to new format");
			for(String b: bugs) {
				final String reason = _cfg.getString(DEPRECATED_CFG_PREFIX + "." + b, null);
				exempts.add(new ExemptionBug(b, null, (reason==null ? "No reason provided" : reason)));
			}
		}
		
		return exempts;
	}
	
	/**
	 * Reads all {@link Configuration} settings starting with {@link ExemptionBug#CFG_PREFIX} in order to create {@link ExemptionBug}s.
	 * Also considers the deprecated settings {@link ExemptionBug#DEPRECATED_CFG_PREFIX} and {@link ExemptionBug#CFG_PREFIX_EXEMPTED_SCOPES} for backward compatibility. 
	 * 
	 * @param _map
	 * @return
	 */
	public static ExemptionSet readFromConfiguration(Map<String, String> _map) {
		final ExemptionSet exempts = new ExemptionSet();
		
		// New format
		for(String k: _map.keySet()) {
			if(k.startsWith((CFG_PREFIX) + ".")) {
				final String[] key_elements = k.substring(CFG_PREFIX.length() + 1).split("\\.");
				final int l = key_elements.length; 
				if(l<2) {
					log.error("Exemption with key [" + k + "] has less than 2 elements");
				}
				else {
					exempts.add(new ExemptionBug(StringUtil.join(Arrays.copyOfRange(key_elements, 0, l-1), "."), key_elements[l-1], _map.get(k)));
				}
			}
		}
		
		// Deprecated configuration format
		if(_map.containsKey(DEPRECATED_CFG_PREFIX)) {
			final String[] bugs = _map.get(DEPRECATED_CFG_PREFIX).split(",");
			if(bugs!=null && bugs.length>0) {
				log.warn("Exemption with key [" + DEPRECATED_CFG_PREFIX + "] is deprecated, switch to new format");
				for(String b: bugs) {
					b = b.trim();
					final String reason = _map.get(DEPRECATED_CFG_PREFIX + "." + b);
					exempts.add(new ExemptionBug(b, null, (reason==null ? "No reason provided" : reason)));
				}
			}
		}
		
		// Deprecated key value from backend (to support backward compatibility with results already existing in backend for apps that scanned with client versions <3.1.12)
		if(_map.containsKey(DEPRECATED_KEY_BACKEND)) {
			final String[] bugs = _map.get(DEPRECATED_KEY_BACKEND).split(",");
			if(bugs!=null && bugs.length>0) {
				for(String b: bugs) {
					b = b.trim();
					final String reason = _map.get(DEPRECATED_KEY_BACKEND + "." + b);
					exempts.add(new ExemptionBug(b, null, (reason==null ? "No reason provided" : reason)));
				}
			}
		}
	
		
		return exempts;
	}

	@Override
	public String toString() {
		return "Exemption [" + this.toShortString() + "]";
	}
	
	public String toShortString() {
		return this.bugId + "." + this.digest;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
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
		ExemptionBug other = (ExemptionBug) obj;
		if (bugId == null) {
			if (other.bugId != null)
				return false;
		} else if (!bugId.equals(other.bugId))
			return false;
		if (digest == null) {
			if (other.digest != null)
				return false;
		} else if (!digest.equals(other.digest))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		return true;
	}
}
