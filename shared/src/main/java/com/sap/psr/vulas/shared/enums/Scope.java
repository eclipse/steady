package com.sap.psr.vulas.shared.enums;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Use in com.sap.psr.vulas.backend.model.Dependency and com.sap.psr.vulas.shared.json.model.Dependency.
 */
public enum Scope {
	COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT, CUSTOM;

	private static final Log log = LogFactory.getLog(Scope.class);
	
	public static final Set<Scope> fromStringArray(String[] _values) {
		final Set<Scope> scopes = new HashSet<Scope>();
		if(_values!=null) {
			for(String v: _values) {
				try {
					scopes.add(Scope.valueOf(v.toUpperCase()));
				} catch (IllegalArgumentException iae) {
					Scope.log.warn("Undefined maven scope " + v.toUpperCase() + " was found in configuration. Changing it to COMPILE");
					scopes.add (Scope.COMPILE);
				}catch (Exception e) {}
			}
		}
		return scopes;
	}


	public static Scope fromString (String _value) {
		try {
			return Scope.valueOf(_value.toUpperCase());
		} catch (IllegalArgumentException iae) {
			Scope.log.warn("Undefined maven scope " + _value.toUpperCase() + " was found in configuration. Changing it to COMPILE");
			return Scope.COMPILE;
		}
    }
    
}