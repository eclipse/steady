package com.sap.psr.vulas.shared.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.StopWatch;

/**
 * Simple cache.
 *
 * @param <S>
 * @param <T>
 */
public class Cache<S, T> {
	
	private static final Log log = LogFactory.getLog(Cache.class);

	private long refreshMilli = -1;
	private int max_size = -1;
	private ObjectFetcher<S, T> reader = null;
	private Map<S,CacheEntry<T>> entries = new HashMap<S,CacheEntry<T>>();
	
	private static int PRINT_STATS_EVERY = 1000;
	private long cacheRequestCount = 0;
	private long cacheHitCount = 0;
	private long cacheMissCount = 0;
	private long cacheDeleteCount = 0;
	private long cacheRefreshCount = 0;
	private long cacheFetchCount = 0;
	private long cacheFetchErrorCount = 0;
	private long cacheFetchDuration = 0;
	
	public static final long MILLI_IN_MIN  = 60L * 1000L;
	public static final long MILLI_IN_HOUR = 60L * MILLI_IN_MIN;
	public static final long MILLI_IN_DAY  = 24L * MILLI_IN_HOUR;
	
	/**
	 * @param _reader
	 * @param _refresh_min
	 */
	public Cache(ObjectFetcher<S, T> _reader, long _refresh_min) {
		this.refreshMilli = _refresh_min * MILLI_IN_MIN;
		this.reader = _reader;
	}
	
	/**
	 * @param _reader
	 * @param _refresh_min
	 */
	public Cache(ObjectFetcher<S, T> _reader, long _refresh_min, int _max_size) {
		this.reader = _reader;
		this.refreshMilli = _refresh_min * MILLI_IN_MIN;
		this.max_size = _max_size;
	}
	
	public synchronized T get(S _key) throws CacheException {
		return this.get(_key, false);
	}
	
	/**
	 * Reads the cache entry for the given key. In case such an object does not exist, the method
	 * calls {@link CveReader#fetch(String)} in order to read the object from the remote store.
	 * @param _key
	 * @return
	 */
	public synchronized T get(S _key, boolean _force_fetch) throws CacheException {
		final long current_time = System.currentTimeMillis();
		
		if(this.entries.size()>this.max_size){
			S to_del=null;
			long ageMilli =-1;
			for(Entry<S, CacheEntry<T>> e:this.entries.entrySet()){
				if(current_time-e.getValue().getCreatedAt()>ageMilli){
					ageMilli=current_time - e.getValue().getCreatedAt();
					to_del=e.getKey();
				}
			}
			if(to_del!=null){
				log.info("Removing key [ "+ to_del.toString()  +"] from cache.");
				this.cacheDeleteCount++;
				this.entries.remove(to_del);
			}
		}
				
		// Print stats
		if(this.cacheRequestCount%PRINT_STATS_EVERY==0)
			this.printStats();
		
		this.cacheRequestCount++;
		CacheEntry<T> e = null;
		
		// Check if the key is known and refresh threshold is passed
		if(this.entries.containsKey(_key)) {
			this.cacheHitCount++;
			e = this.entries.get(_key);			
			if(_force_fetch || current_time - e.getCreatedAt() > this.refreshMilli) {
				this.cacheRefreshCount++;
				e = null;
			}
		} else {
			this.cacheMissCount++;
		}
		
		// (Re)read object if necessary
		if(e==null) {
			this.cacheFetchCount++;
			final StopWatch sw = new StopWatch("Fetch cache entry for key [" + _key + "]").start();
			try {
				e = new CacheEntry<T>(this.reader.fetch(_key));
				sw.stop();
				cacheFetchDuration += sw.getRuntimeMillis();
				this.entries.put(_key,  e);
			} catch (CacheException e1) {
				sw.stop(e1);
				cacheFetchDuration += sw.getRuntimeMillis();
				cacheFetchErrorCount++;
				throw e1;
			}
		}
		
		if(e==null)
			throw new CacheException(_key);
		
		return e.getObject();
	}
	
	private class CacheEntry<U> {
		
		private U obj = null;
		private long createdAt = System.currentTimeMillis();
		
		private CacheEntry(U _obj) {
			this.obj = _obj;
		}
		
		private long getCreatedAt() { return this.createdAt; }
		private U getObject() { return this.obj; }
	}
	
	private void printStats() {
		Cache.log.info("Cache requests/hits/misses: [" + this.cacheRequestCount + "/" + this.cacheHitCount + "/" + this.cacheMissCount + "], refreshes [" + this.cacheRefreshCount + "], fetches, fetch errors and avg. fetch duration [" + this.cacheFetchCount + "/" + this.cacheFetchErrorCount + "/" + ( (float)this.cacheFetchDuration / this.cacheFetchCount ) + "], deletions [" + this.cacheDeleteCount + "]");
	}
}
