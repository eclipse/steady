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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.StringUtil;


/**
 * Exemptions are used to prevent that {@link VulnerableDependency}s result in build exceptions during the execution of the report goal.
 * 
 * Exemptions can be created for bug identifiers, libraries (by specifying their digests) and dependency scopes using the following
 * format: vulas.report.exempt.&lt;bugId&gt;.&lt;digest&gt;.&lt;scope&gt; = &lt;reason&gt;
 * 
 * The elements bugId, digest and scope can also be a star (*) to indicate that all bugs, libraries or scopes are exempted.
 */
public class Exemption {
	
	private static final Log log = LogFactory.getLog(Exemption.class);
	
	private static final String ALL = "*";

	/** Deprecated configuration prefix. **/
	public static final String CFG_PREFIX_EXEMPTED_BUGS = "vulas.report.exceptionExcludeBugs";
	
	/** Deprecated configuration prefix. **/
	public static final String CFG_PREFIX_EXEMPTED_SCOPES = "vulas.report.exceptionScopeBlacklist";
	
	/** New configuration prefix. **/
	private static final String CFG_PREFIX = "vulas.report.exempt";
	
	/** The identifier of a bug, or star (*), which means that the exemption applies to all bugs. */ 
	private String bugId = null;

	/** The digest of a library or star (*), which means that the exemption applies to all libraries. */
	private String digest = null;

	/** The scope of a dependency or star (*), which means that the exemption applies to all scopes. */
	private String scope = null;
	
	private String reason = null;

	/**
	 * Creates a new exemption, whereby parameters equal to null will be interpreted as star (*).
	 * 
	 * @param _bug_id
	 * @param _digest
	 * @param _reason
	 */
	public Exemption(String _bug_id, String _digest, String _scope, String _reason) {
		this.bugId  = (_bug_id==null ? ALL : _bug_id);
		this.digest = (_digest==null ? ALL : _digest);
		this.scope  = ( _scope==null ? ALL : _scope);
		this.reason = _reason;
	}

	public String getBugId() { return bugId; }

	public String getDigest() { return digest; }	

	public String getScope() { return scope; }

	public String getReason() { return reason; }

	/**
	 * Returns true if the given {@link VulnerableDependency} is exempted, false otherwise.
	 * 
	 * @param _vd
	 * @return
	 */
	public boolean isExempted(VulnerableDependency _vd) {
		final boolean is_exempted =
				(ALL.equals(this.bugId)  || this.bugId.equalsIgnoreCase(_vd.getBug().getBugId()))  &&
				(ALL.equals(this.digest) || this.digest.equals(_vd.getDep().getLib().getDigest())) &&
				(ALL.equals(this.scope)  || this.scope.equalsIgnoreCase(_vd.getDep().getScope().toString())); 
		return is_exempted;
	}

	/**
	 * Loops over the given exemptions to find one that exempts the given {@link VulnerableDependency}.
	 * If such an exemption is found, it is returned. Otherwise, the method return null.
	 * 
	 * @param _s
	 * @param _vd
	 * @return
	 */
	public static Exemption isExempted(Set<Exemption> _s, VulnerableDependency _vd) {
		if(_s!=null) {
			for(Exemption e: _s) {
				if(e.isExempted(_vd))
					return e;
			}
		}
		return null;
	}

	/**
	 * Reads all {@link Configuration} settings starting with 'vulas.report.exceptionExcludeBugs' in order to create {@link Exemption}s.
	 * 
	 * @param _cfg
	 * @return
	 */
	public static Set<Exemption> readFromConfiguration(Configuration _cfg) {
		final Set<Exemption> s = new HashSet<Exemption>();
		
		// New format
		Iterator<String> iter = _cfg.subset(CFG_PREFIX).getKeys();
		while(iter.hasNext()) {
			final String k = iter.next();
			if(!k.equals("")) {
				final String[] key_elements = k.split("\\.");
				final int l = key_elements.length; 
				if(l<3) {
					log.error("Exemption with key [" + CFG_PREFIX + "." + k + "] has less than 3 elements");
				}
				else {
					s.add(new Exemption(StringUtil.join(Arrays.copyOfRange(key_elements, 0, l-2), "."), key_elements[l-2], key_elements[l-1], _cfg.getString(CFG_PREFIX + "." + k)));
				}
			}
		}
		
		// Deprecated format #1
		final String[] bugs = _cfg.getStringArray(CFG_PREFIX_EXEMPTED_BUGS);
		if(bugs!=null && bugs.length>0) {
			log.warn("Exemption with key [" + CFG_PREFIX_EXEMPTED_BUGS + "] is deprecated, switch to new format");
			for(String b: bugs) {
				final String reason = _cfg.getString(CFG_PREFIX_EXEMPTED_BUGS + "." + b, null);
				s.add(new Exemption(b, null, null, (reason==null ? "No reason provided" : reason)));
			}
		}
		
		// Deprecated format #2
		final String[] scopes = _cfg.getStringArray(CFG_PREFIX_EXEMPTED_SCOPES);
		if(scopes!=null && scopes.length>0) {
			log.warn("Exemption with key [" + CFG_PREFIX_EXEMPTED_SCOPES + "] is deprecated, switch to new format");
			for(String sc: scopes) {
				s.add(new Exemption(null, null, sc, "Scope [" + sc + "] is exempted"));
			}
		}
		
		return s;
	}

	@Override
	public String toString() {
		return this.bugId + "." + this.digest + "." + this.scope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
		Exemption other = (Exemption) obj;
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
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}
}
