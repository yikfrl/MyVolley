package com.android.myvolley;

import java.util.concurrent.BlockingQueue;

/**
 * Created by yangz on 2018/1/29.
 */

public class CacheDispatcher extends Thread {

    private static final boolean DEBUG = VolleyLog.DEBUG;

    private final BlockingQueue<Request<?>> mCacheQueue;
    private final BlockingQueue<Request<?>> mNetworkQueue;
    private final Cache mCache;
    private final ResponseDelivery mDelivery;
    private volatile boolean mQuit = false;

    public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue, Cache cache, ResponseDelivery delivery){
        mCacheQueue = cacheQueue;
        mNetworkQueue = networkQueue;
        mCache = cache;
        mDelivery = delivery;
    }

    public void quit(){
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        if (DEBUG) VolleyLog.v("Start new dispatcher");
    }
}
