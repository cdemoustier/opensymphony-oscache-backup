/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.general;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.base.NeedsRefreshException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Testing concurrent API accesses.
 * 
 * @author $Author: larst $
 * @version $Revision: 385 $
 */
public class TestConcurrent extends TestCase {
    
    private static transient final Log log = LogFactory.getLog(GeneralCacheAdministrator.class); //TestConcurrency.class

    // Static instance of a cache administrator
    private GeneralCacheAdministrator admin = null;

    // Constants needed in the tests
    private final String KEY = "ConcurrentKey";
    private final String VALUE = "ConcurrentContent";
    private static final int THREAD_COUNT = 5;
    private static final int CACHE_SIZE_THREAD = 2000;
    private static final int CACHE_SIZE = THREAD_COUNT * CACHE_SIZE_THREAD;

    public TestConcurrent(String str) {
        super(str);
    }

    /**
    * This methods returns the name of this test class to JUnit
    * <p>
    * @return The name of this class
    */
    public static Test suite() {
        return new TestSuite(TestConcurrent.class);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a new Cache
        if (admin == null) {
            Properties config = new Properties();
            config.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, Integer.toString(CACHE_SIZE));
            admin = new GeneralCacheAdministrator(config);
            assertNotNull(admin);
            log.info("Cache Size = " + admin.getCache().getSize());
        }
    }

    /**
    * Tests concurrent accesses.
    * @see http://jira.opensymphony.com/browse/CACHE-279
    */
    public void testConcurrentCreation10000() {
        Thread[] thread = new Thread[THREAD_COUNT];
        
        log.info("Ramping threads...");
        for (int idx = 0; idx < THREAD_COUNT; idx++) {
            CreationTest runner = new CreationTest(idx);
            thread[idx] = new Thread(runner);
            thread[idx].start();
        }

        log.info("Waiting....");
        boolean stillAlive;
        do {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // do nothing
            }

            stillAlive = false;
            for (int i = 0; i < thread.length; i++) {                
                stillAlive |= thread[i].isAlive();
            }
        } while (stillAlive);
        log.info("All threads finished. Cache Size = " + admin.getCache().getSize());
        
        assertTrue("Unexpected amount of objects in the cache: " + admin.getCache().getSize(), CACHE_SIZE == admin.getCache().getSize());
    }
 
    private class CreationTest implements Runnable {
        
        private String prefixKey;
        
        public CreationTest(int idx) {
            prefixKey = KEY + "_" + Integer.toString(idx) + "_";
            Thread.currentThread().setName("CreationTest-"+idx);
            log.info(Thread.currentThread().getName() + " is running...");
        }
        
        public void run() {
            for (int i = 0; i < CACHE_SIZE_THREAD; i++) {
                String key = prefixKey + Integer.toString(i);
                try {
                    // Get from the cache
                    admin.getFromCache(key);
                } catch (NeedsRefreshException nre) {
                    // Get the value
                    // Store in the cache
                    admin.putInCache(key, VALUE);
                }
            }
            log.info(Thread.currentThread().getName() + " finished.");
        }
    }

}
