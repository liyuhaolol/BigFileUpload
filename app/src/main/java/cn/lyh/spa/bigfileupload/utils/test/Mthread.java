package cn.lyh.spa.bigfileupload.utils.test;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Mthread extends Thread{
    private int number;
    private Handler handler;


    public Mthread(int number,Handler handler){
        this.number = number;
        this.handler = handler;
    }
    @Override
    public void run() {
        super.run();
        try {
            Message msg1 = Message.obtain();
            msg1.what = 0;
            msg1.obj = "线程"+number+"开始等待";
            if (handler != null){
                handler.sendMessage(msg1);
            }
            sleep(5000);
            Message msg2 = Message.obtain();
            msg2.what = 0;
            msg2.obj = "线程"+number+"等待结束";
            if (handler != null){
                handler.sendMessage(msg2);
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }
}
