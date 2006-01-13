package com.opensymphony.oscache.algorithm;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.oscache.core.EvictionAlgorithm;

/**
 * An {@link EvictionAlgorithm} that evicts cache entries based on
 * a least recently used (LRU) algorithm.
 */
public class LRUEvictionAlgorithm implements EvictionAlgorithm {
  
  /**
   * A configuration parameter that specifies the maximum size of the cache before
   * elements start getting evicted.
   */
  public static final String SIZE_PARAM = "maxSize";
  private static final int DEFAULT_SIZE = 1000;

  private int maxSize = DEFAULT_SIZE;

  private Set elements = new LinkedHashSet();

  /**
   * Configures the LRU policy. Valid parameters are:
   * <ul>
   * <li>{@link #SIZE_PARAM} - the maximum number of entries to hold in the cache</li>
   * </ul>
   *
   * @param params
   */
  public void init(Properties params) {
    String sizeStr = (String) params.get(SIZE_PARAM);
    if (sizeStr != null) {
      try {
        maxSize = Integer.parseInt(sizeStr);
      } catch (NumberFormatException e) {
        //log.warn("The '" + SIZE_PARAM + "' parameter for the LRU eviciton policy is not a valid integer. Defaulting to " + DEFAULT_SIZE);
      }
    }
  }

  /**
   * Called when an object is put in the cache. This causes the LRU algorithm
   * to update its internal data structure.
   */
  public void put(Object key, Object value) {
    // Move the key to the back of the set
    elements.remove(key);
    elements.add(key);
  }

  /**
   * Called when an object is retrieved from the cache. This causes the LRU
   * algorithm to update its internal data structure.
   */
  public void get(Object key, Object value) {
    // Move the key to the back of the set
    elements.remove(key);
    elements.add(key);
  }

  /**
   * Called when an object is removed from the cache. This causes the LRU
   * algorithm to update its internal data structure.
   *
   * @param key
   * @param value
   */
  public void remove(Object key, Object value) {
    elements.remove(key);
  }

  /**
   * Evict a cache entry if the cache has grown too large. The entry to be evicted
   * will be the one that was used least-recently.
   *
   * @return the object that was evicted, or <code>null</code> if the cache
   *         has not yet reached the specified maximum size.
   */
  public Object evict() {
    Object toEvict = null;
    if (elements.size() > maxSize) {
      // Remove the first element (this is the one that was used least-recently)
      Iterator it = elements.iterator();
      toEvict = it.next();
      it.remove();
    }
    return toEvict;
  }
}
