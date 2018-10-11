package com.sap.psr.vulas.shared.cache;

public class CacheException extends Exception {
		
	public CacheException(Object _key) {
		super("Cache problem with key [" + _key.toString() + "]");
	}
	
	public CacheException(Object _key, Throwable _cause) {
		super("Cache problem with key [" + _key.toString() + "]", _cause);
	}
}
