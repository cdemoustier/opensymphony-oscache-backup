/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.base.algorithm.UnlimitedCache;
import com.opensymphony.oscache.base.events.CacheEventListener;
import com.opensymphony.oscache.base.persistence.PersistenceListener;

import java.util.Date;

import javax.swing.event.EventListenerList;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public interface Cache {
    /**
     * An event that origininated from within another event.
     */
    public static final String NESTED_EVENT = "NESTED";

    /**
     * Allows the capacity of the cache to be altered dynamically. Note that
     * some cache implementations may choose to ignore this setting (eg the
     * {@link UnlimitedCache} ignores this call).
     *
     * @param capacity the maximum number of items to hold in the cache.
     */
    public abstract void setCapacity(int capacity);

    /**
     * Checks if the cache was flushed more recently than the CacheEntry provided.
     * Used to determine whether to refresh the particular CacheEntry.
     *
     * @param cacheEntry The cache entry which we're seeing whether to refresh
     * @return Whether or not the cache has been flushed more recently than this cache entry was updated.
     */
    public abstract boolean isFlushed(CacheEntry cacheEntry);

    /**
     * Retrieve an object from the cache specifying its key.
     *
     * @param key             Key of the object in the cache.
     *
     * @return The object from cache
     *
     */
    public abstract Object get(Object key) ;

    /**
     * Retrieve an object from the cache specifying its key.
     *
     * @param key             Key of the object in the cache.
     * @param refreshPeriod   How long before the object needs refresh. To
     * allow the object to stay in the cache indefinitely, supply a value
     * of {@link CacheEntry#INDEFINITE_EXPIRY}.
     *
     * @return The object from cache
     *
     */
    public abstract Object get(Object key, int refreshPeriod);

    /**
     * Retrieve an object from the cache specifying its key.
     *
     * @param key             Key of the object in the cache.
     * @param refreshPeriod   How long before the object needs refresh. To
     * allow the object to stay in the cache indefinitely, supply a value
     * of {@link CacheEntry#INDEFINITE_EXPIRY}.
     * @param cronExpiry      A cron expression that specifies fixed date(s)
     *                        and/or time(s) that this cache entry should
     *                        expire on.
     *
     * @return The object from cache
     *
     */
    public abstract Object get(Object key, int refreshPeriod, String cronExpiry);

    /**
     * Set the listener to use for data persistence. Only one
     * <code>PersistenceListener</code> can be configured per cache.
     *
     * @param listener The implementation of a persistance listener
     */
    public abstract void setPersistenceListener(PersistenceListener listener);

    /**
     * Retrieves the currently configured <code>PersistenceListener</code>.
     *
     * @return the cache's <code>PersistenceListener</code>, or <code>null</code>
     * if no listener is configured.
     */
    public abstract PersistenceListener getPersistenceListener();

    /**
     * Register a listener for Cache events. The listener must implement
     * one of the child interfaces of the {@link CacheEventListener} interface.
     *
     * @param listener  The object that listens to events.
     */
    public abstract void addCacheEventListener(CacheEventListener listener, Class clazz);

    /**
     * Cancels any pending update for this cache entry. This should <em>only</em>
     * be called by the thread that is responsible for performing the update ie
     * the thread that received the original {@link NeedsRefreshException}.<p/>
     * If a cache entry is not updated (via {@link #putInCache} and this method is
     * not called to let OSCache know the update will not be forthcoming, subsequent
     * requests for this cache entry will either block indefinitely (if this is a new
     * cache entry or cache.blocking=true), or forever get served stale content. Note
     * however that there is no harm in cancelling an update on a key that either
     * does not exist or is not currently being updated.
     *
     * @param key The key for the cache entry in question.
     */
    public abstract void cancelUpdate(String key);

    /**
     * Flush all entries in the cache on the given date/time.
     *
     * @param date The date at which all cache entries will be flushed.
     */
    public abstract void flushAll(Date date);

    /**
     * Flush all entries in the cache on the given date/time.
     *
     * @param date The date at which all cache entries will be flushed.
     * @param origin The origin of this flush request (optional)
     */
    public abstract void flushAll(Date date, String origin);

    /**
     * Flush the cache entry (if any) that corresponds to the cache key supplied.
     * This call will flush the entry from the cache and remove the references to
     * it from any cache groups that it is a member of. On completion of the flush,
     * a <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
     *
     * @param key The key of the entry to flush
     */
    public abstract void flushEntry(String key);

    /**
     * Flush the cache entry (if any) that corresponds to the cache key supplied.
     * This call will mark the cache entry as flushed so that the next access
     * to it will cause a {@link NeedsRefreshException}. On completion of the
     * flush, a <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
     *
     * @param key The key of the entry to flush
     * @param origin The origin of this flush request (optional)
     */
    public abstract void flushEntry(String key, String origin);

   
    /**
     * Put an object in the cache specifying the key to use.
     *
     * @param key       Key of the object in the cache.
     * @param content   The object to cache.
     */
    public abstract void put(Object key, Object content);

    /**
     * Put an object in the cache specifying the key and refresh policy to use.
     *
     * @param key       Key of the object in the cache.
     * @param content   The object to cache.
     * @param policy   Object that implements refresh policy logic
     */
    public abstract void put(Object key, Object content, EntryRefreshPolicy policy);

    /**
     * Put an object into the cache specifying both the key to use and the
     * cache groups the object belongs to.
     *
     * @param key       Key of the object in the cache
     * @param content   The object to cache
     * @param policy    Object that implements the refresh policy logic
     */
    public abstract void put(Object key, Object content, EntryRefreshPolicy policy, String origin);

    /**
     * Unregister a listener for Cache events.
     *
     * @param listener  The object that currently listens to events.
     */
    public abstract void removeCacheEventListener(CacheEventListener listener, Class clazz);

    /**
    * Completely clears the cache.
    */
    public abstract void clear();

    /**
     * Completely removes a cache entry from the cache and its associated cache
     * groups.
     *
     * @param key The key of the entry to remove.
     */
    public abstract void removeEntry(String key);

    public abstract EventListenerList getListenerList();
}
