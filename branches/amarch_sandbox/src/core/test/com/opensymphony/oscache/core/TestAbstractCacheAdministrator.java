/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import com.opensymphony.oscache.algorithm.LRUEvictionAlgorithm;
import com.opensymphony.oscache.core.AbstractCacheAdministrator;
import com.opensymphony.oscache.core.MemoryCache;

import junit.framework.TestCase;

/**
 * Test class for the AbstractCacheAdministrator class. It tests some of the
 * public methods of the admin. Some others cannot be tested since they are
 * linked to the property file used for the tests, and since this file
 * will change, the value of some parameters cannot be asserted
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestAbstractCacheAdministrator extends TestCase {
    // Constants used in the tests
    private final String CACHE_PATH_PROP = "cache.path";
    private final String CONTENT = "Content for the abstract cache admin test";
    private final String ENTRY_KEY = "Test Abstract Admin Key";
    private final String INVALID_PROP_NAME = "INVALID_PROP_NAME";
    private final String TEST_LOG = "test log";

   

    public void testGetAndPut() {
    		Cache cache = new MemoryCache(5);
    		cache.setEvictionAlgorithm(new LRUEvictionAlgorithm()	);
    		Object value = cache.get(ENTRY_KEY);
    		assertNull(value);
    		cache.put(ENTRY_KEY, CONTENT);
    		value = cache.get(ENTRY_KEY);
    		assertEquals(value, CONTENT);    		
    }
    
    /**
     * Cannot be tested since CacheContents is an interface
     */
    public void testCacheContents() {
    }

    /**
     * We cannot test this method because the value depends on the property
     */
    public void testGetCachePath() {
    }



    /**
     * We cannot test this method because the value depends on the property
     */
    public void testIsFileCaching() {
    }

    /**
     * We cannot test this method because the value depends on the property
     */
    public void testIsMemoryCaching() {
    }

  

    // Abstract method that returns an instance of an admin
    protected Cache getAdmin() {
    		return new MemoryCache() {
    		};
    }
}
