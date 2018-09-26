package cns.workspace.lib.androidsdk.httputils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cns.workspace.lib.androidsdk.httputils.listener.DisposeUploadListener;
import cns.workspace.lib.androidsdk.httputils.response.CommonFileCallback;
import cns.workspace.lib.androidsdk.utils.CnsCommonUtil;
import cns.workspace.lib.androidsdk.utils.md5.MD5resultListener;
import cns.workspace.lib.androidsdk.utils.md5.MD5utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

public class BigFileUploadCenter extends Thread implements Runnable{

    protected final int DEFAULT_PICE_SIZE = 1 * 1024 * 1024;//默认一片的大小,1M

    private long startOffset = 0;//一片起始的位置
    private long endOffset = 0;//一片结束的位置
    private int mPiceRealSize = 0; //每一片的实际大小

    private String filePath;
    private String url;
    private RandomAccessFile file;//上传的文件

    private String md5String;

    private PiceRequestBody body;

    //private long mLastCalculateLength = 0;//最新的计算长度,暂时没用到

    private long mLastCompleteLength = 0;//最新完成长度

    private long fileLength = 0;

    private boolean isDev;

    private Handler handler;

    private Call mCurrentCall;

    private String mResponse;     //返回数据

    private DisposeUploadListener listener;

    private volatile boolean isExit = false;//是否结束发送文件

    private int progress = 0;


    private final static String UPLOAD_FAILD = "上传中出错";
    //private final static String UPLOAD_CANCEL = "上传停止";
    private final static String UPLOAD_ERROR = "上传异常终止";
    private final static String MD5_ERROR = "无法计算MD5值";
    private final static String FILE_ERROR = "无法读取文件";


