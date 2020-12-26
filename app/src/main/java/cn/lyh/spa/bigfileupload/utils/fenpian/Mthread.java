package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.util.Log;

public class Mthread extends Thread{
    private int number;


    public Mthread(int number){
        this.number = number;
    }
    @Override
    public void run() {
        super.run();
        try {
            Log.e("qwer","线程"+number+"开始等待");
            sleep(5000);
            Log.e("qwer","线程"+number+"等待结束");
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }
}
