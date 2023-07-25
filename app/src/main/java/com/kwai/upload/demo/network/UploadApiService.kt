package com.kwai.upload.demo.network

import com.kwai.upload.demo.rsp.*
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


/**
 * author: zhouzhihui
 * created on: 2023/7/12 10:23
 * description:
 */
interface UploadApiService {
    @POST("/")
    fun applyUpload(
        @Query("Action") action: String,
        @Body body: RequestBody
    ): Observable<BasicResponse<ApplyUploadRsp>>

    @POST("/")
    fun commitUpload(
        @Query("Action") action: String,
        @Body body: RequestBody
    ): Observable<BasicResponse<CommitUploadRsp>>

    // @POST("/")
    // fun describeMediaInfo(
    //     @Query("Action") action: String,
    //     @Body body: RequestBody
    // ): Observable<BasicResponse<DescribeMediaInfoRsp>>
    //
    // @POST("/")
    // fun deleteMedia(
    //     @Query("Action") action: String,
    //     @Body body: RequestBody
    // ): Observable<BasicResponse<DeleteMediaRsp>>
}