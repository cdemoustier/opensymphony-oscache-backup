/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import com.opensymphony.oscache.base.NeedsRefreshException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Servlet used to test the web portion of osCache. It performs the operations
 * received by parameter
 *
 * $Id$
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public class OscacheServlet extends HttpServlet {
    /** Output content type */
    private static final String CONTENT_TYPE = "text/html";

    /** Clean up resources */
    public void destroy() {
    }

    /**
     * Process the HTTP Get request
     * <p>
     * @param request The HTTP request
     * @param response The servlet response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean varForceRefresh = false;
        int refreshPeriod = 0;
        int scope = PageContext.APPLICATION_SCOPE;
        String forceCacheUse = null;
        String key = null;

        // Cache item
        Long item;

        // Get the admin
        ServletCacheAdministrator admin = ServletCacheAdministrator.getInstance(getServletContext());

        // Translate parameters
        try {
            String paramValue = request.getParameter("forceRefresh");

            if ((paramValue != null) && (paramValue.length() > 0)) {
                varForceRefresh = Boolean.valueOf(paramValue).booleanValue();
            }

            paramValue = request.getParameter("scope");

            if ((paramValue != null) && (paramValue.length() > 0)) {
                scope = getScope(paramValue);
            }

            paramValue = request.getParameter("refreshPeriod");

            if ((paramValue != null) && (paramValue.length() > 0)) {
                refreshPeriod = Integer.valueOf(paramValue).intValue();
            }

            forceCacheUse = request.getParameter("forcecacheuse");
            key = request.getParameter("key");
        } catch (Exception e) {
            getServletContext().log("Error while retrieving the servlet parameters: " + e.toString());
        }

        // Check if all the items should be flushed
        if (varForceRefresh) {
            admin.flushAll();
        }

        try {
            // Get the data from the cache
            item = (Long) admin.getFromCache(scope, request, key, refreshPeriod);
        } catch (NeedsRefreshException nre) {
            // Check if we want to force the use of an item already in cache
            if ("yes".equals(forceCacheUse)) {
                admin.cancelUpdate(scope, request, key);
                item = (Long) nre.getCacheContent();
            } else {
                item = new Long(System.currentTimeMillis());
                admin.putInCache(scope, request, key, item);
            }
        }

        // Generate the output
        response.setContentType(CONTENT_TYPE);

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>OscacheServlet</title></head>");
        out.println("<body>");
        out.println("<b>This is some cache content </b>: " + item.toString() + "<br>");
        out.println("<b>Cache key</b>: " + admin.getCacheKey() + "<br>");
        out.println("<b>Entry key</b>: " + admin.generateEntryKey("Test_key", request, scope) + "<br>");
        out.println("</body></html>");
    }

    /**Initialize global variables*/
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Return the scope number corresponding to it's string name
     */
    private int getScope(String value) {
        if ((value != null) && (value.equalsIgnoreCase("session"))) {
            return PageContext.SESSION_SCOPE;
        } else {
            return PageContext.APPLICATION_SCOPE;
        }
    }
}
