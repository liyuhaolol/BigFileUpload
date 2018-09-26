package cn.lyh.spa.bigfileupload;


import android.os.HandlerThread;

import com.alibaba.fastjson.TypeReference;

import java.io.File;

import cns.workspace.lib.androidsdk.httputils.CommonOkHttpClient;
import cns.workspace.lib.androidsdk.httputils.listener.DisposeDataHandle;
import cns.workspace.lib.androidsdk.httputils.listener.DisposeDataListener;
import cns.workspace.lib.androidsdk.httputils.listener.DisposeUploadListener;
import cns.workspace.lib.androidsdk.httputils.request.CommonRequest;
import cns.workspace.lib.androidsdk.httputils.request.RequestParams;
import okhttp3.Call;

/**
 * Created by zhaolb on 2017/12/26.
 */

public class RequestCenter {




    private static Call postRequest(String url, RequestParams params, RequestParams headers, final DisposeDataListener listener, TypeReference<?> typeReference) {
        //创建网络请求
        Call call = CommonOkHttpClient.sendResquest(CommonRequest.
                createPostRequest(url, params, headers, true), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                listener.onSuccess(responseObj);
            }

            @Override
            public void onFailure(Object reasonObj) {
                listener.onFailure(reasonObj);

            }
        }, typeReference, true));

        return call;
    }


    public static void uploadBigFile(String path , DisposeUploadListener listener){
        HandlerThread thread = new HandlerThread("1");
    }
}
