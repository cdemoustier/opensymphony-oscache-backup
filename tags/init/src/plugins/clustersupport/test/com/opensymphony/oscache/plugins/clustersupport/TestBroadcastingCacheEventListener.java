/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.InitializationException;
import com.opensymphony.oscache.base.events.CacheEntryEvent;
import com.opensymphony.oscache.base.events.CacheGroupEvent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test all the public methods of the broadcasting listener and assert the
 * return values
 *
 * @version        $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class TestBroadcastingCacheEventListener extends TestCase {
    /**
     * The persistance listener used for the tests
     */
    private static BroadcastingCacheEventListener listener = null;

    /**
     * A cache instance to use for the tests
     */
    private static Cache cache = null;

    /**
     * Cache group
     */
    private final String GROUP = "test group";

    /**
     * Object key
     */
    private final String KEY = "Test clustersupport persistence listener key";

    public TestBroadcastingCacheEventListener(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestBroadcastingCacheEventListener.class);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a listener
        if (listener == null) {
            listener = new BroadcastingCacheEventListener();

            Config config = new Config();
            config.set("cache.cluster.multicast.ip", "231.12.21.132");

            try {
                listener.initialize(null, config);
            } catch (InitializationException e) {
                fail(e.getMessage());
            }

            cache = new Cache(true, false);
            assertNotNull(listener);
            assertNotNull(cache);
        }
    }

    public void testCacheEntryAdded() {
        CacheEntry entry = new CacheEntry(KEY, null);
        CacheEntryEvent event = new CacheEntryEvent(cache, entry);
        listener.cacheEntryAdded(event);
    }

    public void testCacheEntryFlushed() {
        CacheEntry entry = new CacheEntry(KEY, null);
        CacheEntryEvent event = new CacheEntryEvent(cache, entry);
        listener.cacheEntryFlushed(event);
    }

    public void testCacheEntryRemoved() {
        CacheEntry entry = new CacheEntry(KEY, null);
        CacheEntryEvent event = new CacheEntryEvent(cache, entry);
        listener.cacheEntryRemoved(event);
    }

    public void testCacheEntryUpdated() {
        CacheEntry entry = new CacheEntry(KEY, null);
        CacheEntryEvent event = new CacheEntryEvent(cache, entry);
        listener.cacheEntryUpdated(event);
    }

    public void testCacheGroupFlushed() {
        CacheEntry entry = new CacheEntry(KEY, null);
        CacheGroupEvent event = new CacheGroupEvent(cache, GROUP);
        listener.cacheGroupFlushed(event);
    }
}
