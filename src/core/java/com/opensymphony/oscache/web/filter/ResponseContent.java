/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web.filter;

import java.io.*;

import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Holds the servlet response in a byte array so that it can be held
 * in the cache (and, since this class is serializable, optionally
 * persisted to disk).
 *
 * @version $Revision$
 * @author  <a href="mailto:sergek@lokitech.com">Serge Knystautas</a>
 */
public class ResponseContent implements Serializable {
    private transient ByteArrayOutputStream bout = new ByteArrayOutputStream(1000);
    private Locale locale = null;
    private String contentEncoding = null;
    private String contentType = null;
    private byte[] content = null;
    private long expires = Long.MAX_VALUE;
    private long lastModified = -1;

    public String getContentType() {
        return contentType;
    }
    
    /**
     * Set the content type. We capture this so that when we serve this
     * data from cache, we can set the correct content type on the response.
     */
    public void setContentType(String value) {
        contentType = value;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long value) {
        lastModified = value;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Set the Locale. We capture this so that when we serve this data from
     * cache, we can set the correct locale on the response.
     */
    public void setLocale(Locale value) {
        locale = value;
    }

    /**
     * @return the expires date and time in miliseconds when the content is stale
     */
    public long getExpires() {
        return expires;
    }

    /**
     * Sets the expires date and time in miliseconds.
     * @param value time in miliseconds when the content will expire
     */
    public void setExpires(long value) {
        expires = value;
    }

    /**
     * Get an output stream. This is used by the {@link SplitServletOutputStream}
     * to capture the original (uncached) response into a byte array.
     */
    public OutputStream getOutputStream() {
        return bout;
    }

    /**
     * Gets the size of this cached content.
     *
     * @return The size of the content, in bytes. If no content
     * exists, this method returns <code>-1</code>.
     */
    public int getSize() {
        return (content != null) ? content.length : (-1);
    }

    /**
     * Called once the response has been written in its entirety. This
     * method commits the response output stream by converting the output
     * stream into a byte array.
     */
    public void commit() {
        content = bout.toByteArray();
    }

    /**
     * Writes this cached data out to the supplied <code>ServletResponse</code>.
     *
     * @param response The servlet response to output the cached content to.
     * @throws IOException
     */
    public void writeTo(ServletResponse response) throws IOException {
        writeTo(response, false, false);
    }

    /**
     * Writes this cached data out to the supplied <code>ServletResponse</code>.
     *
     * @param response The servlet response to output the cached content to.
     * @param fragment is true if this content a fragment or part of a page
     * @param acceptsGZip is true if client browser supports gzip compression
     * @throws IOException
     */
    public void writeTo(ServletResponse response, boolean fragment, boolean acceptsGZip) throws IOException {
        //Send the content type and data to this response
        if (contentType != null) {
            response.setContentType(contentType);
        }
        
        // Don't support gzip compression if the content is a fragment of a page
        if (fragment) {
            acceptsGZip = false;
        }

        // Don't add in the Last-Modified header in a fragment of a page
        if ((!fragment) && (lastModified != -1) && (response instanceof HttpServletResponse)) {
            ((HttpServletResponse) response).setDateHeader(CacheFilter.HEADER_LAST_MODIFIED, lastModified);
        } // FIXME maybe remove header if it exists

        if (locale != null) {
            response.setLocale(locale);
        }

        OutputStream out = new BufferedOutputStream(response.getOutputStream());

        if (isContentGZiped()) {
            if (acceptsGZip) {
                ((HttpServletResponse) response).addHeader(CacheFilter.HEADER_CONTENT_ENCODING, "gzip");
                response.setContentLength(content.length);
                out.write(content);
            } else {
                // client doesn't support, so we have to uncompress it
                ByteArrayInputStream bais = new ByteArrayInputStream(content);
                GZIPInputStream zis = new GZIPInputStream(bais);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int numBytesRead = 0;
                byte[] tempBytes = new byte[4196];

                while ((numBytesRead = zis.read(tempBytes, 0, tempBytes.length)) != -1) {
                    baos.write(tempBytes, 0, numBytesRead);
                }

                byte[] result = baos.toByteArray();

                response.setContentLength(result.length);
                out.write(result);
            }
        } else {
            // the content isn't compressed
            // regardless if the client browser supports gzip we will just return the content
            response.setContentLength(content.length);
            out.write(content);
        }
        out.flush();
    }
    
    
    /**
     * @return true if the content is GZIP compressed
     */
    public boolean isContentGZiped() {
        return "gzip".equals(contentEncoding);
    }
}
