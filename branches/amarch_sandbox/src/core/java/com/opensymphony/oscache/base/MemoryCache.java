/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheEntryEventListener;
import com.opensymphony.oscache.base.events.CacheEntryEventType;
import com.opensymphony.oscache.base.events.CacheEventListener;
import com.opensymphony.oscache.base.events.CacheGroupEvent;
import com.opensymphony.oscache.base.events.CacheMapAccessEvent;
import com.opensymphony.oscache.base.events.CacheMapAccessEventListener;
import com.opensymphony.oscache.base.events.CacheMapAccessEventType;
import com.opensymphony.oscache.base.events.CachePatternEvent;
import com.opensymphony.oscache.base.events.CachewideEvent;
import com.opensymphony.oscache.base.events.CachewideEventType;
import com.opensymphony.oscache.base.persistence.PersistenceListener;
import com.opensymphony.oscache.util.FastCronParser;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an interface to the cache itself. Creating an instance of this class
 * will create a cache that behaves according to its construction parameters.
 * The public API provides methods to manage objects in the cache and configure
 * any cache event listeners.
 *
 * @version        $Revision$
 * @author <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:tgochenour@peregrine.com">Todd Gochenour</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class MemoryCache implements Serializable, Cache {
    private static transient final Log log = LogFactory.getLog(Cache.class);

    /**
     * A list of all registered event listeners for this cache.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Date of last complete cache flush.
     */
    private Date flushDateTime = null;

    /**
     * The actual cache map. This is where the cached objects are held.
     */
    private Map cacheMap = new ConcurrentHashMap();

    private PersistenceListener persistenceListener;

    /**
     * Indicates whether the cache blocks requests until new content has
     * been generated or just serves stale content instead.
     */
    private boolean blocking = false;
    private boolean overflowPersistence;
    private boolean unlimitedDiskCache;
    private boolean useMemoryCaching;
    private int capacity;

	/**
	 * 
	 */
	public MemoryCache() {

		// TODO Auto-generated constructor stub
	}
    /**
     * Create a new Cache
     *
     * @param useMemoryCaching Specify if the memory caching is going to be used
     * @param unlimitedDiskCache Specify if the disk caching is unlimited
     * @param overflowPersistence Specify if the persistent cache is used in overflow only mode
     */
    public MemoryCache(boolean useMemoryCaching, boolean unlimitedDiskCache, boolean overflowPersistence) {
        this(useMemoryCaching, unlimitedDiskCache, overflowPersistence, false, 0);
    }

    /**
     * Create a new Cache.
     *
     * If a valid algorithm class is specified, it will be used for this cache.
     * Otherwise if a capacity is specified, it will use LRUCache.
     * If no algorithm or capacity is specified UnlimitedCache is used.
     *
     * @see com.opensymphony.oscache.base.algorithm.LRUCache
     * @see com.opensymphony.oscache.base.algorithm.UnlimitedCache
     * @param useMemoryCaching Specify if the memory caching is going to be used
     * @param unlimitedDiskCache Specify if the disk caching is unlimited
     * @param overflowPersistence Specify if the persistent cache is used in overflow only mode
     * @param blocking This parameter takes effect when a cache entry has
     * just expired and several simultaneous requests try to retrieve it. While
     * one request is rebuilding the content, the other requests will either
     * block and wait for the new content (<code>blocking == true</code>) or
     * instead receive a copy of the stale content so they don't have to wait
     * (<code>blocking == false</code>). the default is <code>false</code>,
     * which provides better performance but at the expense of slightly stale
     * data being served.
     * @param capacity The capacity
     */
    public MemoryCache(boolean useMemoryCaching, boolean unlimitedDiskCache, boolean overflowPersistence, boolean blocking, int capacity) {
        try {
            cacheMap = new ConcurrentHashMap();
        } catch (Exception e) {
            log.error("Invalid class name for cache algorithm class. " + e.toString());
        }

        this.unlimitedDiskCache = unlimitedDiskCache;
        this.overflowPersistence = overflowPersistence;
        this.useMemoryCaching = useMemoryCaching;

        this.blocking = blocking;
    }

    /**
	 * @param capacity2
	 */
	public MemoryCache(int capacity) {
		this.capacity = capacity;
	}
	
	/* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#setCapacity(int)
         */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#isFlushed(com.opensymphony.oscache.base.CacheEntry)
         */
    public boolean isFlushed(CacheEntry cacheEntry) {
        if (flushDateTime != null) {
            long lastUpdate = cacheEntry.getLastUpdate();

            return (flushDateTime.getTime() >= lastUpdate);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#getFromCache(java.lang.String)
         */
    public Object get(Object key){
        return get(key, CacheEntry.INDEFINITE_EXPIRY, null);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#getFromCache(java.lang.String, int)
         */
    public Object get(Object key, int refreshPeriod){
        return get(key, refreshPeriod, null);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#getFromCache(java.lang.String, int, java.lang.String)
         */
    public Object get(Object key, int refreshPeriod, String cronExpiry){
        CacheEntry cacheEntry = getCacheEntry(key, null, null);

        Object content = cacheEntry.getContent();
        CacheMapAccessEventType accessEventType = CacheMapAccessEventType.HIT;

        boolean reload = false;

        // Check if this entry has expired or has not yet been added to the cache. If
        // so, we need to decide whether to block, serve stale content or throw a
        // NeedsRefreshException
        if (this.isStale(cacheEntry, refreshPeriod, cronExpiry)) {
			
			
            EntryUpdateState updateState = cacheEntry.getUpdateState();
            synchronized (updateState) {
                if (updateState.isAwaitingUpdate() || updateState.isCancelled() || updateState.isComplete()) {
                    // No one else is currently updating this entry - grab ownership
                    updateState.startUpdate();

                    if (cacheEntry.isNew()) {
                        accessEventType = CacheMapAccessEventType.MISS;
                    } else {
                        accessEventType = CacheMapAccessEventType.STALE_HIT;
                    }
                } else if (updateState.isUpdating()) {
                    // Another thread is already updating the cache. We block if this
                    // is a new entry, or blocking mode is enabled. Either putInCache()
                    // or cancelUpdate() can cause this thread to resume.
                    if (cacheEntry.isNew() || blocking) {
                        do {
                            try {
                                updateState.wait();
                            } catch (InterruptedException e) {
                            }
                        } while (updateState.isUpdating());

                        if (updateState.isCancelled()) {
                            // The updating thread cancelled the update, let this one have a go.
                            updateState.startUpdate();

                            if (cacheEntry.isNew()) {
                                accessEventType = CacheMapAccessEventType.MISS;
                            } else {
                                accessEventType = CacheMapAccessEventType.STALE_HIT;
                            }
                        } else if (updateState.isComplete()) {
                            reload = true;
                        } else {
                            log.error("Invalid update state for cache entry " + key);
                        }
                    }
                } else {
                    reload = true;
                }
            }
        }

        // If reload is true then another thread must have successfully rebuilt the cache entry
        if (reload) {
            cacheEntry = (CacheEntry) cacheMap.get(key);

            if (cacheEntry != null) {
                content = cacheEntry.getContent();
            } else {
                log.error("Could not reload cache entry after waiting for it to be rebuilt");
            }
        }

        dispatchCacheMapAccessEvent(accessEventType, cacheEntry, null);

        // If we didn't end up getting a hit then we need to throw a NRE
        if (accessEventType != CacheMapAccessEventType.HIT) {
//            throw new NeedsRefreshException(content);
        }

        return content;
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#setPersistenceListener(com.opensymphony.oscache.base.persistence.PersistenceListener)
         */
    public void setPersistenceListener(PersistenceListener listener) {
        this.persistenceListener = listener;
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#getPersistenceListener()
         */
    public PersistenceListener getPersistenceListener() {
        return this.persistenceListener;
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#addCacheEventListener(com.opensymphony.oscache.base.events.CacheEventListener, java.lang.Class)
         */
    public void addCacheEventListener(CacheEventListener listener, Class clazz) {
        if (CacheEventListener.class.isAssignableFrom(clazz)) {
            listenerList.add(clazz, listener);
        } else {
            log.error("The class '" + clazz.getName() + "' is not a CacheEventListener. Ignoring this listener.");
        }
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#cancelUpdate(java.lang.String)
         */
    public void cancelUpdate(String key) {
        EntryUpdateState state;

        if (key != null) {
            CacheEntry cacheEntry = (CacheEntry) cacheMap.get(key);
            state = (EntryUpdateState) cacheEntry.getUpdateState();

            if (state != null) {
                synchronized (state) {
                    state.cancelUpdate();
                    state.notify();
                }
            }
        }
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#flushAll(java.util.Date)
         */
    public void flushAll(Date date) {
        flushAll(date, null);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#flushAll(java.util.Date, java.lang.String)
         */
    public void flushAll(Date date, String origin) {
        flushDateTime = date;

        if (listenerList.getListenerCount() > 0) {
            dispatchCachewideEvent(CachewideEventType.CACHE_FLUSHED, date, origin);
        }
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#flushEntry(java.lang.String)
         */
    public void flushEntry(String key) {
        flushEntry(key, null);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#flushEntry(java.lang.String, java.lang.String)
         */
    public void flushEntry(String key, String origin) {
        flushEntry(getCacheEntry(key, null, origin), origin);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#putInCache(java.lang.String, java.lang.Object)
         */
    public void put(Object key, Object content) {
        put(key, content, null);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#putInCache(java.lang.String, java.lang.Object, com.opensymphony.oscache.base.EntryRefreshPolicy)
         */
    public void put(Object key, Object content, EntryRefreshPolicy policy) {
        put(key, content, null, null);
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#putInCache(java.lang.String, java.lang.Object, java.lang.String[], com.opensymphony.oscache.base.EntryRefreshPolicy, java.lang.String)
         */
    public void put(Object key, Object content, EntryRefreshPolicy policy, String origin) {
        CacheEntry cacheEntry = getCacheEntry(key, policy, origin);
        boolean isNewEntry = cacheEntry.isNew();

        synchronized (cacheEntry) {
            cacheEntry.setContent(content);
            cacheMap.put(key, cacheEntry);
           
        }

        // Signal to any threads waiting on this update that it's now ready for them
        // in the cache!
        completeUpdate(cacheEntry);

        if (listenerList.getListenerCount() > 0) {
            CacheEntryEvent event = new CacheEntryEvent(this, cacheEntry, origin);

            if (isNewEntry) {
                dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_ADDED, event);
            } else {
                dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_UPDATED, event);
            }
        }
    }


    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#removeCacheEventListener(com.opensymphony.oscache.base.events.CacheEventListener, java.lang.Class)
         */
    public void removeCacheEventListener(CacheEventListener listener, Class clazz) {
        listenerList.remove(clazz, listener);
    }

    /**
     * Get an entry from this cache or create one if it doesn't exist.
     *
     * @param key    The key of the cache entry
     * @param policy Object that implements refresh policy logic
     * @param origin The origin of request (optional)
     * @return CacheEntry for the specified key.
     */
    protected CacheEntry getCacheEntry(Object key, EntryRefreshPolicy policy, String origin) {
        CacheEntry cacheEntry = null;

        // Verify that the key is valid
        if (key == null) {
            throw new IllegalArgumentException("getCacheEntry called with an empty or null key");
        }

        cacheEntry = (CacheEntry) cacheMap.get(key);

        // if the cache entry does not exist, create a new one
        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("No cache entry exists for key='" + key + "', creating");
            }

            cacheEntry = new CacheEntry(key, policy);
            cacheMap.put(key, cacheEntry);
        }

        return cacheEntry;
    }

    /**
     * Indicates whether or not the cache entry is stale.
     *
     * @param cacheEntry     The cache entry to test the freshness of.
     * @param refreshPeriod  The maximum allowable age of the entry, in seconds.
     * @param cronExpiry     A cron expression specifying absolute date(s) and/or time(s)
     * that the cache entry should expire at. If the cache entry was refreshed prior to
     * the most recent match for the cron expression, the entry will be considered stale.
     *
     * @return <code>true</code> if the entry is stale, <code>false</code> otherwise.
     */
    protected boolean isStale(CacheEntry cacheEntry, int refreshPeriod, String cronExpiry) {
        boolean result = cacheEntry.needsRefresh(refreshPeriod) || isFlushed(cacheEntry);

        if ((cronExpiry != null) && (cronExpiry.length() > 0)) {
            try {
                FastCronParser parser = new FastCronParser(cronExpiry);
                result = result || parser.hasMoreRecentMatch(cacheEntry.getLastUpdate());
            } catch (ParseException e) {
                log.warn(e);
            }
        }

        return result;
    }

    /**
     * Completely clears the cache.
     */
    public void clear() {
        cacheMap.clear();
    }

    /**
     * Removes the update state for the specified key and notifies any other
     * threads that are waiting on this object. This is called automatically
     * by the {@link #putInCache} method.
     *
     * @param key The cache key that is no longer being updated.
     */
    protected void completeUpdate(CacheEntry cacheEntry) {
        EntryUpdateState state;

        state = (EntryUpdateState) cacheEntry.getUpdateState();

        if (state != null) {
            synchronized (state) {
                if (!state.isUpdating()) {
                    state.startUpdate();
                }

                state.completeUpdate();
                state.notifyAll();
            }
        }
    }

    /* (non-Javadoc)
         * @see com.opensymphony.oscache.base.CacheAPI#removeEntry(java.lang.String)
         */
    public void removeEntry(String key) {
        removeEntry(key, null);
    }

    /**
     * Completely removes a cache entry from the cache and its associated cache
     * groups.
     *
     * @param key    The key of the entry to remove.
     * @param origin The origin of this remove request.
     */
    protected void removeEntry(String key, String origin) {
        CacheEntry cacheEntry = (CacheEntry) cacheMap.get(key);
        cacheMap.remove(key);

        if (listenerList.getListenerCount() > 0) {
            CacheEntryEvent event = new CacheEntryEvent(this, cacheEntry, origin);
            dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_REMOVED, event);
        }
    }

    /**
     * Dispatch a cache entry event to all registered listeners.
     *
     * @param eventType   The type of event (used to branch on the proper method)
     * @param event       The event that was fired
     */
    private void dispatchCacheEntryEvent(CacheEntryEventType eventType, CacheEntryEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CacheEntryEventListener.class) {
                if (eventType.equals(CacheEntryEventType.ENTRY_ADDED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cacheEntryAdded(event);
                } else if (eventType.equals(CacheEntryEventType.ENTRY_UPDATED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cacheEntryUpdated(event);
                } else if (eventType.equals(CacheEntryEventType.ENTRY_FLUSHED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cacheEntryFlushed(event);
                } else if (eventType.equals(CacheEntryEventType.ENTRY_REMOVED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cacheEntryRemoved(event);
                }
            }
        }
    }

    /**
     * Dispatch a cache group event to all registered listeners.
     *
     * @param eventType The type of event (this is used to branch to the correct method handler)
     * @param group     The cache group that the event applies to
     * @param origin      The origin of this event (optional)
     */
    private void dispatchCacheGroupEvent(CacheEntryEventType eventType, String group, String origin) {
        CacheGroupEvent event = new CacheGroupEvent(this, group, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CacheEntryEventListener.class) {
                if (eventType.equals(CacheEntryEventType.GROUP_FLUSHED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cacheGroupFlushed(event);
                }
            }
        }
    }

    /**
     * Dispatch a cache map access event to all registered listeners.
     *
     * @param eventType     The type of event
     * @param entry         The entry that was affected.
     * @param origin        The origin of this event (optional)
     */
    private void dispatchCacheMapAccessEvent(CacheMapAccessEventType eventType, CacheEntry entry, String origin) {
        CacheMapAccessEvent event = new CacheMapAccessEvent(eventType, entry, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CacheMapAccessEventListener.class) {
                ((CacheMapAccessEventListener) listeners[i + 1]).accessed(event);
            }
        }
    }

    /**
     * Dispatch a cache pattern event to all registered listeners.
     *
     * @param eventType The type of event (this is used to branch to the correct method handler)
     * @param pattern     The cache pattern that the event applies to
     * @param origin      The origin of this event (optional)
     */
    private void dispatchCachePatternEvent(CacheEntryEventType eventType, String pattern, String origin) {
        CachePatternEvent event = new CachePatternEvent(this, pattern, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CacheEntryEventListener.class) {
                if (eventType.equals(CacheEntryEventType.PATTERN_FLUSHED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cachePatternFlushed(event);
                }
            }
        }
    }

    /**
     * Dispatches a cache-wide event to all registered listeners.
     *
     * @param eventType The type of event (this is used to branch to the correct method handler)
     * @param origin The origin of this event (optional)
     */
    private void dispatchCachewideEvent(CachewideEventType eventType, Date date, String origin) {
        CachewideEvent event = new CachewideEvent(this, date, origin);

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CacheEntryEventListener.class) {
                if (eventType.equals(CachewideEventType.CACHE_FLUSHED)) {
                    ((CacheEntryEventListener) listeners[i + 1]).cacheFlushed(event);
                }
            }
        }
    }

    /**
     * Flush a cache entry. On completion of the flush, a
     * <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
     *
     * @param entry The entry to flush
     * @param origin The origin of this flush event (optional)
     */
    private void flushEntry(CacheEntry entry, String origin) {
    	Object key = entry.getKey();

        // Flush the object itself
        entry.flush();

        if (!entry.isNew()) {
            // Update the entry's state in the map
            cacheMap.put(key, entry);
        }

        // Trigger an ENTRY_FLUSHED event. [CACHE-107] Do this for all flushes.
        if (listenerList.getListenerCount() > 0) {
            CacheEntryEvent event = new CacheEntryEvent(this, entry, origin);
            dispatchCacheEntryEvent(CacheEntryEventType.ENTRY_FLUSHED, event);
        }
    }

    public EventListenerList getListenerList() {
        return listenerList;
    }
}
