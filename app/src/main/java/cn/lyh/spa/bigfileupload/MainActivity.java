package cn.lyh.spa.bigfileupload;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lyh.spa.bigfileupload.network.HttpConstants;
import cn.lyh.spa.bigfileupload.utils.MIOUtils;
import cn.lyh.spa.bigfileupload.utils.MMM;
import cn.lyh.spa.bigfileupload.utils.fenpian.FenPian;
import cn.lyh.spa.bigfileupload.utils.fenpian.FinishListener;
import cn.lyh.spa.bigfileupload.utils.fenpian.Fthread;
import cn.lyh.spa.bigfileupload.utils.fenpian.MessageThread;
import cn.lyh.spa.bigfileupload.utils.fenpian.Mthread;
import cn.lyh.spa.bigfileupload.utils.fenpian.ThreadPool;
import cn.lyh.spa.bigfileupload.utils.md5.MD5resultListener;
import cn.lyh.spa.bigfileupload.utils.md5.MD5utils;
import spa.lyh.cn.peractivity.PermissionActivity;
import spa.lyh.cn.utils_io.IOUtils;

public class MainActivity extends PermissionActivity {
    private TextView path,process,md5T,size;

    private String stringPath;

    private String md5String;

    private long startOffSet = 0;

    Uri uri;

    MessageThread msgT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = findViewById(R.id.path);
        stringPath = "/storage/emulated/0/a.apk";
        path.setText(stringPath);
        process = findViewById(R.id.process);
        size = findViewById(R.id.size);
        md5T = findViewById(R.id.md5);
        MD5utils.getFileMD5sync(this,path.getText().toString(), new MD5resultListener() {
            @Override
            public void onResult(String md5) {
                md5T.setText(md5);
            }
        });
        askForPermission(NOT_REQUIRED_ONLY_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        msgT = new MessageThread();
        msgT.startPoolThread();

        /*pool.setFinishListener(new FinishListener() {
            @Override
            public void finish() {
                Log.e("qwer","执行结束");
            }
        });*/
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.select:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.start:
                /*if (!TextUtils.isEmpty(path.getText().toString())){

                    md5String = md5T.getText().toString();
                    uploadFile(HttpConstants.UPLOAD_URL,stringPath);
                }*/
                if (msgT != null){
                    msgT.startPoolThread();
                }
                break;
            case R.id.stop:
                //center.stopUpload();
                if (msgT != null){
                    msgT.stopPoolThread();
                }
                break;
            case R.id.android10:
                if (uri != null){
                    FenPian.test(this,uri);
                }else {
                    showToast("没有文件信息");
                }
                break;
        }
    }

    private void uploadFile(String url,String filePath){
        /*center = new BigFileUploadCenter( url,filePath,true, new DisposeUploadListener() {
            @Override
            public void onPiceSuccess(int progress, String currentSize, String sumSize) {
                process.setText(progress+"%");
                size.setText(currentSize+"/"+sumSize);
            }

            @Override
            public void onSuccess(String response) {
                showToast("上传成功");
            }

            @Override
            public void onFailure(String reason) {
                showToast(reason);
            }

            @Override
            public void onCancel(long startOffset) {
                showToast("上传停止");
                startOffSet = startOffset;
            }
        });
        center.start(startOffSet);*/
    }


    private void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            stringPath = MMM.getFilePathByUri(this, uri);
            path.setText(stringPath);

            FileInputStream fis = MIOUtils.getFileInPutStream(this,uri);
            if (fis != null){
                MD5utils.getFileMD5sync(fis, new MD5resultListener() {
                    @Override
                    public void onResult(String md5) {
                        md5T.setText(md5);
                    }
                });
            }else {
                md5T.setText("");
            }
        }
    }

}
