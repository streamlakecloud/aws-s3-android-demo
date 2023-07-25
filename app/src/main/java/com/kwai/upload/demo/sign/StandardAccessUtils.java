package com.kwai.upload.demo.sign;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import okhttp3.Request;
import okio.Buffer;

public class StandardAccessUtils {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private static Map<String, String> signatureAdd(SignatureVO signatureVO) throws Exception {
        String accessKeyId = signatureVO.getAccessKeyId();
        String accessKeySecret = signatureVO.getAccessKeySecret();
        String service = signatureVO.getService();
        String host = signatureVO.getHost();
        String contentType = signatureVO.getContentType();
        String region = signatureVO.getRegion();
        String action = signatureVO.getAction();
        String version = signatureVO.getVersion();
        String algorithm = signatureVO.getAlgorithm();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 注意时区，否则容易出错
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Date 为 UTC 标准时间的日期，取值需要和公共参数 X-TC-Timestamp 换算的 UTC 标准时间日期一致
        String date = sdf.format(new Date(Long.parseLong(timestamp + "000")));

        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = signatureVO.getHttpRequestMethod();
        String canonicalUri = "/";
        String canonicalQueryString = signatureVO.getCanonicalQueryString();
        String canonicalHeaders = signatureVO.getCanonicalHeaders();
        String signedHeaders = signatureVO.getSignedHeaders();

        String payload = signatureVO.getPayload();
        String hashedRequestPayload = sha256Hex(payload);
        String canonicalRequest =
                httpRequestMethod + "\n"
                        + canonicalUri + "\n"
                        + canonicalQueryString + "\n"
                        + canonicalHeaders + "\n"
                        + signedHeaders + "\n"
                        + hashedRequestPayload;

        // ************* 步骤 2：拼接待签名字符串 *************
        String credentialScope = date + "/" + service + "/" + "sl_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign =
                algorithm + "\n"
                        + timestamp + "\n"
                        + credentialScope + "\n"
                        + hashedCanonicalRequest;

        // ************* 步骤 3：计算签名 *************
        byte[] secretDate = hmac256(("SL" + accessKeySecret).getBytes(UTF8), date);
        byte[] secretService = hmac256(secretDate, service);
        byte[] secretSigning = hmac256(secretService, "sl_request");
        String signature = DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();

        // ************* 步骤 4：拼接 Authorization *************
        String authorization = algorithm + " " + "Credential=" + accessKeyId + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature + "sl_request";

        Map<String, String> header = new TreeMap<String, String>();
        header.put("Authorization", authorization);
        header.put("Content-Type", contentType);
        header.put("Host", host);
        header.put("X-SL-Action", action);
        header.put("X-SL-Timestamp", timestamp);
        header.put("X-SL-Version", version);
        header.put("X-SL-Region", region);
        header.put("X-SL-Program-Language", "Java");
        header.put("SignatureVersion", "1");
        header.put("AccessKey", accessKeyId);

        StringBuilder sb = new StringBuilder();
        sb.append("curl -X POST https://").append(host).append("/?Action=").append(action)
                .append(" -H \"Authorization: ").append(authorization).append("\"")
                .append(" -H \"Content-Type: ").append(contentType).append("\"")
                .append(" -H \"Host: ").append(host).append("\"")
                .append(" -H \"X-SL-Action: ").append(action).append("\"")
                .append(" -H \"X-SL-Timestamp: ").append(timestamp).append("\"")
                .append(" -H \"X-SL-Version: ").append(version).append("\"")
                .append(" -H \"X-SL-Region: ").append(region).append("\"")
                .append(" -H \"X-SL-Program-Language: ").append("Java").append("\"")
                .append(" -H \"SignatureVersion: ").append("1").append("\"")
                .append(" -H \"AccessKey: ").append(accessKeyId).append("\"")
                .append(" -d '").append(payload).append("'");
        Log.i("zzh", "http signature=" + sb);
        return header;
    }

    public static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(UTF8));
    }

    public static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(UTF8));
        return DatatypeConverter.printHexBinary(d).toLowerCase();
        // return new String(Base64.decode(d, Base64.DEFAULT)).toLowerCase();
    }

    public static String bodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            Log.e("zzh", "body to string error", e);
            return e.toString();
        }
    }

    public static Request.Builder sign(String ak, String sk, Request request) {
        String query = request.url().query();
        String action = query.substring(query.indexOf("=") + 1);
        String bodyStr = bodyToString(request);
        Log.i("zzh", "request =" + request);
        Log.i("zzh", "request url=" + request.url());
        Log.i("zzh", "request body str=" + StandardAccessUtils.bodyToString(request));
        String host = "vod.streamlakeapi.com";
        SignatureVO signatureVO = new SignatureVO()
                .setAccessKeyId(ak)
                .setAccessKeySecret(sk)
                // .setProgramLanguage("Java")
                .setAlgorithm("SL-HMAC-SHA256")
                .setAction(action)
                .setHost(host)
                .setContentType("application/json")
                .setRegion("beijing")
                .setVersion("2022-06-23")
                .setService("vod")
                .setCanonicalHeaders("content-type:application/json\nhost:vod.streamlakeapi.com")
                .setCanonicalQueryString(query)
                .setHttpRequestMethod("POST")
                .setSignedHeaders("content-type;host")
                .setPayload(bodyStr);
        Map<String, String> map = new HashMap<>();
        try {
            if (request.url().toString().contains(host)) {
                map = signatureAdd(signatureVO);
            }
        } catch (Exception e) {
            Log.e("zzh", "sign error", e);
        }
        Request.Builder builder = request.newBuilder();
        if (map != null) {
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return builder;
    }
}
