package cn.lyh.spa.bigfileupload.utils;

import android.content.Context;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MIOUtils {

    public static FileInputStream getFileInPutStream(Context context, Uri uri){
        FileInputStream fis = null;
        try{
            fis = (FileInputStream) context.getContentResolver().openInputStream(uri);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return fis;
    }


    /**
     * 生成时间戳为基准的随机数
     * @return
     */
    public static String upvideotimeStamp(){
        long time = System.currentTimeMillis();
        long i = (long) (1000 + Math.random() * (9999 - 1000+1 ));
        return String.valueOf(time)+ String.valueOf(i)+"0";
    }
}
