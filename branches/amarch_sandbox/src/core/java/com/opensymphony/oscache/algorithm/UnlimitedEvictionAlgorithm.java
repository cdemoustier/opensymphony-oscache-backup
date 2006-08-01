package com.opensymphony.oscache.algorithm;

import java.util.Properties;

import com.opensymphony.oscache.core.EvictionAlgorithm;

/**
 * An {@link EvictionAlgorithm} that never evicts cache entries - the cache
 * grows without bound.
 */
public class UnlimitedEvictionAlgorithm implements EvictionAlgorithm {

	/**
	 * Initialises the unlimited eviction policy. This policy takes no
	 * parameters, there is nothing to configure.
	 */
	public void init(Properties params) {
	}

	/**
	 * Called whenever an object is put in the cache. This implementation does
	 * nothing.
	 */
	public Object evaluatePut(Object key) {
		return null;
	}

	/**
	 * Called whenever an object is retrieved from the cache. This
	 * implementation does nothing.
	 */
	public void evaluateGet(Object key) {
	}

	/**
	 * Called whenever an object is removed from the cache. This implementation
	 * does nothing.
	 */
	public void evaluateRemove(Object key) {
	}

	/**
	 * Because this is an unlimited cache, no cache entries ever get evicted by
	 * this implementation.
	 * 
	 * @return always returns <code>null</code>.
	 */
	public Object evict() {
		return null;
	}

	public void setCapacity(int max_entries) {
		// TODO Auto-generated method stub
		
	}
	
	
	public int getCapacity() {
		return -1;
	}
}
