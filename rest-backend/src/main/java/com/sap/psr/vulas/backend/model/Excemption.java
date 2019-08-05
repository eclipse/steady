package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Holds information regarding the exception of a {@link VulnerableDependency}.
 * This information is computed by comparing several goal configuration settings with the properties of the {@link VulnerableDependency}.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Excemption {

	private Boolean excludedScope = null;
	
	private Boolean excludedBug = null;
	
	private String excludedBugReason = null;
	
	/**
	 * <p>isExcludedScope.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean isExcludedScope() {
		return excludedScope;
	}

	/**
	 * <p>Setter for the field <code>excludedScope</code>.</p>
	 *
	 * @param excludedScope a {@link java.lang.Boolean} object.
	 */
	public void setExcludedScope(Boolean excludedScope) {
		this.excludedScope = excludedScope;
	}

	/**
	 * <p>isExcludedBug.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean isExcludedBug() {
		return excludedBug;
	}

	/**
	 * <p>Setter for the field <code>excludedBug</code>.</p>
	 *
	 * @param excludedBug a {@link java.lang.Boolean} object.
	 */
	public void setExcludedBug(Boolean excludedBug) {
		this.excludedBug = excludedBug;
	}

	/**
	 * <p>Getter for the field <code>excludedBugReason</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getExcludedBugReason() {
		return excludedBugReason;
	}

	/**
	 * <p>Setter for the field <code>excludedBugReason</code>.</p>
	 *
	 * @param excludedBugReason a {@link java.lang.String} object.
	 */
	public void setExcludedBugReason(String excludedBugReason) {
		this.excludedBugReason = excludedBugReason;
	}
}
