/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;


/**
 * This exception is thrown when retrieving an item from cache and it is
 * expired.
 * Note that for fault tolerance purposes, it is possible to retrieve the
 * current cached object from the exception.
 *
 * @author        <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @version        $Revision$
 */
public final class NeedsRefreshException extends Exception {
    /**
     * Current object in the cache
     */
    private Object cacheContent = null;

    /**
     * Create a NeedsRefreshException
     */
    public NeedsRefreshException(Object cacheContent) {
        super();
        this.cacheContent = cacheContent;
    }

    /**
     * Retrieve current object in the cache
     */
    public Object getCacheContent() {
        return cacheContent;
    }
}
