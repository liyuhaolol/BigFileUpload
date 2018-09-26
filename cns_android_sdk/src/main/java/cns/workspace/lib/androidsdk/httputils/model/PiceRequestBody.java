package cns.workspace.lib.androidsdk.httputils.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class PiceRequestBody extends RequestBody{

    private ByteArrayInputStream mSource;//当前需要传输的一片
    private int mCurrentCompleteLength; //当前已经完成的长度，写入多少增加多少
    private int piceLength;//添加的参数

    public PiceRequestBody(byte[] datas,int length){
        this.piceLength = length;
        mSource = new ByteArrayInputStream(datas,0,length);
    }

    @Override
    public long contentLength() throws IOException {
        return piceLength;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        //这里这样处理是由于可以得到进度的连续变化数值，而不需要等到一片传完才等获取已经传输的长度
        while ((len = mSource.read(buf)) != -1){
            sink.write(buf,0,len);
            sink.flush();
            mCurrentCompleteLength += len;
        }

        mSource.reset();
        mSource.close();

    }

    public int getmCurrentCompleteLength(){
        return mCurrentCompleteLength;
    }

    @Override
    public MediaType contentType() {
        //服务器支持的contenttype 类型
        return MediaType.parse("application/octet-stream");
    }

}
