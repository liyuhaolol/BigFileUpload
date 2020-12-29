package cn.lyh.spa.bigfileupload.utils.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import cn.lyh.spa.bigfileupload.utils.fenpian.ThreadPool;

public class MessageThread extends Thread implements Runnable{
    private Handler handler;
    private ThreadPool pool;

    public MessageThread(){
        start();
    }

    @Override
    public void run() {
        super.run();
        Log.e("qwer","消息中心启动");
        //注册消息
        Looper.prepare();
        handler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 0){
                    Log.e("qwer",msg.obj+"");
                }else {
                    Looper.myLooper().quit();
                }

            }
        };
        Looper.loop();
        Log.e("qwer","消息中心关闭");
    }

    public void startPoolThread(){
        pool = new ThreadPool(handler);
        pool.start();
    }

    public void stopPoolThread(){
        if (pool != null){
            pool.stopPoolThread();
        }
    }
}
