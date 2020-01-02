package com.sap.psr.vulas.report;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.sap.psr.vulas.shared.json.model.VulnerableDependency;


/**
 * Bug exemptions prevent that a given {@link VulnerableDependency} causes a build exception during the execution of the report goal.
 * 
 * They are created for a given bug, with or without a digest:
 * Without digest, a bug is exempted no matter which library contains its vulnerable code.
 * With digest, the bug is exempted only in regards to the library with the given digest. If the vulnerable
 * code of the bug occurs in another library, a build exception is still thrown. 
 */
public class BugExemption {

	private String bugId = null;

	private String libDigest = null;

	/**
	 * Constructor for exemptions without digest.
	 * 
	 * @param _bug_id
	 */
	public BugExemption(String _bug_id) {
		this(_bug_id, null);
	}

	/**
	 * Constructor for exemptions with digest.
	 * 
	 * @param _bug_id
	 * @param _digest
	 */
	public BugExemption(String _bug_id, String _digest) {
		this.bugId = _bug_id;
		this.libDigest = _digest;
	}

	public String getBugId() {
		return bugId;
	}

	public String getLibDigest() {
		return libDigest;
	}

	/**
	 * Returns true if the given {@link VulnerableDependency} is exempted, false otherwise.
	 * 
	 * @param _vd
	 * @return
	 */
	public boolean isExempted(VulnerableDependency _vd) {
		return _vd.getBug().getBugId().equalsIgnoreCase(this.bugId) && (this.libDigest==null || this.libDigest.equals(_vd.getDep().getLib().getDigest()));
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
		final Iterator<String> iter = _cfg.getKeys();
		while(iter.hasNext()) {
			final String k = iter.next();
			final int ind = k.lastIndexOf('.');
			if(ind==-1) {
				s.add(new BugExemption(k));
			} else {
				s.add(new BugExemption(k.substring(0, ind), k.substring(ind+1)));
			}
		}
		return s;
	}

	@Override
	public String toString() {
		return this.bugId + "." + (this.libDigest==null?"*":this.libDigest);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((libDigest == null) ? 0 : libDigest.hashCode());
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
		return true;
	}
}
