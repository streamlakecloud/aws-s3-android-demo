package com.kwai.upload.demo.network;

import android.util.Log;

import com.kwai.upload.demo.BuildConfig;
import com.kwai.upload.demo.sign.StandardAccessUtils;
import com.kwai.upload.demo.util.RequestUtil;
import com.kwai.upload.demo.util.ZipHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author: zhouzhihui
 * created on: 2023/7/12 10:52
 * description:
 */
public abstract class BaseNetWork {

    protected <T> T getApi(Class<T> clazz, String baseUrl) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient());
        return setRetrofitBuilder(retrofitBuilder).build().create(clazz);
    }

    protected OkHttpClient.Builder setHttpClientBuilder(OkHttpClient.Builder builder) {
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Log.i("zzh", "request =" + request);
                Log.i("zzh", "request headers=" + request.headers());
                Log.i("zzh", "request url=" + request.url());
                Log.i("zzh", "request body str=" + RequestUtil.bodyToString(request));
                Request.Builder builder;
                if (BuildConfig.NETWORK_ENV_TEST) {
                    builder = request.newBuilder();
                    builder.addHeader("Content-Type", "application/json");
                    builder.addHeader("trace-context", "{\"laneId\":\"PRT.StreamLake\"}"); // 测试环境需要添加泳道
                    builder.addHeader("AccessKey", "6f1e2e7a143f4c7486eaf336c92c0fe0");
                } else {
                    String accessKey = "b44275774f4b4aa39af012363e7d7689";
                    String secretKey = "a8c4ad8b10964a2e9f3385fbb545cd57";
                    builder = StandardAccessUtils.sign(accessKey, secretKey, request);
                }

                Request requestNew = builder.build();
                Response originalResponse = chain.proceed(requestNew);
                //            // 读取服务器返回的结果
                ResponseBody responseBody = originalResponse.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();
                // 获取content的压缩类型
                String encoding = originalResponse.header("Content-Encoding");
                Buffer clone = buffer.clone();
                String bodyString = praseBodyString(responseBody, encoding, clone);
                Log.i("zzh", "requestNew =" + requestNew);
                Log.i("zzh", "requestNew body str=" + RequestUtil.bodyToString(requestNew));
                Log.i("zzh", "get http rsp=" + bodyString);
                return originalResponse;
            }
        });
        return builder;
    }

    protected Retrofit.Builder setRetrofitBuilder(Retrofit.Builder builder) {
        builder.addConverterFactory(GsonConverterFactory.create());// 请求的结果转为实体类;
        // 适配RxJava2.0,RxJava1.x则为RxJavaCallAdapterFactory.create()
        builder.addCallAdapterFactory(RxJava3CallAdapterFactory.create());
        return builder;
    }

    protected OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder = setHttpClientBuilder(builder);
        return builder.build();
    }

    public static String praseBodyString(ResponseBody responseBody, String encoding, Buffer clone) {
        String bodyString = "";// 解析response content
        try {
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {// content使用gzip压缩
                bodyString = ZipHelper.decompressForGzip(clone.readByteArray());// 解压
            } else if (encoding != null && encoding.equalsIgnoreCase("zlib")) {// content使用zlib压缩
                bodyString = ZipHelper.decompressToStringForZlib(clone.readByteArray());// 解压
            } else {// content没有被压缩
                Charset charset = Charset.forName("UTF-8");
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(charset);
                }
                bodyString = clone.readString(charset);
            }
        } catch (Exception e) {
        }
        return bodyString;
    }
}
