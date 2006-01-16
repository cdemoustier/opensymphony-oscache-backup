/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A CacheEntry instance represents one entry in the cache. It holds the object that
 * is being cached, along with a host of information about that entry such as the
 * cache key, the time it was cached, whether the entry has been flushed or not and
 * the groups it belongs to.
 *
 * @author        <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author        <a href="mailto:tgochenour@peregrine.com">Todd Gochenour</a>
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:oscache@andresmarch.com">Andres March</a>
 */
public class CacheEntry implements Map.Entry, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3776911995865680219L;

	/**
 * Default initialization value for the creation time and the last
 * update time. This is a placeholder that indicates the value has
 * not been set yet.
 */
    private static final byte NOT_YET = -1;

    /**
 * Specifying this as the refresh period for the
 * {@link #needsRefresh(int)} method will ensure
 * an entry does not become stale until it is
 * either explicitly flushed or a custom refresh
 * policy causes the entry to expire.
 */
    public static final int INDEFINITE_EXPIRY = -1;

	

    /**
 * The entry refresh policy object to use for this cache entry. This is optional.
 */
    private EntryRefreshPolicy policy = null;
    
    
    
    private int state;
    
    public static final int STATE_VALID = 0;
    public static final int STATE_UPDATING = 1;

    /**
 * The actual content that is being cached. Wherever possible this object
 * should be serializable. This allows <code>PersistenceListener</code>s
 * to serialize the cache entries to disk or database.
 */
    private Object content = null;


    /**
 *  The unique cache key for this entry
 */
    private Object key;

    /**
 * <code>true</code> if this entry was flushed
 */
    private boolean wasFlushed = false;

    /**
 * The time this entry was created.
 */
    private long created = NOT_YET;

    /**
 * The time this emtry was last updated.
 */
    private long lastUpdate = NOT_YET;
    
    /**
     * The set of cache groups that this cache entry belongs to, if any.
     */
    private Set groups = null;

    

    /**
     * Construct a new CacheEntry using the key provided.
     *
     * @param key    The key of this CacheEntry
     */
    public CacheEntry(Object key, Object value) {
		this(key, null, value);		
}
    
    /**
 * Construct a CacheEntry.
 *
 * @param key     The unique key for this <code>CacheEntry</code>.
 * @param policy  The object that implements the refresh policy logic. This
 * parameter is optional.
 */
    public CacheEntry(Object key, EntryRefreshPolicy policy, Object value) {
        this.key = key;

        this.policy = policy;
        this.created = System.currentTimeMillis();
        this.state = STATE_VALID;
        setValue(value);
    }

    


	/**
 * Sets the actual content that is being cached. Wherever possible this
 * object should be <code>Serializable</code>, however it is not an
 * absolute requirement when using a memory-only cache. Being <code>Serializable</code>
 * allows <code>PersistenceListener</code>s to serialize the cache entries to disk
 * or database.
 *
 * @param value The content to store in this CacheEntry.
 */
    public Object setValue(Object value) {
        content = value;
        lastUpdate = System.currentTimeMillis();
        wasFlushed = false;
		return value;
    }

    /**
 * Get the cached content from this CacheEntry.
 *
 * @return The content of this CacheEntry.
 */
    public Object getValue() {
        return content;
    }

    /**
 * Get the date this CacheEntry was created.
 *
 * @return The date this CacheEntry was created.
 */
    public long getCreated() {
        return created;
    }


    /**
 * Get the key of this CacheEntry
 *
 * @return The key of this CacheEntry
 */
    public Object getKey() {
        return key;
    }

    /**
 * Set the date this CacheEntry was last updated.
 *
 * @param update The time (in milliseconds) this CacheEntry was last updated.
 */
    public void setLastUpdate(long update) {
        lastUpdate = update;
    }

    /**
 * Get the date this CacheEntry was last updated.
 *
 * @return The date this CacheEntry was last updated.
 */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
 * Indicates whether this CacheEntry is a freshly created one and
 * has not yet been assigned content or placed in a cache.
 *
 * @return <code>true</code> if this entry is newly created
 */
    public boolean isNew() {
        return lastUpdate == NOT_YET;
    }

    /**
 * Get the size of the cache entry in bytes (roughly).<p>
 *
 *
 * @return The approximate size of the entry in bytes, or -1 if the
 * size could not be estimated.
 */
    public int getSize() {
    		ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(bout);
			out.writeObject(this);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		byte[] bytes = bout.toByteArray();
		return bytes.length;
        
    }

    /**
 * Flush the entry from cache.
 * note that flushing the cache doesn't actually remove the cache contents
 * it just tells the CacheEntry that it needs a refresh next time it is asked
 * this is so that the content is still there for a <usecached />.
 */
    public void flush() {
        wasFlushed = true;
    }

    /**
 * Check if this CacheEntry needs to be refreshed.
 *
 * @param refreshPeriod The period of refresh (in seconds). Passing in
 * {@link #INDEFINITE_EXPIRY} will result in the content never becoming
 * stale unless it is explicitly flushed, or expired by a custom
 * {@link EntryRefreshPolicy}. Passing in 0 will always result in a
 * refresh being required.
 *
 * @return Whether or not this CacheEntry needs refreshing.
 */
    public boolean needsRefresh(int refreshPeriod) {
        boolean needsRefresh;

        // needs a refresh if it has never been updated
        if (lastUpdate == NOT_YET) {
            needsRefresh = true;
        }
        // Was it flushed from cache?
        else if (wasFlushed) {
            needsRefresh = true;
        } else if (refreshPeriod == 0) {
            needsRefresh = true;
        }
        // check what the policy has to say if there is one
        else if (policy != null) {
            needsRefresh = policy.needsRefresh(this);
        }
        // check if the last update + update period is in the past
        else if ((refreshPeriod >= 0) && (System.currentTimeMillis() >= (lastUpdate + (refreshPeriod * 1000L)))) {
            needsRefresh = true;
        } else {
            needsRefresh = false;
        }

        return needsRefresh;
    }

    public int getState() {
        return state;
    }


	public boolean isUpdating() {
		return state == STATE_UPDATING;
	}


	public void startUpdate() {
		
		state = STATE_UPDATING;
	}
	
	/**
     * Updates the state to <code>UPDATE_CANCELLED</code>. This should <em>only<em>
     * be called by the thread that managed to get the update lock.
     */
    public void cancelUpdate() {
        if (state != STATE_UPDATING) {
            throw new IllegalStateException("Cannot cancel cache update - current state (" + state + ") is not UPDATE_IN_PROGRESS");
        }

        state = STATE_VALID;
    }
    
    /**
     * Updates the state to <code>UPDATE_COMPLETE</code>. This should <em>only</em>
     * be called by the thread that managed to get the update lock.
     */
    public void completeUpdate() {
        if (state != STATE_UPDATING) {
            throw new IllegalStateException("Cannot complete cache update - current state (" + state + ") is not UPDATE_IN_PROGRESS");
        }

        state = STATE_VALID;
    }

	/**
	 * @return Returns the content.
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * @param content The content to set.
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * @return Returns the groups.
	 */
	public Set getGroups() {
		if (groups == null) groups = new HashSet();
		return groups;
	}

	/**
	 * @param groups The groups to set.
	 */
	public void setGroups(Set groups) {
		this.groups = groups;
	}

	/**
	 * @return Returns the policy.
	 */
	public EntryRefreshPolicy getPolicy() {
		return policy;
	}

	/**
	 * @param policy The policy to set.
	 */
	public void setPolicy(EntryRefreshPolicy policy) {
		this.policy = policy;
	}

}
