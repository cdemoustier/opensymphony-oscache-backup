/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core.algorithm;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.opensymphony.oscache.algorithm.FIFOEvictionAlgorithm;
import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.EvictionAlgorithm;
import com.opensymphony.oscache.core.MemoryCache;

/**
 * Test class for the FIFOCache class. It tests that the algorithm reacts as
 * expected when entries are removed
 * 
 * $Id$
 * 
 * @version $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestFIFOCache extends TestQueueCache {
	/**
	 * FIFO Cache object
	 */
	private static FIFOEvictionAlgorithm algorithm = null;

	/**
	 * Constructor
	 * <p>
	 * 
	 * @param str
	 *            The test name (required by JUnit)
	 */
	public TestFIFOCache(String str) {
		super(str);
	}

	/**
	 * This methods returns the name of this test class to JUnit
	 * <p>
	 * 
	 * @return The test for this class
	 */
	public static Test suite() {
		return new TestSuite(TestFIFOCache.class);
	}

	/**
	 * Abstract method used by the TestAbstractCache class
	 * <p>
	 * 
	 * @return A cache instance
	 */
	public EvictionAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * This method is invoked before each testXXXX methods of the class. It set
	 * ups the variables required for each tests.
	 */
	public void setUp() {
		// Create a cache instance on first invocation
		if (algorithm == null) {
			algorithm = new FIFOEvictionAlgorithm();
			assertNotNull(algorithm);
		}
	}

	/**
	 * Test the cache algorithm
	 */
	public void testRemoveItem() {
		// Add 2 elements in the cache and ensure that the one to remove is the
		// first
		// inserted
		algorithm.evaluatePut(KEY);
		algorithm.evaluatePut(KEY + 1);
		assertTrue(KEY.equals(algorithm.evict()));
	}
}
