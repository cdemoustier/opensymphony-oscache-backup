/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.algorithm;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import com.opensymphony.oscache.core.EvictionAlgorithm;

/**
 * FIFO (First In First Out) based queue algorithm for the cache.
 * 
 */
public class FIFOEvictionAlgorithm implements EvictionAlgorithm {
	/**
	 * A configuration parameter that specifies the maximum size of the cache
	 * before elements start getting evicted.
	 */
	public static final String SIZE_PARAM = "maxSize";

	private static final int DEFAULT_SIZE = 1000;

	private int maxSize = DEFAULT_SIZE;

	private Set elements = new LinkedHashSet();

	/**
	 * Configures the LRU policy. Valid parameters are:
	 * <ul>
	 * <li>{@link #SIZE_PARAM} - the maximum number of entries to hold in the
	 * cache</li>
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
				// log.warn("The '" + SIZE_PARAM + "' parameter for the FIFO
				// eviciton policy is not a valid integer. Defaulting to " +
				// DEFAULT_SIZE);
			}
		}
	}

	/**
	 * An object was retrieved from the cache. This implementation does noting
	 * since this event has no impact on the FIFO algorithm.
	 * 
	 * @param key
	 *            The cache key of the item that was retrieved.
	 */
	public void evaluateGet(Object key) {
	}

	/**
	 * An object was put in the cache. This implementation just adds the key to
	 * the end of the list if it doesn't exist in the list already.
	 * 
	 * @param key
	 *            The cache key of the item that was put.
	 */
	public Object evaluatePut(Object key) {
		if (!elements.contains(key)) {
			elements.add(key);
		}
		return evict();
	}

	/**
	 * An item needs to be removed from the cache. The FIFO implementation
	 * removes the first element in the list (ie, the item that has been in the
	 * cache for the longest time).
	 * 
	 * @return The key of whichever item was removed.
	 */
	public Object evict() {
		Object toEvict = null;
		if (elements.size() > maxSize) {
			// Remove the first element (this is the one that was used
			// least-recently)
			Iterator it = elements.iterator();
			toEvict = it.next();
			it.remove();
		}
		return toEvict;
	}

	/**
	 * Remove specified key since that object has been removed from the cache.
	 * 
	 * @param key
	 *            The cache key of the item that was removed.
	 */
	public void evaluateRemove(Object key) {
		elements.remove(key);
	}
}
