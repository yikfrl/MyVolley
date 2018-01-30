package com.android.myvolley;

import android.annotation.TargetApi;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;

import com.android.myvolley.toolbox.Volley;

import java.util.concurrent.BlockingQueue;

/**
 * Created by yangz on 2018/1/29.
 */

public class NetworkDispatcher extends Thread {

    private final BlockingQueue<Request<?>> mQueue;
    private final Network mNetwork;
    private final Cache mCache;
    private final ResponseDelivery mDelivery;
    private volatile boolean mQuit;

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery){
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
    }

    public void quit(){
        mQuit = true;
        interrupt();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addTrafficStatsTag(Request<?> request){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Request<?> request;
        while(true){
            long startTime = SystemClock.elapsedRealtime();

            request = null;
            try {
                request = mQueue.take();
            } catch (InterruptedException e) {
                if(mQuit){
                    return;
                }
                continue;
            }

            try{
                request.addMarker("network-queue-take");

                if(request.isCanceled()){
                    request.finish("network-discard-cancelled");
                    continue;
                }

                addTrafficStatsTag(request);

                NetworkResponse networkResponse = mNetwork.performRequest(request);
                request.addMarker("network-http-complete");

                if(networkResponse.notModified && request.hasHadDeliveried()){
                    //TODO 可能会有这个场景吗？
                    request.finish("not-modified");
                    continue;
                }

                Response<?> response =  request.parseNetwordResponse(networkResponse);
                request.addMarker("netword-parse-complete");

                if(request.shouldCache() && response.cacheEntry != null){
                    mCache.put(request.getCacheKey(), response.cacheEntry);
                    request.addMarker("network-cache-written");
                }

                request.mardDeliveried();
                mDelivery.postResponse(request, response);

            } catch (VolleyError volleyError) {
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTime);
                parseAndDeliverNetworkError(request, volleyError);
            } catch (Exception e){
                VolleyLog.e(e, "Unhandled exception %s" + e.toString());
                VolleyError volleyError = new VolleyError(e);
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTime);
                mDelivery.postError(request, volleyError);
            }
        }
    }
}
