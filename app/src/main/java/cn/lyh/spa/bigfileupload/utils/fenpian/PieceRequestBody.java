package cn.lyh.spa.bigfileupload.utils.fenpian;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class PieceRequestBody extends RequestBody {
    private ByteArrayInputStream mSource; //当前需要传输的一片
    private long pieceRealSize;
    private MultpartListener multpartListener;

    public PieceRequestBody(byte[] datas,long pieceRealSize,MultpartListener multpartListener) {
        this.pieceRealSize = pieceRealSize;
        this.multpartListener = multpartListener;
        mSource = new ByteArrayInputStream(datas,0,(int)pieceRealSize);
    }

    @Override
    public long contentLength() throws IOException {
        //需要指定此次请求的内容长度，以从数据圆中实际读取的长度为准
        return pieceRealSize;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("application/octet-stream");
    }

    @Override
    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        byte[] buf = new byte[8192];
        int len;

        //这里这样处理是由于可以得到进度的连续变化数值，而不需要等到一片传完才等获取已经传输的长度
        while((len = mSource.read(buf)) != -1) {
            bufferedSink.write(buf,0,len);
            bufferedSink.flush();
            if(multpartListener != null){
                multpartListener.onPiece(len);
            }
        }

        mSource.reset();
        mSource.close();
    }
}
