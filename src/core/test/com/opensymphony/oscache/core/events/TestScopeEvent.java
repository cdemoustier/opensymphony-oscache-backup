/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.core.events;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Date;

import com.opensymphony.oscache.events.ScopeEvent;
import com.opensymphony.oscache.events.ScopeEventType;

/**
 * This is the test class for the ScopeEvent class. It checks that the
 * public methods are working properly
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestScopeEvent extends TestCase {
    private final int SCOPE = 3;

    /**
     * Constructor
     * <p>
     * @param str The test name (required by JUnit)
     */
    public TestScopeEvent(String str) {
        super(str);
    }

    /**
     * This methods returns the name of this test class to JUnit
     * <p>
     * @return The name of this class
     */
    public static Test suite() {
        return new TestSuite(TestScopeEvent.class);
    }

    /**
     * Test the ScopeEvent class
     */
    public void testScopeEvent() {
        Date date = new Date();

        // Create an object and check the parameters
        ScopeEvent event = new ScopeEvent(ScopeEventType.ALL_SCOPES_FLUSHED, SCOPE, date, null);
        assertEquals(event.getEventType(), ScopeEventType.ALL_SCOPES_FLUSHED);
        assertEquals(event.getScope(), SCOPE);
        assertTrue(event.getDate().equals(date));

        event = new ScopeEvent(ScopeEventType.SCOPE_FLUSHED, SCOPE, date, null);
        assertEquals(event.getEventType(), ScopeEventType.SCOPE_FLUSHED);
        assertEquals(event.getScope(), SCOPE);
        assertTrue(event.getDate().equals(date));
    }
}
