/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.InitializationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the ClusterManager class
 *
 * @version        $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class TestClusterManager extends TestCase {
    /**
     * Name of the cache to flush
     */
    private final String CACHE_NAME = "test";

    /**
     * Cache group
     */
    private final String GROUP = "test group";

    /**
     * Object key
     */
    private final String KEY = "Test clustersupport persistence listener key";

    public TestClusterManager(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestClusterManager.class);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
    }

    public void testCacheManager() {
        Config config = new Config();

        try {
            ClusterManager cm = new ClusterManager(config);

            // Send some flush signals
            cm.signalEntryFlush(KEY, CACHE_NAME);
            cm.signalGroupFlush(GROUP, CACHE_NAME);

            // Simulate receiving some signals
            cm.handleNotification(new ClusterNotification(ClusterNotification.FLUSH_KEY, CACHE_NAME, GROUP));
            cm.handleNotification(new ClusterNotification(ClusterNotification.FLUSH_GROUP, CACHE_NAME, GROUP));

            // Shutdown the cache manager
            cm.shutdown();
        } catch (InitializationException e) {
            fail("Could not initialize ClusterManager - " + e);
        }
    }
}
