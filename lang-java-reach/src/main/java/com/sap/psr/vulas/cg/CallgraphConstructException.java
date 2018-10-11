package com.sap.psr.vulas.cg;

public class CallgraphConstructException extends Exception {
	public CallgraphConstructException (String _msg) {
		super(_msg);
	}
	public CallgraphConstructException (String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
