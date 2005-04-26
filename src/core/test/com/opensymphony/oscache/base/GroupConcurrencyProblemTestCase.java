package com.opensymphony.oscache.base;

import junit.framework.TestCase;

import com.opensymphony.oscache.general.GeneralCacheAdministrator;

public class GroupConcurrencyProblemTestCase extends TestCase {
	private static GeneralCacheAdministrator cache = new GeneralCacheAdministrator();

	public static void main( String[] args ) {
		System.out.println( "START" );

		// Create some clients and start them running.
		for ( int i = 0; i < 100; i++ ) {
			System.out.println( "Creating thread: " + i );

			new Client( i, cache ).start();
		}

		System.out.println( "END" );
	}
}

/* Inner class to hammer away at the cache. */
class Client extends Thread {
	private static final int MAX_ITERATIONS = 1000;

	private int id;
	private GeneralCacheAdministrator cache;

	public Client(
		int newId,
		GeneralCacheAdministrator newCache
	) {
		super();
		id = newId;
		cache = newCache;
	}

	public void run() {
		for ( int i = 0; i < MAX_ITERATIONS; i++ ) {

			/* Put an entry from this Client into the shared group.
			 */
			cache.putInCache(
				Integer.toString( id ),
				"Some interesting data",
				new String[] { "GLOBAL_GROUP" }
			);

			// Flush that group.
			cache.flushGroup( "GLOBAL_GROUP" );
		}
	}
}
