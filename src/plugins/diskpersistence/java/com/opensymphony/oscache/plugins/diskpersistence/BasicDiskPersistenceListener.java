/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.diskpersistence;

import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.persistence.CachePersistenceException;
import com.opensymphony.oscache.base.persistence.PersistenceListener;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

import java.util.Set;

import javax.servlet.jsp.PageContext;

/**
 * Persist the cache data to disk.
 *
 * The code in this class is totally not thread safe it is the resonsibility
 * of the cache using this persistence listener to handle the concurrency.
 *
 * @version        $Revision$
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class BasicDiskPersistenceListener extends AbstractDiskPersistenceListener {
    /**
 * Build cache file name for the specified cache entry key.
 *
 * @param key   Cache Entry Key.
 * @return char[] file name.
 */
    protected char[] getCacheFileName(String key) {
        if ((key == null) || (key.length() == 0)) {
            throw new IllegalArgumentException("Invalid key '" + key + "' specified to getCacheFile.");
        }

        char[] chars = key.toCharArray();
        char[] fileChars = new char[chars.length];

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
                case '.':
                case '/':
                case '\\':
                case ' ':
                case ':':
                case ';':
                case '"':
                case '\'':
                    fileChars[i] = '_';
                    break;
                default:
                    fileChars[i] = c;
            }
        }

        return fileChars;
    }
}
