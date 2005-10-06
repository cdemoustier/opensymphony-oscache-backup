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
public class CacheFilter implements Filter, ICacheKeyProvider, ICacheGroupsProvider {
    // Header
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_CACHE_CONTROL = "Cache-control";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    // Fragment parameter
    public static final int FRAGMENT_AUTODETECT = -1;
    public static final int FRAGMENT_NO = 0;
    public static final int FRAGMENT_YES = 1;
    
    // No cache parameter
    public static final int NOCACHE_OFF = 0;
    public static final int NOCACHE_SESSION_ID_IN_URL = 1;
    
    // Expires parameter
    public static final long EXPIRES_OFF = 0;
    public static final long EXPIRES_ON = 1;
    public static final long EXPIRES_TIME = -1;

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
    private int nocache = NOCACHE_OFF; // defines special no cache option for the requests - default is off
    private long expires = EXPIRES_ON; // defines if the expires-header will be sent - default is on
    private ICacheKeyProvider cacheKeyProvider = this; // the provider of the cache key - default is the CacheFilter itselfs
    private ICacheGroupsProvider cacheGroupsProvider = this; // the provider of the cache groups - default is the CacheFilter itselfs

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

        // avoid reentrance (CACHE-128) and check if request is cacheable
        if (isFilteredBefore(request) || !isCacheable(request)) {
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(REQUEST_FILTERED, Boolean.TRUE);

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // checks if the response well be a fragment of a page
        boolean fragmentRequest = isFragment(httpRequest);

        // avoid useless session creation for application scope pages (CACHE-129)
        Cache cache;
        if (cacheScope == PageContext.SESSION_SCOPE) {
            cache = admin.getSessionScopeCache(httpRequest.getSession(true));
        } else {
            cache = admin.getAppScopeCache(config.getServletContext());
        }

        // generate the cache entry key
        String key = cacheKeyProvider.createCacheKey(httpRequest, admin, cache);

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

                CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper((HttpServletResponse) response, fragmentRequest, time * 1000, expires);
                chain.doFilter(request, cacheResponse);
                cacheResponse.flushBuffer();

                // Only cache if the response is cacheable
                if (isCacheable(cacheResponse)) {
                    // get the cache groups of the content
                    String[] groups = cacheGroupsProvider.createCacheGroups(httpRequest, admin, cache);
                    // Store as the cache content the result of the response
                    cache.putInCache(key, cacheResponse.getContent(), groups, expiresRefreshPolicy, null);
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
     * 
     * <li><b>time</b> - the default time (in seconds) to cache content for. The default
     * value is 3600 seconds (one hour).</li>
     * 
     * <li><b>scope</b> - the default scope to cache content in. Acceptable values
     * are <code>application</code> (default), <code>session</code>, <code>request</code> and
     * <code>page</code>.</li>
     * 
     * <li><b>fragment</b> - defines if this filter handles fragments of a page. Acceptable values
     * are <code>auto</code> (default) for auto detect, <code>no</code> and <code>yes</code>.</li>
     * 
     * <li><b>nocache</b> - defines which objects shouldn't be cached. Acceptable values
     * are <code>off</code> (default) and <code>sessionIdInURL</code> if the session id is
     * contained in the URL.</li>
     * 
     * <li><b>expires</b> - defines if the expires header will be sent in the response. Acceptable values are
     * <code>off</code> for don't sending the header, even it is set in the filter chain, 
     * <code>on</code> (default) for sending it if it is set in the filter chain and 
     * <code>time</code> the expires information will be intialized based on the time parameter and creation time of the content.</li>
     * 
     * <li><b>ICacheKeyProvider</b> - Class implementing the interface <code>ICacheKeyProvider</code>.
     * A developer can implement a method which provides cache keys based on the request, 
     * the servlect cache administrator and cache.</li>
     * 
     * <li><b>ICacheGroupsProvider</b> - Class implementing the interface <code>ICacheGroupsProvider</code>.
     * A developer can implement a method which provides cache groups based on the request, 
     * the servlect cache administrator and cache.</li>
     *
     * @param filterConfig The filter configuration
     */
    public void init(FilterConfig filterConfig) {
        //Get whatever settings we want...
        config = filterConfig;
        admin = ServletCacheAdministrator.getInstance(config.getServletContext());

        // filter parameter time
        try {
            time = Integer.parseInt(config.getInitParameter("time"));
        } catch (Exception e) {
            log.info("Could not get init parameter 'time', defaulting to one hour.");
        }
        
        // setting the refresh period for this cache filter
        expiresRefreshPolicy = new ExpiresRefreshPolicy(time);

        // filter parameter scope
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

        // filter parameter fragment
        try {
            String fragmentString = config.getInitParameter("fragment");
            
            if (fragmentString.equals("no")) {
                fragment = FRAGMENT_NO;
            } else if (fragmentString.equals("yes")) {
                fragment = FRAGMENT_YES;
            } else if (fragmentString.equalsIgnoreCase("auto")) {
                fragment = FRAGMENT_AUTODETECT;
            } else {
                // FIXME in 2.2 RC the values were -1, 0. In 2.2.1 or >= 2.3 delete this code
                try {
                    fragment = Integer.parseInt(fragmentString);

                    if ((fragment < FRAGMENT_AUTODETECT) || (fragment > FRAGMENT_YES)) {
                        log.info("Wrong init parameter 'fragment', setting to 'auto detect': " + fragment);
                        fragment = FRAGMENT_AUTODETECT;
                    }
                    
                    log.warn("The used value '" + fragmentString + "' for the fragment parameter is deprecated.");
                } catch (Exception e2) {
                    log.info("Could not get init parameter 'fragment', defaulting to 'auto detect'.");
                }
                // end of deletion
            }
        } catch (Exception e) {
            log.info("Could not get init parameter 'fragment', defaulting to 'auto detect'.");
        }
        
        // filter parameter nocache
        try {
            String nocacheString = config.getInitParameter("nocache");
            
            if (nocacheString.equals("off")) {
                nocache = NOCACHE_OFF;
            } else if (nocacheString.equalsIgnoreCase("sessionIdInURL")) {
                nocache = NOCACHE_SESSION_ID_IN_URL;
            } 
        } catch (Exception e) {
            log.info("Could not get init parameter 'nocache', defaulting to 'off'.");
        }

        // filter parameter expires
        try {
            String expiresString = config.getInitParameter("expires");
            
            if (expiresString.equals("off")) {
                expires = EXPIRES_OFF;
            } else if (expiresString.equals("on")) {
                expires = EXPIRES_ON;
            } else if (expiresString.equalsIgnoreCase("time")) {
                expires = EXPIRES_TIME;
            } 
        } catch (Exception e) {
            log.info("Could not get init parameter 'expires', defaulting to 'on'.");
        }

        // filter parameter ICacheKeyProvider
        try {
            String className = config.getInitParameter("ICacheKeyProvider");
            
            try {
                Class clazz = Class.forName(className);

                if (!ICacheKeyProvider.class.isAssignableFrom(clazz)) {
                    log.error("Specified class '" + className + "' does not implement ICacheKeyProvider. Ignoring this provider.");
                } else {
                    cacheKeyProvider = (ICacheKeyProvider) clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                log.error("Class '" + className + "' not found. Ignoring this cache key provider.", e);
            } catch (InstantiationException e) {
                log.error("Class '" + className + "' could not be instantiated because it is not a concrete class. Ignoring this cache key provider.", e);
            } catch (IllegalAccessException e) {
                log.error("Class '" + className + "' could not be instantiated because it is not public. Ignoring this cache key provider.", e);
            }
        } catch (Exception e) {
            log.info("Could not get init parameter 'ICacheKeyProvider', defaulting to " + this.getClass().getName() + ".");
        }

        // filter parameter ICacheGroupsProvider
        try {
            String className = config.getInitParameter("ICacheGroupsProvider");
            
            try {
                Class clazz = Class.forName(className);

                if (!ICacheGroupsProvider.class.isAssignableFrom(clazz)) {
                    log.error("Specified class '" + className + "' does not implement ICacheGroupsProvider. Ignoring this provider.");
                } else {
                    cacheGroupsProvider = (ICacheGroupsProvider) clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                log.error("Class '" + className + "' not found. Ignoring this cache key provider.", e);
            } catch (InstantiationException e) {
                log.error("Class '" + className + "' could not be instantiated because it is not a concrete class. Ignoring this cache groups provider.", e);
            } catch (IllegalAccessException e) {
                log.error("Class '" + className + "' could not be instantiated because it is not public. Ignoring this cache groups provider.", e);
            }
        } catch (Exception e) {
            log.info("Could not get init parameter 'ICacheGroupsProvider', defaulting to " + this.getClass().getName() + ".");
        }
    }

    /**
     * @see com.opensymphony.oscache.web.filter.ICacheKeyProvider#createCacheKey(javax.servlet.http.HttpServletRequest, ServletCacheAdministrator, Cache)
     */
    public String createCacheKey(HttpServletRequest httpRequest, ServletCacheAdministrator scAdmin, Cache cache) {
        return scAdmin.generateEntryKey(null, httpRequest, cacheScope);
    }

    /**
     * @see com.opensymphony.oscache.web.filter.ICacheGroupsProvider#createCacheGroups(javax.servlet.http.HttpServletRequest, ServletCacheAdministrator, Cache)
     */
    public String[] createCacheGroups(HttpServletRequest httpRequest, ServletCacheAdministrator scAdmin, Cache cache) {
        return null;
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
     * isCacheable is a method allowing a subclass to decide if a request is
     * cachable or not.
     * 
     * @param request The servlet request
     * @return Returns a boolean indicating if the request can be cached or not.
     */
    protected boolean isCacheable(ServletRequest request) {
        // TODO implement CACHE-137 and CACHE-141 here
        boolean cachable = request instanceof HttpServletRequest;

        if (cachable) {
            HttpServletRequest requestHttp = (HttpServletRequest) request;
            if (nocache == NOCACHE_SESSION_ID_IN_URL) { // don't cache requests if session id is in the URL
                cachable = !requestHttp.isRequestedSessionIdFromURL();
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("<cache>: the request " + ((cachable) ? "is" : "is not") + " cachable.");
        }
        
        return cachable;
    }
    
    /**
     * isCacheable is a method allowing subclass to decide if a response is
     * cachable or not.
     * 
     * @param cacheResponse The HTTP servlet response
     * @return Returns a boolean indicating if the response can be cached or not.
     */
    protected boolean isCacheable(CacheHttpServletResponseWrapper cacheResponse) {
        // TODO implement CACHE-137 and CACHE-141 here
        // Only cache if the response was 200
        boolean cachable = cacheResponse.getStatus() == HttpServletResponse.SC_OK;

        if (log.isDebugEnabled()) {
            log.debug("<cache>: the response " + ((cachable) ? "is" : "is not") + " cachable.");
        }
        
        return cachable;
    }

    /**
     * Check if the client browser support gzip compression.
     * 
     * @param request the http request
     * @return true if client browser supports GZIP
     */
    protected boolean acceptsGZipEncoding(HttpServletRequest request) {
        String acceptEncoding = request.getHeader(HEADER_ACCEPT_ENCODING);
        return  (acceptEncoding != null) && (acceptEncoding.indexOf("gzip") != -1);
    }

}
