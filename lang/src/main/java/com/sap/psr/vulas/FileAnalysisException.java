package com.sap.psr.vulas;

public class FileAnalysisException extends Exception {
	public FileAnalysisException(String _msg) {
		super(_msg);
	}
	public FileAnalysisException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
