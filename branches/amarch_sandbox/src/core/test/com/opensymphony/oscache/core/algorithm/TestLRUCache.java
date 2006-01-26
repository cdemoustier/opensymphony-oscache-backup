/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core.algorithm;

import com.opensymphony.oscache.algorithm.LRUEvictionAlgorithm;
import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.EvictionAlgorithm;
import com.opensymphony.oscache.core.MemoryCache;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the LRUCache class. It only tests that the algorithm reacts as
 * expected when entries are removed. All the other tests related to the LRU
 * algorithm are in the TestNonQueueCache class, since those tests are shared
 * with the TestUnlimitedCache class.
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestLRUCache extends TestQueueCache {
    /**
     * LRU Cache object
     */
    private static Cache cache = null;
	private EvictionAlgorithm algorithm;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestLRUCache(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestLRUCache.class);
    }

    /**
     * Abstract method used by the TestAbstractCache class
     * <p>
     * @return  A cache instance
     */
    public Cache getCache() {
        return cache;
    }
    
    /**
     * Abstract method used by the TestAbstractCache class
     * <p>
     * @return  A cache instance
     */
    public EvictionAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // Create a cache instance on first invocation
        if (cache == null) {
        	cache = new MemoryCache();
        algorithm = new LRUEvictionAlgorithm();
        	cache.setEvictionAlgorithm(algorithm);
        }
    }

    /**
     * Test the cache algorithm
     */
    public void testRemoveItem() {
        // Add 3 elements
    	algorithm.put(KEY, KEY);
    	algorithm.put(KEY + 1, KEY);
    	algorithm.put(KEY + 2, KEY);

        // Get the last element
    	algorithm.get(KEY, KEY);

        // The least recently used item is key + 1
        assertTrue((KEY + 1).equals(algorithm.evict()));
    }
}
