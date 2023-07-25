package com.kwai.upload.demo.rsp

import com.google.gson.annotations.SerializedName

class ResponseMeta {
    @SerializedName("RequestId")
    var requestId: String = ""

    @SerializedName("ErrorCode")
    var errorCode: String = ""

    @SerializedName("ErrorMessage")
    var errorMessage: String = ""

}