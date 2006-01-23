/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.events;

import com.opensymphony.oscache.core.Cache;

import java.util.Date;

/**
 * A <code>CachewideEvent<code> represents and event that occurs on
 * the the entire cache, eg a cache flush or clear.
 *
 * @version $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class CachewideEvent extends CacheEvent {
    

    /**
     * The date/time for when the flush is scheduled
     */
    private Date date = null;
    
    /**
     * Get an event type for a cache flush event.
     */
    public static int CACHE_FLUSHED = 0;

    /**
     * Constructs a cachewide event with the specified origin.
     *
     * @param cache   The cache map that the event occurred on.
     * @param date    The date/time that this cachewide event is scheduled for
     * (eg, the date that the cache is to be flushed).
     */
    public CachewideEvent(Cache cache, Date date) {
       
        super(cache, CACHE_FLUSHED);
        this.date = date;
    }
    
    /**
     * Retrieve the date/time that the cache flush is scheduled for.
     */
    public Date getDate() {
        return date;
    }
}
