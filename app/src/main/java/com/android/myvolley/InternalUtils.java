package com.android.myvolley;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yikfrl on 2018/1/29.
 */

public class InternalUtils {

    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private static String convertToHex(byte[] bytes){
        char[] hexChars = new char[bytes.length*2];

        for(int j = 0; j<bytes.length; j++){
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = HEX_CHARS[v >>> 4];
            hexChars[j*2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String sha1Hash(String text){
        String hash = null;
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            final byte[] bytes = text.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            hash = convertToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
