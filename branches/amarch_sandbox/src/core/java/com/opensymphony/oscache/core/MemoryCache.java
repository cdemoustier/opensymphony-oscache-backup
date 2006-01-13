/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
	 * Date of last complete cache flush.
	 */
	private Date flushDateTime = null;

	/**
	 * The actual cache map. This is where the cached objects are held.
	 */
	private Map store = new HashMap();
	
	private int capacity; 

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
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.core.CacheAPI#flushAll(java.util.Date)
	 */
	public void flushAll(Date date) {
		flushAll(date, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.core.CacheAPI#flushAll(java.util.Date,
	 *      java.lang.String)
	 */
	public void flushAll(Date date, String origin) {
		flushDateTime = date;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.core.CacheAPI#putInCache(java.lang.String,
	 *      java.lang.Object)
	 */
	public CacheEntry put(Object key, CacheEntry cacheEntry) {
		return put(key, cacheEntry, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.core.CacheAPI#putInCache(java.lang.String,
	 *      java.lang.Object, com.opensymphony.oscache.core.EntryRefreshPolicy)
	 */
	public CacheEntry put(Object key, CacheEntry cacheEntry, EntryRefreshPolicy policy) {
		return put(key, cacheEntry, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.core.CacheAPI#putInCache(java.lang.String,
	 *      java.lang.Object, java.lang.String[],
	 *      com.opensymphony.oscache.core.EntryRefreshPolicy, java.lang.String)
	 */
	public CacheEntry put(Object key, CacheEntry cacheEntry, EntryRefreshPolicy policy,
			String origin) {
		
			return (CacheEntry) store.put(key, cacheEntry);
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
	 * @param capacity The capacity to set.
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public synchronized int size() {
		// TODO Auto-generated method stub
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

	/**
	 * This method does nothing since there is nothing left to configure.
	 */
	protected void init(Properties props) {
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
