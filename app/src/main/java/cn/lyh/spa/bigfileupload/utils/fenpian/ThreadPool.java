package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lyh.spa.bigfileupload.utils.test.Mthread;

public class ThreadPool extends Thread{
    ExecutorService service;

    private final Handler handler;
    private Object res;
    private int threads;
    private long chunks;
    private int pieceSize;

    public ThreadPool(Handler handler, Object res,int threads,long chunks,int pieceSize){
        this.handler = handler;
        this.res = res;
        this.threads = threads;
        this.chunks = chunks;
        this.pieceSize = pieceSize;
    }


    @Override
    public void run() {
        super.run();
        service = Executors.newFixedThreadPool(threads);
        for (int i = 0;i < chunks; i++){
            service.execute(new Mthread(i,handler));
        }
        service.shutdown();
        while (!service.isTerminated()){
            //线程阻塞
        }

        Message msg = Message.obtain();
        msg.what = MultipartUploadCenter.TASK_FINISH;
        if (handler != null){
            handler.sendMessage(msg);
        }

    }

    public void stopPoolThread(){
        if (service != null && !service.isTerminated()){
            service.shutdownNow();
        }
    }

}
