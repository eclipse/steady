package com.sap.psr.vulas.backend.requests;

import java.io.Serializable;

import com.sap.psr.vulas.backend.HttpResponse;

public interface ResponseCondition extends Serializable {
	
	/** Returns true of the {@link HttpResponse} meets the condition, false otherwise. */
	public boolean meetsCondition(HttpResponse _response);
}
