package cn.lyh.spa.bigfileupload.network;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.HandlerThread;

import com.alibaba.fastjson.TypeReference;

import java.io.File;

import cn.lyh.spa.bigfileupload.BuildConfig;
import cn.lyh.spa.bigfileupload.utils.fenpian.MultipartUploadCenter;
import cn.lyh.spa.bigfileupload.utils.fenpian.UploadTaskListener;
import okhttp3.Call;
import spa.lyh.cn.lib_https.HttpClient;
import spa.lyh.cn.lib_https.listener.DisposeDataHandle;
import spa.lyh.cn.lib_https.listener.DisposeDataListener;
import spa.lyh.cn.lib_https.request.CommonRequest;
import spa.lyh.cn.lib_https.request.RequestParams;


public class RequestCenter {




    private static Call postRequest(Activity activity,String url, RequestParams params, RequestParams headers, final DisposeDataListener listener, TypeReference<?> typeReference) {
        //创建网络请求
        Call call = HttpClient.getInstance(activity).sendResquest(CommonRequest.
                createPostRequest(url,params,headers,true),new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                listener.onSuccess(responseObj);
            }

            @Override
            public void onFailure(Object reasonObj) {
                listener.onFailure(reasonObj);
            }
        }));

        return call;
    }


    /*public static void uploadBigFile(String path , DisposeUploadListener listener){
        HandlerThread thread = new HandlerThread("1");
    }*/



    //这里暂时优先使用uri。后期补充。
    public static void uploadPic(Context context, String identification, Uri uri, UploadTaskListener listener){
        RequestParams bodyParams = new RequestParams();

        /*bodyParams.put("videoIdentification", id);

         */
        //传图片用的参数
        bodyParams.put("siteId", String.valueOf(1));
        bodyParams.put("identification", identification);
        bodyParams.put("videoIdentification", identification);
        bodyParams.put("videoCallback",HttpConstants.VIDEO_CALLBACK);
        MultipartUploadCenter.getInstance()
                .setUp(context,
                        HttpConstants.UPLOAD_PIC,
                        HttpConstants.MERGE_PIC,
                        bodyParams,
                        null,
                        uri,
                        BuildConfig.DEBUG,
                        listener)
                .startTasks();
    }
}
