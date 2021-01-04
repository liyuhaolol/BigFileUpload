package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lyh.spa.bigfileupload.utils.MIOUtils;
import cn.lyh.spa.bigfileupload.utils.test.Mthread;
import spa.lyh.cn.lib_https.request.RequestParams;

public class ThreadPool extends Thread{
    private ExecutorService service;

    private Handler handler;
    private Object res;
    private int threads;
    private long chunks;
    private int pieceSize;
    private Context context;
    private String fileName;
    private String url;
    private RequestParams bodyParams,headerParams;
    private int mode;

    public ThreadPool(Context context,Handler handler, Object res, int threads, long chunks, int pieceSize,String fileName,String url,RequestParams bodyParams,RequestParams headerParams){
        this.handler = handler;
        this.res = res;
        this.threads = threads;
        this.chunks = chunks;
        this.pieceSize = pieceSize;
        this.context = context;
        this.fileName = fileName;
        this.url = url;
        this.bodyParams = bodyParams;
        this.headerParams = headerParams;
    }


    @Override
    public void run() {
        super.run();
        service = Executors.newFixedThreadPool(threads);
        for (int i = 0;i < chunks; i++){
            service.execute(new FilePartUploadThread(context,handler,i,pieceSize,fileName,getFileInputStream(res),url,bodyParams,headerParams));
        }
        service.shutdown();
        while (!service.isTerminated()){
            //线程阻塞
        }

        Message msg = Message.obtain();
        if (mode == MultipartUploadCenter.TASK_CANCAL){
            //取消
            msg.what = MultipartUploadCenter.TASK_CANCAL;
        }else if (mode == MultipartUploadCenter.TASK_FAIL){
            //失败
            msg.what = MultipartUploadCenter.TASK_FAIL;
        }else {
            //执行完毕
            msg.what = MultipartUploadCenter.TASK_SUCCESS;
        }

        if (handler != null){
            handler.sendMessage(msg);
        }
        release();

    }

    public void stopPoolThread(int mode){
        if (service != null && !service.isTerminated()){
            if (mode == MultipartUploadCenter.TASK_FAIL){
                this.mode = MultipartUploadCenter.TASK_FAIL;
            }else if (mode == MultipartUploadCenter.TASK_CANCAL){
                this.mode = MultipartUploadCenter.TASK_CANCAL;
            }else {
                Log.e(MultipartUploadCenter.TAG,"停止线程池参数不对，不能停止任务");
                return;
            }
            service.shutdownNow();
        }
    }

    private FileInputStream getFileInputStream(Object res){
        FileInputStream fis = null;
        try{
            if (res instanceof File){
                fis = new FileInputStream((File) res);
            }
            if (res instanceof Uri){
                fis = MIOUtils.getFileInPutStream(context,(Uri) res);
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return fis;
    }

    private void release(){
        service = null;

        handler = null;
        res = null;
        threads = 0;
        chunks = 0;
        pieceSize = 0;
        context = null;
        fileName = null;
        url = null;
        bodyParams = null;
        headerParams = null;
        mode = 0;
    }

}
