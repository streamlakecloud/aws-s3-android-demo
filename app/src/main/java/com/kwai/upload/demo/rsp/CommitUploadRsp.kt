package com.kwai.upload.demo.rsp

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * author: zhouzhihui
 * created on: 2023/7/12 11:12
 * description:
 */
class CommitUploadRsp : Serializable {
    @SerializedName("MediaId", alternate = ["mediaId"])
    var mediaId: String = ""

    @SerializedName("MediaSort", alternate = ["mediaSort"])
    var mediaSort = ""

    override fun toString(): String {
        return "{mediaId=${mediaId} mediaSort=${mediaSort}}"
    }
}