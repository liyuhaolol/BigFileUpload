package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import spa.lyh.cn.lib_https.HttpClient;
import spa.lyh.cn.lib_https.exception.OkHttpException;
import spa.lyh.cn.lib_https.listener.UploadProgressListener;
import spa.lyh.cn.lib_https.request.RequestParams;
import spa.lyh.cn.lib_https.request.UploadProgressRequestBody;

public class FilePartUploadThread extends Thread implements Runnable{
    private int pieceSize;
    private int chunk;
    private FileInputStream fis;
    private int mPiceRealSize;
    private Handler handler;
    private MultipartBody body;
    private RequestParams bodyParams,headerParams;
    private String url;
    private String fileName;
    private Call mCurrentCall;
    private Context context;
    private String mResponse;

    private boolean shouldLock;


    public FilePartUploadThread(Context context,Handler handler,int chunk, int pieceSize, String fileName,FileInputStream fis,String url,RequestParams bodyParams,RequestParams headerParams){
        this.context = context;
        this.pieceSize = pieceSize;
        this.chunk = chunk;
        this.fis = fis;
        this.handler = handler;
        this.url = url;
        this.fileName = fileName;
        if (bodyParams != null){
            this.bodyParams = new RequestParams();
            this.bodyParams.put("chunk", String.valueOf(chunk));
            for (Map.Entry<String, String> entry : bodyParams.urlParams.entrySet()) {
                //变成私有变量，避免线程锁的问题
                this.bodyParams.put(entry.getKey(), entry.getValue());
            }
        }
        if (headerParams != null){
            this.headerParams = new RequestParams();
            for (Map.Entry<String, String> entry : headerParams.urlParams.entrySet()) {
                //变成私有变量，避免线程锁的问题
                this.headerParams.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void run() {
        super.run();
        try{
            //1.读流
            byte[] datas = readPice(fis);
            if (mPiceRealSize == -1 || mPiceRealSize == 0){
                //没有读到分片，说明出了问题
                //回调
                sendMsg(MultipartUploadCenter.MULT_PART_FAIL,"第"+chunk+"片读取流错误");
                Thread.sleep(5000);//延迟5秒等待被结束
                return;
            }
            //执行网络请求
            uploadRequest(datas);

            if (shouldLock){
                Thread.sleep(5000);//延迟5秒等待被结束
            }
        }catch (Exception e){
        }
        release();
    }


    /**
     * 读一片
     * @return
     * @throws IOException
     */
    private byte[] readPice(FileInputStream fis) throws IOException {
        byte[] datas = new byte[pieceSize];
        if (chunk*pieceSize == fis.skip(chunk*pieceSize)){
            mPiceRealSize = fis.read(datas);
            fis.close();
        }else {
            //跳过流出现问题
            mPiceRealSize = 0;
        }
        return datas;
    }

    private void uploadRequest(byte[] datas) throws IOException {

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        if (bodyParams != null) {
            for (Map.Entry<String, String> entry : bodyParams.urlParams.entrySet()) {
                //将请求参数遍历添加到我们的请求构建类中
                multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        PieceRequestBody requestBody = new PieceRequestBody(datas, mPiceRealSize, new MultpartListener() {
            @Override
            public void onPiece(long piece) {
                sendMsg(MultipartUploadCenter.MULT_PART_PROGRESS,piece);
            }
        });
        multipartBodyBuilder.addFormDataPart("file",fileName,requestBody);
        body = multipartBodyBuilder.build();

        //添加请求头
        Headers.Builder mHeaderBuild = new Headers.Builder();
        if (headerParams != null) {
            for (Map.Entry<String, String> entry : headerParams.urlParams.entrySet()) {
                mHeaderBuild.add(entry.getKey(), entry.getValue());
            }
        }
        //生成header
        Headers mHeader = mHeaderBuild.build();


        Request request = new Request.Builder()
                .url(url)
                .headers(mHeader)
                .post(body)
                .build();


        mCurrentCall = HttpClient.getInstance(context).getOkHttpClient().newCall(request);

        mResponse = null;

        Response execute = mCurrentCall.execute();
        if (!execute.isSuccessful()){
            sendMsg(MultipartUploadCenter.MULT_PART_FAIL,"第"+chunk+"片请求失败");
            shouldLock = true;
            return;
        }
        ResponseBody body = execute.body();
        mResponse = body.string();

        if (!TextUtils.isEmpty(mResponse)){
            Result result = JSONObject.parseObject(mResponse,Result.class);
            if (result != null){
                if (result.code != 200){
                    sendMsg(MultipartUploadCenter.MULT_PART_FAIL,"第"+chunk+"片Code:"+result.code);
                    shouldLock = true;
                }
            }else {
                sendMsg(MultipartUploadCenter.MULT_PART_FAIL,"第"+chunk+"片分片泛型解析错误");
                shouldLock = true;
            }
        }else {
            sendMsg(MultipartUploadCenter.MULT_PART_FAIL,"第"+chunk+"片Response为空");
            shouldLock = true;
        }
    }

    private void sendMsg(int what,String info){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = info;
        if (handler != null){
            handler.sendMessage(msg);
        }
    }

    private void sendMsg(int what,long bytelength){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = bytelength;
        if (handler != null){
            handler.sendMessage(msg);
        }
    }

    private void release(){
        pieceSize = 0;
        chunk = 0;
        fis = null;
        mPiceRealSize = 0;
        handler = null;
        body = null;
        bodyParams = null;
        headerParams = null;
        url = null;
        fileName = null;
        mCurrentCall = null;
        context = null;
        mResponse = null;
        shouldLock = false;
    }
}
