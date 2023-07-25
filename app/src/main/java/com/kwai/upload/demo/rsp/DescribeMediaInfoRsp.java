package com.kwai.upload.demo.rsp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * author: zhouzhihui
 * created on: 2023/7/13 15:55
 * description:
 */
public class DescribeMediaInfoRsp implements Serializable {
    @SerializedName(value = "MediaId", alternate = {"mediaId"}) public String mediaId; // 视频ID

    @SerializedName(value = "BasicInfo", alternate = {"basicInfo"}) public BasicInfo basicInfo; // 媒资基础信息

    @SerializedName(value = "SourceInfo", alternate = {"sourceInfo"}) public SourceInfo sourceInfo; // 原文件信息

    @SerializedName(value = "TranscodeInfos", alternate = {"transcodeInfos"}) public List<TranscodeInfo> transcodeInfos; // 转码文件信息列表

    @SerializedName(value = "SnapshotInfo", alternate = {"snapshotInfo"}) public SnapshotInfo snapshotInfo; // 截图信息

    @Override
    public String toString() {
        return "{" +
                "mediaId='" + mediaId + '\'' +
                ", basicInfo=" + basicInfo +
                ", sourceInfo=" + sourceInfo +
                ", transcodeInfos=" + transcodeInfos +
                ", snapshotInfo=" + snapshotInfo +
                '}';
    }

    public static class BasicInfo implements Serializable {
        @SerializedName(value = "SubAppId", alternate = {"subAppId"}) public String subAppId; // 视频ID
        @SerializedName(value = "MediaId", alternate = {"mediaId"}) public String mediaId; // 视频ID
        @SerializedName(value = "Title", alternate = {"title"}) public String title; // 视频ID
        @SerializedName(value = "Description", alternate = {"description"}) public String description; // 视频ID
        @SerializedName(value = "CoverUrl", alternate = {"coverUrl"}) public String coverUrl; // 视频ID
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public String createTime; // 视频ID
        @SerializedName(value = "UpdateTime", alternate = {"updateTime"}) public String updateTime; // 视频ID
    }

    public static class SourceInfo implements Serializable {
        @SerializedName(value = "URLPath", alternate = {"uRLPath"}) public String URLPath; //
        @SerializedName(value = "PlayUrl", alternate = {"playUrl"}) public String playUrl; //
        @SerializedName(value = "Format", alternate = {"format"}) public String format; //
        @SerializedName(value = "Duration", alternate = {"duration"}) public double duration; // 文件时长，单位秒
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public String createTime; //
        @SerializedName(value = "Width", alternate = {"width"}) public int width; //
        @SerializedName(value = "Height", alternate = {"height"}) public int height; //
        @SerializedName(value = "Fps", alternate = {"fps"}) public double fps; // 帧率，单位：Hz
        @SerializedName(value = "Bitrate", alternate = {"bitrate"}) public int bitrate; // 文件码率,单位：Kbps
        @SerializedName(value = "FileSize", alternate = {"fileSize"}) public long fileSize; // 文件大小，单位：byte
        @SerializedName(value = "HdrType", alternate = {"hdrType"}) public String hdrType; // ● SDR● HDR10● HDR10+● Dolby Vision● HLG● SDR+
        @SerializedName(value = "VideoStreams", alternate = {"videoStreams"}) public List<VideoStream> videoStreams; //
        @SerializedName(value = "AudioStreams", alternate = {"audioStreams"}) public List<AudioStream> audioStreams; //

        @Override
        public String toString() {
            return "{" +
                    "URLPath='" + URLPath + '\'' +
                    ", playUrl='" + playUrl + '\'' +
                    ", format='" + format + '\'' +
                    ", duration=" + duration +
                    ", createTime='" + createTime + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", fps=" + fps +
                    ", bitrate=" + bitrate +
                    ", fileSize=" + fileSize +
                    ", hdrType='" + hdrType + '\'' +
                    ", videoStreams=" + videoStreams +
                    ", audioStreams=" + audioStreams +
                    '}';
        }
    }

    public static class TranscodeInfo implements Serializable {
        @SerializedName(value = "TranscodeTemplateId", alternate = {"transcodeTemplateId"}) public int transcodeTemplateId; //
        @SerializedName(value = "URLPath", alternate = {"uRLPath"}) public String URLPath; //
        @SerializedName(value = "PlayUrl", alternate = {"playUrl"}) public String playUrl; //
        @SerializedName(value = "Format", alternate = {"format"}) public String format; //
        @SerializedName(value = "Duration", alternate = {"duration"}) public double duration; // 文件时长，单位秒
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public String createTime; //
        @SerializedName(value = "Width", alternate = {"width"}) public int width; //
        @SerializedName(value = "Height", alternate = {"height"}) public int height; //
        @SerializedName(value = "Fps", alternate = {"fps"}) public double fps; // 帧率，单位：Hz
        @SerializedName(value = "FileSize", alternate = {"fileSize"}) public long fileSize; // 文件大小，单位：byte
        @SerializedName(value = "VideoMaxBitrate", alternate = {"videoMaxBitrate"}) public int videoMaxBitrate; // 峰值码率，单位：Kbps
        @SerializedName(value = "VideoStreams", alternate = {"videoStreams"}) public List<VideoStream> videoStreams; //
        @SerializedName(value = "AudioStreams", alternate = {"audioStreams"}) public List<AudioStream> audioStreams; //
        @SerializedName(value = "HdrType", alternate = {"hdrType"}) public String hdrType; // ● SDR● HDR10● HDR10+● Dolby Vision● HLG● SDR+
        @SerializedName(value = "Bitrate", alternate = {"bitrate"}) public int bitrate; // 文件码率,单位：Kbps
    }

