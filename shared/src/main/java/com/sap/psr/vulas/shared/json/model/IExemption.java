package com.sap.psr.vulas.shared.json.model;

import java.util.Set;

public interface IExemption {
	
	/**
	 * Returns true if the given {@link VulnerableDependency} is exempted, false otherwise.
	 * 
	 * @param the vulnerable dependency to be checked
	 * @return true if the vulnerable dependency is exemption, false otherwise
	 */
	public boolean isExempted(VulnerableDependency _vd);
	
	/**
	 * Returns the exemption reason. Only relevant if {@link #isExempted(VulnerableDependency)} returns true.
	 * 
	 * @return the reason for the exemption
	 */
	public String getReason();
}
