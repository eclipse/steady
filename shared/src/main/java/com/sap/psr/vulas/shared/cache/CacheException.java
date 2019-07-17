package com.sap.psr.vulas.shared.cache;

/**
 * <p>CacheException class.</p>
 *
 */
public class CacheException extends Exception {
		
	/**
	 * <p>Constructor for CacheException.</p>
	 *
	 * @param _key a {@link java.lang.Object} object.
	 */
	public CacheException(Object _key) {
		super("Cache problem with key [" + _key.toString() + "]");
	}
	
	/**
	 * <p>Constructor for CacheException.</p>
	 *
	 * @param _key a {@link java.lang.Object} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public CacheException(Object _key, Throwable _cause) {
		super("Cache problem with key [" + _key.toString() + "]", _cause);
	}
}
