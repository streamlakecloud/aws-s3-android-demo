package com.kwai.upload.demo.network

import android.util.Log
import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.auth.SignerFactory
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.util.Md5Utils
import com.kwai.upload.demo.AwsApp
import com.kwai.upload.demo.BuildConfig
import com.kwai.upload.demo.req.MediaDeleteItem
import com.kwai.upload.demo.rsp.*
import com.kwai.upload.demo.sign.SLAWSS3V4Signer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File


/**
 * author: zhouzhihui
 * created on: 2023/7/12 10:21
 * description:
 */
object UploaderServiceNetwork : BaseNetWork() {
    // 接口文档：https://docs.corp.kuaishou.com/k/home/VWzpf73ltfKI/fcACRW8Ds6c4dz-7xrg8CShdf#section=h.8bzipfuktn0b
    // ams s3 git：https://github.com/aws-amplify/aws-sdk-android/tree/main, https://github.com/awslabs/aws-sdk-android-samples/tree/main
    // per https://halo.corp.kuaishou.com/help/docs/b3b6c4c8bdc955a7affd4bccbec32095
    // 京东文档：https://docs.jdcloud.com/cn/object-storage-service/sdk-android
    private const val BASE_URL = "https://vod.streamlakeapi.com"
    private const val BASE_URL_TEST = "https://gw-streamlake-prt.test.gifshow.com"
    private val uploadApiService: UploadApiService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val test = BuildConfig.NETWORK_ENV_TEST
        Log.i("zzh:whb", "is net work test=${test}")
        getApi(UploadApiService::class.java, if (test) BASE_URL_TEST else BASE_URL)
    }


    /**
    SessionKey	String	否,上一次请求返回的会话 key，在重试时建议携带上
    MediaSort	String	否,媒资类型：（默认值为media）
    ● media - 媒资
    ● attachedMedia - 辅助媒资
    ● liveRecord - 直转点
    ● liveRecordRtc - RTC直转点
    ● liveRecordLive - Live直转点
    FilePath	String	否,本地文件路径，包含文件名
    ● 参数携带：以filepath为名字生成文件存储key
    ● 参数未携带：自动随机生成文件存储key
    Format	String	否,文件格式。
    如未指定Format，则以系统识别出的 Format 为准。若遇到特殊文件无法识别，Format 可能为空,（当前文件上传格式只支持视频与音频，其他格式暂不支持）
    StorageBucket	String	否,指定存储桶，默认账号下存储桶
     */
    @JvmStatic
    fun applyUpload(
        filePath: String,
        format: String,
        sessionKey: String? = null,
        mediaSort: String? = null
    ): Observable<BasicResponse<ApplyUploadRsp>> {
        return uploadApiService.applyUpload("ApplyUploadInfo", createRequestBody {
            put("FilePath", filePath)
            put("Format", format)
        }).subscribeOn(Schedulers.io())
    }

    @JvmStatic
    fun commitUpload(sessionKey: String): Observable<BasicResponse<CommitUploadRsp>> {
        return uploadApiService.commitUpload("CommitUpload", createRequestBody {
            put("SessionKey", sessionKey)
        }).subscribeOn(Schedulers.io())
    }

    @JvmStatic
    fun localUpdateByS3(file: File, applyRsp: ApplyUploadRsp): Observable<PutObjectResult> {
        val o1: Observable<PutObjectResult> =
            Observable.create(object : ObservableOnSubscribe<PutObjectResult> {
                override fun subscribe(e: ObservableEmitter<PutObjectResult>) {
                    /*
                    * 由于android sdk用chunk 方式进行传输，并且把chunk-signature放置在body中，oss不支持这种case；sdk中支持的参数 S3ClientOptions.builder.disableChunkedEncoding在判断的时候并没有采用，
                    * 因而需要更深层次的hack，需要继承AWSS3V4Signer.java并且覆盖原始的 processRequestPayload（计算payload的签名并放在body的开头）和calculateContentHash（长度包含签名部分），代码如下：
                    * */
                    SignerFactory.registerSigner("SLAWSS3V4Signer", SLAWSS3V4Signer::class.java)
                    // System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
                    val configuration = ClientConfiguration()
                        .withProtocol(Protocol.HTTPS)
                    configuration.signerOverride = "SLAWSS3V4Signer"
                    // configuration.setCurlLogging(true);
                    // per https://halo.corp.kuaishou.com/help/docs/b3b6c4c8bdc955a7affd4bccbec32095
                    val opts = S3ClientOptions.builder()
                        .setPathStyleAccess(false)
                        .setPayloadSigningEnabled(false)
                        .disableChunkedEncoding()
                        .skipContentMd5Check(true) // MUST skip. kos don't return etag which S3 sdk expects
                        .build()

                    val sessionCredentials = BasicSessionCredentials(
                        applyRsp.uploadAuth.secretId,
                        applyRsp.uploadAuth.secretKey,
                        applyRsp.uploadAuth.token
                    )
                    var credentials = BasicAWSCredentials(applyRsp.uploadAuth.secretId, applyRsp.uploadAuth.secretKey)
                    // var region: Region = Region(applyRsp.uploadAddress.region, "")
                    val amazonS3Client = AmazonS3Client(credentials, configuration)
                    amazonS3Client.setS3ClientOptions(opts)
                    amazonS3Client.endpoint = applyRsp.uploadAddress.uploadEndpoint
                    val objectMetadata = ObjectMetadata()
                    // objectMetadata.setHeader("ContentEncoding", "base64")
                    // objectMetadata.contentType = Mimetypes.MIMETYPE_OCTET_STREAM
                    // objectMetadata.setHeader("Content-Transfer-Encoding", "binary")
                    val md5Arr = Md5Utils.computeMD5Hash(file)
                    // val md5 = MD5Util.getMD5String(md5Arr)
                    // val md5_4 = AwsUtil.computeContentMD5Header(FileInputStream(file))
                    // val md5_5 = String(Base64.encode(md5Arr, Base64.NO_WRAP))
                    // val md5_6 = String(Base64.encode(md5Arr, Base64.DEFAULT))
                    // val md5_7 = (Base64.encodeToString(md5Arr, Base64.NO_WRAP))
                    // md5=UTXS+WGD6MuDQkfWm97cqA== etag=5135D2F96183E8CB834247D69BDEDCA8
                    // Log.i("zzh", "aws upload file md5=${md5} md5_5=${md5_5} md5_6=${md5_6} md5_7=${md5_7}") // ClonG2SuiT4AAAAAAAAIqw1689166419
                    // objectMetadata.contentMD5 = md5_7// manually set md5 here
                    // objectMetadata.setHeader(Headers.ETAG, md5_4)

                    val putObjectRequest = PutObjectRequest(
                        applyRsp.uploadAddress.StorageBucket,
                        applyRsp.uploadAddress.uploadPath,
                        file
                    )//.withMetadata(objectMetadata)
                    val pores = amazonS3Client.putObject(putObjectRequest)
                    if (pores == null) {
                        e.onError(Exception("pores is null"))
                    } else {
                        Log.i("zzh", "after upload md5=${pores.contentMd5} etag=${pores.eTag}")
                        e.onNext(pores)
                        e.onComplete()
                    }
                }
            }).subscribeOn(Schedulers.io())
        return o1
    }

    @JvmStatic
    fun localUpdateByS3_New(file: File, applyRsp: ApplyUploadRsp): Observable<PutObjectResult> {
        val o1: Observable<PutObjectResult> =
            Observable.create(object : ObservableOnSubscribe<PutObjectResult> {
                override fun subscribe(e: ObservableEmitter<PutObjectResult>) {
                    val configuration = ClientConfiguration()
                        .withProtocol(Protocol.HTTPS)
                    val opts = S3ClientOptions.builder()
                        .setPathStyleAccess(false)
                        .setPayloadSigningEnabled(false)
                        .disableChunkedEncoding()
                        .skipContentMd5Check(true) // MUST skip. kos don't return etag which S3 sdk expects
                        .build()
                    var credentials = BasicAWSCredentials(applyRsp.uploadAuth.secretId, applyRsp.uploadAuth.secretKey)
                    val amazonS3Client = AmazonS3Client(credentials, configuration)
                    amazonS3Client.setS3ClientOptions(opts)
                    amazonS3Client.endpoint = applyRsp.uploadAddress.uploadEndpoint

                    val trans = TransferUtility.builder().context(AwsApp.INSTANCE).s3Client(amazonS3Client).build()
                    val observer: TransferObserver = trans.upload(applyRsp.uploadAddress.StorageBucket, applyRsp.uploadAddress.uploadPath, file)//manual storage permission
                    observer.setTransferListener(object : TransferListener {
                        override fun onStateChanged(id: Int, state: TransferState) {
                            if (state == TransferState.COMPLETED) {
                                Log.d("zzh","localUpdateByS3 new success")
                            } else if (state == TransferState.FAILED) {
                                Log.d("zzh","localUpdateByS3 new fail")
                            }
                        }
                        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                            if(bytesCurrent == bytesTotal){
                            }
                        }
                        override fun onError(id: Int, ex: Exception) {
                            Log.e("zzh", "localUpdateByS3 new error", ex)
                        }
                    })
                    e.onNext(PutObjectResult())
                    e.onComplete()
                }
            }).subscribeOn(Schedulers.io())
        return o1
    }

    @JvmStatic
    fun describeMediaInfo(mediaId: String): Observable<BasicResponse<DescribeMediaInfoRsp>> {
        // return uploadApiService.describeMediaInfo("DescribeMediaInfo", createRequestBody {
        //     put("MediaId", mediaId)
        // }).subscribeOn(Schedulers.io())
        return Observable.just(BasicResponse())
    }

    @JvmStatic
    fun deleteMedia(mediaId: String, deleteItems: List<MediaDeleteItem>? = emptyList()): Observable<BasicResponse<DeleteMediaRsp>> {
        // return uploadApiService.deleteMedia("DeleteMedia", createRequestBody {
        //     put("MediaId", mediaId)
        //     // put("DeleteItems", deleteItems)
        // }).subscribeOn(Schedulers.io())
        return Observable.just(BasicResponse())
    }

    @JvmStatic
    fun test(): Observable<Int> {
        val o2: Observable<Int> = Observable.create(
            ObservableOnSubscribe<Int> { e ->
                // e.onError(Exception("test o2 error"))
                e.onNext(2)
                e.onComplete()
            }).subscribeOn(Schedulers.io())
        return o2
    }

    private fun createRequestBody(params: JSONObject.() -> Unit): RequestBody {
        val str = JSONObject().apply {
            params()
        }.toString()
        return RequestBody.create(MediaType.parse("Content-Type, application/json"), str)
    }
}