    public BigFileUploadCenter(String url,String filePath,boolean isDev,DisposeUploadListener listener){
        this.filePath = filePath;
        this.url = url;
        this.isDev = isDev;
        this.listener = listener;
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 如果需要从某个位置开始，调用此方法;
     * @param startOffset
     */
    public void start(long startOffset) {
        this.startOffset = startOffset;
        this.mLastCompleteLength = startOffset;
        start();
    }

    /**
     * 停止下载
     */
    public void stopUpload(){
        if (mCurrentCall != null){
            if (!mCurrentCall.isCanceled()){
                mCurrentCall.cancel();
            }
        }
    }

    @Override
    public void run() {
        //1.准备
        prepare();
        //2.循环发送
        while (!isExit){
            try {
                byte[] datas = readPice();
                int piceSize = getPiceRealSize();
                if (piceSize == -1 || piceSize == 0){
                    //没有读取更多分片
                    isExit = true;
                    break;
                }
                endOffset = startOffset + piceSize;//设置下一个偏移量的位置
                //执行网络请求
                uploadRequest(datas);

                if (isPiceSuccessful()){
                    //完成一片
                    startOffset = endOffset;//设置下次起始的位置
                    mLastCompleteLength = endOffset;//设置当前完成长度
                    //计算百分比
                    float mProgress = CnsCommonUtil.getNumber((float)mLastCompleteLength/(float)fileLength)*100;
                    if (progress != (int) mProgress){
                        //两次进度不相等时，调用回调
                        progress = (int) mProgress;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPiceSuccess(progress,CnsCommonUtil.convertFileSize(mLastCompleteLength),CnsCommonUtil.convertFileSize(fileLength));//回调一片完成
                            }
                        });
                    }

                }else {
                    //失败
                    isExit = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(UPLOAD_FAILD);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                //这里处理网络请求超时，等其他错误
                isExit = true;
                if (e.getMessage() != null){
                    if (e.getMessage().equals("Canceled")){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onCancel(startOffset);
                            }
                        });
                    }else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailure(UPLOAD_FAILD);
                            }
                        });
                    }
                }
            }
        }
        //判断整个任务是否结束
        if (isFinished()){
            final String size = CnsCommonUtil.convertFileSize(mLastCompleteLength);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onPiceSuccess(100,size,size);
                    listener.onSuccess(mResponse);
                }
            });
        }/*else {
            //任务没有结束却跳出了循环
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(UPLOAD_ERROR);
                }
            });
        }*/
        //3.释放资源
        release();
    }


    /**
     * 上传前准备操作
     */
    private void prepare(){
        if (isDev){
            Log.e("UploadUrl",url);
        }
        try {
            //初始化文件
            file = new RandomAccessFile(filePath,"r");
            file.seek(mLastCompleteLength);//将位置初始化到上次完成的位置

            md5String = MD5utils.getFileMD5(filePath);
            if (TextUtils.isEmpty(md5String)){
                //无法得到md5值
                isExit = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailure(MD5_ERROR);
                    }
                });
            }else {
                if (file == null || file.length() <= 0){
                    //当file为空,或者取不到长度时
                    isExit = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(FILE_ERROR);
                        }
                    });
                    return;
                }
                fileLength = fileSize();//设置文件总长度
                isExit = false;
                //初始化进度到0位,但是不重置
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPiceSuccess(0,CnsCommonUtil.convertFileSize(mLastCompleteLength),CnsCommonUtil.convertFileSize(fileLength));
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            file = null;
            isExit = true;//结束发送循环
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(FILE_ERROR);
                }
            });
        }

    }


    private void uploadRequest(byte[] datas) throws IOException {


        body =new PiceRequestBody(datas);

        String filename = filePath.substring(filePath.lastIndexOf("/")+1);//取得文件名
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "Keep-Alive")//保持长连接
                .addHeader("Content-Disposition", "attachment; filename=" + encodeName(filename))
                .addHeader("Content-Range", "bytes " + startOffset + "-" + (endOffset - 1) + "/" + fileSize())
                .addHeader("file-md5", md5String)
                .addHeader("Session-ID", "123123123131")
                .addHeader("auto-rename", "1")
                .addHeader("path", "Private%252Fa.apk")
                .post(body)
                .build();
        if (isDev){
            Log.e("Connection","Keep-Alive");
            Log.e("Content-Disposition","attachment; filename=" + encodeName(filename));
            Log.e("Content-Range","bytes " + startOffset + "-" + (endOffset - 1) + "/" + fileSize());
            Log.e("file-md5",md5String);
            Log.e("Session-ID","123123123131");
            Log.e("auto-rename","1");
            Log.e("path","Private%252Fa.apk");
        }



        mCurrentCall = CommonOkHttpClient.getOkHttpClient().newCall(request);

        mResponse = null;

        Response execute = mCurrentCall.execute();
        if (!execute.isSuccessful()){
            Log.e("uploadManager",execute.body().string());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(UPLOAD_FAILD);
                }
            });
            isExit = true;
            return;
        }
        ResponseBody body = execute.body();
        mResponse = body.string();

        Log.e("uploadManager",mResponse);
    }


    private void release() {
        try {
            file.close();
            body = null;
            mResponse = null;
            mCurrentCall = null;
            mPiceRealSize = 0;
            fileLength = 0;
            mLastCompleteLength = 0;
            progress = 0;
            isExit = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private String encodeName(String name) {
        try {
            return URLEncoder.encode(name,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读一片
     * @return
     * @throws IOException
     */
    private byte[] readPice() throws IOException{
        byte[] datas = new byte[DEFAULT_PICE_SIZE];
        mPiceRealSize = file.read(datas,0, DEFAULT_PICE_SIZE);
        return datas;
    }


    /**
     *
     * @return
     */
    protected long fileSize() {
        try {
            return file.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 得到真正的一片大小
     * @return
     */
    protected int getPiceRealSize() {
        return mPiceRealSize;
    }

    /**
     * 得到完成进度
     * @return
     */
    /*protected long getCurrentCompleteLength() {
        if(body == null) {
            return super.getCurrentCompleteLength();
        }
        return super.getCurrentCompleteLength() + mRequestBody.mCurrentCompleteLength;
    }*/


    private boolean isPiceSuccessful() {
        try{
            String response = mResponse;
            JSONObject jObj = new JSONObject(response);
            int code = jObj.optInt("code");
            if(code == 200 || isFinished()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean isFinished() {
        try {
            String response = mResponse;
            JSONObject jsonObject = new JSONObject(response);
            int code = jsonObject.optInt("code");
            if(code == 0) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    protected class PiceRequestBody extends RequestBody {

        private ByteArrayInputStream mSource; //当前需要传输的一片
        private int mCurrentCompleteLength; //当前已经完成的长度，写入多少增加多少

        PiceRequestBody(byte[] datas) {
            mSource = new ByteArrayInputStream(datas,0,getPiceRealSize());
        }

        @Override
        public long contentLength() throws IOException {
            //需要指定此次请求的内容长度，以从数据圆中实际读取的长度为准
            return getPiceRealSize();
        }

        @Override
        public MediaType contentType() {
            //服务器支持的contenttype 类型
            return MediaType.parse("application/octet-stream");
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            byte[] buf = new byte[8192];
            int len = 0;

            //这里这样处理是由于可以得到进度的连续变化数值，而不需要等到一片传完才等获取已经传输的长度
            while((len = mSource.read(buf)) != -1) {
                sink.write(buf,0,len);
                sink.flush();
                mCurrentCompleteLength += len;
            }

            mSource.reset();
            mSource.close();
        }
    }
}
