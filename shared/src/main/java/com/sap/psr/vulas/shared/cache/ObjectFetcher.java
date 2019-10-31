package com.sap.psr.vulas.shared.cache;

/**
 * Used to fetch objects from a remote backing store.
 *
 * @param <S>
 * @param <T>
 */
public interface ObjectFetcher<S, T> {

  /**
   * This method is called by {@link Cache#get(Object)} in case of a cache miss for the given key.
   * The method must be implemented in order to fetch the object with the given key from the
   * (remote) backing store.
   *
   * @param _key a S object.
   * @throws com.sap.psr.vulas.shared.cache.CacheException
   * @return a T object.
   */
  public T fetch(S _key) throws CacheException;
}
