/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import java.util.Date;
import java.util.Map;

import com.opensymphony.oscache.events.CacheListener;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public interface Cache extends Map {

	/**
	 * Allows the capacity of the cache to be altered dynamically. Note that
	 * some cache implementations may choose to ignore this setting (eg the
	 * {@link UnlimitedCache} ignores this call).
	 * 
	 * @param capacity
	 *            the maximum number of items to hold in the cache.
	 */
	public abstract void setCapacity(int capacity);

	/**
	 * Retrieves an object from the cache.
	 * 
	 * @param key
	 *            the key of the object to retrieve.
	 * @return the cached object, or <code>null</code> if the object could not
	 *         be found and could not be loaded.
	 */
	public abstract Object get(Object key, int refreshPeriod, String cronExpiry);

	/**
	 * Retrieves an object from the cache.
	 * 
	 * @param key
	 *            the key of the object to retrieve.
	 * @return the cached object, or <code>null</code> if the object could not
	 *         be found and could not be loaded.
	 */
	public abstract Object get(Object key, int refreshPeriod);

	/**
	 * Retrieve an object from the cache specifying its key.
	 * 
	 * @param key
	 *            Key of the object in the cache.
	 * 
	 * @return The object from cache
	 * 
	 */
	public abstract CacheEntry getEntry(Object key);
	
	/**
	 * Store the supplied entry in the cache.
	 * 
	 * @param key
	 *            the key to store the entry under.
	 * @param value
	 *            the object to store.
	 * @return the previous object that was stored under this key, if any.
	 */
	public abstract Object put(Object key, Object value, String[] groups,
			EntryRefreshPolicy policy);

	/**
	 * Store the supplied entry in the cache.
	 * 
	 * @param key
	 *            the key to store the entry under.
	 * @param value
	 *            the object to store.
	 * @return the previous object that was stored under this key, if any.
	 */
	public abstract Object put(Object key, Object value,
			EntryRefreshPolicy policy);

	/**
	 * Store the supplied entry in the cache.
	 * 
	 * @param key
	 *            the key to store the entry under.
	 * @param value
	 *            the object to store.
	 * @return the previous object that was stored under this key, if any.
	 */
	public abstract Object put(Object key, Object value, String[] groups);

	

	/**
	 * Flushes all unexpired objects that belong to the supplied group. On
	 * completion this method fires a <tt>CacheEntryEvent.GROUP_FLUSHED</tt>
	 * event.
	 * 
	 * @param group
	 *            The group to flush
	 */
	public void flushGroup(String group) ;
	
	/**
	 * Flush all entries in the cache on the given date/time.
	 * 
	 * @param date
	 *            The date at which all cache entries will be flushed.
	 */
	public abstract void flushAll(Date date);

	/**
	 * Completely clears the cache.
	 */
	public abstract void clear();

	/**
	 * Adds a listener that will receive notifications when cache events occur.
	 * Listeners will be notified of cache events in the same order as they have
	 * been added to the cache.
	 * 
	 * @param listener
	 *            the listener to receive the events.
	 */
	void addCacheListener(CacheListener listener);

	/**
	 * Removes a listener from the cache.
	 * 
	 * @param listener
	 *            the listener to remove.
	 * @return <code>true</code> if the listener was removed successfully,
	 *         <code>false</code> if the listener could not be found.
	 */
	boolean removeCacheListener(CacheListener listener);
	
	public void setEvictionAlgorithm(EvictionAlgorithm algorithm);

	public abstract int getCapacity();

}