    public static class SnapshotInfo implements Serializable {
        @SerializedName(value = "CoverSnapshotInfos", alternate = {"coverSnapshotInfos"}) public List<CoverSnapshotInfo> coverSnapshotInfos; //
        @SerializedName(value = "SampleSnapshotInfos", alternate = {"sampleSnapshotInfos"}) public List<SampleSnapshotInfo> sampleSnapshotInfos; //
        @SerializedName(value = "SpriteSnapshotInfos", alternate = {"spriteSnapshotInfos"}) public List<SpriteSnapshotInfo> spriteSnapshotInfos; //
        @SerializedName(value = "MaskSnapshotInfos", alternate = {"maskSnapshotInfos"}) public List<MaskSnapshotInfo> maskSnapshotInfos; //
    }

    public static class VideoStream implements Serializable {
        @SerializedName(value = "Duration", alternate = {"duration"}) public double duration; // 文件时长，单位秒
        @SerializedName(value = "Width", alternate = {"width"}) public int width; //
        @SerializedName(value = "Height", alternate = {"height"}) public int height; //
        @SerializedName(value = "Fps", alternate = {"fps"}) public double fps; // 帧率，单位：Hz
        @SerializedName(value = "Bitrate", alternate = {"bitrate"}) public int bitrate; // 文件码率,单位：Kbps
        @SerializedName(value = "Rotate", alternate = {"rotate"}) public String rotate; // ● 0● 90● 180● 270
        @SerializedName(value = "Codec", alternate = {"codec"}) public String codec; // 编码类型
    }

    public static class AudioStream implements Serializable {
        @SerializedName(value = "Duration", alternate = {"duration"}) public double duration; // 文件时长，单位秒
        @SerializedName(value = "Bitrate", alternate = {"bitrate"}) public int bitrate; // 文件码率,单位：Kbps
        @SerializedName(value = "Channels", alternate = {"channels"}) public int channels; // 声道
        @SerializedName(value = "SampleRate", alternate = {"sampleRate"}) public int sampleRate; // 采样率，单位Hz
        @SerializedName(value = "Codec", alternate = {"codec"}) public String codec; // 编码类型
    }

    public static class CoverSnapshotInfo implements Serializable {
        @SerializedName(value = "Name", alternate = {"name"}) public String name; //
        @SerializedName(value = "Type", alternate = {"type"}) public String type; //
        @SerializedName(value = "UrlPath", alternate = {"urlPath"}) public String urlPath; //
        @SerializedName(value = "CdnUrl", alternate = {"cdnUrl"}) public String cdnUrl; //
        @SerializedName(value = "Format", alternate = {"format"}) public String format; //
        @SerializedName(value = "Duration", alternate = {"duration"}) public double duration; // 文件时长，单位秒
        @SerializedName(value = "Width", alternate = {"width"}) public int width; //
        @SerializedName(value = "Height", alternate = {"height"}) public int height; //
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public long createTime; //
        @SerializedName(value = "StorageInfo", alternate = {"storageInfo"}) public StorageInfo storageInfo; //
    }

    public static class SampleSnapshotInfo implements Serializable {
        @SerializedName(value = "Name", alternate = {"name"}) public String name; //
        @SerializedName(value = "Type", alternate = {"type"}) public String type; //
        @SerializedName(value = "TemplateId", alternate = {"templateId"}) public String templateId; //
        @SerializedName(value = "Format", alternate = {"format"}) public String format; //
        @SerializedName(value = "Width", alternate = {"width"}) public int width; //
        @SerializedName(value = "Height", alternate = {"height"}) public int height; //
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public long createTime; //
        @SerializedName(value = "SampleSnapshotUrlInfos", alternate = {"sampleSnapshotUrlInfos"}) public List<SampleSnapshotUrlInfo> sampleSnapshotUrlInfos; //
    }

    public static class SpriteSnapshotInfo implements Serializable {
        @SerializedName(value = "Name", alternate = {"name"}) public String name; //
        @SerializedName(value = "Type", alternate = {"type"}) public String type; //
        @SerializedName(value = "UrlPath", alternate = {"urlPath"}) public String urlPath; //
        @SerializedName(value = "CdnUrl", alternate = {"cdnUrl"}) public String cdnUrl; //
        @SerializedName(value = "TemplateId", alternate = {"templateId"}) public String templateId; //
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public long createTime; //
        @SerializedName(value = "StorageInfo", alternate = {"storageInfo"}) public StorageInfo storageInfo; //
    }

    public static class MaskSnapshotInfo implements Serializable {
        @SerializedName(value = "Name", alternate = {"name"}) public String name; //
        @SerializedName(value = "Type", alternate = {"type"}) public String type; //
        @SerializedName(value = "UrlPath", alternate = {"urlPath"}) public String urlPath; //
        @SerializedName(value = "CdnUrl", alternate = {"cdnUrl"}) public String cdnUrl; //
        @SerializedName(value = "CreateTime", alternate = {"createTime"}) public long createTime; //
        @SerializedName(value = "StorageInfo", alternate = {"storageInfo"}) public StorageInfo storageInfo; //
    }

    public static class StorageInfo {
        @SerializedName(value = "StorageBucket", alternate = {"storageBucket"}) public String storageBucket; //
        @SerializedName(value = "StorageKey", alternate = {"storageKey"}) public String storageKey; //
    }

    public static class SampleSnapshotUrlInfo {
        @SerializedName(value = "UrlPath", alternate = {"urlPath"}) public String urlPath; //
        @SerializedName(value = "CdnUrl", alternate = {"cdnUrl"}) public String cdnUrl; //
        @SerializedName(value = "StorageInfo", alternate = {"storageInfo"}) public StorageInfo storageInfo; //
    }
}
