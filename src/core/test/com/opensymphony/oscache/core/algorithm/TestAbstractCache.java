/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core.algorithm;

import com.opensymphony.oscache.core.BaseCache;
import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.EvictionAlgorithm;

import junit.framework.TestCase;

/**
 * Test class for the AbstractCache class. It tests all public methods of
 * the AbstractCache and assert the results. It is design to run under JUnit.
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public abstract class TestAbstractCache extends TestCase {
    /**
     * Invalid cache capacity
     */
    protected final int INVALID_MAX_ENTRIES = 0;

    /**
     * Cache capacity
     */
    protected final int MAX_ENTRIES = 3;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    protected TestAbstractCache(String str) {
        super(str);
    }

    /**
     * Test the method that verify if the cache contains a specific key
     */
    public abstract void testContainsKey();

    /**
     * Test the get from the cache
     */
    public abstract void testGet();

    /**
     * Test the capacity setting
     */
    public void testGetSetMaxEntries() {
    	getAlgorithm().setCapacity(MAX_ENTRIES);
        assertEquals(MAX_ENTRIES, getAlgorithm().getCapacity());

        // Specify an invalid capacity
        try {
        	getAlgorithm().setCapacity(INVALID_MAX_ENTRIES);
            fail("Cache capacity set with an invalid argument");
        } catch (Exception e) {
            // This is what we expect
        }
    }

    /**
     * Test the iterator retrieval
     */
    public abstract void testIterator();

    /**
     * Test the put into the cache
     */
    public abstract void testPut();

    /**
     * Test the remove from the cache
     */
    public abstract void testRemove();

    /**
     * Test the specific details about the cache algorithm
     */
    public abstract void testRemoveItem();


    // Abstract method that returns an instance of an admin
    protected abstract EvictionAlgorithm getAlgorithm();
}
