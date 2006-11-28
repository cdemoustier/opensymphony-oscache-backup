package com.opensymphony.oscache.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opensymphony.oscache.algorithm.LRUEvictionAlgorithm;
import com.opensymphony.oscache.events.CacheEntryEvent;
import com.opensymphony.oscache.events.CacheEvent;
import com.opensymphony.oscache.events.CacheGroupEvent;
import com.opensymphony.oscache.events.CacheListener;
import com.opensymphony.oscache.events.CacheMapAccessEvent;
import com.opensymphony.oscache.events.CachewideEvent;
import com.opensymphony.oscache.util.FastCronParser;

/**
 * A base class that provides most of the core caching functionality for a
 * concrete cache implementation.
 */
public abstract class BaseCache implements Cache {

	private EvictionAlgorithm algorithm = new LRUEvictionAlgorithm();

	private String name;

	private final Object LISTENER_LOCK = new Object();

	private List listeners;

	/**
	 * Date of last complete cache flush.
	 */
	private Date flushDateTime = null;

	private Map groupMap = new HashMap();

	private int capacity;

	/**
	 * Clears the entire cache. This will result in a
	 * {@link CacheEvent#CLEAR_EVENT} being fired even if the cache already
	 * contained no entries.
	 */
	public synchronized void clear() {
		clearInternal();
	}

	/**
	 * Shuts down the cache by waiting for any asynchronous cache loaders to
	 * complete. Depending on the loader that is in use and how much load it is
	 * under, this operation may take a long time to complete.
	 */
	public void shutdown() {

	}

	/**
	 * Retrieves an object from the cache.
	 * 
	 * @param key
	 *            the key of the object to retrieve.
	 * @return the cached object, or <code>null</code> if the object could not
	 *         be found and could not be loaded.
	 */
	public synchronized Object get(Object key) {
		return get(key, CacheEntry.INDEFINITE_EXPIRY);
	}

	/**
	 * Retrieves an object from the cache.
	 * 
	 * @param key
	 *            the key of the object to retrieve.
	 * @return the cached object, or <code>null</code> if the object could not
	 *         be found and could not be loaded.
	 */
	public synchronized Object get(Object key, int refreshPeriod) {
		return get(key, refreshPeriod, null);
	}

	/**
	 * Retrieves an object from the cache.
	 * 
	 * @param key
	 *            the key of the object to retrieve.
	 * @return the cached object, or <code>null</code> if the object could not
	 *         be found and could not be loaded.
	 */
	public synchronized Object get(Object key, int refreshPeriod,
			String cronExpiry) {
		CacheEntry cacheEntry = getEntry(key);
		Object content = null;
		if (cacheEntry != null) {
			if (!this.isStale(cacheEntry, refreshPeriod, cronExpiry)) {
				content = cacheEntry.getValue();
			}
		}
		return content;

	}

	public synchronized Object remove(Object key) {
		CacheEntry result = removeInternal(key);
		if (result != null) {
			algorithm.evaluateRemove(key);
			fireEntryEvent(result, CacheEntryEvent.ENTRY_REMOVED);
		}
		return result;
	}

	/**
	 * Put an object in the cache specifying the key to use.
	 * 
	 * @param key
	 *            Key of the object in the cache.
	 * @param content
	 *            The object to cache.
	 */
	public synchronized Object put(Object key, Object content) {
		return put(key, content, null, null);
	}

	/**
	 * Put an object in the cache specifying the key and refresh policy to use.
	 * 
	 * @param key
	 *            Key of the object in the cache.
	 * @param content
	 *            The object to cache.
	 * @param policy
	 *            Object that implements refresh policy logic
	 */
	public synchronized Object put(Object key, Object content,
			EntryRefreshPolicy policy) {
		return put(key, content, null, policy);
	}

