package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool extends Thread{
    ExecutorService service;
    FinishListener listener;


    @Override
    public void run() {
        super.run();
        service = Executors.newFixedThreadPool(3);
        for (int i=1;i<=20;i++){
            service.execute(new Mthread(i));
        }
        service.shutdown();
        while (!service.isTerminated()){
            //线程阻塞
        }
        if (listener != null){
            listener.finish();
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
}
