package com.opensymphony.oscache.core;

import java.util.Properties;

/**
 * Controls the behaviour for evicting cache entries.
 */
public interface EvictionAlgorithm {
	/**
	 * Configures the eviction policy using the supplied properties.
	 * 
	 * @param params
	 *            the configuration parameters for the eviction policy.
	 */
	void init(Properties params);

	/**
	 * Called whenever an object is placed in the cache. The implementation of
	 * this method may want to use this information to update its eviction data.
	 * 
	 * @param entry
	 *            the entry of the object that was put in the cache.
	 * @return entry evicted due to the put action
	 */
	Object evaluatePut(Object key);

	/**
	 * Called whenever an object was retrieved from the cache. The
	 * implementation of this method may want to use this information to update
	 * its eviction data.
	 * 
	 * @param key
	 *            the key of the object that was retrieved from the cache.
	 * @param value
	 *            the value of the object that was retrieved from the cache.
	 */
	void evaluateGet(Object key);

	/**
	 * Called whenever an object is removed from the cache. The implementation
	 * of this method may want to use this information to update its eviction
	 * data.
	 * 
	 * @param key
	 *            the key of the object that was removed from the cache.
	 * @param value
	 *            the value of the object that was removed from the cache.
	 */
	void evaluateRemove(Object key);

	/**
	 * Causes the eviction policy to evict a cache entry if required.
	 * 
	 * @return the cache entry that was evicted, or <code>null</code> if no
	 *         eviction took place.
	 */
	Object evict();

	void setCapacity(int max_entries);

	int getCapacity();
}
