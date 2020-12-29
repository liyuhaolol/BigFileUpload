package cn.lyh.spa.bigfileupload.utils.test;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import cn.lyh.spa.bigfileupload.utils.MIOUtils;

public class FenPian {
    private static Uri mUri;

    /**
     * 上传前准备操作
     */
    public static void test(Context context ,Uri uri){
        mUri = uri;
        try{
            FileInputStream sizeStream = MIOUtils.getFileInPutStream(context,mUri);
            if (sizeStream != null){
                FileChannel channel = sizeStream.getChannel();
                if (channel != null){
                    long fileSize = channel.size();
                    Log.e("qwer","文件大小为：" + fileSize);
                    channel.close();
                    sizeStream.close();
                }else {
                    Log.e("qwer","通道为null");
                }
                //读取文件大小结束
                Fthread f1 = new Fthread(context,mUri,0);
                Fthread f2 = new Fthread(context,mUri,1);
                f1.start();
                f2.start();
            }else {
                Log.e("qwer","流为null");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
