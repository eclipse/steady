package com.sap.psr.vulas.shared.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CacheTest {

  @Test
  public void testWithMaxSize() {
    final Cache<String, String> cache =
        new Cache<String, String>(
            new ObjectFetcher<String, String>() {
              public String fetch(String _src) {
                return _src + "-value";
              }
            },
            10,
            10);

    // Fill cache with 0-9
    for (int i = 0; i < 10; i++) {
      try {
        System.out.println(i + " -> " + cache.get(Integer.toString(i)));
      } catch (CacheException e) {
        e.printStackTrace();
        assertTrue(false);
      }
    }

    assertEquals(10, cache.getCacheRequest());
    assertEquals(0, cache.getCacheHit());
    assertEquals(10, cache.getCacheMiss());
    assertEquals(10, cache.getCacheFetch());
    assertEquals(0, cache.getCacheDelete());

    // Read 5-9, and fill 10-14 (thereby deleting 0-4)
    for (int i = 5; i < 15; i++) {
      try {
        System.out.println(i + " -> " + cache.get(Integer.toString(i)));
      } catch (CacheException e) {
        e.printStackTrace();
        assertTrue(false);
      }
    }

    assertEquals(20, cache.getCacheRequest());
    assertEquals(5, cache.getCacheHit());
    assertEquals(15, cache.getCacheMiss());
    assertEquals(15, cache.getCacheFetch());
    assertEquals(5, cache.getCacheDelete());
  }

  @Test
  public void testWithoutMaxSize() {
    final Cache<String, String> cache =
        new Cache<String, String>(
            new ObjectFetcher<String, String>() {
              public String fetch(String _src) {
                return _src + "-value";
              }
            },
            10);

    // Fill cache with 0-9
    for (int i = 0; i < 10; i++) {
      try {
        System.out.println(i + " -> " + cache.get(Integer.toString(i)));
      } catch (CacheException e) {
        e.printStackTrace();
        assertTrue(false);
      }
    }

    assertEquals(10, cache.getCacheRequest());
    assertEquals(0, cache.getCacheHit());
    assertEquals(10, cache.getCacheMiss());
    assertEquals(10, cache.getCacheFetch());
    assertEquals(0, cache.getCacheDelete());

    // Read 5-9, and fill 10-14 (thereby deleting 0-4)
    for (int i = 5; i < 15; i++) {
      try {
        System.out.println(i + " -> " + cache.get(Integer.toString(i)));
      } catch (CacheException e) {
        e.printStackTrace();
        assertTrue(false);
      }
    }

    assertEquals(20, cache.getCacheRequest());
    assertEquals(5, cache.getCacheHit());
    assertEquals(15, cache.getCacheMiss());
    assertEquals(15, cache.getCacheFetch());
    assertEquals(0, cache.getCacheDelete()); // Only difference to testWithMaxSize()
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalMaxSize() {
    @SuppressWarnings("unused")
    final Cache<String, String> cache =
        new Cache<String, String>(
            new ObjectFetcher<String, String>() {
              public String fetch(String _src) {
                return _src + "-value";
              }
            },
            10,
            0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalFetcher() {
    @SuppressWarnings("unused")
    final Cache<String, String> cache = new Cache<String, String>(null, 10, 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalRefresh() {
    @SuppressWarnings("unused")
    final Cache<String, String> cache =
        new Cache<String, String>(
            new ObjectFetcher<String, String>() {
              public String fetch(String _src) {
                return _src + "-value";
              }
            },
            -1,
            10);
  }
}
