package com.teaphy.okhttptest.retrofit;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.teaphy.okhttptest.R;
import com.teaphy.okhttptest.urlConstant.HttpConstant;
import com.teaphy.okhttptest.request.ProgressRequestBody;
import com.teaphy.okhttptest.request.ProgressResponseBody;
import com.teaphy.okhttptest.retrofit.api.PersonService;
import com.teaphy.okhttptest.retrofit.bean.ResultInfo;
import com.teaphy.okhttptest.retrofit.interceptor.HttpLoggingInterceptor;
import com.teaphy.okhttptest.util.LogUtil;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AtyRetrofitForFile extends AppCompatActivity {

	// JSON传值
	public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
	// 图片上传
	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
	// 文件（text）上传
	public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

	// 保存路径
	private static final String SAVE_REAL_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

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

	RxPermissions mRxPermissions;

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
				MultipartBody.Part part = MultipartBody.Part.createFormData("upfile", time + ".png", proBody);
				Retrofit retrofit = new Retrofit.Builder()
						.baseUrl(HttpConstant.BASE_URL)
						.addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加Rxjava
						.addConverterFactory(GsonConverterFactory.create()) //
						.build();

				PersonService personService = retrofit.create(PersonService.class);
				Observable<ResultInfo<String>> observable = personService.uploadFile(part, "张三");
				observable.subscribeOn(Schedulers.newThread())
						.subscribe(new Observer<ResultInfo<String>>() {
							@Override
							public void onSubscribe(Disposable d) {

							}

							@Override
							public void onNext(ResultInfo<String> stringResultInfo) {
								Logger.d(stringResultInfo.toString());
							}

							@Override
							public void onError(Throwable e) {
								Logger.e(e.toString());
							}

							@Override
							public void onComplete() {

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
				checkStoragePermission();
				break;
		}
	}

	private void checkStoragePermission() {
		mRxPermissions = new RxPermissions(this);
		mRxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
				.subscribe(granted -> {
					if (granted) { // 在android 6.0之前会默认返回true
						// 已经获取权限
						doDownload();
					} else {
						// 未获取权限
						Toast.makeText(this, "未获取权限", Toast.LENGTH_SHORT).show();
					}
				});
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

//        ProgressResponseBody.ProgressResponseListener mResponseListener = (bytesRead, contentLength, done) -> {
//            LogUtil.i("bytesRead: " + bytesRead + " | contentLength:" + contentLength);
//            int progress = (int) (100 * bytesRead / contentLength) ;
//            Observable.just(progress)
//                    .subscribeOn(AndroidSchedulers.mainThread())
//                    .subscribe(pro -> {
//                        if (progress % 5 == 0) {
//                            sbDownload.setProgress(pro);
//                        } else if (progress == 99) {
//                            sbDownload.setProgress(100);
//                        }
//
//                        tvDownload.setText(pro + "/100");
//                    });
//        };
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(chain -> {
//                    //拦截
//                    Response originalResponse = chain.proceed(chain.request());
//                    //包装响应体并返回
//                    return originalResponse.newBuilder()
//                            .body(new ProgressResponseBody(originalResponse.body(), mResponseListener))
//                            .build();
//                })
//                .build();
//
		String BASE_URL = "http://f.hiphotos.baidu.com/";
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加Rxjava
//                .addConverterFactory(GsonConverterFactory.create()) //
//                .client(client)
//                .build();
		RetrofitDownFactory
				.newInstance()
				.createDownRetrofit(BASE_URL, new ProgressResponseBody.ProgressResponseListener() {
					@Override
					public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
						LogUtil.i("bytesRead: " + bytesRead + " | contentLength:" + contentLength);
						int progress = (int) (100 * bytesRead / contentLength);
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
					}
				})
				.create(PersonService.class)
				.downloadPicFromNet(url)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new DisposableSubscriber<ResponseBody>() {
					@Override
					public void onNext(ResponseBody responseBody) {
						try {
							Flowable.just(responseBody.bytes())
									.subscribeOn(Schedulers.io())
									.observeOn(AndroidSchedulers.mainThread())
									.doOnComplete(() -> install())
									.subscribe(bytes -> {
										boolean newFile = false;
										File foder = new File(SAVE_REAL_PATH);
										if (!foder.exists()) {
											foder.mkdirs();
										}

										File myCaptureFile = new File(foder, "myApp.apk");
										if (!myCaptureFile.exists()) {
											newFile = myCaptureFile.createNewFile();
										}

										if (newFile) {
											BufferedSink sink = Okio.buffer(Okio.sink(myCaptureFile));
											sink.write(responseBody.bytes());
											sink.flush();
											sink.close();
											LogUtil.i("sink.close() ");

										}
									});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onError(Throwable e) {
						LogUtil.i("onError : " + e.getMessage());
					}

					@Override
					public void onComplete() {
					}
				});
//				.flatMap(new Function<ResponseBody, Flowable<File>>() {
//					@Override
//					public Flowable<File> apply(@NonNull ResponseBody responseBody) throws Exception {
//						File myCaptureFile;
//						try {
////                    InputStream inputStream = response.body().byteStream();
////                    BufferedSource source =       Okio.buffer(Okio.source(inputStream));
//							boolean newFile = false;
//							File foder = new File(SAVE_REAL_PATH);
//							if (!foder.exists()) {
//								foder.mkdirs();
//							}
//
//							myCaptureFile = new File(foder, "myApp.apk");
//							if (!myCaptureFile.exists()) {
//								newFile = myCaptureFile.createNewFile();
//							}
//
//							if (newFile) {
//								BufferedSink sink = Okio.buffer(Okio.sink(myCaptureFile));
//								sink.write(responseBody.bytes());
//								sink.flush();
//								sink.close();
//							}
//						} catch (IOException ignored) {
//							LogUtil.i("IOException : " + ignored.getMessage());
//							return Flowable.empty();
//						}
//						return Flowable.just(myCaptureFile);
//					}
//				})
//
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(new Consumer<File>() {
//					@Override
//					public void accept(@NonNull File file) throws Exception {
//						install();
//					}
//				});

//		observable.subscribeOn(Schedulers.newThread())
//				.subscribe(new Observer<ResponseBody>() {
//					@Override
//					public void onSubscribe(Disposable d) {
//
//					}
//
//					@Override
//					public void onNext(ResponseBody responseBody) {
//						try {
////                    InputStream inputStream = response.body().byteStream();
////                    BufferedSource source =       Okio.buffer(Okio.source(inputStream));
//							boolean newFile = false;
//							File foder = new File(SAVE_REAL_PATH);
//							if (!foder.exists()) {
//								foder.mkdirs();
//							}
//
//							File myCaptureFile = new File(foder, "myApp.apk");
//							if (!myCaptureFile.exists()) {
//								newFile = myCaptureFile.createNewFile();
//							}
//
//							if (newFile) {
//								BufferedSink sink = Okio.buffer(Okio.sink(myCaptureFile));
//								sink.write(responseBody.bytes());
//								sink.flush();
//								sink.close();
//								install();
//							}
//						} catch (IOException ignored) {
//							LogUtil.i("IOException : " + ignored.getMessage());
//						}
//					}
//
//					@Override
//					public void onError(Throwable e) {
//						LogUtil.i("onError : " + e.getMessage());
//					}
//
//					@Override
//					public void onComplete() {
//
//					}
//				});
	}

	/**
	 * 通过隐式意图调用系统安装程序安装APK
	 */
	public void install() {
		File file = new File(
				SAVE_REAL_PATH
				, "myApp.apk");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		// 由于没有在Activity环境下启动Activity,设置下面的标签
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		LogUtil.i("Build.VERSION.SDK_INT : " + Build.VERSION.SDK_INT);
		LogUtil.i("Build.VERSION_CODES.N : " + Build.VERSION_CODES.N);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判读版本是否在7.0以上
			LogUtil.i("install ");
			//参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
			Uri apkUri =
					FileProvider.getUriForFile(this, "com.todo.httpdemo.fileprovider", file);
			//添加这一句表示对目标应用临时授权该Uri所代表的文件
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
		} else {
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
		}
		startActivity(intent);
	}
}
