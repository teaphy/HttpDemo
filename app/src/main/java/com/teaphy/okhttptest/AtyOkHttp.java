package com.teaphy.okhttptest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.view.RxView;
import com.teaphy.okhttptest.urlConstant.HttpConstant;
import com.teaphy.okhttptest.util.LogUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class AtyOkHttp extends AppCompatActivity {

    // JSON传值
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    // 图片上传
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    // 文件（text）上传
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    public static final int REQUEST_CODE_PICK_IMAGE = 0;

    @BindView(R.id.tvResult)
    TextView tvResult;
    @BindView(R.id.btnRegister)
    Button btnRegister;
    @BindView(R.id.btnQueryPersons)
    Button btnQueryPersons;
    @BindView(R.id.btnQueryScore)
    Button btnQueryScore;
    @BindView(R.id.btnLoad)
    Button btnLoad;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_ok_http);
        ButterKnife.bind(this);


        setListener();
    }

    private void setListener() {
        RxView.clicks(btnRegister)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(view -> {
                    doRegister();
                });
        RxView.clicks(btnQueryPersons)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(view -> {
                    queryPersons();
                });

        RxView.clicks(btnQueryScore)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(view -> {
                    queryScore();
                });

        RxView.clicks(btnLoad)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(aVoid -> {
                    Intent intent = new Intent(this, AtyOkHttpForLoad.class);
                    startActivity(intent);
                });
    }

    /**
     * 注册
     */
    private void doRegister() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", "10001001")
                .addEncoded("name", "张三")
                .add("pwd", "asdf")
                .build();
//        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(HttpConstant.BASE_URL + HttpConstant.ACTION_REGISTER)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i(" ############ doRegister - onFailure  ############ : " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String strResult = response.body().string();
                    Observable.just(strResult)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> tvResult.setText(res));
                    LogUtil.i(" ############ queryPersons - onResponse  ############ : " + strResult);
                } else {
                    String strResult = response.body().string();
                    LogUtil.i(" ############ queryPersons - onResponse  ############ : " + strResult);
                }
            }
        });
    }

    private void queryPersons() {
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(HttpConstant.BASE_URL + HttpConstant.ACTION_QUERY_PERSON)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i(" ############ queryPersons - onFailure  ############ : " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String strResult = response.body().string();
                    Observable.just(strResult)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> tvResult.setText(res));
                    LogUtil.i(" ############ queryPersons - onResponse  ############ : " + strResult);
                }
            }
        });
    }

    /**
     * 成绩查询
     */
    private void queryScore() {

        final OkHttpClient client = new OkHttpClient();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "张三");
//        RequestBody body = new FormBody.Builder()
//                .add("name", "张三")
//                .build();
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(HttpConstant.BASE_URL + HttpConstant.ACTION_QUERY_SCORE)
                .post(body)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i(" ############ queryPersons - onFailure  ############ : " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String strResult = response.body().string();
                    Observable.just(strResult)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> tvResult.setText(res));
                    LogUtil.i(" ############ queryPersons - onResponse  ############ : " + strResult);
                }
            }
        });
    }

}
