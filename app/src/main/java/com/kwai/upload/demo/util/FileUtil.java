package com.kwai.upload.demo.util;

import static android.os.Environment.DIRECTORY_DCIM;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.zxhy.android.greenscreen.base.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * author: zhouzhihui
 * created on: 2023/7/12 12:11
 * description:
 */
public class FileUtil {
    private static final List<String> IMG_LIST = Arrays.asList("png", "jpg", "jpeg", "bmp", "gif");
    private static final List<String> VIDEO_LIST = Arrays.asList("mp4", "m2v", "mkv", "mov", "avi", "flv", "wmv");
    private static final List<String> AUDIO_LIST = Arrays.asList("mp3", "wav", "ogg");
    /**
     * 拍照后的临时保存路径，用于下一步的编辑
     */
    private static final String TMP_PHOTO_NAME = "photo.jpg";
    public static final String TEMPLATE_PREFIX = "template_";
    private static final String TAG = "FileUtils";

    /**
     * 保存图片,SDK > 28, https://zhuanlan.zhihu.com/p/452181115
     */
    private void insertImage(Context context, Bitmap bitmap) {
        // 拿到 MediaStore.Images 表的uri
        Uri tableUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 创建图片索引
        ContentValues value = new ContentValues();
        value.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        value.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        value.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/devdroid");
        value.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        // 将该索引信息插入数据表，获得图片的Uri
        Uri imageUri = context.getContentResolver().insert(tableUri, value);
        try {
            // 通过图片uri获得输出流
            OutputStream os = context.getContentResolver().openOutputStream(imageUri);
            // 图片压缩保存
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidImageFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            return true;
        }
        return false;
    }

