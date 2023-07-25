package com.kwai.upload.demo.rsp;

import com.amazonaws.util.StringUtils;
import com.google.gson.annotations.SerializedName;

public class BasicResponse<T> {
    @SerializedName("ResponseMeta")
    public ResponseMeta responseMeta;

    @SerializedName("ResponseData")
    public T responseData;

    public boolean isSuccess() {
        return responseMeta != null && ("0".equals(responseMeta.getErrorCode()) || StringUtils.isBlank(responseMeta.getErrorCode()));
    }
}
