package com.sap.psr.vulas.backend.requests;

import com.sap.psr.vulas.backend.HttpResponse;

public class StatusCondition implements ResponseCondition {

	private int status;
	
	public StatusCondition(int _status) { this.status = _status; }
	
	/**
	 * Returns true if the HTTP status of the given {@link HttpResponse} equals the status of the condition, false otherwise.
	 */
	@Override
	public boolean meetsCondition(HttpResponse _response) { return (_response!=null && _response.getStatus()==this.status); }

	public String toString() { return "[HTTP RC==" + this.status + "]"; }
}