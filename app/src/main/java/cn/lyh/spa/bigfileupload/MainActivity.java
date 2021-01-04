package cn.lyh.spa.bigfileupload;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileInputStream;

import cn.lyh.spa.bigfileupload.network.RequestCenter;
import cn.lyh.spa.bigfileupload.utils.MIOUtils;
import cn.lyh.spa.bigfileupload.utils.MMM;
import cn.lyh.spa.bigfileupload.utils.fenpian.MultipartUploadCenter;
import cn.lyh.spa.bigfileupload.utils.fenpian.UploadTaskListener;
import cn.lyh.spa.bigfileupload.utils.md5.MD5resultListener;
import cn.lyh.spa.bigfileupload.utils.md5.MD5utils;
import spa.lyh.cn.lib_https.model.Progress;
import spa.lyh.cn.peractivity.PermissionActivity;

public class MainActivity extends PermissionActivity {
    private TextView path,process,md5T,size,url;

    private String stringPath;

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = findViewById(R.id.path);
        stringPath = "/storage/emulated/0/a.apk";
        path.setText(stringPath);
        process = findViewById(R.id.process);
        size = findViewById(R.id.size);
        url = findViewById(R.id.url);
        md5T = findViewById(R.id.md5);
        MD5utils.getFileMD5sync(this,path.getText().toString(), new MD5resultListener() {
            @Override
            public void onResult(String md5) {
                md5T.setText(md5);
            }
        });
        askForPermission(NOT_REQUIRED_ONLY_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.select:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");//无类型限制
                String[] mimetypes = {"image/*", "video/*","audio/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.start:
                if (uri != null){
                    uploadFile();
                }
                break;
            case R.id.stop:
                MultipartUploadCenter.getInstance().cancalTasks();
                break;
        }
    }

    private void uploadFile(){
        RequestCenter.uploadPic(this, MIOUtils.upvideotimeStamp(), uri, new UploadTaskListener() {
            @Override
            public void onSuccess(String info) {
                url.setText(info);
            }

            @Override
            public void onFailure(int status,String msg) {
                url.setText(msg);
            }

            @Override
            public void onProgress(Progress progress) {
                process.setText(progress.getProgress()+"%");
                size.setText(progress.getCurrentSize()+"/"+progress.getSumSize());
            }
        });
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
