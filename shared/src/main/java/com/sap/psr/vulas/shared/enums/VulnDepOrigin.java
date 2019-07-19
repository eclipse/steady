package com.sap.psr.vulas.shared.enums;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Use in com.sap.psr.vulas.backend.model.VulnerableDependency and com.sap.psr.vulas.shared.json.model.VulnerableDependency
 */
public enum VulnDepOrigin {
	CC, BUNDLEDCC, AFFLIBID, BUNDLEDAFFLIBID;

	/** Constant <code>log</code> */
	private static final Log log = LogFactory.getLog(VulnDepOrigin.class);
	
	/**
	 * <p>fromStringArray.</p>
	 *
	 * @param _values an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.Set} object.
	 */
	public static final Set<VulnDepOrigin> fromStringArray(String[] _values) {
		final Set<VulnDepOrigin> vulndeporigins = new HashSet<VulnDepOrigin>();
		if(_values!=null) {
			for(String v: _values) {				
				try {
					vulndeporigins.add(VulnDepOrigin.valueOf(v.toUpperCase()));
				} catch(IllegalArgumentException e) {
					VulnDepOrigin.log.warn("Invalid vulndeporigin [" + v + "] ignored, use one of [CC, BUNDLEDCC, AFFLIBID, BUNDLEDAFFLIBID]");
				}
			}
		}
		return vulndeporigins;
	}


	/**
	 * <p>fromString.</p>
	 *
	 * @param _value a {@link java.lang.String} object.
	 * @param _default a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
	 * @return a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
	 */
	public static VulnDepOrigin fromString(String _value, VulnDepOrigin _default) {
		try {
			return VulnDepOrigin.valueOf(_value.toUpperCase());
		} catch (IllegalArgumentException iae) {
			VulnDepOrigin.log.warn("Invalid vulndeporigin [" + _value + "], returning default [" + _default + "]");
			return _default;
		}
    }    
}
