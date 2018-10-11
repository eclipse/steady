package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Holds information regarding the exception of a {@link VulnerableDependency}.
 * This information is computed by comparing several goal configuration settings with the properties of the {@link VulnerableDependency}.
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Excemption {

	private Boolean excludedScope = null;
	
	private Boolean excludedBug = null;
	
	private String excludedBugReason = null;
	
	public Boolean isExcludedScope() {
		return excludedScope;
	}

	public void setExcludedScope(Boolean excludedScope) {
		this.excludedScope = excludedScope;
	}

	public Boolean isExcludedBug() {
		return excludedBug;
	}

	public void setExcludedBug(Boolean excludedBug) {
		this.excludedBug = excludedBug;
	}

	public String getExcludedBugReason() {
		return excludedBugReason;
	}

	public void setExcludedBugReason(String excludedBugReason) {
		this.excludedBugReason = excludedBugReason;
	}
}
