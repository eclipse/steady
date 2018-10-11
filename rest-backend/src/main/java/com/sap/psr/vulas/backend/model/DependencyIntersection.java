package com.sap.psr.vulas.backend.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructType;

/**
 * Set of {@link ConstructId}s of type {@link ConstructType#CLAS} that exist in two {@link Dependency}s of
 * an {@link Application}.
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyIntersection {

	private Dependency d1 = null;
	
	private Dependency d2 = null;
	
	private Collection<ConstructId> constructs = null;
	
	public DependencyIntersection(Dependency _d1, Dependency _d2, long _constructs) {
		
	}

	public Dependency getD1() {
		return d1;
	}

	public void setD1(Dependency d1) {
		this.d1 = d1;
	}

	public Dependency getD2() {
		return d2;
	}

	public void setD2(Dependency d2) {
		this.d2 = d2;
	}

	public Collection<ConstructId> getConstructs() {
		return constructs;
	}

	public void setConstructs(Collection<ConstructId> constructs) {
		this.constructs = constructs;
	}
}