	/**
	 * Put in object into the cache, specifying both the key to use and the
	 * cache groups the object belongs to.
	 * 
	 * @param key
	 *            Key of the object in the cache
	 * @param content
	 *            The object to cache
	 * @param groups
	 *            The cache groups to add the object to
	 */
	public synchronized Object put(Object key, Object content, String[] groups) {
		return put(key, content, groups, null);
	}

	/**
	 * Store the supplied entry in the cache.
	 * 
	 * @param key
	 *            the key to store the entry under.
	 * @param value
	 *            the object to store.
	 * @return the previous object that was stored under this key, if any.
	 */
	public synchronized Object put(Object key, Object value, String[] groups,
			EntryRefreshPolicy policy) {
		CacheEntry newEntry = new CacheEntry(key, value, groups, policy);
		CacheEntry oldEntry = putInternal(newEntry);
		Object evictionKey = algorithm.evaluatePut(key);

		// Remove an entry from the cache if the eviction algorithm says we need
		// to
		if (evictionKey != null) {
			remove(evictionKey);
		}

		addGroupMappings(newEntry);

		// fire off a notification message
		if (oldEntry == null) {
			fireEntryEvent(newEntry, CacheEntryEvent.ENTRY_ADDED);
			return null;
		}
		fireEntryEvent(newEntry, CacheEntryEvent.ENTRY_UPDATED);

		return oldEntry.getValue();
	}

	/**
	 * Get an entry from this cache.
	 * 
	 * @param key
	 *            The key of the cache entry
	 * @return CacheEntry for the specified key.
	 */
	public synchronized CacheEntry getEntry(Object key) {
		CacheEntry cacheEntry = getInternal(key);
		if (cacheEntry != null) {
			algorithm.evaluateGet(key);
			fireEvent(new CacheMapAccessEvent(this, cacheEntry,
					CacheMapAccessEvent.HIT));
		} else {
			fireEvent(new CacheMapAccessEvent(this, cacheEntry,
					CacheMapAccessEvent.MISS));
		}
		return cacheEntry;
	}

	/**
	 * Adds a listener that will receive notifications when cache events occur.
	 * 
	 * @param listener
	 *            the listener to receive the events.
	 */
	public void addCacheListener(CacheListener listener) {
		synchronized (LISTENER_LOCK) {
			if (listeners == null)
				listeners = new ArrayList();
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener from the cache.
	 * 
	 * @param listener
	 *            the listener to remove.
	 * @return <code>true</code> if the listener was removed successfully,
	 *         <code>false</code> if the listener could not be found.
	 */
	public boolean removeCacheListener(CacheListener listener) {
		synchronized (LISTENER_LOCK) {
			if (listeners != null)
				return listeners.remove(listener);
			return false;
		}
	}

	/**
	 * Flushes all unexpired objects that belong to the supplied group. On
	 * completion this method fires a <tt>CacheEntryEvent.GROUP_FLUSHED</tt>
	 * event.
	 * 
	 * @param group
	 *            The group to flush
	 */
	public synchronized void flushGroup(String group) {
		// Flush all objects in the group
		Set groupEntries = (Set) groupMap.get(group);

		if (groupEntries != null) {
			Iterator itr = groupEntries.iterator();
			Object key;
			CacheEntry entry;

			while (itr.hasNext()) {
				key = itr.next();
				entry = getEntry(key);
				if (entry != null
						&& !entry.needsRefresh(CacheEntry.INDEFINITE_EXPIRY)) {
					entry.flush();
					fireEntryEvent(entry, CacheEntryEvent.ENTRY_FLUSHED);
				}
			}
		}

		groupMap.remove(group);

		fireEvent(new CacheGroupEvent(this, group));

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
			}
		}

		return result;
	}

	/**
	 * Checks if the cache was flushed more recently than the CacheEntry
	 * provided. Used to determine whether to refresh the particular CacheEntry.
	 * 
	 * @param cacheEntry
	 *            The cache entry which we're seeing whether to refresh
	 * @return Whether or not the cache has been flushed more recently than this
	 *         cache entry was updated.
	 */
	public boolean isFlushed(CacheEntry cacheEntry) {
		if (flushDateTime != null) {
			long lastUpdate = cacheEntry.getLastUpdate();

			return (flushDateTime.getTime() >= lastUpdate);
		} else {
			return false;
		}
	}

