package com.teaphy.okhttptest.request;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 包装的请求体，处理进度
 */
public class ProgressRequestBody extends RequestBody {
    // 实际的待包装请求体
    protected RequestBody mDelegate;
    // 进度回调接口
    protected ProgressRequestListener mListener;
    // 包装完成的BufferedSink
    protected CountingSink mCountingSink;

    public ProgressRequestBody(RequestBody delegate, ProgressRequestListener listener) {
        mDelegate = delegate;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    /**
     * 重写调用实际的响应体的contentType
     * @return MediaType
     */
    @Override
    public long contentLength() {
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 重写进行写入
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        //写入
        mDelegate.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;
        public CountingSink(Sink delegate) {
            super(delegate);
        }
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            // 增加当前写入的字节数
            bytesWritten += byteCount;
            // 回调
            mListener.onProgress((int) (100F * bytesWritten / contentLength()));
        }
    }

    public interface ProgressRequestListener {
        void onProgress(int progress);
    }
}
