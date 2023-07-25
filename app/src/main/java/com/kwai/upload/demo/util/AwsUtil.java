package com.kwai.upload.demo.util;

import com.amazonaws.util.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * author: zhouzhihui
 * created on: 2023/7/12 20:34
 * description:
 */
public class AwsUtil {
    // 以下 Java 代码示例描述了如何对提交到亚马逊的上传数据计算 Content-MD5：
    public static String computeContentMD5Header(FileInputStream fis )
            throws IOException, NoSuchAlgorithmException {

        DigestInputStream dis = new DigestInputStream( fis, MessageDigest.getInstance( "MD5" ));

        byte[] buffer = new byte[8192];
        while( dis.read( buffer ) > 0 );

        // String md5Content = new String(org.apache.commons.codec.binary.Base64.encodeBase64(dis.getMessageDigest().digest()));
        String md5Content = Base64.encodeAsString(dis.getMessageDigest().digest());
        // String md5Content = Base64.encodeToString(lBytes, Base64.NO_WRAP);

        // Effectively resets the stream to be beginning of the file
        // via a FileChannel.
        fis.getChannel().position( 0 );

        return md5Content;
    }

    // 以下 Java 代码示例描述了如何对下载的报告计算 MD5 校验和：
    public static String computeContentMD5Header(InputStream inputStream) {
        // Consume the stream to compute the MD5 as a side effect.
        DigestInputStream s;
        try {
            s = new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
            // drain the buffer, as the digest is computed as a side-effect
            byte[] buffer = new byte[8192];
            while(s.read(buffer) > 0);
            // return new String(org.apache.commons.codec.binary.Base64.encodeBase64(s.getMessageDigest().digest()), "UTF-8");
            return Base64.encodeAsString(s.getMessageDigest().digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
