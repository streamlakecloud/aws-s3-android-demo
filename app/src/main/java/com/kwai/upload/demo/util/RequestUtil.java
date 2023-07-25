package com.kwai.upload.demo.util;

import android.util.Log;

import okhttp3.Request;
import okio.Buffer;

/**
 * author: zhouzhihui
 * created on: 2023/7/12 13:42
 * description:
 */
public class RequestUtil {
    public static String bodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final Exception e) {
            Log.e("zzh", "body to string error", e);
            return e.toString();
        }
    }
}
