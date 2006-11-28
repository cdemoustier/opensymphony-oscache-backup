package com.opensymphony.oscache.events;

import java.util.EventObject;

import com.opensymphony.oscache.core.Cache;

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
  
  public static final int LOAD_FAILED = 8;
  private Cache cache;
  private int eventType;

  /**
   * An optional tag that can be attached to the event to specify the event's origin.
   */
  protected String origin = null;

  public CacheEvent(Cache source, int eventType) {
    super(source);
    this.cache = source;
    this.eventType = eventType;
  }
  

  /**
   * Creates a cache event object that came from the specified origin.
   *
   * @param origin A string that indicates where this event was fired from.
   * This value is optional; <code>null</code> can be passed in if an
   * origin is not required.
   */
  public CacheEvent(String origin) {
	  super(origin);
      this.origin = origin;
  }

  /**
   * Retrieves the origin of this event, if one was specified. This is most
   * useful when an event handler causes another event to fire - by checking
   * the origin the handler is able to prevent recursive events being
   * fired.
   */
  public String getOrigin() {
      return origin;
  }

  public Cache getCache() {
    return (Cache) getSource();
  }

  public boolean isLoadFailedEvent() {
    return eventType == LOAD_FAILED;
  }

  public String getEventTypeString() {
    switch (eventType) {

      case LOAD_FAILED:
        return "LOAD FAILED";
    }
    return "UNKNOWN";
  }

  public int getEventType() {
    return eventType;
  }

  public String toString() {
    return getClass().getName() + "[" + getEventTypeString() + ": cache=" + getSource() + "]";
  }
}
