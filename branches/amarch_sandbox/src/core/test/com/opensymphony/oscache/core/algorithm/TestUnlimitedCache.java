/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core.algorithm;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.opensymphony.oscache.algorithm.UnlimitedEvictionAlgorithm;
import com.opensymphony.oscache.core.BaseCache;
import com.opensymphony.oscache.core.EvictionAlgorithm;
import com.opensymphony.oscache.core.MemoryCache;

/**
 * Test class for the Unlimited cache algorithm. Most of the tests are done
 * in the TestNonQueueCache class, so only algorithm specific tests are done
 * here. Since this is an unlimited cache, there's not much to test about
 * the algorithm.
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestUnlimitedCache extends TestQueueCache {
    /**
     * Unlimited Cache object
     */
    private static MemoryCache cache = null;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestUnlimitedCache(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The test for this class
     */
    public static Test suite() {
        return new TestSuite(TestUnlimitedCache.class);
    }

    /**
     * Abstract method used by the TestAbstractCache class
     * <p>
     * @return  A cache instance
     */
    public BaseCache getCache() {
        return cache;
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // Create a cache instance on first invocation
        if (cache == null) {
            cache = new MemoryCache();
            cache.setEvictionAlgorithm(new UnlimitedEvictionAlgorithm());
            assertNotNull(cache);
        }
    }

    /**
     * Test the getter and setter for the max entries. It overrides the TestQueueCache
     * one since it shouldn't have any effect in unlimited cache
     */
    public void testGetSetMaxEntries() {
        // Check that the max entries cannot be changed
        int entryCount = getCache().getCapacity();
        getCache().setCapacity(entryCount - 1);
        assertEquals(entryCount, getCache().getCapacity());
    }

    /**
     * Test the cache algorithm
     */
    public void testRemoveItem() {
        // Add an item, and ensure that it is not removable
    	getAlgorithm().evaluatePut(KEY);
        assertNull(getAlgorithm().evict());
    }

	@Override
	protected EvictionAlgorithm getAlgorithm() {
		// TODO Auto-generated method stub
		return cache.getEvictionAlgorithm();
	}
}
