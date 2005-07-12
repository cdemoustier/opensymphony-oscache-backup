/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import com.opensymphony.oscache.base.CacheImpl;


/**
 * A simple unlimited cache that has no upper bound to the number of
 * cache entries it can contain.
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class UnlimitedCache extends CacheImpl {
    /**
     * Creates an unlimited cache by calling the super class's constructor
     * with an <code>UNLIMITED</code> maximum number of entries.
     */
    public UnlimitedCache() {
        super(-1);
    }

    /**
     * Overrides the <code>setMaxEntries</code> with an empty implementation.
     * This property cannot be modified and is ignored for an
     * <code>UnlimitedCache</code>.
     */
    public void setMaxEntries(int maxEntries) {
    }

    /**
     * Implements <code>itemRetrieved</code> with an empty implementation.
     * The unlimited cache doesn't care that an item was retrieved.
     */
    protected void itemRetrieved(Object key) {
    }

    /**
     * Implements <code>itemPut</code> with an empty implementation.
     * The unlimited cache doesn't care that an item was put in the cache.
     */
    protected void itemPut(Object key) {
    }

    /**
     * This method just returns <code>null</code> since items should
     * never end up being removed from an unlimited cache!
     */
    protected Object removeItem() {
        return null;
    }

    /**
     * An empty implementation. The unlimited cache doesn't care that an
     * item was removed.
     */
    protected void itemRemoved(Object key) {
    }
}
