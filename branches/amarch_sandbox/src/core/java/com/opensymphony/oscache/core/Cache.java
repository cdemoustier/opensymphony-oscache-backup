/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import java.util.Date;
import java.util.Map;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public interface Cache extends Map {
	/**
	 * An event that origininated from within another event.
	 */
	public static final String NESTED_EVENT = "NESTED";

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

}
