/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.general;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.CacheEntry;
import com.opensymphony.oscache.core.DummyAlwayRefreshEntryPolicy;
import com.opensymphony.oscache.core.TestAbstractCacheAdministrator;
import com.opensymphony.oscache.extra.CacheEntryEventListenerImpl;
import com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl;

/**
 * Test all the public methods of the GeneralCacheAdministrator class. Since
 * this class extends the TestAbstractCacheAdministrator class, the
 * AbstractCacheAdministrator is tested when invoking this class.
 * 
 * $Id: TestGeneralCacheAdministrator.java 350 2006-01-26 06:48:46 +0000 (Thu,
 * 26 Jan 2006) dres $
 * 
 * @version $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class TestGeneralCacheAdministrator extends
		TestAbstractCacheAdministrator {
	// Constants used thru all the tests
	private static final String KEY = "Test General Cache Admin Key";

	private static final int NO_REFRESH_NEEDED = CacheEntry.INDEFINITE_EXPIRY;

	private static final int REFRESH_NEEDED = 0;

	private static final String CONTENT = "Content for the general cache admin test";

	private static final String GROUP1 = "group1";

	private static final String GROUP2 = "group2";

	private static final String GROUP3 = "group3";

	// Constants for listener counters
	private static final int NB_CACHE_HITS = 7;

	private static final int NB_CACHE_STALE_HITS = 7;

	private static final int NB_CACHE_MISSED = 1;

	private static final int NB_ADD = 7;

	private static final int NB_UPDATED = 2;

	private static final int NB_FLUSH = 4;

	private static final int NB_REMOVED = 0;

	private static final int NB_GROUP_FLUSH = 2;

	private static final int NB_PATTERN_FLUSH = 1;

	// Static instance of a cache administrator
	private Cache admin = null;

	// Declare the listeners
	private CacheEntryEventListenerImpl cacheEntryEventListener = null;

	private CacheMapAccessEventListenerImpl cacheMapAccessEventListener = null;

	/**
	 * Class constructor
	 * <p>
	 * 
	 * @param str
	 *            Test name (required by JUnit)
	 */
	public TestGeneralCacheAdministrator() {
	}

	/**
	 * Test suite required to test this project
	 * <p>
	 * 
	 * @return suite The test suite
	 */
	public static Test suite() {
		return new TestSuite(TestGeneralCacheAdministrator.class);
	}

	/**
	 * Abstract method used by the TestAbstractCacheAdministrator class
	 * <p>
	 * 
	 * @return An administrator instance
	 */
	public Cache getAdmin() {
		return admin;
	}

	/**
	 * This method is invoked before each testXXXX methods of the class. It set
	 * ups the variables required for each tests.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void setUp() throws FileNotFoundException, IOException {
		// At first invocation, create a administrator
		Properties props = new Properties();
		props.load(new FileInputStream(
				"src/core/test/oscacheMemoryOnly.properties"));
		admin = new GeneralCacheAdministrator(props).getCache();
		assertNotNull(admin);
		cacheEntryEventListener = new CacheEntryEventListenerImpl();
		cacheMapAccessEventListener = new CacheMapAccessEventListenerImpl();

		// Register the listeners on the cache map
		admin.addCacheListener(cacheEntryEventListener);
		admin.addCacheListener(cacheMapAccessEventListener);
	}

	/**
	 * Validate the CacheEntryEventListener's data
	 */
	public void testCacheEntryEventListenerCounters() {
		populate();
		assertEquals(NB_ADD, cacheEntryEventListener.getEntryAddedCount());
		assertEquals(NB_REMOVED, cacheEntryEventListener.getEntryRemovedCount());
		assertEquals(NB_UPDATED, cacheEntryEventListener.getEntryUpdatedCount());
		assertEquals(NB_GROUP_FLUSH, cacheEntryEventListener
				.getGroupFlushedCount());
		assertEquals(NB_FLUSH, cacheEntryEventListener.getEntryFlushedCount());
	}

	/**
	 * Validate the CacheEntryEventListener's data
	 */
	public void testCacheMapAccessEventListenerCounters() {
		populate();

		int missCount = cacheMapAccessEventListener.getMissCount();

		if (NB_CACHE_MISSED != missCount) {
			fail("We expected "
					+ NB_CACHE_MISSED
					+ " misses but got "
					+ missCount
					+ "."
					+ " This is probably due to existing disk cache, delete it and re-run"
					+ " the test");
		}

		assertEquals(NB_CACHE_HITS, cacheMapAccessEventListener.getHitCount());
		assertEquals(NB_CACHE_STALE_HITS, cacheMapAccessEventListener
				.getStaleHitCount());
	}

	/**
	 * Ensure that the cache groupings work correctly
	 */
	public void testGroups() {
		// Flush a non-existent group - should be OK and will still fire a
		// GROUP_FLUSHED event
		admin.flushGroup(GROUP1);

		// Add some items to various group combinations
		admin.put("1", "item 1"); // No groups
		admin.put("2", "item 2", new String[] { GROUP1 }); // Just group 1
		admin.put("3", "item 3", new String[] { GROUP2 }); // Just group 2
		admin.put("4", "item 4", new String[] { GROUP1, GROUP2 }); // groups 1
		// & 2
		admin.put("5", "item 5", new String[] { GROUP1, GROUP2, GROUP3 }); // groups
		// 1,2
		// & 3

		admin.flushGroup(GROUP3); // This should flush item 5 only
		assertNotNull(checkObj("5", NO_REFRESH_NEEDED, true));
		assertNotNull(checkObj("4", NO_REFRESH_NEEDED, false));

		admin.flushGroup(GROUP2); // This should flush items 3 and 4
		assertNotNull(checkObj("1", NO_REFRESH_NEEDED, false));
		assertNotNull(checkObj("2", NO_REFRESH_NEEDED, false));
		assertNotNull(checkObj("3", NO_REFRESH_NEEDED, true));
		assertNotNull(checkObj("4", NO_REFRESH_NEEDED, true));

		admin.flushGroup(GROUP1); // Flushes item 2
		assertNotNull(checkObj("1", NO_REFRESH_NEEDED, false));
		assertNotNull(checkObj("2", NO_REFRESH_NEEDED, true));

		// Test if regrouping a cache entry works
		admin.put("A", "ABC", new String[] { "A" });
		admin.put("A", "ABC", new String[] { "A", "B" });
		admin.put("B", "DEF", new String[] { "B" });
		admin.flushGroup("B");
		assertNotNull(checkObj("A", NO_REFRESH_NEEDED, true));
	}

	/**
	 * Test the main cache functionalities, which are storing and retrieving
	 * objects from it
	 */
	public void testputAndGetFromCache() {
		// Put some item in cache and get it back right away. It should not need
		// to be refreshed
		admin.put(KEY, CONTENT);

		String cacheContent = (String) checkObj(KEY, NO_REFRESH_NEEDED, false);
		assertTrue(CONTENT.equals(cacheContent));

		// Get the item back again and expect a refresh
		cacheContent = (String) checkObj(KEY, REFRESH_NEEDED, true);
		assertTrue(CONTENT.equals(cacheContent));

		admin.put(KEY, null); // This will still update the cache - cached
		// items can be null

		// Call the get with invalid values
		invalidGetArgument(null, 0);

		// Try to retrieve the values
		assertNull(checkObj(KEY, NO_REFRESH_NEEDED, false));

		// Try to retrieve an item that is not in the cache
		Object obj = checkObj("Not in cache", NO_REFRESH_NEEDED, true);
		assertNull(obj);
	}

	/**
	 * Test the main cache functionalities, which are storing and retrieving
	 * objects from it
	 */
	public void testputAndGetFromCacheWithPolicy() {
		String key = "policy";

		// We put content in the cache and get it back
		admin.put(key, CONTENT, new DummyAlwayRefreshEntryPolicy());

		CacheEntry entry = admin.getEntry(key);

		assertTrue("Should have got a refresh.", entry.needsRefresh(-1));

	}

	protected void tearDown() throws Exception {
		if (admin != null) {
			admin.removeCacheListener(cacheEntryEventListener);
			admin.removeCacheListener(cacheMapAccessEventListener);
		}
	}

	/**
	 * Utility method that tries to get an item from the cache and verify if all
	 * goes as expected
	 * <p>
	 * 
	 * @param key
	 *            The item key
	 * @param refresh
	 *            The timestamp specifiying if the item needs refresh
	 * @param exceptionExpected
	 *            Specify if we expect a NeedsRefreshException
	 */
	private Object checkObj(String key, int refresh, boolean exceptionExpected) {
		// Cache entry
		CacheEntry entry = null;

		// try to find an object
		entry = admin.getEntry(key);

		if (entry != null) {
			if ((entry.needsRefresh(refresh) && !exceptionExpected)
					|| (exceptionExpected && !entry.needsRefresh(refresh))) {
				fail("Expected NeedsRefreshException!");
			}

			return entry.getContent();
		}
		return null;

	}

	/**
	 * Method that try to retrieve data from the cache but specify wrong
	 * arguments
	 * <p>
	 * 
	 * @param key
	 *            The cache item key
	 * @param refresh
	 *            The timestamp specifiying if the item needs refresh
	 */
	private void invalidGetArgument(String key, int refresh) {
		CacheEntry entry = admin.getEntry(key);

		if (entry != null && !entry.needsRefresh(refresh)) {
			fail("Expected NeedsRefreshException!");
		}
	}

	private void populate() {
		for (int i = 0; i < 7; i++) {
			String[] groups = ((i & 1) == 0) ? new String[] { GROUP1, GROUP2 }
					: new String[] { GROUP3 };
			admin.put(KEY + i, CONTENT + i, groups);
		}

		// register one miss.
		checkObj("Not in cache", NO_REFRESH_NEEDED, true);

		// register 7 hits
		for (int i = 0; i < 7; i++) {

			admin.get(KEY + i, NO_REFRESH_NEEDED);

		}

		for (int i = 0; i < 7; i++) {

			admin.get(KEY + i, 0);

		}

		admin.put(KEY + 1, CONTENT);
		admin.put(KEY + 2, CONTENT);
		admin.flushGroup(GROUP1);
		admin.flushGroup(GROUP2);
	}
}
