package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

public class FilePartUploadThread extends Thread implements Runnable{
    private int pieceSize;
    private FileInputStream fis;


    public FilePartUploadThread(int pieceSize,FileInputStream fis){
        this.pieceSize = pieceSize;
        this.fis = fis;
    }

    @Override
    public void run() {
        super.run();
    }


    /**
     * 读一片
     * @return
     * @throws IOException
     */
    private byte[] readPice(FileInputStream fis) throws IOException {
        byte[] datas = new byte[pieceSize];
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
