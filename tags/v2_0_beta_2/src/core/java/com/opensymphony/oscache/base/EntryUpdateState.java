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
    private static final int NOT_YET_UPDATING = -1;

    /**
     * Update in progress state
     */
    private static final int UPDATE_IN_PROGRESS = 0;

    /**
     * Update complete state
     */
    private static final int UPDATE_COMPLETE = 1;

    /**
     * Update cancelled state
     */
    private static final int UPDATE_CANCELLED = 2;

    /**
     * Current update state
     */
    private int state = NOT_YET_UPDATING;

    public boolean isAwaitingUpdate() {
        return state == NOT_YET_UPDATING;
    }

    public boolean isCancelled() {
        return state == UPDATE_CANCELLED;
    }

    public boolean isComplete() {
        return state == UPDATE_COMPLETE;
    }

    public boolean isUpdating() {
        return state == UPDATE_IN_PROGRESS;
    }

    /**
     * Updates the state to <code>UPDATE_CANCELLED</code>. This should <em>only<em>
     * be called by the thread that managed to get the update lock.
     */
    public void cancelUpdate() {
        if (state != UPDATE_IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel cache update - current state (" + state + ") is not UPDATE_IN_PROGRESS");
        }

        state = UPDATE_CANCELLED;
    }

    /**
     * Updates the state to <code>UPDATE_COMPLETE</code>. This should <em>only</em>
     * be called by the thread that managed to get the update lock.
     */
    public void completeUpdate() {
        if (state != UPDATE_IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete cache update - current state (" + state + ") is not UPDATE_IN_PROGRESS");
        }

        state = UPDATE_COMPLETE;
    }

    /**
     * Attempt to change the state to <code>UPDATE_IN_PROGRESS</code>. Calls
     * to this method must be synchronized on the EntryUpdateState instance.
     */
    public void startUpdate() {
        if ((state != NOT_YET_UPDATING) && (state != UPDATE_CANCELLED)) {
            throw new IllegalStateException("Cannot begin cache update - current state (" + state + ") is not NOT_YET_UPDATING or UPDATE_CANCELLED");
        }

        state = UPDATE_IN_PROGRESS;
    }
}
