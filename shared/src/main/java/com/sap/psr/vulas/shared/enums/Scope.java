package com.sap.psr.vulas.shared.enums;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Use in com.sap.psr.vulas.backend.model.Dependency and com.sap.psr.vulas.shared.json.model.Dependency.
 */
public enum Scope {
	COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT;

	/** Constant <code>log</code> */
	private static final Log log = LogFactory.getLog(Scope.class);
	
	/**
	 * <p>fromStringArray.</p>
	 *
	 * @param _values an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.Set} object.
	 */
	public static final Set<Scope> fromStringArray(String[] _values) {
		final Set<Scope> scopes = new HashSet<Scope>();
		if(_values!=null) {
			for(String v: _values) {				
				try {
					scopes.add(Scope.valueOf(v.toUpperCase()));
				} catch(IllegalArgumentException e) {
					Scope.log.warn("Invalid scope [" + v + "] ignored, use one of [COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM, IMPORT]");
				}
			}
		}
		return scopes;
	}


	/**
	 * <p>fromString.</p>
	 *
	 * @param _value a {@link java.lang.String} object.
	 * @param _default a {@link com.sap.psr.vulas.shared.enums.Scope} object.
	 * @return a {@link com.sap.psr.vulas.shared.enums.Scope} object.
	 */
	public static Scope fromString(String _value, Scope _default) {
		try {
			return Scope.valueOf(_value.toUpperCase());
		} catch (IllegalArgumentException iae) {
			Scope.log.warn("Invalid scope [" + _value + "], returning default [" + _default + "]");
			return _default;
		}
    }    
}
