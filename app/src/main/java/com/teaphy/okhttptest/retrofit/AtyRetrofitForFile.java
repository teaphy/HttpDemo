package com.teaphy.okhttptest.retrofit;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;
import com.teaphy.okhttptest.R;
import com.teaphy.okhttptest.http.HttpConstant;
import com.teaphy.okhttptest.request.ProgressRequestBody;
import com.teaphy.okhttptest.request.ProgressResponseBody;
import com.teaphy.okhttptest.retrofit.bean.ResultInfo;
import com.teaphy.okhttptest.retrofit.interceptor.HttpLoggingInterceptor;
import com.teaphy.okhttptest.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AtyRetrofitForFile extends AppCompatActivity {

    // JSON传值
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    // 图片上传
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    // 文件（text）上传
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath():
            "/mnt/sdcard";//保存到SD卡
    private static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/http";//保存的确切位置

    public static final int REQUEST_CODE_PICK_IMAGE = 0;

    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.sb_upload)
    SeekBar sbUpload;
    @BindView(R.id.tv_upload)
    TextView tvUpload;
    @BindView(R.id.btnUpload)
    Button btnUpload;
    @BindView(R.id.sb_download)
    SeekBar sbDownload;
    @BindView(R.id.tv_download)
    TextView tvDownload;
    @BindView(R.id.btnDownload)
    Button btnDownload;
    @BindView(R.id.iv_download)
    ImageView ivDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_ok_http_for_load);
        ButterKnife.bind(this);

        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (null != data) {
                LogUtil.i("############ : " + data.getData().getPath());
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                File file = new File(picturePath);
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();

                RequestBody body = RequestBody.create(MEDIA_TYPE_PNG, file);
                //RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "张三");
                ProgressRequestBody proBody = new ProgressRequestBody(body, progress -> {
                    Observable.just(progress)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(pro -> {
                                if (progress % 5 == 0) {
                                    sbUpload.setProgress(pro);
                                } else if (progress == 99) {
                                    sbUpload.setProgress(100);
                                }

                                tvUpload.setText(pro + "/100");
                            });

                });
                long time = System.currentTimeMillis();
                MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", time +".png", proBody);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(HttpConstant.BASE_URL)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加Rxjava
                        .addConverterFactory(GsonConverterFactory.create()) //
                        .build();

                PersonService personService = retrofit.create(PersonService.class);
                Observable<ResultInfo<String>> observable = personService.uploadFile(part, "张三");
                observable.subscribeOn(Schedulers.newThread())
                        .subscribe(new Observer<ResultInfo<String>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.e(e.toString());
                            }

                            @Override
                            public void onNext(ResultInfo<String> stringResultInfo) {
                                Logger.d(stringResultInfo.toString());
                            }
                        });

            }
        }

    }

    private void init() {
        sbDownload.setMax(100);
        sbUpload.setMax(100);
        sbDownload.setProgress(0);
        sbUpload.setProgress(0);
    }

    @OnClick({R.id.btnUpload, R.id.btnDownload})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUpload:
                doUpLoad();
                break;
            case R.id.btnDownload:
                doDownload();
                break;
        }
    }

    /**
     * 上传文件
     */
    private void doUpLoad() {
        getImageFromAlbum();
    }

    protected void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * 下载文件
     */
    private void doDownload() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Level.BASIC);

        //String url = "http://b.hiphotos.baidu.com/image/h%3D300/sign=1b921b860d24ab18ff16e73705fbe69a/86d6277f9e2f070861ccd4a0ed24b899a801f241.jpg";
       // String url = "image/h%3D300/sign=e50211178e18367ab28979dd1e738b68/0b46f21fbe096b63a377826e04338744ebf8aca6.jpg";
        String url = "http://gdown.baidu.com/data/wisegame/8d5889f722f640c8/weixin_800.apk";

        ProgressResponseBody.ProgressResponseListener mResponseListener = (bytesRead, contentLength, done) -> {
            LogUtil.i("bytesRead: " + bytesRead + " | contentLength:" + contentLength);
            int progress = (int) (100 * bytesRead / contentLength) ;
            Observable.just(progress)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(pro -> {
                        if (progress % 5 == 0) {
                            sbDownload.setProgress(pro);
                        } else if (progress == 99) {
                            sbDownload.setProgress(100);
                        }

                        tvDownload.setText(pro + "/100");
                    });
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    //拦截
                    Response originalResponse = chain.proceed(chain.request());
                    //包装响应体并返回
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), mResponseListener))
                            .build();
                })
                .build();

        String BASE_URL = "http://f.hiphotos.baidu.com/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加Rxjava
                .addConverterFactory(GsonConverterFactory.create()) //
                .client(client)
                .build();
        PersonService personService = retrofit.create(PersonService.class);
        Observable<ResponseBody> observable = personService.downloadPicFromNet(url);

        observable.subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
//                    InputStream inputStream = response.body().byteStream();
//                    BufferedSource source =       Okio.buffer(Okio.source(inputStream));
                            boolean newFile = false;
                            File foder = new File(SAVE_REAL_PATH);
                            if (!foder.exists()) {
                                foder.mkdirs();
                            }
                            long time = System.currentTimeMillis();
                            File myCaptureFile = new File(SAVE_REAL_PATH, time + ".apk");
                            if (!myCaptureFile.exists()) {
                                newFile = myCaptureFile.createNewFile();
                            }

                            if (newFile) {
                                BufferedSink sink = Okio.buffer(Okio.sink(myCaptureFile));
                                sink.write(responseBody.bytes());
                                sink.flush();
                                sink.close();
                                Observable.just(myCaptureFile)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(file -> {
                                            Glide.with(AtyRetrofitForFile.this)
                                                    .load(myCaptureFile)
                                                    .asBitmap()
                                                    .into(ivDownload);
                                        });


                            }
                        } catch (IOException ignored) {

                        }
                    }
                });

    }
}
