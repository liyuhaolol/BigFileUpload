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
}
