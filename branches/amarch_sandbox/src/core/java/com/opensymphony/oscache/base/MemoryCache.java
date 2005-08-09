/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.oscache.base.events.CacheMapAccessEventType;
import com.opensymphony.oscache.util.FastCronParser;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

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
public class MemoryCache implements Serializable, Cache {
	private static transient final Log log = LogFactory
			.getLog(MemoryCache.class);

	/**
	 * Date of last complete cache flush.
	 */
	private Date flushDateTime = null;

	/**
	 * The actual cache map. This is where the cached objects are held.
	 */
	private Map cacheMap = new ConcurrentHashMap();
	
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
	 * @see com.opensymphony.oscache.base.Cache#isFlushed(com.opensymphony.oscache.base.CacheEntry)
	 */
	public boolean isFlushed(CacheEntry cacheEntry) {
		if (flushDateTime != null) {
			long lastUpdate = cacheEntry.getLastUpdate();

			return (flushDateTime.getTime() >= lastUpdate);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#getFromCache(java.lang.String)
	 */
	public CacheEntry get(Object key) {
		return get(key, CacheEntry.INDEFINITE_EXPIRY, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#getFromCache(java.lang.String,
	 *      int)
	 */
	public CacheEntry get(Object key, int refreshPeriod) {
		return get(key, refreshPeriod, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#getFromCache(java.lang.String,
	 *      int, java.lang.String)
	 */
	public CacheEntry get(Object key, int refreshPeriod, String cronExpiry) {
		CacheEntry cacheEntry = getCacheEntry(key, null, null);

		Object content = cacheEntry.getContent();
		CacheMapAccessEventType accessEventType = CacheMapAccessEventType.HIT;

		boolean reload = false;

		// Check if this entry has expired or has not yet been added to the
		// cache. If
		// so, we need to decide whether to block or serve stale content
		if (this.isStale(cacheEntry, refreshPeriod, cronExpiry)) {

			synchronized (cacheEntry) {
				if (!cacheEntry.isUpdating()) {
					// No one else is currently updating this entry - grab
					// ownership
					cacheEntry.startUpdate();

					if (cacheEntry.isNew()) {
						accessEventType = CacheMapAccessEventType.MISS;
					} else {
						accessEventType = CacheMapAccessEventType.STALE_HIT;
					}
				} else {
					// Another thread is already updating the cache. We block if
					// this
					// is a new entry, or blocking mode is enabled. Either
					// putInCache()
					// or cancelUpdate() can cause this thread to resume.
					if (cacheEntry.isNew()) {
						do {
							try {
								cacheEntry.wait();
							} catch (InterruptedException e) {
							}
						} while (cacheEntry.isUpdating());
						
							// The updating thread cancelled the update, let
							// this one have a go.
						cacheEntry.startUpdate();

						if (cacheEntry.isNew()) {
							accessEventType = CacheMapAccessEventType.MISS;
						} else {
							accessEventType = CacheMapAccessEventType.STALE_HIT;
						}						
						
					}
					reload = true;
				} 
			}
		}

		// If reload is true then another thread must have successfully rebuilt
		// the cache entry
		if (reload) {
			cacheEntry = (CacheEntry) cacheMap.get(key);

			if (cacheEntry != null) {
				content = cacheEntry.getContent();
			} else {
				log
						.error("Could not reload cache entry after waiting for it to be rebuilt");
			}
		}


		// If we didn't end up getting a hit then we need to throw a NRE
		if (accessEventType != CacheMapAccessEventType.HIT) {
			
		}

		return cacheEntry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#cancelUpdate(java.lang.String)
	 */
	public void cancelUpdate(String key) {

		if (key != null) {
			CacheEntry cacheEntry = (CacheEntry) cacheMap.get(key);

			if (cacheEntry != null) {
				synchronized (cacheEntry) {
					cacheEntry.cancelUpdate();
					cacheEntry.notify();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#flushAll(java.util.Date)
	 */
	public void flushAll(Date date) {
		flushAll(date, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#flushAll(java.util.Date,
	 *      java.lang.String)
	 */
	public void flushAll(Date date, String origin) {
		flushDateTime = date;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#flushEntry(java.lang.String)
	 */
	public void flushEntry(String key) {
		flushEntry(key, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#flushEntry(java.lang.String,
	 *      java.lang.String)
	 */
	public void flushEntry(String key, String origin) {
		flushEntry(getCacheEntry(key, null, origin), origin);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#putInCache(java.lang.String,
	 *      java.lang.Object)
	 */
	public CacheEntry put(Object key, CacheEntry cacheEntry) {
		return put(key, cacheEntry, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#putInCache(java.lang.String,
	 *      java.lang.Object, com.opensymphony.oscache.base.EntryRefreshPolicy)
	 */
	public CacheEntry put(Object key, CacheEntry cacheEntry, EntryRefreshPolicy policy) {
		return put(key, cacheEntry, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#putInCache(java.lang.String,
	 *      java.lang.Object, java.lang.String[],
	 *      com.opensymphony.oscache.base.EntryRefreshPolicy, java.lang.String)
	 */
	public CacheEntry put(Object key, CacheEntry cacheEntry, EntryRefreshPolicy policy,
			String origin) {
		
			return (CacheEntry) cacheMap.put(key, cacheEntry);
	}

	/**
	 * Get an entry from this cache or create one if it doesn't exist.
	 * 
	 * @param key
	 *            The key of the cache entry
	 * @param policy
	 *            Object that implements refresh policy logic
	 * @param origin
	 *            The origin of request (optional)
	 * @return CacheEntry for the specified key.
	 */
	protected CacheEntry getCacheEntry(Object key, EntryRefreshPolicy policy,
			String origin) {
		CacheEntry cacheEntry = null;

		// Verify that the key is valid
		if (key == null) {
			throw new IllegalArgumentException(
					"getCacheEntry called with an empty or null key");
		}

		cacheEntry = (CacheEntry) cacheMap.get(key);

		// if the cache entry does not exist, create a new one
		if (cacheEntry == null) {
			if (log.isDebugEnabled()) {
				log.debug("No cache entry exists for key='" + key
						+ "', creating");
			}

			cacheEntry = new CacheEntry(key, policy);
			cacheMap.put(key, cacheEntry);
		}

		return cacheEntry;
	}

	/**
	 * Indicates whether or not the cache entry is stale.
	 * 
	 * @param cacheEntry
	 *            The cache entry to test the freshness of.
	 * @param refreshPeriod
	 *            The maximum allowable age of the entry, in seconds.
	 * @param cronExpiry
	 *            A cron expression specifying absolute date(s) and/or time(s)
	 *            that the cache entry should expire at. If the cache entry was
	 *            refreshed prior to the most recent match for the cron
	 *            expression, the entry will be considered stale.
	 * 
	 * @return <code>true</code> if the entry is stale, <code>false</code>
	 *         otherwise.
	 */
	protected boolean isStale(CacheEntry cacheEntry, int refreshPeriod,
			String cronExpiry) {
		boolean result = cacheEntry.needsRefresh(refreshPeriod)
				|| isFlushed(cacheEntry);

		if ((cronExpiry != null) && (cronExpiry.length() > 0)) {
			try {
				FastCronParser parser = new FastCronParser(cronExpiry);
				result = result
						|| parser
								.hasMoreRecentMatch(cacheEntry.getLastUpdate());
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

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.oscache.base.CacheAPI#removeEntry(java.lang.String)
	 */
	public CacheEntry removeEntry(String key) {
		return removeEntry(key, null);
	}

	/**
	 * Completely removes a cache entry from the cache and its associated cache
	 * groups.
	 * 
	 * @param key
	 *            The key of the entry to remove.
	 * @param origin
	 *            The origin of this remove request.
	 */
	protected CacheEntry removeEntry(String key, String origin) {
		CacheEntry cacheEntry = (CacheEntry) cacheMap.get(key);
		return (CacheEntry) cacheMap.remove(key);
	}

	/**
	 * Flush a cache entry. On completion of the flush, a
	 * <tt>CacheEntryEventType.ENTRY_FLUSHED</tt> event is fired.
	 * 
	 * @param entry
	 *            The entry to flush
	 * @param origin
	 *            The origin of this flush event (optional)
	 */
	private void flushEntry(CacheEntry entry, String origin) {
		Object key = entry.getKey();

		// Flush the object itself
		entry.flush();

		if (!entry.isNew()) {
			// Update the entry's state in the map
			cacheMap.put(key, entry);
		}

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


}
