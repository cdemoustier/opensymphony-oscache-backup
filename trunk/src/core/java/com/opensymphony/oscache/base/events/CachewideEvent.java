/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.events;

import com.opensymphony.oscache.base.Cache;

/**
 * A <code>CachewideEvent<code> represents and event that occurs on
 * the the entire cache, eg a cache flush or clear.
 *
 * @version $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class CachewideEvent extends CacheEvent {
    /**
     * The cache where the event occurred.
     */
    private Cache map = null;

    /**
     * Constructs a cachewide event with no origin.
     *
     * @param map     The cache map that the event occurred on.
     */
    public CachewideEvent(Cache map) {
        this(map, null);
    }

    /**
     * Constructs a cachewide event with the specified origin.
     *
     * @param map     The cache map that the event occurred on.
     * @param origin  An optional tag that can be attached to the event to
     * specify the event's origin. This is useful to prevent events from being
     * fired recursively in some situations, such as when an event handler
     * causes another event to be fired.
     */
    public CachewideEvent(Cache map, String origin) {
        super(origin);
        this.map = map;
    }

    /**
     * Retrieve the cache map that the event occurred on.
     */
    public Cache getMap() {
        return map;
    }
}
