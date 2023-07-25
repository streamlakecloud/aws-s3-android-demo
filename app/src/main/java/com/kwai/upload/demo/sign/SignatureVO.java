package com.kwai.upload.demo.sign;

public class SignatureVO {
    /**
     * StreamLake 密钥ak
     */
    private String accessKeyId;
    /**
     * StreamLake 密钥sk
     */
    private String accessKeySecret;
    /**
     * StreamLake 加签算法
     */
    private String algorithm;
    /**
     * StreamLake 服务编码
     */
    private String service;
    /**
     * StreamLake request host
     */
    private String host;
    /**
     * StreamLake request content-type
     */
    private String contentType;
    /**
     * StreamLake request region
     */
    private String region;
    /**
     * StreamLake request action
     */
    private String action;
    /**
     * StreamLake request version
     */
    private String version;
    /**
     * HTTP 请求方法（GET、POST ）
     */
    private String httpRequestMethod;
    /**
     * 发起 HTTP 请求 URL 中的查询字符串，
     * 对于 GET 请求，则为 URL 中问号（?）后面的字符串内容，例如：Limit=10&Offset=0。
     */
    private String canonicalQueryString;
    /**
     * 参与签名的头部信息，
     * 至少包含 host 和 content-type 两个头部，
     * 也可加入自定义的头部参与签名以提高自身请求的唯一性和安全性。
     */
    private String canonicalHeaders;
    /**
     * 参与签名的头部信息，说明此次请求有哪些头部参与了签名，
     * 和 CanonicalHeaders 包含的头部内容是一一对应的。
     * content-type 和 host 为必选头部。
     */
    private String signedHeaders;
    /**
     * 请求正文
     */
    private String payload;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public SignatureVO setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
        return this;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public SignatureVO setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
        return this;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public SignatureVO setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public String getService() {
        return service;
    }

    public SignatureVO setService(String service) {
        this.service = service;
        return this;
    }

    public String getHost() {
        return host;
    }

    public SignatureVO setHost(String host) {
        this.host = host;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public SignatureVO setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public SignatureVO setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getAction() {
        return action;
    }

    public SignatureVO setAction(String action) {
        this.action = action;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SignatureVO setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getHttpRequestMethod() {
        return httpRequestMethod;
    }

    public SignatureVO setHttpRequestMethod(String httpRequestMethod) {
        this.httpRequestMethod = httpRequestMethod;
        return this;
    }

    public String getCanonicalQueryString() {
        return canonicalQueryString;
    }

    public SignatureVO setCanonicalQueryString(String canonicalQueryString) {
        this.canonicalQueryString = canonicalQueryString;
        return this;
    }

    public String getCanonicalHeaders() {
        return canonicalHeaders;
    }

    public SignatureVO setCanonicalHeaders(String canonicalHeaders) {
        this.canonicalHeaders = canonicalHeaders;
        return this;
    }

    public String getSignedHeaders() {
        return signedHeaders;
    }

    public SignatureVO setSignedHeaders(String signedHeaders) {
        this.signedHeaders = signedHeaders;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public SignatureVO setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public SignatureVO() {
    }
}
