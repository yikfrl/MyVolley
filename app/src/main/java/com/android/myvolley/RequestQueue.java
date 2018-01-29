package com.android.myvolley;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangz on 2018/1/26.
 */

public class RequestQueue {

    public static interface RequestFinishedListener<T>{
        public void onRequestFinished(Request<T> request);
    }

    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    private final Map<String, Queue<Request<?>>> mWaitingRequests = new HashMap<>();

    private final Set<Request<?>> mCurrentRequests = new HashSet<>();

    private final PriorityBlockingQueue<Request<?>> mCacheQueue = new PriorityBlockingQueue<>();

    private final PriorityBlockingQueue<Request<?>> mNetworkQueue = new PriorityBlockingQueue<>();

    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    private final Cache mCache;

    private final Network mNetwork;

    private final ResponseDelivery mDelivery;

    private NetworkDispatcher[] mDispatchers;

    private CacheDispatcher mCacheDispatcher;

    private List<RequestFinishedListener> mFinishedListeners = new ArrayList<>();

    public RequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery){
        mCache = cache;
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery;
    }

    public RequestQueue(Cache cache, Network network, int threadPoolSize){
        this(cache,network,threadPoolSize,new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RequestQueue(Cache cache, Network network){
        this(cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
    }

    public void start(){

    }

    public void stop(){
        if(mCacheDispatcher != null){
            mCacheDispatcher.quit();
        }
        for(int i = 0 ;i<mDispatchers.length ; i++){
            if(mDispatchers[i] != null){
                mDispatchers[i].quit();
            }
        }
    }
}
