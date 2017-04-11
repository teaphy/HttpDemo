package com.teaphy.okhttptest.retrofit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.orhanobut.logger.Logger;
import com.teaphy.okhttptest.R;
import com.teaphy.okhttptest.urlConstant.HttpConstant;
import com.teaphy.okhttptest.retrofit.api.PersonService;
import com.teaphy.okhttptest.retrofit.bean.Person;
import com.teaphy.okhttptest.retrofit.bean.ResultInfo;
import com.teaphy.okhttptest.retrofit.bean.Score;
import com.teaphy.okhttptest.retrofit.interceptor.HttpLoggingInterceptor;
import com.teaphy.okhttptest.util.LogUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AtyRetrofit extends AppCompatActivity {


	@BindView(R.id.btn_get)
	Button btnGet;
	@BindView(R.id.btn_getForRestful)
	Button btnGetForRestful;
	@BindView(R.id.btn_post)
	Button btnPost;
	@BindView(R.id.btn_postWithBody)
	Button btnPostWithBody;
	@BindView(R.id.btn_getRxJava)
	Button btnGetRxJava;
	@BindView(R.id.btn_loadFile)
	Button btnLoadFile;
	@BindView(R.id.btn_loadAnyFiles)
	Button btnLoadAnyFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_retrofit);

		ButterKnife.bind(this);
	}

	@OnClick({R.id.btn_get, R.id.btn_getForRestful, R.id.btn_post, R.id.btn_postWithBody,
			R.id.btn_getRxJava, R.id.btn_loadFile, R.id.btn_loadAnyFiles})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_get:
				doGet();
				break;
			case R.id.btn_getForRestful:
				doGetForRestful();
				break;
			case R.id.btn_post:
				doPost();
				break;
			case R.id.btn_postWithBody:
				doPostWithBody();
				break;
			case R.id.btn_getRxJava:
				doGetRxJava();
				break;
			case R.id.btn_loadFile:
				Intent intent = new Intent(this, AtyRetrofitForFile.class);
				startActivity(intent);
				break;
			case R.id.btn_loadAnyFiles:
				Intent intentAnyFiles = new Intent(this, AtyRetrofitForAnyFiles.class);
				startActivity(intentAnyFiles);
				break;
			default:
				break;
		}
	}

	/**
	 * 普通Get
	 */
	private void doGet() {

		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Level.BASIC);
		OkHttpClient client = new OkHttpClient.Builder()
				//log请求参数
				.addInterceptor(interceptor)
				.build();

		Retrofit mRetrofit = new Retrofit.Builder()
				.baseUrl(HttpConstant.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.client(client)
				.build();

		PersonService personService = mRetrofit.create(PersonService.class);
		Call<ResultInfo<List<Person>>> call = personService.queryPersons();

		call.enqueue(new Callback<ResultInfo<List<Person>>>() {
			@Override
			public void onResponse(Call<ResultInfo<List<Person>>> call, Response<ResultInfo<List<Person>>> response) {
				if (response.isSuccessful()) {
					ResultInfo<List<Person>> info = response.body();
					Log.i("Teaphy", "onResponse: " + info.toString());
					Logger.d("onResponse: " + info.toString());
				}
			}

			@Override
			public void onFailure(Call<ResultInfo<List<Person>>> call, Throwable t) {
				Logger.d("onFailure: " + t.toString());
			}
		});

	}

	/**
	 * Restful Url 带参 Get
	 */
	private void doGetForRestful() {
		Retrofit mRetrofit = new Retrofit.Builder()
				.baseUrl(HttpConstant.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		PersonService personService = mRetrofit.create(PersonService.class);
		Call<ResultInfo<List<Score>>> call = personService.queryScore("李四");
		call.enqueue(new Callback<ResultInfo<List<Score>>>() {
			@Override
			public void onResponse(Call<ResultInfo<List<Score>>> call, Response<ResultInfo<List<Score>>> response) {
				if (response.isSuccessful()) {
					ResultInfo<List<Score>> info = response.body();
					LogUtil.i("onResponse: " + info.toString());
				}
			}

			@Override
			public void onFailure(Call<ResultInfo<List<Score>>> call, Throwable t) {
				LogUtil.i("onFailure: " + t.toString());
			}
		});
	}

	/**
	 * post 请求
	 */
	private void doPost() {
		Retrofit mRetrofit = new Retrofit.Builder()
				.baseUrl(HttpConstant.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		PersonService personService = mRetrofit.create(PersonService.class);
		Call<ResultInfo<Person>> call = personService.register("10001", "李四", "123456");
		call.enqueue(new Callback<ResultInfo<Person>>() {
			@Override
			public void onResponse(Call<ResultInfo<Person>> call, Response<ResultInfo<Person>> response) {
				if (response.isSuccessful()) {
					ResultInfo<Person> info = response.body();
					LogUtil.i("onResponse: " + info.toString());
				}
			}

			@Override
			public void onFailure(Call<ResultInfo<Person>> call, Throwable t) {
				LogUtil.i("onFailure: " + t.toString());
			}
		});
	}

	/**
	 * post 请求参数为 RequestBody
	 */
	private void doPostWithBody() {
		Retrofit mRetrofit = new Retrofit.Builder()
				.baseUrl(HttpConstant.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		PersonService personService = mRetrofit.create(PersonService.class);

		RequestBody body = new FormBody.Builder()
				.addEncoded("id", "1001")
				.add("name", "张三")
				.add("pwd", "123456")
				.build();
		Call<ResultInfo<Person>> call = personService.register(body);
		call.enqueue(new Callback<ResultInfo<Person>>() {
			@Override
			public void onResponse(Call<ResultInfo<Person>> call, Response<ResultInfo<Person>> response) {
				if (response.isSuccessful()) {
					ResultInfo<Person> info = response.body();
					LogUtil.i("onResponse: " + info.toString());
				}
			}

			@Override
			public void onFailure(Call<ResultInfo<Person>> call, Throwable t) {
				LogUtil.i("onFailure: " + t.toString());
			}
		});
	}

	private void doGetRxJava() {
		Retrofit mRetrofit = new Retrofit.Builder()
				.baseUrl(HttpConstant.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build();

		PersonService personService = mRetrofit.create(PersonService.class);
		Observable<ResultInfo<List<Person>>> observable = personService.queryPersonsForRxJava();

		observable.subscribeOn(Schedulers.io()) // 网络请求的线程发起的订阅
				.flatMap(listResultInfo -> Observable.fromIterable(listResultInfo.getResult()))
				.observeOn(AndroidSchedulers.mainThread()) // 调度我们的OnNext方法的线程，也就是UI线程
				.subscribe(new Observer<Person>() {
					@Override
					public void onSubscribe(Disposable d) {

					}

					@Override
					public void onNext(Person person) {
						LogUtil.i("RxJava - Person: " + person.toString());
					}

					@Override
					public void onError(Throwable e) {
						LogUtil.i("RxJava - Person: " + e.toString());
					}

					@Override
					public void onComplete() {

					}
				});

	}
}
