package com.sap.psr.vulas.backend.requests;

import java.io.Serializable;

import com.sap.psr.vulas.backend.HttpResponse;

/**
 * <p>ResponseCondition interface.</p>
 *
 */
public interface ResponseCondition extends Serializable {
	
	/**
	 * Returns true of the {@link HttpResponse} meets the condition, false otherwise.
	 *
	 * @param _response a {@link com.sap.psr.vulas.backend.HttpResponse} object.
	 * @return a boolean.
	 */
	public boolean meetsCondition(HttpResponse _response);
}
