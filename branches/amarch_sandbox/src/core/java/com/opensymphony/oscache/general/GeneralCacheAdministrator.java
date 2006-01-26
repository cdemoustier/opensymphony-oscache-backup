/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.general;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.oscache.core.AbstractCacheAdministrator;
import com.opensymphony.oscache.core.Cache;
import com.opensymphony.oscache.core.CacheEntry;
import com.opensymphony.oscache.core.EntryRefreshPolicy;
import com.opensymphony.oscache.core.MemoryCache;

/**
 * A GeneralCacheAdministrator creates, flushes and administers the cache.
 *
 * EXAMPLES :
 * <pre><code>
 * // ---------------------------------------------------------------
 * // Typical use with fail over
 * // ---------------------------------------------------------------
 * String myKey = "myKey";
 * String myValue;
 * int myRefreshPeriod = 1000;
 * try {
 *     // Get from the cache
 *     myValue = (String) admin.getFromCache(myKey, myRefreshPeriod);
 * } catch (NeedsRefreshException nre) {
 *     try {
 *         // Get the value (probably by calling an EJB)
 *         myValue = "This is the content retrieved.";
 *         // Store in the cache
 *         admin.putInCache(myKey, myValue);
 *     } catch (Exception ex) {
 *         // We have the current content if we want fail-over.
 *         myValue = (String) nre.getCacheContent();
 *         // It is essential that cancelUpdate is called if the
 *         // cached content is not rebuilt
 *         admin.cancelUpdate(myKey);
 *     }
 * }
 *
 *
 *
 * // ---------------------------------------------------------------
 * // Typical use without fail over
 * // ---------------------------------------------------------------
 * String myKey = "myKey";
 * String myValue;
 * int myRefreshPeriod = 1000;
 * try {
 *     // Get from the cache
 *     myValue = (String) admin.getFromCache(myKey, myRefreshPeriod);
 * } catch (NeedsRefreshException nre) {
 *     try {
 *         // Get the value (probably by calling an EJB)
 *         myValue = "This is the content retrieved.";
 *         // Store in the cache
 *         admin.putInCache(myKey, myValue);
 *         updated = true;
 *     } finally {
 *         if (!updated) {
 *             // It is essential that cancelUpdate is called if the
 *             // cached content could not be rebuilt
 *             admin.cancelUpdate(myKey);
 *         }
 *     }
 * }
 * // ---------------------------------------------------------------
 * // ---------------------------------------------------------------
 * </code></pre>
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class GeneralCacheAdministrator extends AbstractCacheAdministrator {
    private static transient final Log log = LogFactory.getLog(GeneralCacheAdministrator.class);

    /**
     * Create the cache administrator.
     */
    public GeneralCacheAdministrator() {
        this(null);
    }

    /**
     * Create the cache administrator with the specified properties
     */
    public GeneralCacheAdministrator(Properties p) {
        super(p);
        log.info("Constructed GeneralCacheAdministrator()");
        createCache();
    }


    /**
     * Shuts down the cache administrator.
     */
    public void destroy() {
//        finalizeListeners(applicationCache);
    }

    

    /**
     * Sets the cache capacity (number of items). If the cache contains
     * more than <code>capacity</code> items then items will be removed
     * to bring the cache back down to the new size.
     *
     * @param capacity The new capacity of the cache
     * @throws IllegalAccessException 
     */
    public void setCacheCapacity(int capacity)  {
        getCache().setCapacity(capacity);
    }

    /**
     * Creates a cache in this admin
     */
    private void createCache() {
        log.info("Creating new cache");

        Cache applicationCache = new MemoryCache(cacheCapacity);

        configureStandardListeners(applicationCache);
        addCache(DEFAULT_REGION, applicationCache);
    }

	
}
