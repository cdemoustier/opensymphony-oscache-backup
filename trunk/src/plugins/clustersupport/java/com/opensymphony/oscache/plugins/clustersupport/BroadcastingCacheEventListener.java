/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.*;
import com.opensymphony.oscache.base.events.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a CacheEntryEventListener. It broadcasts the flush events
 * to other listening caches. This allows caches to be clustered.
 *
 * @version        $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class BroadcastingCacheEventListener implements CacheEntryEventListener, LifecycleAware {
    private final static Log log = LogFactory.getLog(BroadcastingCacheEventListener.class);
    static ClusterManager cm;

    /**
     * Reference count to keep track of how many instances of this listener
     * are in existence. Once this listener is no longer used we can shut
     * down the cluster manager.
     */
    static int referenceCount = 0;

    public BroadcastingCacheEventListener() {
        log.info("BroadcastingCacheEventListener registered");
    }

    // --------------------------------------------------------
    // The remaining events are of no interest to this listener
    // --------------------------------------------------------
    public void cacheEntryAdded(CacheEntryEvent event) {
    }

    /**
     * Event fired when an entry is flushed from the cache. This broadcasts
     * the flush message to any listening nodes on the network.
     */
    public void cacheEntryFlushed(CacheEntryEvent event) {
        Cache cache = event.getMap();

        if ((cache.getName() != null) && !Cache.NESTED_EVENT.equals(event.getOrigin()) && !ClusterManager.CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cacheEntryFlushed called (" + event + ")");
            }

            cm.signalEntryFlush(event.getKey(), cache.getName());
        }
    }

    public void cacheEntryRemoved(CacheEntryEvent event) {
    }

    public void cacheEntryUpdated(CacheEntryEvent event) {
    }

    public void cacheGroupAdded(CacheGroupEvent event) {
    }

    public void cacheGroupEntryAdded(CacheGroupEvent event) {
    }

    public void cacheGroupEntryRemoved(CacheGroupEvent event) {
    }

    /**
     * Event fired when an entry is removed from the cache. This broadcasts
     * the remove method to any listening nodes on the network, as long as
     * this event wasn't from a broadcast in the first place. The
     */
    public void cacheGroupFlushed(CacheGroupEvent event) {
        Cache cache = event.getMap();

        if ((cache.getName() != null) && !Cache.NESTED_EVENT.equals(event.getOrigin()) && !ClusterManager.CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cacheGroupFushed called (" + event + ")");
            }

            cm.signalGroupFlush(event.getGroup(), cache.getName());
        }
    }

    public void cacheGroupRemoved(CacheGroupEvent event) {
    }

    public void cacheGroupUpdated(CacheGroupEvent event) {
    }

    public void cachePatternFlushed(CachePatternEvent event) {
        Cache cache = event.getMap();

        if ((cache.getName() != null) && !Cache.NESTED_EVENT.equals(event.getOrigin()) && !ClusterManager.CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cachePatternFushed called (" + event + ")");
            }

            cm.signalPatternFlush(event.getPattern(), cache.getName());
        }
    }

    public void cacheFlushed(CachewideEvent event) {
        Cache cache = event.getMap();

        if ((cache.getName() != null) && !Cache.NESTED_EVENT.equals(event.getOrigin()) && !ClusterManager.CLUSTER_ORIGIN.equals(event.getOrigin())) {
            if (log.isDebugEnabled()) {
                log.debug("cacheFushed called (" + event + ")");
            }

            cm.signalCacheFlush(cache.getName());
        }
    }

    /**
     * Shuts down the {@link ClusterManager} instance being managed by this listener
     * once this listener is no longer in use.
     *
     * @throws FinalizationException
     */
    public synchronized void finialize() throws FinalizationException {
        referenceCount--;

        if (referenceCount == 0) {
            cm.shutdown();
        }
    }

    /**
     * Initializes the broadcasting listener by creating a {@link ClusterManager}
     * instance to handle incoming and outgoing messages. If this listener is
     * used multiple times, it is reference-counted so we know when to shutdown
     * the <code>ClusterManagaer</code> again.
     *
     * @param config An OSCache configuration object.
     * @throws InitializationException If this listener has already been initialized.
     */
    public synchronized void initialize(AbstractCacheAdministrator admin, Config config) throws InitializationException {
        if (referenceCount == 0) {
            cm = new ClusterManager(config);
            cm.setAdministrator(admin);
        }

        referenceCount++;
    }
}
