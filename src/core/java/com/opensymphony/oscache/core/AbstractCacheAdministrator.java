/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.oscache.events.CacheEntryEventListener;
import com.opensymphony.oscache.events.CacheListener;
import com.opensymphony.oscache.events.CacheMapAccessEventListener;
import com.opensymphony.oscache.util.StringUtil;

/**
 * An AbstractCacheAdministrator defines an abstract cache administrator,
 * implementing all the basic operations related to the configuration of a
 * cache, including assigning any configured event handlers to cache objects.
 * <p>
 * 
 * Extend this class to implement a custom cache administrator.
 * 
 * @version $Revision$
 * @author a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="mailto:fabian.crabus@gurulogic.de">Fabian Crabus</a>
 * @author <a
 *         href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris
 *         Miller</a>
 */
public abstract class AbstractCacheAdministrator implements
		java.io.Serializable {
	public static final String DEFAULT_REGION = "DEFAULT_REGION";

	private static transient final Log log = LogFactory
			.getLog(AbstractCacheAdministrator.class);

	/**
	 * A configuration parameter that specifies the initial size of the thread
	 * pool used for handling asynchronous cache laods. This only needs to be
	 * specified if the cache has an asynchronous cache loader that doesn't
	 * implement {@link ConnectorService}.
	 */
	public static final String MIN_THREADS_PARAM = "minThreads";

	/**
	 * A configuration parameter that specifies the maximum size of the thread
	 * pool used for handling asynchronous cache laods. This only needs to be
	 * specified if the cache has an asynchronous cache loader that doesn't
	 * implement {@link ConnectorService}.
	 */
	public static final String MAX_THREADS_PARAM = "maxThreads";

	private static final int DEFAULT_MIN_LOADER_THREADS = 0;

	private static final int DEFAULT_MAX_LOADER_THREADS = 10;

	private int minThreads = DEFAULT_MIN_LOADER_THREADS;

	private int maxThreads = DEFAULT_MAX_LOADER_THREADS;

	/**
	 * An integer cache configuration property that specifies the maximum number
	 * of objects to hold in the cache. Setting this to a negative value will
	 * disable the capacity functionality - there will be no limit to the number
	 * of objects that are held in cache.
	 */
	public final static String CACHE_CAPACITY_KEY = "cache.capacity";

	/**
	 * A String cache configuration property that specifies the classname of an
	 * alternate caching algorithm. This class must extend
	 * {@link com.opensymphony.oscache.core.algorithm.AbstractConcurrentReadCache}
	 * By default caches will use
	 * {@link com.opensymphony.oscache.core.algorithm.LRUCache} as the default
	 * algorithm if the cache capacity is set to a postive value, or
	 * {@link com.opensymphony.oscache.core.algorithm.UnlimitedCache} if the
	 * capacity is negative (ie, disabled).
	 */
	public final static String CACHE_ALGORITHM_KEY = "cache.algorithm";

	/**
	 * A String cache configuration property that holds a comma-delimited list
	 * of classnames. These classes specify the event handlers that are to be
	 * applied to the cache.
	 */
	public static final String CACHE_LISTENERS_KEY = "cache.listeners";

	protected Config config = null;

	/**
	 * Holds a list of all the registered listeners. listeners are specified
	 * using the {@link #CACHE_LISTENERS_KEY} configuration key.
	 */
	private List listeners;

	/**
	 * The algorithm class being used, as specified by the
	 * {@link #CACHE_ALGORITHM_KEY} configuration property.
	 */
	protected String algorithmClass = null;

	/**
	 * The cache capacity (number of entries), as specified by the
	 * {@link #CACHE_CAPACITY_KEY} configuration property.
	 */
	protected int cacheCapacity = -1;

	private Map regions = new HashMap();

	/**
	 * Create the AbstractCacheAdministrator. This will initialize all values
	 * and load the properties from oscache.properties.
	 */
	protected AbstractCacheAdministrator() {
		this(null);
	}

	/**
	 * Create the AbstractCacheAdministrator.
	 * 
	 * @param p
	 *            the configuration properties for this cache.
	 */
	protected AbstractCacheAdministrator(Properties p) {
		loadProps(p);
		initCacheParameters();

		if (log.isDebugEnabled()) {
			log.debug("Constructed AbstractCacheAdministrator()");
		}
	}

	private Cache getCache(String regionName) {
		
		return (Cache) regions.get(regionName);
	}


	/**
	 * Sets the algorithm to use for the cache.
	 * 
	 * @see com.opensymphony.oscache.core.algorithm.LRUCache
	 * @see com.opensymphony.oscache.core.algorithm.FIFOCache
	 * @see com.opensymphony.oscache.core.algorithm.UnlimitedCache
	 * @param newAlgorithmClass
	 *            The class to use (eg.
	 *            <code>"com.opensymphony.oscache.core.algorithm.LRUCache"</code>)
	 */
	public void setAlgorithmClass(String newAlgorithmClass) {
		algorithmClass = newAlgorithmClass;
	}

	/**
	 * Retrieves an array containing instances all of the {@link CacheListener}
	 * classes that are specified in the OSCache configuration file.
	 */
	protected void initCacheListeners() {		

		List classes = StringUtil.split(
				config.getProperty(CACHE_LISTENERS_KEY), ',');
		listeners = new ArrayList();

		for (int i = 0; i < classes.size(); i++) {
			String className = (String) classes.get(i);

			try {
				Class clazz = Class.forName(className);

				if (!CacheListener.class.isAssignableFrom(clazz)) {
					log
							.error("Specified listener class '"
									+ className
									+ "' does not implement CacheListener. Ignoring this listener.");
				} else {
					listeners.add(clazz.newInstance());
				}
			} catch (ClassNotFoundException e) {
				log.error("CacheListener class '" + className
						+ "' not found. Ignoring this listener.", e);
			} catch (InstantiationException e) {
				log
						.error(
								"CacheListener class '"
										+ className
										+ "' could not be instantiated because it is not a concrete class. Ignoring this listener.",
								e);
			} catch (IllegalAccessException e) {
				log
						.error(
								"CacheListener class '"
										+ className
										+ "' could not be instantiated because it is not public. Ignoring this listener.",
								e);
			}
		}
	}

	/**
	 * Applies all of the recognised listener classes to the supplied cache
	 * object. Recognised classes are {@link CacheEntryEventListener} and
	 * {@link CacheMapAccessEventListener}.
	 * <p>
	 * 
	 * @param cache
	 *            The cache to apply the configuration to.
	 * @return cache The configured cache object.
	 */
	protected Cache configureStandardListeners(Cache cache) {

		if (config.getProperty(CACHE_LISTENERS_KEY) != null) {
			// Grab all the specified listeners and add them to the cache's
			// listener list. Note that listeners that implement more than
			// one of the event interfaces will be added multiple times.
			initCacheListeners();

			for (int i = 0; i < listeners.size(); i++) {
				// Pass through the configuration to those listeners that
				// require it
				if (listeners.get(i) instanceof LifecycleAware) {
					try {
						((LifecycleAware) listeners.get(i)).initialize(cache,
								config);
					} catch (InitializationException e) {
						log.error("Could not initialize listener '"
								+ listeners.get(i).getClass().getName()
								+ "'. Listener ignored.", e);

						continue;
					}
				}
				cache.addCacheListener((CacheListener) listeners.get(i));

			}
		}

		return cache;
	}

	/**
	 * Finalizes all the listeners that are associated with the given cache
	 * object. Any <code>FinalizationException</code>s that are thrown by the
	 * listeners will be caught and logged.
	 */
	protected void finalizeListeners(Cache cache) {
		// It's possible for cache to be null if getCache() was never called
		// (CACHE-63)
		if (cache == null) {
			return;
		}

		
	}

	/**
	 * Initialize the core cache parameters from the configuration properties.
	 * The parameters that are initialized are:
	 * <ul>
	 * <li>the algorithm class ({@link #CACHE_ALGORITHM_KEY})</li>
	 * <li>the cache size ({@link #CACHE_CAPACITY_KEY})</li>
	 * <li>whether the cache is blocking or non-blocking ({@link #CACHE_BLOCKING_KEY})</li>
	 * <li>whether caching to memory is enabled ({@link #CACHE_MEMORY_KEY})</li>
	 * <li>whether the persistent cache is unlimited in size ({@link #CACHE_DISK_UNLIMITED_KEY})</li>
	 * </ul>
	 */
	private void initCacheParameters() {
		algorithmClass = config.getProperty(CACHE_ALGORITHM_KEY);

		String cacheSize = config.getProperty(CACHE_CAPACITY_KEY);

		try {
			if ((cacheSize != null) && (cacheSize.length() > 0)) {
				cacheCapacity = Integer.parseInt(cacheSize);
			}
		} catch (NumberFormatException e) {
			log
					.error("The value supplied for the cache capacity, '"
							+ cacheSize
							+ "', is not a valid number. The cache capacity setting is being ignored.");
		}
	}

	/**
	 * Load the properties file from the classpath.
	 */
	private void loadProps(Properties p) {
		config = new Config(p);
	}

	/**
	 * Grabs a cache
	 * 
	 * @return The cache
	 * @throws IllegalAccessException 
	 */
	public Cache getCache() throws RuntimeException {
		if (regions.size() != 1) throw new RuntimeException("More than 1 region configured.  Please use getCache(String regionName)");
		return (Cache) regions.get(DEFAULT_REGION);
	}
	
	public void addCache(String region, Cache cache) {
		regions.put(region, cache);
		
	}
}
