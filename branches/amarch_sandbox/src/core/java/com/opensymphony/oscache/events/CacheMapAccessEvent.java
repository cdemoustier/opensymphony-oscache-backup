/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.events;

import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.CacheEntry;

/**
 * Cache map access event. This is the object created when an event occurs on a
 * cache map (cache Hit, cache miss). It contains the entry that was referenced
 * by the event and the event type.
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public class CacheMapAccessEvent extends CacheEntryEvent {
	  /**
     * Get an event type for a cache hit.
     */
    public static int HIT = 0;

    /**
     * Get an event type for a cache miss.
     */
    public static int MISS = 1;

    /**
     * Get an event type for when the data was found in the cache but was stale.
     */
    public static int STALE_HIT = 2;

    /**
     * Constructor.
     * <p>
     * @param eventType   Type of the event.
     * @param entry       The cache entry that the event applies to.
     */
    public CacheMapAccessEvent(Cache source, CacheEntry entry, int eventType ) {
        super(source, entry, eventType);
    }

  
}
