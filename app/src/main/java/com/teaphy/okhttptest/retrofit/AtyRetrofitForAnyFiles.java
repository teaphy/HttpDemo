package com.teaphy.okhttptest.retrofit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.teaphy.okhttptest.R;
import com.teaphy.okhttptest.adapter.ImagesAdapter;
import com.teaphy.okhttptest.urlConstant.HttpConstant;
import com.teaphy.okhttptest.request.ProgressRequestBody;
import com.teaphy.okhttptest.retrofit.api.PersonService;
import com.teaphy.okhttptest.retrofit.bean.ResultInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AtyRetrofitForAnyFiles extends AppCompatActivity {

    public static final int REQUEST_IMAGE = 0x01;

    // JSON传值
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    // 图片上传
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    // 文件（text）上传
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.btn_submit)
    Button btnSubmit;
    @BindView(R.id.rv_images)
    RecyclerView rvImages;
    @BindView(R.id.sb_upload)
    SeekBar sbUpload;
    @BindView(R.id.tv_upload)
    TextView tvUpload;
    @BindView(R.id.activity_aty_retrofit_for_any_files)
    LinearLayout activityAtyRetrofitForAnyFiles;

    ImagesAdapter mAdapter;
    List<String> mLists;
    ProgressRequestBody proBody;
    List<MultipartBody.Part> mParts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aty_retrofit_for_any_files);
        ButterKnife.bind(this);

        initData();

        initView();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                // Get the result list of select image paths
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                if (null != path && path.size() > 0) {
                    mLists.addAll(path);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void initData() {
        mLists = new ArrayList<>();
        mParts = new ArrayList<>();
    }

    private void initView() {
        mAdapter = new ImagesAdapter(this, R.layout.item_images, mLists);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvImages.setLayoutManager(manager);
        rvImages.setAdapter(mAdapter);
    }

    @OnClick({R.id.btn_add, R.id.btn_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                selectImages();
                break;
            case R.id.btn_submit:
                configImagesRequestBody();
                break;
        }
    }

    /**
     * 选择照片
     */
    private void selectImages() {
        MultiImageSelector.create(this)
                .showCamera(true) // show camera or not. true by default
                .count(9) // max select image size, 9 by default. used width #.multi()
                .single() // single mode
                .multi() // multi mode, default mode;
                .start(this, REQUEST_IMAGE);
    }

    /**
     * 上传多张照片
     */
    private void  uploadAnyFiles() {

        Logger.d("uploadAnyFiles");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HttpConstant.BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加Rxjava
                .addConverterFactory(GsonConverterFactory.create()) //
                .build();

//        ProgressRequestBody fileBodys =
        if (null == proBody) {
            Log.d("AtyRetrofitForAnyFiles", "flieBodys is NULL");
            return;
        }



        PersonService personService = retrofit.create(PersonService.class);
        Observable<ResultInfo<String>> observable = personService.uploadAnyFiles(mParts, "张三");
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

    private ProgressRequestBody configImagesRequestBody() {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
        Logger.d("configImagesRequestBody");

        Observable.from(mLists)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
//                        MultipartBody multipartBody = multipartBuilder.build();
                        Logger.d("上传图片");
                        uploadAnyFiles();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.getMessage());
                    }

                    @Override
                    public void onNext(String path) {
                        File file = new File(path);
                        long time = System.currentTimeMillis();
                        RequestBody body = RequestBody.create(MEDIA_TYPE_PNG, file);

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

                        MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", time + ".png", proBody);
//                        multipartBuilder.addPart(part);
                        mParts.add(part);
                    }
                });


        return proBody;
    }
}
