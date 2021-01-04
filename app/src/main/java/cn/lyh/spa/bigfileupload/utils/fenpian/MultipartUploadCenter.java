package cn.lyh.spa.bigfileupload.utils.fenpian;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.alibaba.fastjson.TypeReference;

import java.io.File;
import java.text.DecimalFormat;

import spa.lyh.cn.lib_https.HttpClient;
import spa.lyh.cn.lib_https.listener.DisposeDataHandle;
import spa.lyh.cn.lib_https.listener.DisposeDataListener;
import spa.lyh.cn.lib_https.model.Progress;
import spa.lyh.cn.lib_https.request.CommonRequest;
import spa.lyh.cn.lib_https.request.RequestParams;
import spa.lyh.cn.utils_io.IOUtils;

public class MultipartUploadCenter {
    public final static String TAG = "MultipartUploadCenter";
    public final static int TASK_SUCCESS = 1000;//上传任务结束
    public final static int TASK_FAIL = 1001;//上传任务失败
    public final static int TASK_CANCAL = 1002;//上传任务取消
    public final static int MULT_PART_FAIL = 1003;//单片上传失败
    public final static int MULT_PART_PROGRESS = 1004;//分片进度回调
    public final static int MERGE_SUCCESS = 1005;//合并成功
    public final static int MERGE_FAIL = 1006;//合并失败
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String info;
            switch (msg.what){
                case TASK_SUCCESS:
                    //线程池任务结束，开始调用合并接口
                    mergeTask();
                    break;
                case TASK_FAIL:
                    if (listener != null){
                        listener.onFailure(TASK_FAIL,"");
                    }
                    if (isDev){
                        Log.e(TAG,"上传任务失败");
                    }
                    release();
                    break;
                case TASK_CANCAL:
                    if (listener != null){
                        listener.onFailure(TASK_CANCAL,"");
                    }
                    if (isDev){
                        Log.e(TAG,"任务取消");
                    }
                    release();
                    break;
                case MERGE_SUCCESS:
                    //整个任务结束，释放资源
                    info = (String) msg.obj;
                    if (listener != null){
                        listener.onSuccess(info);
                    }
                    if (isDev){
                        Log.e(TAG,"任务结束，释放资源");
                    }
                    release();
                    break;
                case MERGE_FAIL:
                    //合并失败，释放资源
                    info = (String) msg.obj;
                    if (listener != null){
                        listener.onFailure(MERGE_FAIL,info);
                    }
                    if (isDev){
                        Log.e(TAG,"合并失败，释放资源");
                    }
                    release();
                    break;
                case MULT_PART_FAIL:
                    if (listener != null){
                        info = (String) msg.obj;
                        if (!TextUtils.isEmpty(info)){
                            listener.onFailure(MULT_PART_FAIL,info);
                        }
                    }
                    stopTasks();
                    break;
                case MULT_PART_PROGRESS:
                    long multPart = (long) msg.obj;
                    currentLength += multPart;
                    //计算百分比
                    mProgress = getNumber((float)currentLength/(float)fileSize)*100;
                    int currentSize = (int) mProgress;
                    //判断整型进度去重只传100次0到100
                    if (currentSize != lastProgress){
                        lastProgress = currentSize;
                        p = new Progress(true,lastProgress,convertFileSize(currentLength),convertFileSize(fileSize));
                        if (isDev){
                            Log.e(TAG,p.getProgress()+"%   "+p.getCurrentSize()+"/"+p.getSumSize());
                        }
                        if (listener != null){
                            listener.onProgress(p);
                        }
                    }
                    break;
            }
            return true;
        }
    });
    private long currentLength;

    private int lastProgress = -1;

    private float mProgress;

    private Progress p;

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

    private static String android = "/Android";//内部路径

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
        if (res instanceof String){
            String storagePath = Environment.getExternalStorageDirectory().getPath();
            String path = (String) res;
            if (path.startsWith(storagePath+android)){
                //进入私有存储空间,使用File操作
                this.res = new File(path);
            }else {
                //外部存储空间，使用Uri操作
                this.res = IOUtils.getFileUri(context,path);
            }
        }else if (res instanceof File || res instanceof Uri){
            this.res = res;
        }else {
            Log.e(TAG,"上传文件只能为String型路径，File型文件，Uri型文件");
        }
        initFileInfo(res);
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

    private void initFileInfo(Object res){
        if (res instanceof File){
            File file = (File) res;
            if (file.exists()){
                fileName = file.getName();
                fileSize = file.length();
                bodyParams.put("fileOriName",fileName);
                bodyParams.put("videoName",fileName );
            }else {
                Log.e(TAG,"文件不存在");
            }
        }else if (res instanceof Uri){
            Uri uri = (Uri) res;
            documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile != null){
                fileName = documentFile.getName();
                fileSize = documentFile.length();
                bodyParams.put("fileOriName",fileName);
                bodyParams.put("videoName",fileName );
            }else {
                Log.e(TAG,"DocumentFile未能正确获取");
            }
        }else {
            Log.e(TAG,"未能初始化文件信息");
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
                sendMsg(MULT_PART_PROGRESS,0);
            }else {
                if (!pool.isAlive()){
                    pool = new ThreadPool(context,handler,res,number,chunks,pieceSize,fileName,uploadUrl,bodyParams,headerParams);
                    pool.start();
                    sendMsg(MULT_PART_PROGRESS,0);
                }else {
                    Log.e(TAG,"任务正在进行，请不要重复启动");
                }
            }

        }else {
            Log.e(TAG,"存在一个参数未能初始化或不正确:"+errorTag);
        }
    }

    private void stopTasks(){
        if (pool != null){
            pool.stopPoolThread(TASK_FAIL);
        }
    }

    public void cancalTasks(){
        if (pool != null){
            pool.stopPoolThread(TASK_CANCAL);
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
                    String info;
                    if (!TextUtils.isEmpty(result.info.url)){
                        info = result.info.url;
                    }else {
                        info = "";
                    }
                    sendMsg(MultipartUploadCenter.MERGE_SUCCESS,info);

                }else {
                    sendMsg(MultipartUploadCenter.MERGE_FAIL,MERGE_MSG+" Code:"+result.code);
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                sendMsg(MultipartUploadCenter.MERGE_FAIL,MERGE_MSG);
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

        currentLength = 0;

        lastProgress = -1;

        mProgress = 0;

        p = null;
    }

    /**
     * 得到对应位数小数<P/>
     * Created by liyuhao on 2016/3/24.<P/>
     * @param number float的数
     * @return float的数
     */
    public float getNumber(float number){
        DecimalFormat df = new DecimalFormat("#.##############");
        float f=Float.valueOf(df.format(number));
        return f;
    }

    /**
     * 计算文件大小<P/>
     * Created by liyuhao on 2016/3/24.<P/>
     * @param size 字节数
     * @return 对应的G，M，K
     */
    private String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.2f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.2f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.2f KB", f);
        } else
            return String.format("%d B", size);
    }


    private void sendMsg(int what){
        Message msg = Message.obtain();
        msg.what = what;
        if (handler != null){
            handler.sendMessage(msg);
        }
    }
    private void sendMsg(int what,long bytelength){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = bytelength;
        if (handler != null){
            handler.sendMessage(msg);
        }
    }

    private void sendMsg(int what,String info){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = info;
        if (handler != null){
            handler.sendMessage(msg);
        }
    }

}
