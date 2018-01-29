package com.android.myvolley;

import java.util.Collections;
import java.util.Map;

/**
 * Created by yangz on 2018/1/29.
 */

public interface Cache {

    public Entry get(String key);
    public void put(String key, Entry entry);
    public void initialize();
    public void invalidate(String key, boolean fullExpire);
    public void remove(String key);
    public void clear();


    public static class Entry{
        public byte[] date;
        public String etag;
        public long serverDate;
        public long lastModified;
        public long ttl;
        public long softTtl;
        public Map<String, String> responseHeaders = Collections.emptyMap();
        public boolean isExpired(){
            return this.ttl < System.currentTimeMillis();
        }
        public boolean refreshNeeded(){
            return this.softTtl < System.currentTimeMillis();
        }
    }
}
