/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Properties;

/**
 * Test the Cache class for any concurrency problems
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:chris@chris.com">Chris Miller</a>
 */
public class TestConcurrency extends TestCase {
    // Static variables required thru all the tests
    private static GeneralCacheAdministrator admin = null;

    // Constants needed in the tests
    private final String KEY = "key";
    private final String VALUE = "This is some content";
    private final int ITERATION_COUNT = 50;
    private final int THREAD_COUNT = 30;
    private final int UNIQUE_KEYS = 15;

    /**
     * Class constructor.
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestConcurrency(String str) {
        super(str);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // At first invocation, create a new Cache
        if (admin == null) {
            admin = new GeneralCacheAdministrator();
            assertNotNull(admin);
        }
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestConcurrency.class);
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
            GetEntry getEntry = new GetEntry(key, VALUE, -1, false);
            Thread thread = new Thread(getEntry);
            thread.start();
            getEntry = new GetEntry(key, VALUE, -1, false);
            thread = new Thread(getEntry);
            thread.start();

            // OK, those threads should now be blocked waiting for the new cache
            // entry to appear. Sleep for a bit to simulate the time taken to
            // build the cache entry
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }

            // Putting the entry in the cache should unblock the previous threads
            admin.putInCache(key, VALUE);
        }
    }

    /**
     * Check that the cache handles simultaneous attempts to access a
     * new cache entry correctly
     */
    public void testNewEntryCancel() {
        String key = "newCancel";
        String NEW_VALUE = VALUE + "...";

        try {
            admin.getFromCache(key, -1);
            fail("NeedsRefreshException should have been thrown");
        } catch (NeedsRefreshException nre) {
            // Fire off another thread to get the same cache entry
            GetEntry getEntry = new GetEntry(key, NEW_VALUE, -1, true);
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
                Object newValue = admin.getFromCache(key, -1);
                assertEquals(NEW_VALUE, newValue);
            } catch (NeedsRefreshException e) {
                admin.cancelUpdate(key);
                fail("A NeedsRefreshException should not have been thrown");
            }
        }
    }

    /**
     * Verify that we can concurrently access the cache without problems
     */
    public void testPut() {
        Thread thread = null;

        for (int idx = 0; idx < THREAD_COUNT; idx++) {
            OSGeneralTest runner = new OSGeneralTest();
            thread = new Thread(runner);
            thread.start();
        }

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
        } while (thread.isAlive());
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
                Thread.sleep(200);
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

    private class GetEntry implements Runnable {
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

        public void run() {
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

    private class OSGeneralTest implements Runnable {
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

        public void run() {
            for (int i = 0; i < ITERATION_COUNT; i++) {
                doit(i);
            }
        }
    }
}
