/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base.algorithm;

import com.opensymphony.oscache.base.CacheImpl;
import com.opensymphony.oscache.util.ClassLoaderUtil;

import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * <p>LRU (Least Recently Used) algorithm for the cache.</p>
 *
 * <p>This class tries to provide the best possible performance by first
 * attempting to use the JDK 1.4.x <code>LinkedHashSet</code> class,
 * followed by the Jakarta commons-collections <code>SequencedHashMap</code>
 * class, and finally resorting to the <code>LinkedList</code> class if
 * neither of the above classes are available. If this class has to revert
 * to using a <code>LinkedList</code> a warning is logged since the performance
 * penalty can be severe.</p>
 *
 * <p>No synchronization is required in this class since the
 * <code>AbstractConcurrentReadCache</code> already takes care of any
 * synchronization requirements.</p>
 *
 * @version        $Revision$
 * @author <a href="mailto:salaman@teknos.com">Victor Salaman</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class LRUCache extends CacheImpl {
    private static final Log log = LogFactory.getLog(LRUCache.class);

    /**
     * Cache queue containing all cache keys.
     */
    private Collection list;

    /**
     * Jakarta commons collections unfortunately doesn't provide us with an ordered
     * hash set, so we have to use their ordered map instead.
     */
    private Map map;

    /**
     * A flag indicating if we are using a List for the key collection. This happens
     * when we're running under JDK 1.3 or lower and there is no commons-collections
     * in the classpath.
     */
    private boolean isList = false;

    /**
     * A flag indicating if we are using a Map for the key collection. This happens
     * when we're running under JDK 1.3 and commons-collections is available.
     */
    private boolean isMap = false;

    /**
     * A flag indicating if we are using a Set for the key collection. This happens
     * when we're running under JDK 1.4 and is the best case scenario.
     */
    private boolean isSet = false;

    /**
     * A flag indicating whether there is a removal operation in progress.
     */
    private volatile boolean removeInProgress = false;

    /**
     * Constructs an LRU Cache.
     */
    public LRUCache() {
        super();

        // Decide if we're running under JRE 1.4+. If so we can use a LinkedHashSet
        // instead of a LinkedList for a big performance boost when removing elements.
        try {
            ClassLoaderUtil.loadClass("java.util.LinkedHashSet", this.getClass());
            list = new LinkedHashSet();
            isSet = true;
        } catch (ClassNotFoundException e) {
            // There's no LinkedHashSet available so we'll try for the jakarta-collections
            // SequencedHashMap instead [CACHE-47]
            try {
                ClassLoaderUtil.loadClass("org.apache.commons.collections.SequencedHashMap", this.getClass());
                map = new SequencedHashMap();
                isMap = true;
            } catch (ClassNotFoundException e1) {
                // OK, time to get all inefficient and resort to a LinkedList. We log this
                // as a warning since it potentially can have a big impact.
                log.warn("When using the LRUCache under JRE 1.3.x, commons-collections.jar should be added to your classpath to increase OSCache's performance.");
                list = new LinkedList();
                isList = true;
            }
        }
    }

    /**
     * Constructors a LRU Cache of the specified capacity.
     *
     * @param capacity The maximum cache capacity.
     */
    public LRUCache(int capacity) {
        super(capacity);
    }

    /**
     * An item was retrieved from the list. The LRU implementation moves
     * the retrieved item's key to the front of the list.
     *
     * @param key The cache key of the item that was retrieved.
     */
    protected void itemRetrieved(Object key) {
        // Prevent list operations during remove
        while (removeInProgress) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
            }
        }

        // We need to synchronize here because AbstractConcurrentReadCache
        // doesn't prevent multiple threads from calling this method simultaneously.
        if (isMap) {
            synchronized (map) {
                map.remove(key);
                map.put(key, Boolean.TRUE);
            }
        } else {
            synchronized (list) {
                list.remove(key);
                list.add(key);
            }
        }
    }

    /**
     * An object was put in the cache. This implementation adds/moves the
     * key to the end of the list.
     *
     * @param key The cache key of the item that was put.
     */
    protected void itemPut(Object key) {
        // Since this entry was just accessed, move it to the back of the list.
        if (isMap) {
            synchronized (map) { // A further fix for CACHE-44
                map.remove(key);
                map.put(key, Boolean.TRUE);
            }
        } else {
            synchronized (list) { // A further fix for CACHE-44
                list.remove(key);
                list.add(key);
            }
        }
    }

    /**
     * An item needs to be removed from the cache. The LRU implementation
     * removes the first element in the list (ie, the item that was least-recently
     * accessed).
     *
     * @return The key of whichever item was removed.
     */
    protected Object removeItem() {
        removeInProgress = true;

        Object toRemove;

        try {
            toRemove = removeFirst();
        } catch (Exception e) {
            // List is empty.
            // this is theorically possible if we have more than the size concurrent
            // thread in getItem. Remove completed but add not done yet.
            // We simply wait for add to complete.
            do {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ie) {
                }
            } while (isMap ? (map.size() == 0) : (list.size() == 0));

            toRemove = removeFirst();
        }

        removeInProgress = false;

        return toRemove;
    }

    /**
     * Remove specified key since that object has been removed from the cache.
     *
     * @param key The cache key of the item that was removed.
     */
    protected void itemRemoved(Object key) {
        if (isMap) {
            map.remove(key);
        } else {
            list.remove(key);
        }
    }

    /**
     * Removes the first object from the list of keys.
     *
     * @return the object that was removed
     */
    private Object removeFirst() {
        Object toRemove;

        if (isSet) {
            Iterator it = list.iterator();
            toRemove = it.next();
            it.remove();
        } else if (isMap) {
            toRemove = ((SequencedHashMap) map).getFirstKey();
            map.remove(toRemove);
        } else {
            toRemove = ((List) list).remove(0);
        }

        return toRemove;
    }
}
