# AWS SDK upload demo for Android
本项目是一个示例实现， 使用aws-sdk-android，实现客户端上传到兼容 Amazon Web Services (AWS) 的 Simple Storage Service (S3)协议的StreamLake媒体存储功能


## Setup
```groovy
implementation 'com.amazonaws:aws-android-sdk-s3:2.54.0'
```

## Getting Started
使用aws-android-sdk-s3上传到StreamLake媒体存储，主要分为三步串行


### 申请上传
调用ApplyUploadInfo接口，curl描述如下，其中AccessKey为StreamLake颁发的用户唯一身份凭证

代码示例参考[UploadApiService.kt](./app/src/main/java/com/kwai/upload/demo/network/UploadApiService.kt)
```bash
curl --location 'vod.streamlakeapi.com/?Action=ApplyUploadInfo' \
--header 'AccessKey: ${custom_accesskey}' \
--header 'Content-Type: application/json' \
--data '{
    "FilePath": "test.mp4",
    "Format": "mp4"
}'
```
返回ApplyUploadInfo数据结构如下
```json
{
    "ResponseMeta": {
        "RequestId": "ClonG2SuiT4AAAAAAABOzA", 
        "ErrorCode": "0", 
        "ErrorMessage": "success"
    }, 
    "ResponseData": {
        "SessionKey": "ClonG2SuiT4AAAAAAABOzA1689221470", 
        "UploadAddress": {
            "StorageBucket": "sl-a07ff17534a71cd495", 
            "Region": "cn-beijing", 
            "UploadEndpoint": "kms-cn-beijing.streamlakeapi.com", 
            "UploadPath": "media/ClonG2SuiT4AAAAAAABOzA1689221470/video:6926.mp4"
        }, 
        "UploadAuth": {
            "SecretId": "", 
            "SecretKey": "", 
            "Token": "", 
            "ExpiredTime": 1689225070690
        }
    }
}
```

### 开始上传
拿到上传凭证后，调用aws-android-sdk-s3开始上传

代码示例参考 [UploadApiService.kt](./app/src/main/java/com/kwai/upload/demo/network/UploadApiService.kt)
```kotlin
SignerFactory.registerSigner("SLAWSS3V4Signer", SLAWSS3V4Signer::class.java)
                   
val configuration = ClientConfiguration().withProtocol(Protocol.HTTPS)

configuration.signerOverride = "SLAWSS3V4Signer"
val opts = S3ClientOptions.builder()
            .setPathStyleAccess(false)
            .setPayloadSigningEnabled(false)
            .disableChunkedEncoding()
            .skipContentMd5Check(true)
            .build()
var credentials = BasicAWSCredentials(applyRsp.uploadAuth.secretId, applyRsp.uploadAuth.secretKey)
val amazonS3Client = AmazonS3Client(credentials, configuration)
                    amazonS3Client.setS3ClientOptions(opts)
                    amazonS3Client.endpoint = applyRsp.uploadAddress.uploadEndpoint

val putObjectRequest = PutObjectRequest(
                applyRsp.uploadAddress.StorageBucket,
                applyRsp.uploadAddress.uploadPath,
                file
            )
val pores = amazonS3Client.putObject(putObjectRequest)
```

### 确认上传
上传完成后，调用确认上传CommitUpload接口，curl描述如下

代码示例参考 [UploadApiService.kt](./app/src/main/java/com/kwai/upload/demo/network/UploadApiService.kt)
```bash
curl --location 'vod.streamlakeapi.com/?Action=CommitUpload' \
--header 'AccessKey: ${access_key}' \
--header 'Content-Type: application/json' \
--data '{
    "SessionKey": "nv4o2hedf8h89h2"
}'
```
确认上传成功返回接口如下，其中MediaId为唯一媒资ID，可根据MediaId获取媒资的播放url或多码率媒资数据
```json
{
    "ResponseMeta": {
        "RequestId": "ClonG2SuiT4AAAAAAABOzw", 
        "ErrorCode": "0", 
        "ErrorMessage": "success"
    }, 
    "ResponseData": {
        "MediaId": "eb207b7795d138f0", 
        "MediaSort": "media", 
        "FileUrl": ""
    }
}
```


## Author
StreamLake Team



## License
Apache 2.0 License