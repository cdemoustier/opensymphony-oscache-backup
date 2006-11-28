/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.events;

import com.opensymphony.oscache.core.Cache;

/**
 * CacheGroupEvent is an event that occurs at the cache group level
 * (Add, update, remove, flush). It contains the group name and the
 * originating cache object.
 *
 * @version        $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class CacheGroupEvent extends CacheEvent {
    
    public static final int GROUP_FLUSHED = 0;
	/**
     * The group that the event applies to.
     */
    private String group = null;

    /**
     * Constructs a cache group event with no origin
     *
     * @param map     The cache map of the cache entry
     * @param group   The cache group that the event applies to.
     */
    public CacheGroupEvent(Cache map, String group) {
        super(map, CacheGroupEvent.GROUP_FLUSHED);
        this.group = group;
    }


    /**
     * Retrieve the cache group that the event applies to.
     */
    public String getGroup() {
        return group;
    }


    public String toString() {
        return "groupName=" + group;
    }
}
