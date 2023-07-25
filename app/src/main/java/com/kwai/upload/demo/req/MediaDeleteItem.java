package com.kwai.upload.demo.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * author: zhouzhihui
 * created on: 2023/7/13 17:54
 * description:
 */
public class MediaDeleteItem implements Serializable {
    /*
    所指定的删除部分、可选值有：
    MezzanineFiles（删除源文件）
    TranscodeFilesByTemplate（按转码模板删除转码文件）
    * */
    @SerializedName(value = "Type", alternate = {"type"})
    public String type;

    /*
    * 删除由Type参数指定的种类下的转码模板ID（当Type为TranscodeFilesByTemplate时才生效）
        如果不传表示删除参数Type指定种类下所有的视频。
    * */
    @SerializedName(value = "TemplateId", alternate = {"templateId"})
    public String templateId;
}
