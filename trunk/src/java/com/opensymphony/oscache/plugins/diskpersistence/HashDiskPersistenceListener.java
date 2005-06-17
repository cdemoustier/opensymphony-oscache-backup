/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.diskpersistence;

import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.persistence.PersistenceListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Persists cache data to disk. Provides a hash of the standard key name as the file name.
 *
 * A configurable hash algorithm is used to create a digest of the cache key for the
 * disk filename. This is to allow for more sane filenames for objects which dont generate
 * friendly cache keys.
 *
 * @author <a href="mailto:jparrott@soe.sony.com">Jason Parrott</a>
 */
public class HashDiskPersistenceListener extends AbstractDiskPersistenceListener {
    public final static String HASH_ALGORITHM_KEY = "cache.persistence.disk.hash.algorithm";
    public final static String DEFAULT_HASH_ALGORITHM = "MD5";
    protected MessageDigest md = null;

    /**
     * Initializes the <tt>HashDiskPersistenceListener</tt>. Namely this involves only setting up the
     * message digester to hash the key values.
     * @see com.opensymphony.oscache.base.persistence.PersistenceListener#configure(com.opensymphony.oscache.base.Config)
     */
    public PersistenceListener configure(Config config) {
        try {
            if (config.getProperty(HashDiskPersistenceListener.HASH_ALGORITHM_KEY) != null) {
                try {
                    md = MessageDigest.getInstance(config.getProperty(HashDiskPersistenceListener.HASH_ALGORITHM_KEY));
                } catch (NoSuchAlgorithmException e) {
                    md = MessageDigest.getInstance(HashDiskPersistenceListener.DEFAULT_HASH_ALGORITHM);
                }
            } else {
                md = MessageDigest.getInstance(HashDiskPersistenceListener.DEFAULT_HASH_ALGORITHM);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("No hash algorithm available for disk persistence", e);
        }

        return super.configure(config);
    }

    /**
     * Generates a file name for the given cache key. In this case the file name is attempted to be
     * generated from the hash of the standard key name. Cache algorithm is configured via the
     * <em>cache.persistence.disk.hash.algorithm</em> configuration variable.
     * @param key cache entry key
     * @return char[] file name
     */
    protected char[] getCacheFileName(String key) {
        byte[] digest;

        if ((key == null) || (key.length() == 0)) {
            throw new IllegalArgumentException("Invalid key '" + key + "' specified to getCacheFile.");
        }

        digest = md.digest(key.getBytes());

        String hexString = byteArrayToHexString(digest);
        return hexString.toCharArray();
    }

    /**
     * Nibble conversion. Thanks to our friends at:
     * http://www.devx.com/tips/Tip/13540
     * @param in the byte array to convert
     * @return a java.lang.String based version of they byte array
     */
    static String byteArrayToHexString(byte[] in) {
        byte ch = 0x00;
        int i = 0;

        if ((in == null) || (in.length <= 0)) {
            return null;
        }

        String[] pseudo = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F"
        };
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);

            // shift the bits down
            ch = (byte) (ch & 0x0F);

            //	 must do this is high order bit is on!
            out.append(pseudo[(int) ch]); // convert the nibble to a String Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble 
            out.append(pseudo[(int) ch]); // convert the nibble to a String Character
            i++;
        }

        String rslt = new String(out);
        return rslt;
    }
}
