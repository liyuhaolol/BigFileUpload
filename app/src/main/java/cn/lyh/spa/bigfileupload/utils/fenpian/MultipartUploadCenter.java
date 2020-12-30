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

import com.alibaba.fastjson.TypeReference;

import java.io.File;

import spa.lyh.cn.lib_https.HttpClient;
import spa.lyh.cn.lib_https.listener.DisposeDataHandle;
import spa.lyh.cn.lib_https.listener.DisposeDataListener;
import spa.lyh.cn.lib_https.request.CommonRequest;
import spa.lyh.cn.lib_https.request.RequestParams;

public class MultipartUploadCenter {
    private final static String TAG = "MultipartUploadCenter";
    public final static int TASK_SUCCESS = 1000;//上传任务结束
    public final static int TASK_FAIL = 1001;//上传任务失败
    public final static int MULT_PART_FAIL = 1002;//单片上传失败
    public final static int MULT_PART_PROGRESS = 1003;//分片进度回调
    public final static int MERGE_FINISH = 1004;//上传任务失败
    /*public final static int TASK_FAIL = 1001;//上传任务失败
    public final static int TASK_FAIL = 1001;//上传任务失败
    public final static int TASK_FAIL = 1001;//上传任务失败*/
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case TASK_SUCCESS:
                    //线程池任务结束，开始调用合并接口
                    mergeTask();
                    break;
                case MERGE_FINISH:
                    //整个任务结束，释放资源
                    if (isDev){
                        Log.e(TAG,"任务结束，释放资源");
                    }
                    release();
                    break;
            }
            return true;
        }
    });
    private String errorTag;

    private Context context;

    private ThreadPool pool;//线程池

    private Object res;

    private String fileName;

    private long fileSize;

    private int number = 5;//线程并发数

    private int pieceSize = 512 * 1024;//默认一片大小

    private long chunks;//总片数

    private String uploadUrl,mergeUrl;

    private RequestParams bodyParams,headerParams;

    private DocumentFile documentFile;

    private boolean isDev;

    private UploadTaskListener listener;

    private static MultipartUploadCenter instance;

    public static MultipartUploadCenter getInstance(){
        if (instance == null){
            synchronized (MultipartUploadCenter.class){
                if (instance == null){
                    instance = new MultipartUploadCenter();
                }
            }
        }
        return instance;
    }

    public MultipartUploadCenter setThreadNumber(int number){
        this.number = number;
        return this;
    }

    public MultipartUploadCenter setUp(Context context,String uploadUrl, String mergeUrl, RequestParams bodyParams,RequestParams headerParams,Object res,boolean isDev,UploadTaskListener listener){
        this.context = context;
        this.bodyParams = bodyParams;
        this.uploadUrl = uploadUrl;
        this.mergeUrl = mergeUrl;
        this.headerParams = headerParams;
        this.isDev = isDev;
        this.listener = listener;
        if (res instanceof String){}else if (res instanceof File){}else if (res instanceof Uri){}else {
            Log.e(TAG,"上传文件只能为String型路径，File型文件，Uri型文件");
        }
        this.res = res;
        initFileInfo(null);
        prepare();//发送前准备工作
        return this;
    }

    private void prepare(){
        if (documentFile != null){
            countPieceSize();//计算分片大小
            countChunks();//计算总片数
        }
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
        bodyParams.put("chunks", String.valueOf(chunks));//加入总片数参数
    }

    private void initFileInfo(Uri uri){
        documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null){
            fileName = documentFile.getName();
            fileSize = documentFile.length();
            bodyParams.put("fileOriName",fileName);
            bodyParams.put("videoName",fileName );
        }else {
            Log.e(TAG,"DocumentFile未能正确获取");
        }
    }

    private boolean checkCanStart(){
        if (fileSize <= 0){
            errorTag = "fileSize无法获取到文件大小";
            return false;
        }
        if (pieceSize <= 0){
            errorTag = "pieceSize分片大小不对 -> "+pieceSize;
            return false;
        }
        if (context == null){
            errorTag = "context上下文为空";
            return false;
        }
        if (handler == null){
            errorTag = "handler消息处理器为空";
            return false;
        }
        if (res == null){
            errorTag = "res上传文件为空";
            return false;
        }
        if (number < 1){
            errorTag = "number线程并发数小于1";
            return false;
        }
        if (chunks < 1){
            errorTag = "chunks总片数小于1";
            return false;
        }
        if (TextUtils.isEmpty(fileName)){
            errorTag = "fileName未能获取到文件名";
            return false;
        }
        if (TextUtils.isEmpty(uploadUrl)){
            errorTag = "uploadUrl上传文件链接为空";
            return false;
        }
        if (TextUtils.isEmpty(mergeUrl)){
            errorTag = "mergeUrl合并文件链接为空";
            return false;
        }
        return true;
    }


    public void startTasks(){
        if (checkCanStart()){
            if (pool == null){
                pool = new ThreadPool(context,handler,res,number,chunks,pieceSize,fileName,uploadUrl,bodyParams,headerParams);
                pool.start();
            }else {
                if (!pool.isAlive()){
                    pool = new ThreadPool(context,handler,res,number,chunks,pieceSize,fileName,uploadUrl,bodyParams,headerParams);
                    pool.start();
                }else {
                    Log.e("qwer","任务正在进行，请不要重复启动");
                }
            }

        }else {
            Log.e(TAG,"存在一个参数未能初始化或不正确:"+errorTag);
        }
    }

    public void stopTasks(){
        if (pool != null){
            pool.stopPoolThread();
        }
    }

    private static String MERGE_MSG = "合并文件接口出现问题";

    private void mergeTask(){
        //合并接口
        TypeReference typeReference = new TypeReference<Result>(){};
        HttpClient.getInstance(context).sendResquest(CommonRequest.createGetRequest(mergeUrl,bodyParams,headerParams,isDev),new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                Result result = (Result) responseObj;
                if (result.code == 200){
                    if (listener != null){
                        if (result.info != null){
                            listener.onSuccess(result.info.url);
                        }else {
                            listener.onSuccess("");
                        }

                    }
                }else {
                    if (listener != null){
                        listener.onFailure(MERGE_MSG+" Code:"+result.code);
                    }
                }
                sendMsg(MultipartUploadCenter.MERGE_FINISH);
            }

            @Override
            public void onFailure(Object reasonObj) {
                if (listener != null){
                    listener.onFailure(MERGE_MSG);
                }
                sendMsg(MultipartUploadCenter.MERGE_FINISH);
            }
        }, typeReference, isDev));
    }

    private void release(){
        //释放掉所有资源
        errorTag = null;

        context = null;

        pool = null;//线程池

        res = null;

        fileName = null;

        fileSize = 0;

        number = 5;//线程并发数

        pieceSize = 512 * 1024;//默认一片大小

        chunks = 0;//总片数

        uploadUrl = null;

        mergeUrl = null;

        bodyParams = null;

        headerParams = null;

        documentFile = null;

        isDev = false;
    }


    private void sendMsg(int what){
        Message msg = Message.obtain();
        msg.what = what;
        if (handler != null){
            handler.sendMessage(msg);
        }
    }

}