    public static Uri getUriForFile(Context context, File file, String appId) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24
            uri = FileProvider.getUriForFile(context, appId + ".fileProvider", file);
            context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 19
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) { // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) { // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
                path = getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
                path = uri.getPath();
            }
        } else {
            path = getDataColumn(context, uri, null, null);
        }
        return path;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            LogUtil.INSTANCE.e(TAG, "get data column error", e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isVideo(String absolutePath) {
        return !StringUtil.isEmpty(absolutePath) && VIDEO_LIST.contains(getFileSuffix(absolutePath));
    }

    public static long calcVideoFileDuration(String absolutePath) {
        LogUtil.INSTANCE.i(TAG, "start launch record video calc");
        long duration = 0;
        if (StringUtil.isEmpty(absolutePath) || !isVideo(absolutePath)) {
            return duration;
        }
        String longStr = "nil";
        try {
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14) {
                retr.setDataSource(absolutePath, new HashMap<String, String>()); // for link url
            } else {
                retr.setDataSource(absolutePath);
            }
            longStr = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // ms
            retr.release();
            duration = Long.parseLong(longStr);
        } catch (Exception e) {
            LogUtil.INSTANCE.e(TAG, "calc video file duration error", e);
            e.printStackTrace();
        }
        LogUtil.INSTANCE.i(TAG, "end launch record video calc longStr=" + longStr + " absolutePath=" + absolutePath);
        return duration;
    }

    public static String getFileSuffix(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "";
        }
        String[] arr = fileName.split("\\.");
        return arr[arr.length - 1];
    }

    public static File saveToLocalDcm(Bitmap bitmap, String fileName) {
        if (null == bitmap) return null;
        String temp = fileName;
        File dcimFile = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);
        File tempFile = new File(dcimFile, temp);
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            tempFile = null;
        } catch (IOException e) {
            e.printStackTrace();
            tempFile = null;
        }
        return tempFile;
    }

    public static String saveTempBitmap(Bitmap bitmap, File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        try (FileOutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(format, quality, stream);
            stream.flush();
        }
        return file.getAbsolutePath();
    }

    public static File getSavePathFile(Context context) {
        return new File(getExternalFileDir(context), TMP_PHOTO_NAME);
    }

    public static String getSavePath(Context context) {
        return getSavePathFile(context).getAbsolutePath();
    }

    public static void copyFile(File src, File dest) throws IOException {
        copyFile(new FileInputStream(src), dest);
    }

    public static void copyFile(InputStream is, File dest) throws IOException {
        if (is == null) {
            return;
        }
        if (dest.exists()) {
            dest.delete();
        }
        try (BufferedInputStream bis = new BufferedInputStream(is); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest))) {
            byte[] bytes = new byte[1024 * 10];
            int length;
            while ((length = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);
            }
            bos.flush();
        }
    }

    /**
     * 应用外部文件目录
     *
     * @param context
     * @return
     */
    public static File getExternalFileDir(Context context) {
        File fileDir = context.getExternalFilesDir(null);
        if (fileDir == null) {
            fileDir = context.getFilesDir();
        }
        return fileDir;
    }

    /**
     * 应用外部的缓存目录
     *
     * @param context
     * @return
     */
    public static File getExternalCacheDir(Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir;
    }

    public static File getCacheDir(Context context, String child) {
        File cacheDir = new File(context.getCacheDir(), child);
        cacheDir.mkdirs();
        return cacheDir;
    }

    public static File getThumbnailDir(Context context) {
        File fileDir = getExternalFileDir(context);
        File thumbDir = new File(fileDir, "thumb");
        if (!thumbDir.exists()) {
            thumbDir.mkdirs();
        }
        return thumbDir;
    }

    /**
     * 生成唯一标示
     *
     * @return
     */
    public static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * 计算文件的 MD5
     *
     * @param file
     * @return
     */
    public static String getMd5ByFile(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file)) {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            return bi.toString(16);
        }
    }

    public static String readStringFromAssetsFile(Context context, String path) throws IOException {
        try (InputStream is = context.getAssets().open(path)) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            return new String(bytes);
        }
    }


    private static void copyAssetsFile(Context context, File dir, String assetsPath) {
        String fileName = assetsPath.substring(assetsPath.lastIndexOf("/") + 1);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, fileName);
        if (!dest.exists()) {
            try {
                InputStream is = context.getAssets().open(assetsPath);
                copyFile(is, dest);
            } catch (IOException e) {
                LogUtil.INSTANCE.e(TAG, "copyAssetsFile: ", e);
            }
        }
    }

    public static void copyAssetsFileToLocal(Context context, File dir, String assetsPath) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        AssetManager assets = context.getAssets();
        try {
            String[] files = assets.list(assetsPath);
            if (files != null) {
                for (String file : files) {
                    InputStream is = assets.open(assetsPath + File.separator + file);
                    File dest = new File(dir, file);
                    copyFile(is, dest);
                }
            }
        } catch (IOException e) {
            LogUtil.INSTANCE.e(TAG, "copyAssetsFile: ", e);
        }
    }

    public static String readStringFromFile(File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytes = new byte[bis.available()];
            bis.read(bytes);
            return new String(bytes);
        }
    }

    /**
     * 把外部文件拷贝到应用私有目录
     *
     * @param srcFile
     * @param destDir
     * @return
     * @throws IOException
     */
    public static File copyExternalFileToLocal(File srcFile, File destDir) throws IOException {
        if (!srcFile.exists()) {
            throw new IOException("Source file don't exits");
        }
        if (!destDir.exists()) {
            boolean b = destDir.mkdirs();
            if (!b) {
                throw new IOException("Make dest dir failed");
            }
        }
        String name = srcFile.getName();
        String type = name.substring(name.lastIndexOf("."), name.length());
        String md5ByFile = null;
        try {
            md5ByFile = getMd5ByFile(srcFile);
        } catch (Exception e) {
            md5ByFile = getUUID32();
            LogUtil.INSTANCE.e(TAG, "copyExternalFileToLocal: ", e);
        }
        File dest = new File(destDir, md5ByFile + type);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile)); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest))) {
            byte[] bytes = new byte[1024 * 10];
            int length;
            while ((length = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);
            }
            bos.flush();
        }
        return dest;
    }

    public static void deleteDirectory(File root, boolean deleteSelf) {
        if (root == null || !root.exists()) {
            return;
        }
        if (root.isDirectory()) {
            if (root.listFiles() == null || root.listFiles().length <= 0) {
                if (deleteSelf) {
                    root.delete();
                }
            } else {
                for (File f : root.listFiles()) {
                    deleteDirectory(f, true);
                }
            }
        } else {
            if (deleteSelf) {
                root.delete();
            }
        }
    }

    public static File getTmpDir(Context context) {
        File targetDir = context.getExternalFilesDir("tmp");
        if (targetDir.exists()) {
            if (!targetDir.isDirectory()) {
                targetDir.delete();
                targetDir.mkdirs();
            }
        } else {
            targetDir.mkdirs();
        }
        return targetDir;
    }

    public static File getTmpVideoFrameDir(Context context) {
        File tmpFile = getTmpDir(context);
        File file = new File(tmpFile, String.valueOf(System.currentTimeMillis()));
        if (file.exists()) {
            if (!file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }
        } else {
            file.mkdirs();
        }
        return file;
    }

    public static File getVideosDir(Context context) {
        File targetDir = context.getExternalFilesDir("video_files");
        if (targetDir.exists()) {
            if (!targetDir.isDirectory()) {
                targetDir.delete();
                targetDir.mkdirs();
            }
        } else {
            targetDir.mkdirs();
        }
        return targetDir;
    }

    public static void deleteFile(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static boolean isFileExists(String path) {
        return !StringUtil.isEmpty(path) && new File(path).exists();
    }

    public static boolean filesExist(String[] fileArray) {
        for (String file : fileArray) {
            if (!isFileExists(file))
                return false;
        }
        return true;
    }

    public static String getFileNameNoExt(String filePath) {
        int start = filePath.lastIndexOf("/");
        int end = filePath.lastIndexOf(".");
        if (start != -1 && end != -1 && end > start + 1) {
            return filePath.substring(start + 1, end);
        } else {
            return "";
        }
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    // public static File findFileByName(File root, String targetFileName) {
    //     if (root == null || root.listFiles() == null || StringUtil.isEmpty(targetFileName)) {
    //         return null;
    //     }
    //     for (File i : root.listFiles()) {
    //         if (i.isDirectory()) {
    //             File next = findFileByName(i, targetFileName);
    //             if (next != null) {
    //                 return next;
    //             }
    //         } else if (i.exists()) {
    //             String path = i.getAbsolutePath();
    //             if (i.getName().contains(targetFileName) && path.endsWith(Const.VIDEO_TO_IMG_FILE_SUFFIX)) {
    //                 return i;
    //             } else if (i.getName().equals(targetFileName)) {
    //                 return i;
    //             }
    //         }
    //     }
    //     return null;
    // }

    public static void toZip(String srcFileString, String zipFileString) {
        FileOutputStream fos1 = null;
        ZipOutputStream zip = null;
        try {
            fos1 = new FileOutputStream(new File(zipFileString));
            zip = new ZipOutputStream(fos1);
            File f = new File(srcFileString);
            compress(f, zip, f.getName());
            zip.flush();
            zip.close();
            fos1.close();
        } catch (Exception e) {
            try {
                if (zip != null) zip.close();
                if (fos1 != null) fos1.close();
            } catch (IOException ee) {
            }

        }
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name) throws Exception {
        int BUFFER_SIZE = 2 * 1024;
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 空文件夹的处理
                zos.putNextEntry(new ZipEntry(name + "/"));
                // 没有文件，不需要文件的copy
                zos.closeEntry();

            } else {
                for (File file : listFiles) {
                    compress(file, zos, name + "/" + file.getName());
                }
            }
        }
    }
}
