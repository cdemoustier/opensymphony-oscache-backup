package com.opensymphony.oscache.algorithm;

import java.util.Properties;

import com.opensymphony.oscache.core.EvictionAlgorithm;

/**
 * An {@link EvictionAlgorithm} that never evicts cache entries - the
 * cache grows without bound.
 */
public class UnlimitedEvictionAlgorithm implements EvictionAlgorithm {

  /**
   * Initialises the unlimited eviction policy. This policy takes
   * no parameters, there is nothing to configure.
   */
  public void init(Properties params) {
  }

  /**
   * Called whenever an object is put in the cache. This implementation
   * does nothing.
   */
  public void put(Object key, Object value) {
  }

  /**
   * Called whenever an object is retrieved from the cache. This
   * implementation does nothing.
   */
  public void get(Object key, Object value) {
  }

  /**
   * Called whenever an object is removed from the cache. This
   * implementation does nothing.
   */
  public void remove(Object key, Object value) {
  }

  /**
   * Because this is an unlimited cache, no cache entries ever get
   * evicted by this implementation.
   *
   * @return always returns <code>null</code>.
   */
  public Object evict() {
    return null;
  }
}
