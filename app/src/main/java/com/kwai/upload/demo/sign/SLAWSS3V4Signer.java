package com.kwai.upload.demo.sign;

import com.amazonaws.AmazonClientException;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.internal.AWSS3V4Signer;
import com.amazonaws.util.BinaryUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * author: zhouzhihui
 * created on: 2023/7/13 11:20
 * description:
 * copied from https://docs.jdcloud.com/cn/object-storage-service/sdk-android
 */
public class SLAWSS3V4Signer extends AWSS3V4Signer {
    @Override
    protected void processRequestPayload(Request<?> request, AWS4Signer.HeaderSigningResult headerSigningResult) {
    }

    @Override
    protected String calculateContentHash(Request<?> request) {
        request.addHeader("x-amz-content-sha256", "required");
        final String contentLength =
                request.getHeaders().get(Headers.CONTENT_LENGTH);
        final long originalContentLength;
        if (contentLength != null) {
            originalContentLength = Long.parseLong(contentLength);
        } else {
            /**
             * "Content-Length" header could be missing if the caller is
             * uploading a stream without setting Content-Length in
             * ObjectMetadata. Before using sigv4, we rely on HttpClient to
             * add this header by using BufferedHttpEntity when creating the
             * HttpRequest object. But now, we need this information
             * immediately for the signing process, so we have to cache the
             * stream here.
             */
            try {
                originalContentLength = getContentLength(request);
            } catch (IOException e) {
                throw new AmazonClientException(
                        "Cannot get the content-length of the request content.",
                        e);
            }
        }
        request.addHeader("x-amz-decoded-content-length",
                Long.toString(originalContentLength));
        final InputStream payloadStream = getBinaryRequestPayloadStream(request);
        payloadStream.mark(-1);
        final String contentSha256 = BinaryUtils.toHex(hash(payloadStream));
        try {
            payloadStream.reset();
        } catch (final IOException e) {
            throw new AmazonClientException(
                    "Unable to reset stream after calculating AWS4 signature", e);
        }
        return contentSha256;
    }

    private long getContentLength(Request<?> request) throws IOException {
        InputStream content = request.getContent();
        if (!content.markSupported()) {
            throw new AmazonClientException("Failed to get content length");
        }
        final int DEFAULT_BYTE_LENGTH = 4096;
        long contentLength = 0;
        byte[] tmp = new byte[DEFAULT_BYTE_LENGTH];
        int read;
        content.mark(-1);
        while ((read = content.read(tmp)) != -1) {
            contentLength += read;
        }
        content.reset();
        return contentLength;
    }
}
