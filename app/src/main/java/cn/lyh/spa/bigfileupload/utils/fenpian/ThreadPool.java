package cn.lyh.spa.bigfileupload.utils.fenpian;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool extends Thread{

    @Override
    public void run() {
        super.run();
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i=1;i<=20;i++){
            service.execute(new Mthread(i));
        }
        service.shutdown();
        while (!service.isTerminated()){
            //Log.e("qwer","执行结束");
        }
    }
}
