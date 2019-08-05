package com.sap.psr.vulas.java;

/**
 * <p>JarAnalysisException class.</p>
 *
 */
public class JarAnalysisException extends Exception {
	private static final long serialVersionUID = 1L;
	/**
	 * <p>Constructor for JarAnalysisException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 */
	public JarAnalysisException(String _msg) {
		super(_msg);
	}
	/**
	 * <p>Constructor for JarAnalysisException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public JarAnalysisException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
