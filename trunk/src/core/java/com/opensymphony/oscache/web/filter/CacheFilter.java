/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.filter;

import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * CacheFilter is a filter that allows for server-side caching of post-processed servlet content.<p>
 *
 * It also gives great programatic control over refreshing, flushing and updating the cache.<p>
 *
 * @author <a href="mailto:sergek@lokitech.com">Serge Knystautas</a>
 * @author <a href="mailto:mike@atlassian.com">Mike Cannon-Brookes</a>
 * @author <a href="mailto:ltorunski@t-online.de">Lars Torunski</a>
 * @version $Revision$
 */
public class CacheFilter implements Filter {
    // Header
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    // Fragment parameter
    public static final int FRAGMENT_AUTODETECT = -1;
    public static final int FRAGMENT_NO = 0;
    public static final int FRAGMENT_YES = 1;

    // request attribute to avoid reentrance
    private final static String REQUEST_FILTERED = "__oscache_filtered";

    // the policy for the expires header
    private ExpiresRefreshPolicy expiresRefreshPolicy;
    
    // the logger
    private final Log log = LogFactory.getLog(this.getClass());

    // filter variables
    private FilterConfig config;
    private ServletCacheAdministrator admin = null;
    private int cacheScope = PageContext.APPLICATION_SCOPE; // filter scope - default is APPLICATION
    private int fragment = FRAGMENT_AUTODETECT; // defines if this filter handles fragments of a page - default is auto detect
    private int time = 60 * 60; // time before cache should be refreshed - default one hour (in seconds)

    /**
     * Filter clean-up
     */
    public void destroy() {
        //Not much to do...
    }

    /**
     * The doFilter call caches the response by wrapping the <code>HttpServletResponse</code>
     * object so that the output stream can be caught. This works by splitting off the output
     * stream into two with the {@link SplitServletOutputStream} class. One stream gets written
     * out to the response as normal, the other is fed into a byte array inside a {@link ResponseContent}
     * object.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param chain The filter chain
     * @throws ServletException IOException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (log.isInfoEnabled()) {
            log.info("<cache>: filter in scope " + cacheScope);
        }

        // avoid reentrance (CACHE-128)
        if (isFilteredBefore(request)) {
            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(REQUEST_FILTERED, Boolean.TRUE);

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // checks if the response is a fragment of a apge
        boolean fragmentRequest = isFragment(httpRequest);

        // generate the cache entry key
        String key = generateEntryKey(httpRequest);

        // avoid useless session creation for application scope pages (CACHE-129)
        Cache cache;

        if (cacheScope == PageContext.SESSION_SCOPE) {
            cache = admin.getSessionScopeCache(httpRequest.getSession(true));
        } else {
            cache = admin.getAppScopeCache(config.getServletContext());
        }

        try {
            ResponseContent respContent = (ResponseContent) cache.getFromCache(key, time);

            if (log.isInfoEnabled()) {
                log.info("<cache>: Using cached entry for " + key);
            }

            boolean acceptsGZip = false;
            if (!fragmentRequest) {
                long clientLastModified = httpRequest.getDateHeader(HEADER_IF_MODIFIED_SINCE); // will return -1 if no header...

                // only reply with SC_NOT_MODIFIED
                // if the client has already the newest page and the reponse isn't a fragment in a page 
                if ((clientLastModified != -1) && (clientLastModified >= respContent.getLastModified())) {
                    ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                
                acceptsGZip = respContent.isContentGZiped() && acceptsGZipEncoding(httpRequest); 
            }

            respContent.writeTo(response, fragmentRequest, acceptsGZip);
            // acceptsGZip is used for performance reasons above; use the following line for CACHE-49
            // respContent.writeTo(response, fragmentRequest, acceptsGZipEncoding(httpRequest));
        } catch (NeedsRefreshException nre) {
            boolean updateSucceeded = false;

            try {
                if (log.isInfoEnabled()) {
                    log.info("<cache>: New cache entry, cache stale or cache scope flushed for " + key);
                }

                CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper((HttpServletResponse) response, fragmentRequest);
                chain.doFilter(request, cacheResponse);
                cacheResponse.flushBuffer();

                // Only cache if the response was 200
                if (cacheResponse.getStatus() == HttpServletResponse.SC_OK) {
                    //Store as the cache content the result of the response
                    cache.putInCache(key, cacheResponse.getContent(), expiresRefreshPolicy);
                    updateSucceeded = true;
                }
            } finally {
                if (!updateSucceeded) {
                    cache.cancelUpdate(key);
                }
            }
        }
    }

    /**
     * Initialize the filter. This retrieves a {@link ServletCacheAdministrator}
     * instance and configures the filter based on any initialization parameters.<p>
     * The supported initialization parameters are:
     * <ul>
     * <li><b>time</b> - the default time (in seconds) to cache content for. The default
     * value is 3600 seconds (one hour).</li>
     * <li><b>scope</b> - the default scope to cache content in. Acceptable values
     * are <code>application</code> (default), <code>session</code>, <code>request</code> and
     * <code>page</code>.
     * <li><b>fragment</b> - defines if this filter handles fragments of a page. Acceptable values
     * are <code>-1</code> (auto detect), <code>0</code> (false) and <code>1</code> (true).
     * The default value is auto detect.</li>
     *
     * @param filterConfig The filter configuration
     */
    public void init(FilterConfig filterConfig) {
        //Get whatever settings we want...
        config = filterConfig;
        admin = ServletCacheAdministrator.getInstance(config.getServletContext());

        //Will work this out later
        try {
            time = Integer.parseInt(config.getInitParameter("time"));
        } catch (Exception e) {
            log.info("Could not get init parameter 'time', defaulting to one hour.");
        }
        
        // setting the refresh period for this cache filter
        expiresRefreshPolicy = new ExpiresRefreshPolicy(time);

        try {
            String scopeString = config.getInitParameter("scope");

            if (scopeString.equals("session")) {
                cacheScope = PageContext.SESSION_SCOPE;
            } else if (scopeString.equals("application")) {
                cacheScope = PageContext.APPLICATION_SCOPE;
            } else if (scopeString.equals("request")) {
                cacheScope = PageContext.REQUEST_SCOPE;
            } else if (scopeString.equals("page")) {
                cacheScope = PageContext.PAGE_SCOPE;
            }
        } catch (Exception e) {
            log.info("Could not get init parameter 'scope', defaulting to 'application'.");
        }

        try {
            fragment = Integer.parseInt(config.getInitParameter("fragment"));

            if ((fragment < FRAGMENT_AUTODETECT) || (fragment > FRAGMENT_YES)) {
                log.info("Wrong init parameter 'fragment', setting to 'auto detect':" + fragment);
                fragment = FRAGMENT_AUTODETECT;
            }
        } catch (Exception e) {
            log.info("Could not get init parameter 'fragment', defaulting to 'auto detect'.");
        }
    }

