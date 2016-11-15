package com.teaphy.okhttptest;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.view.RxView;
import com.teaphy.okhttptest.urlConstant.HttpConstant;
import com.teaphy.okhttptest.request.ProgressRequestBody;
import com.teaphy.okhttptest.request.ProgressResponseBody;
import com.teaphy.okhttptest.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class AtyOkHttpForLoad extends AppCompatActivity {

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
    ProgressBar sbUpload;
    @BindView(R.id.tv_upload)
    TextView tvUpload;
    @BindView(R.id.btnUpload)
    Button btnUpload;
    @BindView(R.id.sb_download)
    ProgressBar sbDownload;
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

        setListener();
    }

    private void init() {
        sbDownload.setMax(100);
        sbUpload.setMax(100);
        sbDownload.setProgress(0);
        sbUpload.setProgress(0);
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
//                FormBody.Builder formBuilder = new FormBody.Builder();
                // RequestBody formBody = formBuilder.add("name", "asdffasdf").build();
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("name", "张三");


                RequestBody body = multipartBuilder
//                        .addFormDataPart("upfile", null, RequestBody.create(MEDIA_TYPE_PNG, file))
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("upfile", "asdf.jpg",
                                RequestBody.create(MEDIA_TYPE_PNG, file))
                        .addFormDataPart("name", "张三")
                        .build();
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
                final OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(HttpConstant.BASE_URL + HttpConstant.ACTION_UPLOAD_FILE)
                        .post(proBody)
                        .build();

                Call call = client.newCall(request);
                LogUtil.i("############ 图片路径: " + picturePath);
                LogUtil.i("############ : " + "图片开始上传！");
                // ToastUtils.showShort(MainActivity.this, "图片开始上传！");
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtil.i(" ############ doRegister - onFailure  ############ : " + e.toString());
//                        ToastUtils.showShort(MainActivity.this, "上传失败！");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // ToastUtils.showShort(mContext, "上传成功！");
                            String strResult = response.body().string();
                            byte[] bytes = strResult.getBytes("UTF-8");
                            String str = new String(bytes, "UTF-8");
                            Observable.just(strResult)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(res -> tvResult.setText(str));
                            LogUtil.i(" ############ queryPersons - onResponse  ############ : " + str);
                        } else {
                            LogUtil.i(" ############ queryPersons - onResponse  ############ : " + response.body().string());
                        }
                    }
                });

            }
        }

    }

    private void setListener() {
        RxView.clicks(btnUpload)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(view -> {
                    uploadImage();
                });

        RxView.clicks(btnDownload)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(view -> {
                    downloadImage();
                });

    }

    /**
     * 上传头像
     */
    private void uploadImage() {
        getImageFromAlbum();
    }

    protected void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void downloadImage() {
        //String url = "http://b.hiphotos.baidu.com/image/h%3D300/sign=1b921b860d24ab18ff16e73705fbe69a/86d6277f9e2f070861ccd4a0ed24b899a801f241.jpg";
        String url = "http://f.hiphotos.baidu.com/image/h%3D300/sign=e50211178e18367ab28979dd1e738b68/0b46f21fbe096b63a377826e04338744ebf8aca6.jpg";
        //String url = "http://gdown.baidu.com/data/wisegame/8d5889f722f640c8/weixin_800.apk";

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

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    //拦截
                    Response originalResponse = chain.proceed(chain.request());
                    //包装响应体并返回
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), mResponseListener))
                            .build();
                })
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
                        sink.write(response.body().bytes());
                        sink.flush();
                        sink.close();
                        Observable.just(myCaptureFile)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(file -> {
                                    Glide.with(AtyOkHttpForLoad.this)
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
