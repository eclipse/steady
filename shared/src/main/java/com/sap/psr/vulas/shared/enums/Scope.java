package com.sap.psr.vulas.shared.enums;

import java.util.HashSet;
import java.util.Set;

/**
 * Use in com.sap.psr.vulas.backend.model.Dependency and com.sap.psr.vulas.shared.json.model.Dependency.
 */
public enum Scope {
	COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT;
	
	public static final Set<Scope> fromStringArray(String[] _values) {
		final Set<Scope> scopes = new HashSet<Scope>();
		if(_values!=null) {
			for(String v: _values) {
				try {
					scopes.add(Scope.valueOf(v.toUpperCase()));
				} catch (Exception e) {}
			}
		}
		return scopes;
	}
}