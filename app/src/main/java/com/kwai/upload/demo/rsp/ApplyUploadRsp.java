package com.kwai.upload.demo.rsp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * author: zhouzhihui
 * created on: 2023/7/12 11:12
 * description:
 */
public class ApplyUploadRsp implements Serializable {
    @SerializedName(value = "SessionKey", alternate = {"sessionKey"})
    public String sessionKey;

    @SerializedName(value = "UploadAddress", alternate = {"uploadAddress"})
    public UploadAddress uploadAddress;

    @SerializedName(value = "UploadAuth", alternate = {"uploadAuth"})
    public UploadAuth uploadAuth;

    @Override
    public String toString() {
        return "ApplyUploadRsp{" +
                "sessionKey='" + sessionKey + '\'' +
                ", uploadAddress=" + uploadAddress +
                ", uploadAuth=" + uploadAuth +
                '}';
    }

    public static class UploadAddress implements Serializable {
        @SerializedName(value = "StorageBucket", alternate = {"storageBucket"})
        public String StorageBucket; // "mediacloud-demo_test_demo_test_video"

        @SerializedName(value = "Region", alternate = {"region"})
        public String region; // "cn-beijing"

        @SerializedName(value = "UploadEndpoint", alternate = {"uploadEndpoint"})
        public String uploadEndpoint; // "kms-cn-beijing.streamlakeapi.com"

        @SerializedName(value = "UploadPath", alternate = {"uploadPath"})
        public String uploadPath; // "media/Cma5pWRiKbsAAAAAAOD3OA1686730109/test.mp4",  /{mediaSort}/{sessionKey}/{fileNme}

        @Override
        public String toString() {
            return "{" +
                    "StorageBucket='" + StorageBucket + '\'' +
                    ", region='" + region + '\'' +
                    ", uploadEndpoint='" + uploadEndpoint + '\'' +
                    ", uploadPath='" + uploadPath + '\'' +
                    '}';
        }
    }

    public static class UploadAuth implements Serializable {
        @SerializedName(value = "SecretId", alternate = {"secretId"})
        public String secretId; // "iJcsBHyaYXrqKLqV"

        @SerializedName(value = "SecretKey", alternate = {"secretKey"})
        public String secretKey;

        @SerializedName(value = "Token", alternate = {"token"})
        public String token;

        @SerializedName(value = "ExpiredTime", alternate = {"expiredTime"})
        public long expiredTime; // 1686737309984

        @Override
        public String toString() {
            return "{" +
                    "secretId='" + secretId + '\'' +
                    ", secretKey='" + secretKey + '\'' +
                    ", token='" + token + '\'' +
                    ", expiredTime=" + expiredTime +
                    '}';
        }
    }
}