    /**
     * Creates the cache key for the CacheFilter.
     *
     * @param httpRequest
     * @return the cache key
     */
    public String generateEntryKey(HttpServletRequest httpRequest) {
        return admin.generateEntryKey(null, httpRequest, cacheScope);
    }

    /**
     * Checks if the request is a fragment in a page.
     *
     * According to Java Servlet API 2.2 (8.2.1 Dispatching Requests, Included
     * Request Parameters), when a servlet is being used from within an include,
     * the attribute <code>javax.servlet.include.request_uri</code> is set.
     * According to Java Servlet API 2.3 this is excepted for servlets obtained
     * by using the getNamedDispatcher method.
     *
     * @param request the to be handled request
     * @return true if the request is a fragment in a page
     */
    protected boolean isFragment(HttpServletRequest request) {
        if (fragment == FRAGMENT_AUTODETECT) {
            return request.getAttribute("javax.servlet.include.request_uri") != null;
        } else {
            return (fragment == FRAGMENT_NO) ? false : true;
        }
    }

    /**
     * Checks if the request was filtered before, so
     * guarantees to be executed once per request. You
     * can override this methods to define a more specific
     * behaviour.
     *
     * @param request checks if the request was filtered before.
     * @return true if it is the first execution
     */
    protected boolean isFilteredBefore(ServletRequest request) {
        return request.getAttribute(REQUEST_FILTERED) != null;
    }

    /**
     * Check if the client browser support gzip compression.
     * @param request the http request
     * @return true if client browser supports GZIP
     */
    protected boolean acceptsGZipEncoding(HttpServletRequest request) {
        String acceptEncoding = request.getHeader("Accept-Encoding");
        return  (acceptEncoding != null) && (acceptEncoding.indexOf("gzip") != -1);
    }
}
