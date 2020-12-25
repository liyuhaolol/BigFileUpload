package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import cn.lyh.spa.bigfileupload.utils.MIOUtils;

public class FenPian {



    /**
     * 上传前准备操作
     */
    public static void test(FileInputStream fis){
        if (fis != null){
            try{
                FileChannel channel = fis.getChannel();
                long fileSize = channel.size();
                Log.e("qwer",String.valueOf(fileSize));

            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            Log.e("qwer","流为null");
        }
    }

    /**
     * 读一片
     * @return
     * @throws IOException
     */
    private static ByteBuffer[] readPice(FileChannel channel) throws IOException {
        ByteBuffer[] byteBuffer = new ByteBuffer[1*1024*1024];
        //byte[] datas = new byte[1*1024*1024];
        channel.read(byteBuffer,0, 1*1024*1024);
        return byteBuffer;
    }
}
