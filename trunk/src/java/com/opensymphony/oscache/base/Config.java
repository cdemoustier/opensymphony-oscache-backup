/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

import java.util.Properties;

/**
 * Responsible for holding the Cache configuration properties. If the default
 * constructor is used, this class will load the properties from the
 * <code>cache.configuration</code>.
 *
 * @author   <a href="mailto:fabian.crabus@gurulogic.de">Fabian Crabus</a>
 * @version  $Revision$
 */
public class Config implements java.io.Serializable {
    private static final transient Log log = LogFactory.getLog(Config.class);

    /**
     * Name of the properties file.
     */
    private final static String PROPERTIES_FILENAME = "/oscache.properties";

    /**
     * Properties map to hold the cache configuration.
     */
    private Properties properties = null;

    /**
     * Create an OSCache Config that loads properties from oscache.properties.
     * The file must be present in the root of OSCache's classpath. If the file
     * cannot be loaded, an error will be logged and the configuration will
     * remain empty.
     */
    public Config() {
        this(null);
    }

    /**
     * Create an OSCache configuration with the specified properties.
     * Note that it is the responsibility of the caller to provide valid
     * properties as no error checking is done to ensure that required
     * keys are present. If you're unsure of what keys should be present,
     * have a look at a sample oscache.properties file.
     *
     * @param p The properties to use for this configuration. If null,
     * then the default properties are loaded from the <code>oscache.properties</code>
     * file.
     */
    public Config(Properties p) {
        if (log.isDebugEnabled()) {
            log.debug("Config() called");
        }

        if (p == null) {
            loadProps();
        } else {
            this.properties = p;
        }
    }

    /**
     * Retrieve the value of the named configuration property. If the property
     * cannot be found this method will return <code>null</code>.
     *
     * @param key The name of the property.
     * @return The property value, or <code>null</code> if the value could
     * not be found.
     *
     * @throws IllegalArgumentException if the supplied key is null.
     */
    public String getProperty(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (properties == null) {
            return null;
        }

        String value = properties.getProperty(key);
        return value;
    }

    /**
     * Retrieves all of the configuration properties. This property set
     * should be treated as immutable.
     *
     * @return The configuration properties.
     */
    public Properties getProperties() {
        return properties;
    }

    public Object get(Object key) {
        return properties.get(key);
    }

    /**
     * Sets a configuration property.
     *
     * @param key   The unique name for this property.
     * @param value The value assigned to this property.
     *
     * @throws IllegalArgumentException if the supplied key is null.
     */
    public void set(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (value == null) {
            return;
        }

        if (properties == null) {
            properties = new Properties();
        }

        properties.put(key, value);
    }

    /**
     * Load the properties file (<code>oscache.properties</code>)
     * from the classpath. If the file cannot be found or loaded, an error
     * will be logged and no properties will be set.
     */
    private void loadProps() {
        if (log.isDebugEnabled()) {
            log.debug("Getting Config");
        }

        properties = new Properties();

        InputStream in = null;

        try {
            in = Config.class.getResourceAsStream(PROPERTIES_FILENAME);
            properties.load(in);
            log.info("Properties " + properties);
        } catch (Exception e) {
            log.error("Error reading " + PROPERTIES_FILENAME + " in CacheAdministrator.loadProps() " + e);
            log.error("Ensure the " + PROPERTIES_FILENAME + " file is readable and in your classpath.");
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                // Ignore errors that occur while closing file
            }
        }
    }
}
