/*
 * Copyright (c) 2002-2007 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * Test the Cache class for any concurrency problems
 *
 * $Id: TestConcurrency.java 404 2007-02-24 10:21:00Z larst $
 * @version        $Revision: 404 $
 */
public class TestConcurrency2 extends TestCase {

    private static transient final Log log = LogFactory.getLog(GeneralCacheAdministrator.class); //TestConcurrency2.class

    // Static variables required thru all the tests
    private static GeneralCacheAdministrator admin = null;

    // Constants needed in the tests
    private final String KEY = "key";
    private final String VALUE = "This is some content";
    private final int ITERATION_COUNT = 1000;
    private final int THREAD_COUNT = 3;
    private final int UNIQUE_KEYS = 1013;

    /**
     * Class constructor.
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestConcurrency2(String str) {
        super(str);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a new Cache
        if (admin == null) {
            Properties config = new Properties();
            config.setProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, "70");
            config.setProperty(AbstractCacheAdministrator.CACHE_BLOCKING_KEY, "false");
            admin = new GeneralCacheAdministrator(config);
            assertNotNull(admin);
        }
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestConcurrency2.class);
    }

    /**
     * Check that the cache handles simultaneous attempts to access a
     * new cache entry correctly
     */
    public void testNewEntry() {
        String key = "new";

        try {
            admin.getFromCache(key, -1);
            fail("NeedsRefreshException should have been thrown");
        } catch (NeedsRefreshException nre) {
            // Fire off another couple of threads to get the same cache entry
            GetEntry getEntry1 = new GetEntry(key, VALUE, -1, false);
            GetEntry getEntry2 = new GetEntry(key, VALUE, -1, false);

            // OK, those threads should be blocked waiting for the new cache
            // entry to appear. Sleep for a bit to simulate the time taken to
            // build the cache entry
            PutInCache putInCache = new PutInCache(key, VALUE, 500); 
            
            // pass that instance to the MTTR
            TestRunnable[] trs = {getEntry1, getEntry2, putInCache};
            MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);

            // kickstarts the MTTR & fires off threads
            try {
                mttr.runTestRunnables(5000);
            } catch (Throwable e) {
                fail("Thread should have blocked until a new cache entry was ready");
            }
        }
    }

    /**
     * Check that the cache handles simultaneous attempts to access a
     * new cache entry correctly
     */
    public void testNewEntryCancel() {
        final String key = "newCancel";
        final String NEW_VALUE = VALUE + "...";

        try {
            admin.getFromCache(key, -1);
            fail("NeedsRefreshException should have been thrown");
        } catch (NeedsRefreshException nre) {
            // Fire off another thread to get the same cache entry
            // We can't use GrobeUtils, because joining functionality is missing
            GetEntrySimple getEntry = new GetEntrySimple(key, NEW_VALUE, CacheEntry.INDEFINITE_EXPIRY, true);
            Thread thread = new Thread(getEntry);
            thread.start();

            // The above thread will be blocked waiting for the new content
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }

            // Now cancel the update (eg because an exception occurred while building the content).
            // This will unblock the other thread and it will receive a NeedsRefreshException.
            admin.cancelUpdate(key);

            // Wait a bit for the other thread to update the cache
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }

            try {
                Object newValue = admin.getFromCache(key, CacheEntry.INDEFINITE_EXPIRY);
                assertEquals(NEW_VALUE, newValue);
            } catch (NeedsRefreshException e) {
                admin.cancelUpdate(key);
                e.printStackTrace();
                fail("A NeedsRefreshException should not have been thrown. content=" + e.getCacheContent() + ", "+e.getMessage());
            }
        }
    }

    /**
     * Verify that we can concurrently access the cache without problems
     */
    public void testPut() {
        Thread[] thread = new Thread[THREAD_COUNT];

        for (int idx = 0; idx < THREAD_COUNT; idx++) {
            OSGeneralTest runner = new OSGeneralTest();
            thread[idx] = new Thread(runner);
            thread[idx].start();
        }

        boolean stillAlive;

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }

            stillAlive = false;

            int i = 0;

            while ((i < thread.length) && !stillAlive) {
                stillAlive |= thread[i++].isAlive();
            }
        } while (stillAlive);
    }

    /**
     * Check that the cache handles simultaneous attempts to access a
     * stale cache entry correctly
     */
    public void testStaleEntry() {
        String key = "stale";
        assertFalse("The cache should not be in blocking mode for this test.", admin.isBlocking());

        admin.putInCache(key, VALUE);

        try {
            // This should throw a NeedsRefreshException since the refresh
            // period is 0
            admin.getFromCache(key, 0);
            fail("NeedsRefreshException should have been thrown");
        } catch (NeedsRefreshException nre) {
            // Fire off another thread to get the same cache entry.
            // Since blocking mode is currently disabled we should
            // immediately get back the stale entry
            GetEntry getEntry = new GetEntry(key, VALUE, 0, false);
            Thread thread = new Thread(getEntry);
            thread.start();

            // Sleep for a bit to simulate the time taken to build the cache entry
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }

            // Putting the entry in the cache should mean that threads now retrieve
            // the updated entry
            String newValue = "New value";
            admin.putInCache(key, newValue);

            getEntry = new GetEntry(key, newValue, -1, false);
            thread = new Thread(getEntry);
            thread.start();

            try {
                Object fromCache = admin.getFromCache(key, -1);
                assertEquals(newValue, fromCache);
            } catch (NeedsRefreshException e) {
                admin.cancelUpdate(key);
                fail("Should not have received a NeedsRefreshException");
            }

            // Give the GetEntry thread a chance to finish
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * A test for the updating of a stale entry when CACHE.BLOCKING = TRUE
     */
    public void testStaleEntryBlocking() {
        // A test for the case where oscache.blocking = true
        admin.destroy();

        Properties p = new Properties();
        p.setProperty(AbstractCacheAdministrator.CACHE_BLOCKING_KEY, "true");
        admin = new GeneralCacheAdministrator(p);

        assertTrue("The cache should be in blocking mode for this test.", admin.isBlocking());

        // Use a unique key in case these test entries are being persisted
        String key = "blocking";
        String NEW_VALUE = VALUE + " abc";
        admin.putInCache(key, VALUE);

        try {
            // Force a NeedsRefreshException
            admin.getFromCache(key, 0);
            fail("NeedsRefreshException should have been thrown");
        } catch (NeedsRefreshException nre) {
            // Fire off another thread to get the same cache entry.
            // Since blocking mode is enabled this thread should block
            // until the entry has been updated.
            GetEntry getEntry = new GetEntry(key, NEW_VALUE, 0, false);
            Thread thread = new Thread(getEntry);
            thread.start();

            // Sleep for a bit to simulate the time taken to build the cache entry
            try {
                Thread.sleep(20);
            } catch (InterruptedException ie) {
            }

            // Putting the entry in the cache should mean that threads now retrieve
            // the updated entry
            admin.putInCache(key, NEW_VALUE);

            getEntry = new GetEntry(key, NEW_VALUE, -1, false);
            thread = new Thread(getEntry);
            thread.start();

            try {
                Object fromCache = admin.getFromCache(key, -1);
                assertEquals(NEW_VALUE, fromCache);
            } catch (NeedsRefreshException e) {
                admin.cancelUpdate(key);
                fail("Should not have received a NeedsRefreshException");
            }
        }
    }

    private static final int RETRY_BY_THREADS = 100000;
    private static final int NB_THREADS = 4;

    /**
     * Checks whether the cache handles simultaneous attempts to access a
     * stable cache entry correctly when the blocking mode is enabled.
     *
     * Basically N threads are concurrently trying to access a same stale cache entry and each is cancelling its update. Each thread repeat this operation M times.
     * The test is sucessfull if after some time, all threads are properly released
     */
    public void testConcurrentStaleGets() {
        GeneralCacheAdministrator staticAdmin = admin;
        //admin = new GeneralCacheAdministrator(); //avoid poluting other test cases

        try {
            // A test for the case where oscache.blocking = true
            //admin.destroy();
            Properties p = new Properties();
            p.setProperty(AbstractCacheAdministrator.CACHE_BLOCKING_KEY, "true");
            admin = new GeneralCacheAdministrator(p);

            assertTrue("The cache should be in blocking mode for this test.", admin.isBlocking());

            String key = "new";

            //First put a value
            admin.putInCache(key, VALUE);

            try {
                //Then test without concurrency that it is reported as stale when time-to-live is zero 
                admin.getFromCache(key, 0);
                fail("NeedsRefreshException should have been thrown");
            } catch (NeedsRefreshException nre) {
                //Ok this is was is excpected, we can release the update
                admin.cancelUpdate(key);
            }

            //Then ask N threads to concurrently try to access this stale resource and each should receive a NeedsRefreshException, and cancel the update
            TestRunnable[] spawnedThreads = new TestRunnable[NB_THREADS];

            for (int threadIndex = 0; threadIndex < NB_THREADS; threadIndex++) {
                spawnedThreads[threadIndex] = new GetStaleEntryAndCancelUpdate(key, 0, RETRY_BY_THREADS);
            }
            MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(spawnedThreads);
            
            //kickstarts the MTTR & fires off threads
            try {
                mttr.runTestRunnables(120 * 1000);
            } catch (Throwable e) {
                fail("at least one thread did not complete");
                e.printStackTrace();
            }
            
        } finally {
            // avoid poluting other test cases
            admin = staticAdmin;
        }
    }

    private class GetEntry extends TestRunnable {
        String key;
        String value;
        boolean expectNRE;
        int time;

        GetEntry(String key, String value, int time, boolean expectNRE) {
            this.key = key;
            this.value = value;
            this.time = time;
            this.expectNRE = expectNRE;
        }

        public void runTest() {
            try {
                // Get from the cache
                Object fromCache = admin.getFromCache(key, time);
                assertEquals(value, fromCache);
            } catch (NeedsRefreshException nre) {
                if (!expectNRE) {
                    admin.cancelUpdate(key);
                    fail("Thread should have blocked until a new cache entry was ready");
                } else {
                    // Put a new piece of content into the cache
                    admin.putInCache(key, value);
                }
            }
        }
    }
    
    private class GetEntrySimple extends GetEntry {
        GetEntrySimple(String key, String value, int time, boolean expectNRE) {
            super(key, value, time, expectNRE);
        }
        
        public void run() {
            runTest();
        }

    }

    private class PutInCache extends TestRunnable {

        String key;
        String value;
        long wait;

        PutInCache(String key, String value, long wait) {
            this.key = key;
            this.value = value;
            this.wait = wait;
        }

        public void runTest() {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ie) {
                fail("PutInCache thread shouldn't be interrupted.");
            }
            admin.putInCache(key, value);
        }
    }

    /**
      * Basically requests a stale entry, expects to receive a NeedsRefreshException, and always cancels the update.
      */
    private class GetStaleEntryAndCancelUpdate extends TestRunnable {
        String key;
        int retries;
        int time;

        GetStaleEntryAndCancelUpdate(String key, int time, int retries) {
            this.key = key;
            this.time = time;
            this.retries = retries;
        }

        public void runTest() {
            for (int retryIndex = 0; retryIndex < retries; retryIndex++) {
                try {
                    // Get from the cache
                    Object fromCache = admin.getFromCache(key, time);
                    assertNull("Thread index [" + retryIndex + "] expected stale request [" + retryIndex + "] to be received, got [" + fromCache + "]", fromCache);
                } catch (NeedsRefreshException nre) {
                    try {
                        admin.cancelUpdate(key);
                    } catch (Throwable t) {
                        log.error("Thread index [" + retryIndex + "]: Unexpectedly caught exception [" + t + "]", t);
                        fail("Thread index [" + retryIndex + "] : Unexpectedly caught exception [" + t + "]");
                    }
                } catch (Throwable t) {
                    log.error("Thread index [" + retryIndex + "] : Unexpectedly caught exception [" + t + "]", t);
                    fail("Thread index [" + retryIndex + "] : Unexpectedly caught exception [" + t + "]");
                }
            }
        }
    }

    private class OSGeneralTest extends TestRunnable {
        public void doit(int i) {
            int refreshPeriod = 500 /*millis*/;
            String key = KEY + (i % UNIQUE_KEYS);
            admin.putInCache(key, VALUE);

            try {
                // Get from the cache
                admin.getFromCache(KEY, refreshPeriod);
            } catch (NeedsRefreshException nre) {
                // Get the value
                // Store in the cache
                admin.putInCache(KEY, VALUE);
            }

            // Flush occasionally
            if ((i % (UNIQUE_KEYS + 1)) == 0) {
                admin.getCache().flushEntry(key);
            }
        }

        public void runTest() {
            int start = (int) (Math.random() * UNIQUE_KEYS);
            System.out.print(start + " ");

            for (int i = start; i < (start + ITERATION_COUNT); i++) {
                doit(i);
            }
        }
    }
    
    
}
