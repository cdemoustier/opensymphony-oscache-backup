/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.events;

import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.CacheEntry;

/**
 * CacheEntryEvent is the object created when an event occurs on a
 * cache entry (Add, update, remove, flush). It contains the entry itself and
 * its map.
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public class CacheEntryEvent extends CacheEvent {
	
	/**
     * Get an event type for an entry added.
     */
    public static final int ENTRY_ADDED = 0;

    /**
     * Get an event type for an entry updated.
     */
    public static final int ENTRY_UPDATED = 1;

    /**
     * Get an event type for an entry flushed.
     */
    public static final int ENTRY_FLUSHED = 2;

    /**
     * Get an event type for an entry removed.
     */
    public static final int ENTRY_REMOVED = 4;

    /**
     * Get an event type for a group flush event.
     */
    public static final int GROUP_FLUSHED = 8;
    

    /**
     * The entry that the event applies to.
     */
    private CacheEntry entry = null;

    /**
     * Constructs a cache entry event object with no specified origin
     *
     * @param map     The cache map of the cache entry
     * @param entry   The cache entry that the event applies to
     * @param eventType 
     */
    public CacheEntryEvent(Cache map, CacheEntry entry, int eventType) {
        super(map, eventType);
        this.entry = entry;
    }


    /**
     * Retrieve the cache entry that the event applies to.
     */
    public CacheEntry getEntry() {
        return entry;
    }

    

    public String toString() {
        return "key=" + entry.getKey();
    }
}
