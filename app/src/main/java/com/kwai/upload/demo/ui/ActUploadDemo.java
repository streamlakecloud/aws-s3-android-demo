package com.kwai.upload.demo.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.StringUtils;
import com.kwai.upload.demo.network.UploaderServiceNetwork;
import com.kwai.upload.demo.databinding.ActUploadDemoBinding;
import com.kwai.upload.demo.rsp.ApplyUploadRsp;
import com.kwai.upload.demo.rsp.BasicResponse;
import com.kwai.upload.demo.rsp.CommitUploadRsp;
import com.kwai.upload.demo.rsp.DeleteMediaRsp;
import com.kwai.upload.demo.rsp.DescribeMediaInfoRsp;
import com.kwai.upload.demo.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;


public class ActUploadDemo extends AppCompatActivity {
    private static final String TAG = "ActUploadDemo:zzh:whb";
    private ActUploadDemoBinding bind;
    boolean amsOK;
    private ApplyUploadRsp mApplyRsp;
    private CommitUploadRsp mUploadRsp;
    private String[] filePaths;

    public static void launch(Activity activity) {
        activity.startActivity(new Intent(activity, ActUploadDemo.class));
    }

    private ActivityResultLauncher<Intent> chooseFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Log.i(TAG, "choose file result=" + result);
            Intent data = result == null ? null : result.getData();
            Log.i(TAG, "choose file intent data=" + data);
            if (data == null) {
                return;
            }
            Uri uri = null;
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                filePaths = new String[count];
                for (int i = 0; i < count; i++) {
                    uri = data.getClipData().getItemAt(i).getUri();
                    String path = FileUtil.getFilePathByUri(ActUploadDemo.this, uri);
                    filePaths[i] = path;
                }
            } else if (data.getData() != null) {
                uri = data.getData();
                String path = FileUtil.getFilePathByUri(ActUploadDemo.this, uri);
                if (!StringUtils.isBlank(path)) {
                    filePaths = new String[]{path};
                }
            }
            if (filePaths == null || filePaths.length <= 0) {
                if (uri != null) {
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        // 上传完删掉这个临时文件
                        File tempFile = new File(getExternalFilesDir("temp"), uri.getLastPathSegment() + ".mp4");
                        OutputStream outputStream = new FileOutputStream(tempFile);
                        long count = 0;
                        int n;
                        int EOF = -1;
                        byte[] buffer = new byte[4096];
                        while (EOF != (n = is.read(buffer))) {
                            outputStream.write(buffer, 0, n);
                            count += n;
                        }
                        filePaths = new String[]{tempFile.getAbsolutePath()};
                    } catch (Exception e) {
                        Log.e(TAG, "choose file error", e);
                    }
                }
            }
            bind.tvChooseFileDesc.setText("选中的待上传文件：" + Arrays.toString(filePaths));
            bind.applyUploadBtn.setEnabled(filePaths != null && filePaths.length > 0 && filePaths[0] != null);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActUploadDemoBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        bind.selectBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFileLauncher.launch(Intent.createChooser(intent, "Select a File to Upload"));
        });
        bind.applyUploadBtn.setOnClickListener(v -> {
            File file = new File(filePaths[0]);
            UploaderServiceNetwork.applyUpload(file.getName(), ".mp4", null, null).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<BasicResponse<ApplyUploadRsp>>() {
                @Override
                public void accept(BasicResponse<ApplyUploadRsp> rsp) throws Exception {
                    mApplyRsp = rsp.responseData;
                    Log.i(TAG, "apply upload rsp=" + mApplyRsp);
                    bind.applyResultDesc.setText("申请结果：" + mApplyRsp);
                    bind.commitUploadBtn.setEnabled(mApplyRsp != null);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable e) throws Exception {
                    Log.e(TAG, "apply upload error=", e);
                    bind.applyResultDesc.setText("申请结果：" + e);
                }
            });
        });
        bind.commitUploadBtn.setOnClickListener(v -> {
            if (mApplyRsp == null || StringUtils.isBlank(mApplyRsp.sessionKey) || filePaths == null || filePaths.length <= 0) {
                Toast.makeText(this, "data not ready", Toast.LENGTH_SHORT).show();
                return;
            }
            amsOK = false;
            bind.amsUploadResultDesc.setText("ams上传ing.........");
            bind.commitResultDesc.setText("");
            File file = new File(filePaths[0]);
            Observable o1 = UploaderServiceNetwork.localUpdateByS3(file, mApplyRsp);
            // Observable o1 = UploaderServiceNetwork.localUpdateByS3_New(file, mApplyRsp);
            Observable o2 = UploaderServiceNetwork.commitUpload(mApplyRsp.sessionKey);
            Observable.concat(o1, o2).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer() {
                @Override
                public void onSubscribe(Disposable d) {
                    Log.i(TAG, "test accept onSubscribe d=" + d + " thread=" + Thread.currentThread());
                }

                @Override
                public void onNext(Object o) {
                    Log.i(TAG, "test accept onNext o=" + o + " thread=" + Thread.currentThread());
                    if (o instanceof PutObjectResult) {
                        amsOK = true;
                        bind.amsUploadResultDesc.setText("ams上传 OK " + o);
                        bind.commitResultDesc.setText("接口上传确认ing.........");
                    } else if (o instanceof BasicResponse) {
                        mUploadRsp = ((BasicResponse<CommitUploadRsp>) o).responseData;
                        bind.commitResultDesc.setText("接口上传确认 OK " + mUploadRsp);
                        FileUtil.deleteDirectory(getExternalFilesDir("temp"), false);
                        // 测试媒资信息
                        bind.btnMediaInfo.callOnClick();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.i(TAG, "test accept onError e=" + e + " thread=" + Thread.currentThread());
                    if (amsOK) {
                        bind.commitResultDesc.setText("接口上传确认 fail " + e);
                    } else {
                        bind.amsUploadResultDesc.setText("ams上传 fail " + e);
                    }
                }

                @Override
                public void onComplete() {
                    Log.i(TAG, "test accept onComplete  thread=" + Thread.currentThread());
                }
            });
        });

        bind.btnMediaInfo.setOnClickListener(v -> {
            // if (mAUploadRsp == null) {
            //     Toast.makeText(this, "mAUploadRsp is null", Toast.LENGTH_SHORT).show();
            //     return;
            // }
            // mediaId: eb207b7795d138f0
            // Observable o1 = UploaderServiceNetwork.describeMediaInfo("eb207b7795d138f0");
            // o1.observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
            //     @Override
            //     public void accept(Object o) {
            //         DescribeMediaInfoRsp rsp = ((BasicResponse<DescribeMediaInfoRsp>) o).responseData;
            //         Log.i(TAG, "query media info rsp=" + rsp);
            //         bind.mediaInfo.setText("媒资信息：" + String.valueOf(rsp));
            //     }
            // }, new Consumer<Throwable>() {
            //     @Override
            //     public void accept(Throwable e) {
            //         Log.e(TAG, "query media info error=", e);
            //         bind.mediaInfo.setText("媒资信息：" + String.valueOf(e));
            //     }
            // });
        });

        bind.btnDelMediaInfo.setOnClickListener(v -> {
            // if (mAUploadRsp == null) {
            //     Toast.makeText(this, "mAUploadRsp is null", Toast.LENGTH_SHORT).show();
            //     return;
            // }
            // Observable o1 = UploaderServiceNetwork.deleteMedia("eb207b7795d138f0", new ArrayList<>());
            // o1.observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
            //     @Override
            //     public void accept(Object o) {
            //         DeleteMediaRsp rsp = ((BasicResponse<DeleteMediaRsp>) o).responseData;
            //         Log.i(TAG, "delete media rsp=" + rsp);
            //         Toast.makeText(ActUploadDemo.this, "删除结果：" + rsp, Toast.LENGTH_LONG).show();
            //     }
            // }, new Consumer<Throwable>() {
            //     @Override
            //     public void accept(Throwable e) {
            //         Log.e(TAG, "delete media error=", e);
            //         Toast.makeText(ActUploadDemo.this, "delete media error=" + e, Toast.LENGTH_LONG).show();
            //     }
            // });
        });

    }
}