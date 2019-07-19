package com.sap.psr.vulas.monitor;


/**
 * <p>LoaderFilter interface.</p>
 *
 */
public interface LoaderFilter {

	/**
	 * <p>accept.</p>
	 *
	 * @param _loader a {@link com.sap.psr.vulas.monitor.Loader} object.
	 * @return a boolean.
	 */
	public boolean accept(Loader _loader);
}
