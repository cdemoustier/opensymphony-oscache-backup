/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.events;

import java.util.Date;

import com.opensymphony.oscache.core.Cache;

/**
 * A <code>ScopeEvent</code> is created when an event occurs across one or all scopes.
 * This type of event is only applicable to the <code>ServletCacheAdministrator</code>.
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 */
public final class ScopeEvent extends CacheEvent {
    
	/**
     * Specifies an event type for the all scope flushed event.
     */
    public static int ALL_SCOPES_FLUSHED = 0;

    /**
     * Specifies an event type for the flushing of a  specific scope.
     */
    public static int SCOPE_FLUSHED = 1;
	
	
	/**
     * Date that the event applies to.
     */
    private Date date = null;


    /**
     * Scope that applies to this event.
     */
    private int scope = 0;

    /**
     * Constructs a scope event object with no specified origin.
     *
     * @param eventType   Type of the event.
     * @param scope       Scope that applies to the event.
     * @param date        Date that the event applies to.
     */
    public ScopeEvent(Cache cache, int eventType, int scope, Date date) {
        super(cache, eventType);
        this.scope = scope;
        this.date = date;
    }

    /**
     * Retrieve the event date
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * Retrieve the scope that applies to the event.
     */
    public int getScope() {
        return scope;
    }
}
