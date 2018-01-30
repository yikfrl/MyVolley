package com.android.myvolley;

import android.os.Process;

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
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        mCache.initialize();

        Request<?> request;
        while(true){
            request = null;

            try {
                request = mCacheQueue.take();
            } catch (InterruptedException e) {
                if(mQuit){
                    return;
                }
                continue;
            }

            try{
                request.addMarker("cache-queue-take");

                if(request.isCanceled()){
                    request.finish("cache-discard-canceled");
                    continue;
                }

                // Attempt to retrieve this item from cache.
                Cache.Entry entry = mCache.get(request.getCacheKey());
                if(entry == null){
                    request.addMarker("cache-miss");
                    mNetworkQueue.put(request);
                    continue;
                }

                // If it is completely expired, just send it to the network.
                if(entry.isExpired()){
                    request.addMarker("cache-hit-expired");
                    request.setCacheEntry(entry);
                    mNetworkQueue.put(request);
                    continue;
                }

                request.addMarker("cache-hit");
                Response<?> response = request.parseNetwordResponse(new NetworkResponse(entry.data, entry.responseHeaders));
                request.addMarker("cache-hit-parsed");

                if(!entry.refreshNeeded()){
                    mDelivery.postResponse(request, response);
                } else {
                    //TODO 在此场景下一个请求可能会产生两次回复，有没有可能造成困扰？
                    request.addMarker("cache-hit-refresh-needed");
                    request.setCacheEntry(entry);

                    response.intermediate = true;

                    final Request<?> finalRequest = request;
                    mDelivery.postResponse(request, response, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mNetworkQueue.put(finalRequest);
                            } catch (InterruptedException e) {
                                // Not much we can do about this.
                            }
                        }
                    });
                }

            }catch (Exception e){
                VolleyLog.e(e, "Unhandled exception %s",e.toString());
            }

        }

    }
}
