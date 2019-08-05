package com.sap.psr.vulas.cg;

/**
 * <p>CallgraphConstructException class.</p>
 *
 */
public class CallgraphConstructException extends Exception {
	/**
	 * <p>Constructor for CallgraphConstructException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 */
	public CallgraphConstructException (String _msg) {
		super(_msg);
	}
	/**
	 * <p>Constructor for CallgraphConstructException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public CallgraphConstructException (String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
