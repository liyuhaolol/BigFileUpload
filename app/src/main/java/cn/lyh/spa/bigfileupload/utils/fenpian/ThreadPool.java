package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool extends Thread{
    ExecutorService service;
    FinishListener listener;
    private Handler handler;

    public ThreadPool(Handler handler){
        this.handler = handler;
    }


    @Override
    public void run() {
        super.run();
        service = Executors.newFixedThreadPool(3);
        for (int i=1;i<=20;i++){
            service.execute(new Mthread(i,handler));
        }
        service.shutdown();
        while (!service.isTerminated()){
            //线程阻塞
        }
        if (listener != null){
            listener.finish();
        }
        Message msg = Message.obtain();
        msg.what = 1;
        if (handler != null){
            handler.sendMessage(msg);
        }

    }

    public void stopPoolThread(){
        if (service != null && !service.isTerminated()){
            service.shutdownNow();
        }
    }


    public void setFinishListener(FinishListener listener){
        this.listener = listener;
    }

    private void initHandler(){

    }
}
