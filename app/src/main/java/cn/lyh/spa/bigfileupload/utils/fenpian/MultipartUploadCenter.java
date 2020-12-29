package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import spa.lyh.cn.lib_https.request.RequestParams;

public class MultipartUploadCenter {
    public final static int TASK_FINISH = 1000;
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case TASK_FINISH:
                    //线程池任务结束，开始调用合并接口
                    break;
                case 0:
                    Log.e("qwer",msg.obj+"");
                    break;
            }
            return true;
        }
    });
    private Context context;

    private ThreadPool pool;//线程池

    private Object res;

    private String fileName;

    private long fileSize;

    private int number = 5;

    private int pieceSize = 512 * 1024;

    private long chunks;

    public MultipartUploadCenter(Context context,String uploadUrl, String mergeUrl, RequestParams bodyParams,Uri uri){
        this.context = context;
        this.res = uri;
        initFileInfo(uri);
        prepare();//发送前准备工作
    }

    public void setThreadNumber(int number){
        this.number = number;
    }

    public void prepare(){
        countPieceSize();//计算分片大小
        countChunks();
    }

    private void countPieceSize(){
        if (fileSize > 0){
            if (fileSize <= 20 * 1024* 1024){
                //20Mb以内，512K一片
                pieceSize = 512 * 1024;
            }else if (fileSize > 20 * 1024* 1024 && fileSize <= 100 * 1024* 1024){
                //20Mb-100Mb以内，2Mb一片
                pieceSize = 2 * 1024* 1024;
            }else {
                //100Mb以上，10Mb一片
                pieceSize = 10 * 1024* 1024;
            }
        }else {
            pieceSize = 0;
        }
    }

    private void countChunks(){
        //计算切割文件大小
        chunks = fileSize % pieceSize == 0 ? fileSize / pieceSize : (fileSize / pieceSize) + 1;
    }

    public void initFileInfo(Uri uri){
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null){
            fileName = documentFile.getName();
            fileSize = documentFile.length();
        }else {
            //这里要进行回调处理
        }
    }

    private boolean checkCanStart(){
        if (res == null){
            return false;
        }
        if (fileSize <= 0){
            return false;
        }
        if (TextUtils.isEmpty(fileName)){
            return false;
        }
        if (number <= 0){
            return false;
        }
        return true;
    }


    public void startTasks(){
        if (checkCanStart()){
            pool = new ThreadPool(handler,res,number,chunks);
            pool.start();
        }
    }

    public void stopTasks(){
        if (pool != null){
            pool.stopPoolThread();
        }
    }

}
