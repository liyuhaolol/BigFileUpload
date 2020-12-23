package cn.lyh.spa.bigfileupload.network;


import android.app.Activity;
import android.os.HandlerThread;

import com.alibaba.fastjson.TypeReference;

import java.io.File;

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
}
