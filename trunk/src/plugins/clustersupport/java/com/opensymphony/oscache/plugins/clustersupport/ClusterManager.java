/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.clustersupport;

import com.opensymphony.oscache.base.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.javagroups.Address;
import org.javagroups.Channel;

import org.javagroups.blocks.NotificationBus;

import java.io.Serializable;

/**
 * Handles the sending of notification messages across the cluster. This
 * implementation is based on the JavaGroups library.
 *
 * @version        $Revision$
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class ClusterManager implements NotificationBus.Consumer {
    private static final String BUS_NAME = "OSCacheBus";
    private static final String CHANNEL_PROPERTIES = "cache.cluster.properties";
    private static final String MULTICAST_IP_PROPERTY = "cache.cluster.multicast.ip";

    /**
     * The name to use for the origin of cluster events. Using this ensures
     * events are not fired recursively back over the cluster.
     */
    static final String CLUSTER_ORIGIN = "CLUSTER";

    /**
     * The first half of the default channel properties. They default channel properties are:
     * <pre>
     * UDP(mcast_addr=*.*.*.*;mcast_port=45566;ip_ttl=32;mcast_send_buf_size=150000;mcast_recv_buf_size=80000):PING(timeout=2000;num_initial_members=3):MERGE2(min_interval=5000;max_interval=10000):FD_SOCK:VERIFY_SUSPECT(timeout=1500):pbcast.STABLE(desired_avg_gossip=20000):pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):UNICAST(timeout=5000):FRAG(frag_size=8096;down_thread=false;up_thread=false):pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)
     * </pre>
     * Where <code>*.*.*.*</code> is the specified multicast IP, which defaults to <code>231.12.21.132</code>.
     */
    private static final String DEFAULT_CHANNEL_PROPERTIES_PRE = "UDP(mcast_addr=";

    /**
     * The second half of the default channel properties. They default channel properties are:
     * <pre>
     * UDP(mcast_addr=*.*.*.*;mcast_port=45566;ip_ttl=32;mcast_send_buf_size=150000;mcast_recv_buf_size=80000):PING(timeout=2000;num_initial_members=3):MERGE2(min_interval=5000;max_interval=10000):FD_SOCK:VERIFY_SUSPECT(timeout=1500):pbcast.STABLE(desired_avg_gossip=20000):pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):UNICAST(timeout=5000):FRAG(frag_size=8096;down_thread=false;up_thread=false):pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)
     * </pre>
     * Where <code>*.*.*.*</code> is the specified multicast IP, which defaults to <code>231.12.21.132</code>.
     */
    private static final String DEFAULT_CHANNEL_PROPERTIES_POST = ";mcast_port=45566;ip_ttl=32;mcast_send_buf_size=150000;mcast_recv_buf_size=80000):PING(timeout=2000;num_initial_members=3):MERGE2(min_interval=5000;max_interval=10000):FD_SOCK:VERIFY_SUSPECT(timeout=1500):pbcast.STABLE(desired_avg_gossip=20000):pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):UNICAST(timeout=5000):FRAG(frag_size=8096;down_thread=false;up_thread=false):pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";
    private static final String DEFAULT_MULTICAST_IP = "231.12.21.132";
    private final Log log = LogFactory.getLog(ClusterManager.class);
    private AbstractCacheAdministrator admin;
    private NotificationBus bus;
    private boolean shuttingDown = false;

    /**
     * Creates a <code>ClusterManager</code> instance using the supplied
     * configuration.
     */
    ClusterManager(Config config) throws InitializationException {
        String properties = config.getProperty(CHANNEL_PROPERTIES);
        String multicastIP = config.getProperty(MULTICAST_IP_PROPERTY);

        if ((properties == null) && (multicastIP == null)) {
            multicastIP = DEFAULT_MULTICAST_IP;
        }

        if (properties == null) {
            properties = DEFAULT_CHANNEL_PROPERTIES_PRE + multicastIP.trim() + DEFAULT_CHANNEL_PROPERTIES_POST;
        } else {
            properties = properties.trim();
        }

        if (log.isInfoEnabled()) {
            log.info("Starting a new ClusterManager with properties=" + properties);
        }

        try {
            bus = new NotificationBus(BUS_NAME, properties);
            bus.start();
            bus.getChannel().setOpt(Channel.LOCAL, new Boolean(false));
            bus.setConsumer(this);
            log.info("ClusterManager started successfully");
        } catch (Exception e) {
            throw new InitializationException("Initialization failed: " + e);
        }
    }

    /**
     * We are not using the caching, so we just return something that identifies
     * us. This method should never be called directly.
     */
    public Serializable getCache() {
        return "ClusterManager: " + bus.getLocalAddress();
    }

    /**
     * Handles incoming notification messages. This method should never be called
     * directly.
     *
     * @param serializable The incoming message object.
     */
    public void handleNotification(Serializable serializable) {
        if (!(serializable instanceof ClusterNotification)) {
            log.error("An unknown cluster notification message received (class=" + serializable.getClass().getName() + "). Notification ignored.");

            return;
        }

        ClusterNotification msg = (ClusterNotification) serializable;

        if (admin == null) {
            log.warn("Since no cache administrator has been specified for this ClusterManager, the cache named '" + msg.getCacheName() + "' cannot be retrieved. Cluster notification ignored.");
            return;
        }

        // Retrieve the named cache that this message applies to
        Cache cache = admin.getNamedCache(msg.getCacheName());

        if (cache == null) {
            log.warn("A cluster notification (" + msg + ") was received, but no matching cache is registered on this machine. Notification ignored.");

            return;
        }

        if (log.isInfoEnabled()) {
            log.info("Cluster notification (" + msg + ") was received.");
        }

        switch (msg.getType()) {
            case ClusterNotification.FLUSH_KEY:
                cache.flushEntry(msg.getData(), CLUSTER_ORIGIN);
                break;
            case ClusterNotification.FLUSH_GROUP:
                cache.flushGroup(msg.getData(), CLUSTER_ORIGIN);
                break;
            case ClusterNotification.FLUSH_PATTERN:
                cache.flushPattern(msg.getData(), CLUSTER_ORIGIN);
                break;
            default:
                log.error("The cluster notification (" + msg + ") is of an unknown type. Notification ignored.");
        }
    }

    /**
     * A callback that is fired when a new member joins the cluster. This
     * method should never be called directly.
     *
     * @param address The address of the member who just joined.
     */
    public void memberJoined(Address address) {
        log.info("A new member at address '" + address + "' has joined the cluster");
    }

    /**
     * A callback that is fired when an existing member leaves the cluster.
     * This method should never be called directly.
     *
     * @param address The address of the member who left.
     */
    public void memberLeft(Address address) {
        log.info("Member at address '" + address + "' left the cluster");
    }

    /**
     * Shuts down this <code>ClusterManager</code> instance. Care should be
     * taken to ensure that no more notification messages will be sent using
     * this instance once this method is called. The manager however is still
     * able to receive notification events from other nodes in the cluster
     * without any risk of problems.
     */
    void shutdown() {
        if (!shuttingDown) {
            shuttingDown = true;
            log.info("ClusterManager shutting down...");
            bus.stop();
            bus = null;
            log.info("ClusterManager shutdown complete.");
        }
    }

    /**
     * Broadcasts a flush message for the given cache entry.
     * place.
     *
     * @param key The object key to broadcast a flush notification message for
     * @param cacheName The name of the cache to flush
     */
    void signalEntryFlush(String key, String cacheName) {
        if (log.isDebugEnabled()) {
            log.debug("flushEntry called for cache '" + cacheName + "', key '" + key + "'");
        }

        if (!shuttingDown) {
            bus.sendNotification(new ClusterNotification(ClusterNotification.FLUSH_KEY, cacheName, key));
        }
    }

    /**
     * Broadcasts a flush message for the given cache group.
     *
     * @param group The group to broadcast a flush message for
     * @param cacheName The name of the cache to flush
     */
    void signalGroupFlush(String group, String cacheName) {
        if (log.isDebugEnabled()) {
            log.debug("flushGroup called for cache '" + cacheName + "', group '" + group + "'");
        }

        if (!shuttingDown) {
            bus.sendNotification(new ClusterNotification(ClusterNotification.FLUSH_GROUP, cacheName, group));
        }
    }

    /**
     * Broadcasts a flush message for the given cache pattern.
     *
     * @param pattern The pattern to broadcast a flush message for
     * @param cacheName The name of the cache to flush
     */
    void signalPatternFlush(String pattern, String cacheName) {
        if (log.isDebugEnabled()) {
            log.debug("flushPattern called for cache '" + cacheName + "', pattern '" + pattern + "'");
        }

        if (!shuttingDown) {
            bus.sendNotification(new ClusterNotification(ClusterNotification.FLUSH_PATTERN, cacheName, pattern));
        }
    }

    /**
     * Broadcasts a flush message for an entire cache
     *
     * @param cacheName The name of the cache to flush
     */
    void signalCacheFlush(String cacheName) {
        if (log.isDebugEnabled()) {
            log.debug("flushCache called for cache '" + cacheName + "'");
        }

        if (!shuttingDown) {
            bus.sendNotification(new ClusterNotification(ClusterNotification.FLUSH_CACHE, cacheName, null));
        }
    }

    /**
     * Sets the adminstrator for this cluster manager. We need this so we can
     * look up named caches.
     */
    public void setAdministrator(AbstractCacheAdministrator admin) {
        this.admin = admin;
    }
}
