package com.sap.psr.vulas;

/**
 * <p>FileAnalysisException class.</p>
 *
 */
public class FileAnalysisException extends Exception {
	/**
	 * <p>Constructor for FileAnalysisException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 */
	public FileAnalysisException(String _msg) {
		super(_msg);
	}
	/**
	 * <p>Constructor for FileAnalysisException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public FileAnalysisException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
