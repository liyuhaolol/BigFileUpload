package cn.lyh.spa.bigfileupload.network;

import android.content.Context;

import cn.lyh.spa.bigfileupload.BuildConfig;
import spa.lyh.cn.lib_https.MultipartUploadCenter;
import spa.lyh.cn.lib_https.listener.UploadTaskListener;
import spa.lyh.cn.lib_https.request.RequestParams;


public class RequestCenter {


    //大文件上传并不是通用方法，只能用在目前的后台大文件逻辑上
    public static void uploadPic(Context context, String identification, Object res, UploadTaskListener listener){
        RequestParams bodyParams = new RequestParams();
        //已经封装入框架的参数
        //chunk 第几片
        //chunks 总片数
        //fileOriName 文件名
        //videoName 文件名
        //共用参数
        bodyParams.put("siteId", String.valueOf(1));
        //传图片用的参数
        bodyParams.put("identification", identification);
        //传视频的参数
        bodyParams.put("videoIdentification", identification);
        bodyParams.put("videoCallback",HttpConstants.VIDEO_CALLBACK);
        MultipartUploadCenter.getInstance()
                .setUp(context,
                        HttpConstants.UPLOAD_PIC,
                        HttpConstants.MERGE_PIC,
                        bodyParams,
                        null,
                        res,
                        BuildConfig.DEBUG,
                        listener)
                .startTasks();
    }
}
