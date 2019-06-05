package com.sap.psr.vulas.java;

public class JarAnalysisException extends Exception {
	private static final long serialVersionUID = 1L;
	public JarAnalysisException(String _msg) {
		super(_msg);
	}
	public JarAnalysisException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
