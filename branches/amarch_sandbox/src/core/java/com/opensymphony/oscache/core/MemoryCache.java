/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides an interface to the cache itself. Creating an instance of this class
 * will create a cache that behaves according to its construction parameters.
 * The public API provides methods to manage objects in the cache and configure
 * any cache event listeners.
 * 
 * @version $Revision$
 * @author <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:tgochenour@peregrine.com">Todd Gochenour</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a
 *         href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris
 *         Miller</a>
 */
public class MemoryCache extends BaseCache {
	private static transient final Log log = LogFactory
			.getLog(MemoryCache.class);

	/**
	 * The actual cache map. This is where the cached objects are held.
	 */
	private Map store = new HashMap();

	private int capacity = 1000;

	/**
	 * 
	 */
	public MemoryCache() {

		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public MemoryCache(int capacity) {

		this.capacity = capacity;
	}

	/**
	 * Completely clears the cache.
	 */
	public void clear() {
		store.clear();
	}

	/**
	 * @return Returns the capacity.
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity
	 *            The capacity to set.
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public synchronized int size() {
		return store.size();
	}

	public boolean containsKey(Object key) {
		return store.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return store.containsKey(value);
	}

	public void putAll(Map entries) {
		store.putAll(entries);
	}

	public Set keySet() {
		return store.keySet();
	}

	public Collection values() {
		return store.values();
	}

	public Set entrySet() {
		return store.entrySet();
	}

	public synchronized boolean isEmpty() {
		return store.isEmpty();
	}

	public void clearInternal() {
		store.clear();
	}

	protected CacheEntry getInternal(Object key) {
		return (CacheEntry) store.get(key);
	}

	protected CacheEntry putInternal(CacheEntry entry) {
		return (CacheEntry) store.put(entry.getKey(), entry);
	}

	protected CacheEntry removeInternal(Object key) {
		return (CacheEntry) store.remove(key);
	}

}
