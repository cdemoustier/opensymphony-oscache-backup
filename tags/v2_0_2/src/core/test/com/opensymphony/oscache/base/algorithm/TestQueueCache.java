/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import java.util.Iterator;

/**
 * Test class for the QueueCache class, which is the base class for FIFO
 * and LIFO algorithm classes. All the public methods of QueueCache are tested
 * here.
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public abstract class TestQueueCache extends TestAbstractCache {
    /**
     * Entry content
     */
    protected final String CONTENT = "Test Queue Cache content";

    /**
     * Entry key
     */
    protected final String KEY = "Test Queue Cache key";

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestQueueCache(String str) {
        super(str);
    }

    /**
     * Test the specific algorithms
     */
    public abstract void testRemoveItem();

    /**
     * Test the clear
     */
    public void testClear() {
        getCache().clear();
        assertEquals(0, getCache().size());
    }

    /**
     * Test the ContainsKey method
     */
    public void testContainsKey() {
        getCache().put(KEY, CONTENT);
        assertTrue(getCache().containsKey(KEY));
        getCache().clear();
    }

    /**
     * Test the get method
     */
    public void testGet() {
        // Add an entry and verify that it is there
        getCache().put(KEY, CONTENT);
        assertTrue(getCache().get(KEY).equals(CONTENT));

        // Call with invalid parameters
        try {
            getCache().get(null);
            fail("Get called with null parameters!");
        } catch (Exception e) { /* This is what we expect */
        }

        getCache().clear();
    }

    /**
     * Test the getter and setter for the max entries
     */
    public void testGetSetMaxEntries() {
        // Check that the cache is full, then chop it by one and assert that
        // an element has been removed
        for (int count = 0; count < MAX_ENTRIES; count++) {
            getCache().put(KEY + count, CONTENT + count);
        }

        assertEquals(MAX_ENTRIES, getCache().size());
        getCache().setMaxEntries(MAX_ENTRIES - 1);
        assertEquals(MAX_ENTRIES - 1, getCache().getMaxEntries());
        assertEquals(MAX_ENTRIES - 1, getCache().size());

        // Specify an invalid capacity
        try {
            getCache().setMaxEntries(INVALID_MAX_ENTRIES);
            fail("Cache capacity set with an invalid argument");
        } catch (Exception e) {
            // This is what we expect
        }

        getCache().clear();
    }

    /**
     * Test the iterator
     */
    public void testIterator() {
        // Verify that the iterator returns MAX_ENTRIES and no more elements
        int nbEntries = getCache().size();
        Iterator iterator = getCache().entrySet().iterator();
        assertNotNull(iterator);

        for (int count = 0; count < nbEntries; count++) {
            assertNotNull(iterator.next());
        }

        assertTrue(!iterator.hasNext());
    }

    /**
     * Test the put method
     */
    public void testPut() {
        // Put elements in cache
        for (int count = 0; count < MAX_ENTRIES; count++) {
            getCache().put(KEY + count, CONTENT + count);
        }

        // Call with invalid parameters
        try {
            getCache().put(null, null);
            fail("Put called with null parameters!");
        } catch (Exception e) { /* This is what we expect */
        }

        getCache().clear();
    }

    /**
     * Test the remove from cache
     */
    public void testRemove() {
        getCache().put(KEY, CONTENT);

        // Remove the object and assert the return
        assertNotNull(getCache().remove(KEY));
        getCache().clear();
    }
}