	/**
	 * Fires a cache event.
	 * 
	 * @param key
	 *            the key of the object that the event relates to.
	 * @param value
	 *            the object that the event relates to.
	 * @param eventType
	 *            the type of event that occurred. See {@link CacheEvent} for
	 *            the possible event types.
	 */
	protected void fireEntryEvent(CacheEntry entry, int eventType) {
		CacheEntryEvent event = new CacheEntryEvent(this, entry, eventType);
		fireEvent(event);
	}

	/**
	 * Fires a cache event.
	 * 
	 * @param key
	 *            the key of the object that the event relates to.
	 * @param value
	 *            the object that the event relates to.
	 * @param eventType
	 *            the type of event that occurred. See {@link CacheEvent} for
	 *            the possible event types.
	 */
	protected void fireEvent(CacheEvent event) {
		synchronized (LISTENER_LOCK) {
			if (listeners != null) {
				int i = 0;
				for (int size = listeners.size(); i < size; i++) {
					CacheListener listener = (CacheListener) listeners.get(i);
					listener.onChange(event);
				}
			}
		}
	}

	/**
	 * Retrieves the name of this cache instance.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this cache instance to the specified value.
	 * 
	 * @param name
	 *            the new name for the cache.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setEvictionAlgorithm(EvictionAlgorithm algorithm)
			throws IllegalStateException {
		this.algorithm = algorithm;
	}

	public EvictionAlgorithm getEvictionAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Retrieves the cache entry from the underlying datastore. <p/> The
	 * implementation of this method does not need to be synchronized; the
	 * synchronization is already managed by the calling method.
	 * 
	 * @param key
	 *            the key that indentifies the cache entry to retrieve.
	 * @return the cache entry, or <code>null</code> if no cache entry was
	 *         found.
	 */
	protected abstract CacheEntry getInternal(Object key);

	/**
	 * Retrieves the cache entry from the underlying datastore. <p/> The
	 * implementation of this method does not need to be synchronized; the
	 * synchronization is already managed by the calling method.
	 * 
	 * @param key
	 *            the key that indentifies the cache entry to retrieve.
	 * @return the existing cache entry, or <code>null</code> if no cache
	 *         entry was found.
	 */
	protected abstract CacheEntry putInternal(CacheEntry entry);

	/**
	 * Removes a cache entry from the underlying datastore. <p/> The
	 * implementation of this method does not need to be synchronized; the
	 * synchronization is already managed by the calling method.
	 * 
	 * @param key
	 *            the key that indentifies the cache entry to remove.
	 * @return the cache entry that was removed, or <code>null</code> if no
	 *         cache entry was found with that key.
	 */
	protected abstract CacheEntry removeInternal(Object key);

	/**
	 * Cleans out the entire cache. <p/> The implementation of this method does
	 * not need to be synchronized; the synchronization is already managed by
	 * the calling method.
	 */
	protected abstract void clearInternal();

	public void flushAll(Date date) {
		synchronized (flushDateTime) {
			flushDateTime = date;
			clearInternal();
			fireEvent(new CachewideEvent(this, date));
		}
	}

	/**
	 * Add this entry's key to the groups specified by the entry's groups.
	 * 
	 */
	protected void addGroupMappings(CacheEntry entry) {
		// Add this CacheEntry to the groups that it is now a member of
		for (Iterator it = entry.getGroups().iterator(); it.hasNext();) {
			String groupName = (String) it.next();

			if (groupMap == null) {
				groupMap = new HashMap();
			}

			Set group = (Set) groupMap.get(groupName);

			if (group == null) {
				group = new HashSet();
				groupMap.put(groupName, group);
			}

			group.add(entry.getKey());
		}
	}

	public int getCapacity() {
		return capacity;
	}

}
