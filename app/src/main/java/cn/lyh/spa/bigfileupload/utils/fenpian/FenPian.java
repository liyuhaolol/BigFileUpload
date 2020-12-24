package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FenPian {



    /**
     * 上传前准备操作
     */
    public static void test(final FileInputStream fis){
        Thread t = new Thread(){
            @Override
            public void run() {
                try{
                    long fileSize = fis.getChannel().size();
                    Log.e("qwer",String.valueOf(fileSize));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}
