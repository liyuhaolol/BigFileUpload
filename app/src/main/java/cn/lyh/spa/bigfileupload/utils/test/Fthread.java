package cn.lyh.spa.bigfileupload.utils.test;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.lyh.spa.bigfileupload.utils.MIOUtils;

public class Fthread extends Thread{
    private static int a = 1048576;
    private Context context;
    private Uri uri;
    private int piece;

    private int mPiceRealSize;

    public Fthread(Context context,Uri uri,int piece){
        this.context = context;
        this.uri = uri;
        this.piece = piece;
    }

    @Override
    public void run() {
        super.run();
        try{
            byte[] datas = readPice(MIOUtils.getFileInPutStream(context,uri));
            //Log.e("qwer",datas.length+"");
            File path = new File("/storage/emulated/0/Android/data/cn.lyh.spa.bigfileupload/cache/splite");
            if (!path.exists()){
                path.mkdir();
            }
            FileOutputStream fos = new FileOutputStream("/storage/emulated/0/Android/data/cn.lyh.spa.bigfileupload/cache/splite/"+piece);
            fos.write(datas,0,mPiceRealSize);
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 读一片
     * @return
     * @throws IOException
     */
    private byte[] readPice(FileInputStream fis) throws IOException {
        byte[] datas = new byte[a];
        if (a*piece == fis.skip(a*piece)){
            mPiceRealSize = fis.read(datas);
            fis.close();
            Log.e("qwer","实际读取到的流长度："+mPiceRealSize);
        }else {
            Log.e("qwer","跳过流数量有误");
        }
        return datas;
    }
}
