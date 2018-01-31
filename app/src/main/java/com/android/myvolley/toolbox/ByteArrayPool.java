package com.android.myvolley.toolbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangz on 2018/1/31.
 */

public class ByteArrayPool {

    private List<byte[]> mBuffersByLastUse = new LinkedList<>();
    private List<byte[]> mBuffersBySize = new ArrayList<>(64);

    private int mCurrentSize = 0;

    private final int mSizeLimit;

    protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] lhs, byte[] rhs) {
            return lhs.length - rhs.length;
        }
    };

    public ByteArrayPool(int sizeLimit){
        mSizeLimit = sizeLimit;
    }

    public synchronized byte[] getBuf(int len){
        for(int i = 0; i<mBuffersBySize.size(); i++){
            byte buf[] = mBuffersBySize.get(i);
            if(buf.length >= len){
                mCurrentSize -= buf.length;
                mBuffersBySize.remove(i);
                mBuffersByLastUse.remove(i);
                return buf;
            }
        }
        return new byte[len];
    }

    public synchronized void returnBuf(byte[] buf){
        if(buf == null || buf.length > mSizeLimit){
            return;
        }
        mBuffersByLastUse.add(buf);
        int pos = Collections.binarySearch(mBuffersBySize,buf,BUF_COMPARATOR);
        if(pos < 0){
            pos = - pos - 1;
        }
        mBuffersBySize.add(pos, buf);
        mCurrentSize+=buf.length;
        trim();
    }

    private synchronized void trim(){
        while(mCurrentSize > mSizeLimit){
            byte[] buf = mBuffersByLastUse.remove(0);
            mBuffersBySize.remove(buf);
            mCurrentSize -= buf.length;
        }
    }
}
