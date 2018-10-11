package com.sap.psr.vulas.shared.enums;

/**
 * Configures the export of scan results by the export API of the Vulas backend.
 * 
 */
public enum ExportConfiguration {

	OFF, // No export
	AGGREGATED, // Scan results of all apps are aggregated before export
	DETAILED; // Scan results of all apps are exported as is

	public static ExportConfiguration parse(String _value) throws IllegalArgumentException {
		if(_value==null || _value.equals(""))
			throw new IllegalArgumentException("Cannot parse export configuration: No value specified");
		for (ExportConfiguration t : ExportConfiguration.values())
			if (t.name().equalsIgnoreCase(_value))
				return t;
		throw new IllegalArgumentException("Cannot parse export configuration: Invalid value [" + _value + "]");	
	}
}