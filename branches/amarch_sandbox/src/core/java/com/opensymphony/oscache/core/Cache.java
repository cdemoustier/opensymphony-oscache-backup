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
	 * Flush all entries in the cache on the given date/time.
	 * 
	 * @param date
	 *            The date at which all cache entries will be flushed.
	 */
	public abstract void flushAll(Date date);

	/**
	 * Flush all entries in the cache on the given date/time.
	 * 
	 * @param date
	 *            The date at which all cache entries will be flushed.
	 * @param origin
	 *            The origin of this flush request (optional)
	 */
	public abstract void flushAll(Date date, String origin);


	/**
	 * Completely clears the cache.
	 */
	public abstract void clear();
	
	/**
	   * Adds a listener that will receive notifications when cache events occur.
	   * Listeners will be notified of cache events in the same order as they have
	   * been added to the cache.
	   *
	   * @param listener the listener to receive the events.
	   */
	  void addCacheListener(CacheListener listener);

	  /**
	   * Removes a listener from the cache.
	   *
	   * @param listener the listener to remove.
	   * @return <code>true</code> if the listener was removed successfully,
	   *         <code>false</code> if the listener could not be found.
	   */
	  boolean removeCacheListener(CacheListener listener);

}
