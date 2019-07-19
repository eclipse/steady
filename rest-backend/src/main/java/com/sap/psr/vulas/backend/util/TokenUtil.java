package com.sap.psr.vulas.backend.util;

import java.nio.charset.StandardCharsets;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.util.DigestUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>TokenUtil class.</p>
 *
 */
public class TokenUtil {

	/**
	 * Returns a random 64-char long token (MD5 hash generated over a randum number and the current time milliseconds).
	 * Used for generating {@link Tenant} and {@link Space} tokens.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public static String generateToken() {
		long rnd = new Double(Math.abs(Math.random() * 100000000)).longValue();
		long ms  = System.currentTimeMillis();
		return DigestUtil.getDigestAsString(rnd + "-" + ms, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
	}
}
