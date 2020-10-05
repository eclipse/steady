/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.shared.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.util.StopWatch;

/**
 * Simple cache.
 *
 * @param <S>
 * @param <T>
 */
public class Cache<S, T> {

  private static Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private long refreshMilli = -1;
  private int maxSize = -1;
  private LinkedList<S> keys =
      new LinkedList<S>(); // Used to delete oldest entry if cache size is limited (maxSize!=-1)
  private ObjectFetcher<S, T> reader = null;
  private Map<S, CacheEntry<T>> entries = new HashMap<S, CacheEntry<T>>();

  private static int PRINT_STATS_EVERY = 1000;
  private long cacheRequestCount = 0;
  private long cacheHitCount = 0;
  private long cacheMissCount = 0;
  private long cacheDeleteCount = 0;
  private long cacheRefreshCount = 0;
  private long cacheFetchCount = 0;
  private long cacheFetchErrorCount = 0;
  private long cacheFetchDuration = 0;

  /** Constant <code>MILLI_IN_MIN=60L * 1000L</code> */
  public static final long MILLI_IN_MIN = 60L * 1000L;
  /** Constant <code>MILLI_IN_HOUR=60L * MILLI_IN_MIN</code> */
  public static final long MILLI_IN_HOUR = 60L * MILLI_IN_MIN;
  /** Constant <code>MILLI_IN_DAY=24L * MILLI_IN_HOUR</code> */
  public static final long MILLI_IN_DAY = 24L * MILLI_IN_HOUR;

  /**
   * <p>Constructor for Cache.</p>
   *
   * @param _reader a {@link org.eclipse.steady.shared.cache.ObjectFetcher} object.
   * @param _refresh_min a long.
   */
  public Cache(ObjectFetcher<S, T> _reader, long _refresh_min) {
    this(_reader, _refresh_min, -1);
  }

  /**
   * <p>Constructor for Cache.</p>
   *
   * @param _reader a {@link org.eclipse.steady.shared.cache.ObjectFetcher} object.
   * @param _refresh_min a long.
   * @param _max_size a int.
   */
  public Cache(ObjectFetcher<S, T> _reader, long _refresh_min, int _max_size) {
    if (_reader == null) throw new IllegalArgumentException("Object fetcher cannot be [null]");
    if (_refresh_min <= 0)
      throw new IllegalArgumentException("Refresh period must be greater than [0]");
    if (_max_size != -1 && _max_size < 1)
      throw new IllegalArgumentException(
          "Cache size limit must be [-1] (unbounded) or greater than [0]");

    this.reader = _reader;
    this.refreshMilli = _refresh_min * MILLI_IN_MIN;
    this.maxSize = _max_size;
  }

  /**
   * <p>get.</p>
   *
   * @param _key a S object.
   * @return a T object.
   * @throws org.eclipse.steady.shared.cache.CacheException if any.
   */
  public synchronized T get(S _key) throws CacheException {
    return this.get(_key, false);
  }

  /**
   * Reads the cache entry for the given key. In case such an object does not exist, the method
   * calls {@link ObjectFetcher#fetch(Object)} in order to read the object from the remote store.
   *
   * @param _key the key of the entry to be returned
   * @param _force_fetch if true, the entry will be fetched no matter whether it already exists in the cache
   * @return the entry for the given key
   * @throws org.eclipse.steady.shared.cache.CacheException if any.
   */
  public synchronized T get(S _key, boolean _force_fetch) throws CacheException {
    final long current_time = System.currentTimeMillis();

    // Print stats
    if (this.cacheRequestCount % PRINT_STATS_EVERY == 0) this.printStats();

    this.cacheRequestCount++;
    CacheEntry<T> e = null;

    // Check if the key is known and refresh threshold is passed
    if (this.entries.containsKey(_key)) {
      this.cacheHitCount++;
      e = this.entries.get(_key);
      if (_force_fetch || current_time - e.getCreatedAt() > this.refreshMilli) {
        this.cacheRefreshCount++;
        e = null;
      }
    } else {
      this.cacheMissCount++;
    }

    // Fetch object (if necessary)
    if (e == null) {
      this.cacheFetchCount++;
      final StopWatch sw = new StopWatch("Fetch cache entry for key [" + _key + "]").start();
      try {
        e = new CacheEntry<T>(this.reader.fetch(_key));
        sw.stop();
        cacheFetchDuration += sw.getRuntimeMillis();
        this.entries.put(_key, e);
        this.keys.add(_key);
      } catch (CacheException e1) {
        sw.stop(e1);
        cacheFetchDuration += sw.getRuntimeMillis();
        cacheFetchErrorCount++;
        throw e1;
      }

      // Delete oldest entry if max cache size reached
      if (this.maxSize != -1 && this.entries.size() > this.maxSize) {
        final S to_del = this.keys.poll();
        if (to_del != null) {
          this.cacheDeleteCount++;
          this.entries.remove(to_del);
          log.info(
              "Removed key [ "
                  + to_del.toString()
                  + "] from cache due to cache size limit of ["
                  + this.maxSize
                  + "]");
        }
      }
    }

    if (e == null) throw new CacheException(_key);

    return e.getObject();
  }

  /**
   * <p>getCacheRequest.</p>
   *
   * @return a long.
   */
  public long getCacheRequest() {
    return this.cacheRequestCount;
  }
  /**
   * <p>getCacheHit.</p>
   *
   * @return a long.
   */
  public long getCacheHit() {
    return this.cacheHitCount;
  }
  /**
   * <p>getCacheMiss.</p>
   *
   * @return a long.
   */
  public long getCacheMiss() {
    return this.cacheMissCount;
  }
  /**
   * <p>getCacheFetch.</p>
   *
   * @return a long.
   */
  public long getCacheFetch() {
    return this.cacheFetchCount;
  }
  /**
   * <p>getCacheDelete.</p>
   *
   * @return a long.
   */
  public long getCacheDelete() {
    return this.cacheDeleteCount;
  }

  private class CacheEntry<U> {

    private U obj = null;
    private long createdAt = System.currentTimeMillis();

    private CacheEntry(U _obj) {
      this.obj = _obj;
    }

    private long getCreatedAt() {
      return this.createdAt;
    }

    private U getObject() {
      return this.obj;
    }
  }

  private void printStats() {
    Cache.log.info(
        "Cache requests/hits/misses: ["
            + this.cacheRequestCount
            + "/"
            + this.cacheHitCount
            + "/"
            + this.cacheMissCount
            + "], refreshes ["
            + this.cacheRefreshCount
            + "], fetches, fetch errors and avg. fetch duration ["
            + this.cacheFetchCount
            + "/"
            + this.cacheFetchErrorCount
            + "/"
            + ((float) this.cacheFetchDuration / this.cacheFetchCount)
            + "], deletions ["
            + this.cacheDeleteCount
            + "]");
  }
}
