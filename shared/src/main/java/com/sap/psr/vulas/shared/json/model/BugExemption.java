package com.sap.psr.vulas.shared.json.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;


/**
 * Bug exemptions prevent that a given {@link VulnerableDependency} causes a build exception during the execution of the report goal.
 * 
 * They are created for a given bug, with or without a digest:
 * <ul>>
 * <li>Without digest, a bug is exempted no matter which library contains its vulnerable code.
 * <li>With digest, the bug is exempted only in regards to the library with the given digest. If the vulnerable
 * code of the bug occurs in another library, a build exception is still thrown. 
 * </ul>
 */
public class BugExemption {

	private String bugId = null;

	private static final String ALL_LIBS = "*";

	private static final String CFG_PREFIX = "vulas.report.exceptionExcludeBugs";

	/** Can be star (*) or a given digest. */
	private String libDigest = null;

	private String reason = null;

	/**
	 * Constructor for exemptions without digest.
	 * 
	 * @param _bug_id
	 * @param _reason
	 */
	public BugExemption(String _bug_id, String _reason) {
		this(_bug_id, ALL_LIBS, _reason);
	}

	/**
	 * Constructor for exemptions with digest.
	 * 
	 * @param _bug_id
	 * @param _digest
	 * @param _reason
	 */
	public BugExemption(String _bug_id, String _digest, String _reason) {
		this.bugId = _bug_id;
		this.libDigest = _digest;
		this.reason = _reason;
	}

	public String getBugId() {
		return bugId;
	}

	public String getLibDigest() {
		return libDigest;
	}

	public String getReason() {
		return reason;
	}

	/**
	 * Returns true if the given {@link VulnerableDependency} is exempted, false otherwise.
	 * 
	 * @param _vd
	 * @return
	 */
	public boolean isExempted(VulnerableDependency _vd) {
		final boolean is_exempted = _vd.getBug().getBugId().equalsIgnoreCase(this.bugId) && (this.libDigest==null || this.libDigest.equals(ALL_LIBS) || this.libDigest.equals(_vd.getDep().getLib().getDigest())); 
		return is_exempted;
	}

	/**
	 * Returns true if the given {@link VulnerableDependency} is exempted by one or more of the given {@link BugExemption}s, false otherwise.
	 * 
	 * @param _s
	 * @param _vd
	 * @return
	 */
	public static boolean isExempted(Set<BugExemption> _s, VulnerableDependency _vd) {
		if(_s!=null) {
			for(BugExemption e: _s) {
				if(e.isExempted(_vd))
					return true;
			}
		}
		return false;
	}

	/**
	 * Reads all {@link Configuration} settings starting with 'vulas.report.exceptionExcludeBugs' in order to create {@link BugExemption}s.
	 * 
	 * @param _cfg
	 * @return
	 */
	public static Set<BugExemption> readFromConfiguration(Configuration _cfg) {
		final Set<BugExemption> s = new HashSet<BugExemption>();
		final Iterator<String> iter = _cfg.subset(CFG_PREFIX).getKeys();
		while(iter.hasNext()) {
			final String k = iter.next();
			final int ind = k.lastIndexOf('.');

			// Entry "vulas.report.exceptionExcludeBugs.<id> = <reason>" (kept for backward compatibility)
			if(ind==-1) {
				s.add(new BugExemption(k, _cfg.getString(CFG_PREFIX + "." + k)));
			}

			// Entry "vulas.report.exceptionExcludeBugs.<id>.<digest> = <reason>"
			else {
				s.add(new BugExemption(k.substring(0, ind), k.substring(ind+1), _cfg.getString(k)));
			}
		}
		return s;
	}

	@Override
	public String toString() {
		return this.bugId + "." + (this.libDigest==null ? ALL_LIBS : this.libDigest);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((libDigest == null) ? 0 : libDigest.hashCode());
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
		BugExemption other = (BugExemption) obj;
		if (bugId == null) {
			if (other.bugId != null)
				return false;
		} else if (!bugId.equals(other.bugId))
			return false;
		if (libDigest == null) {
			if (other.libDigest != null)
				return false;
		} else if (!libDigest.equals(other.libDigest))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		return true;
	}
}
