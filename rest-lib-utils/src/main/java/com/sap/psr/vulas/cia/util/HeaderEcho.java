package com.sap.psr.vulas.cia.util;

import org.springframework.http.HttpHeaders;

/**
 * Used to echo the Http request header X-Vulas-Echo to clients.
 * This header allows Http clients to maintain the context in which a certain Ajax call has been done.
 */
public class HeaderEcho {
	
	/** Constant <code>ECHO_HEADER="X-Vulas-Echo"</code> */
	public static final String ECHO_HEADER = "X-Vulas-Echo";

	/**
	 * If the given echo value is not null and not an empty {@link String}, the method returns
	 * {@link HttpHeaders} with the identical value for the header field X-Vulas-Echo. Otherwise,
	 * the {@link HttpHeaders} are empty.
	 *
	 * @param _echo_value_in_request a {@link java.lang.String} object.
	 * @return a {@link org.springframework.http.HttpHeaders} object.
	 */
	public static HttpHeaders getHeaders(String _echo_value_in_request) {
		final HttpHeaders headers = new HttpHeaders();
		if(_echo_value_in_request!=null && !_echo_value_in_request.equals("")) {
			headers.set("Access-Control-Expose-Headers", ECHO_HEADER); // https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Expose-Headers
			headers.set(ECHO_HEADER, _echo_value_in_request);
		}
		return headers;
	}
}
