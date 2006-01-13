package com.opensymphony.oscache.core;

import java.util.EventObject;

/**
 * Represents a cache event. Possible event types are:
 * <ul>
 * <li>{@link #ADD_EVENT} - an entry was added to the cache</li>
 * <li>{@link #UPDATE_EVENT} - a cache entry was updated</li>
 * <li>{@link #REMOVE_EVENT} - an entry was removed from the cache</li>
 * <li>{@link #CLEAR_EVENT} - the entire cache was cleared</li>
 * <li>{@link #LOAD_FAILED_EVENT} - a cache load was attempted but the load failed</li>
 * </ul>
 */
public class CacheEvent extends EventObject {
  public static final int ADD_EVENT = 1;
  public static final int UPDATE_EVENT = 2;
  public static final int REMOVE_EVENT = 3;
  public static final int CLEAR_EVENT = 4;
  public static final int LOAD_FAILED_EVENT = -1;
  private Cache cache;
  private Object key;
  private Object value;
  private int eventType;

  public CacheEvent(Cache source, Object key, Object value, int eventType) {
    super(source);
    this.cache = source;
    this.key = key;
    this.value = value;
    this.eventType = eventType;
  }

  public Cache getCache() {
    return (Cache) getSource();
  }

  public Object getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public boolean isAddEvent() {
    return eventType == ADD_EVENT;
  }

  public boolean isUpdateEvent() {
    return eventType == UPDATE_EVENT;
  }

  public boolean isRemoveEvent() {
    return eventType == REMOVE_EVENT;
  }

  public boolean isClearEvent() {
    return eventType == CLEAR_EVENT;
  }

  public boolean isLoadFailedEvent() {
    return eventType == LOAD_FAILED_EVENT;
  }

  public String getEventTypeString() {
    switch (eventType) {
      case ADD_EVENT:
        return "ADD";

      case UPDATE_EVENT:
        return "UPDATE";

      case REMOVE_EVENT:
        return "REMOVE";

      case CLEAR_EVENT:
        return "CLEAR";

      case LOAD_FAILED_EVENT:
        return "LOAD FAILED";
    }
    return "UNKNOWN";
  }

  public int getEventType() {
    return eventType;
  }

  public String toString() {
    return getClass().getName() + "[" + getEventTypeString() + ": cache=" + getSource() + " key: " + getKey() + "]";
  }
}
