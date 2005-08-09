/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;


/**
 * Holds the state of a Cache Entry that is in the process of being (re)generated.
 * This is not synchronized; the synchronization must be handled by the calling
 * classes.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 * @author Author: $
 * @version Revision: $
 */
public class EntryUpdateState {
    /**
     * The initial state when this object is first created
     */
    public static final int NOT_YET_UPDATING = -1;

    /**
     * Update in progress state
     */
    public static final int UPDATE_IN_PROGRESS = 0;

    /**
     * Update complete state
     */
    public static final int UPDATE_COMPLETE = 1;

    /**
     * Update cancelled state
     */
    public static final int UPDATE_CANCELLED = 2;

    /**
     * Current update state
     */
    int state = NOT_YET_UPDATING;

    /**
     * This is the initial state when an instance this object is first created.
     * It indicates that a cache entry needs updating, but no thread has claimed
     * responsibility for updating it yet.
     */
    public boolean isAwaitingUpdate() {
        return state == NOT_YET_UPDATING;
    }

    /**
     * The thread that was responsible for updating the cache entry (ie, the thread
     * that managed to grab the update lock) has decided to give up responsibility
     * for performing the update. OSCache will notify any other threads that are
     * waiting on the update so one of them can take over the responsibility.
     */
    public boolean isCancelled() {
        return state == UPDATE_CANCELLED;
    }

    /**
     * The update of the cache entry has been completed.
     */
    public boolean isComplete() {
        return state == UPDATE_COMPLETE;
    }

    /**
     * The cache entry is currently being generated by the thread that got hold of
     * the update lock.
     */
    public boolean isUpdating() {
        return state == UPDATE_IN_PROGRESS;
    }


    /**
     * Attempt to change the state to <code>UPDATE_IN_PROGRESS</code>. Calls
     * to this method must be synchronized on the EntryUpdateState instance.
     */
    public void startUpdate() {
        if ((state != NOT_YET_UPDATING) && (state != UPDATE_CANCELLED) && (state != UPDATE_COMPLETE)) {
            throw new IllegalStateException("Cannot begin cache update - current state (" + state + ") is not NOT_YET_UPDATING or UPDATE_CANCELLED");
        }

        state = UPDATE_IN_PROGRESS;
    }
}
