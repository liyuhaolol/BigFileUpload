package cn.lyh.spa.bigfileupload.utils.fenpian;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class MultpartRequestBody extends RequestBody {
    private MultipartBody mMultipartBody;
    private MultpartListener multpartListener;

    public MultpartRequestBody(MultipartBody multipartBody){
        this(multipartBody,null);
    }

    public MultpartRequestBody(MultipartBody multipartBody, MultpartListener multpartListener){
        this.mMultipartBody = multipartBody;
        this.multpartListener = multpartListener;
    }
    @Override
    public MediaType contentType() {
        return mMultipartBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mMultipartBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //这里需要另一个代理类来获取写入的长度
        ForwardingSink forwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                if(multpartListener != null){
                    multpartListener.onPiece(byteCount);
                }
                super.write(source, byteCount);
            }
        };
        //转一下
        BufferedSink bufferedSink = Okio.buffer(forwardingSink);
        //写数据
        mMultipartBody.writeTo(bufferedSink);
        //刷新一下数据
        bufferedSink.flush();
    }
}
