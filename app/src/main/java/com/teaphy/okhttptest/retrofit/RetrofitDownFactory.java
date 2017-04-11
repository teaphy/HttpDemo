package com.teaphy.okhttptest.retrofit;


import com.teaphy.okhttptest.request.ProgressResponseBody;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author todo
 * @desc
 * @date 2017/4/11
 */

public class RetrofitDownFactory {
	private volatile static RetrofitDownFactory mFactory = null;

	public static RetrofitDownFactory newInstance() {

		if (mFactory == null) {
			synchronized (RetrofitDownFactory.class) {
				if (mFactory == null) {
					mFactory = new RetrofitDownFactory();
				}
			}
		}

		return mFactory;
	}

	public Retrofit createDownRetrofit(String baseUrl,  ProgressResponseBody.ProgressResponseListener responseListener) {
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(chain -> {
					//拦截
					Response originalResponse = chain.proceed(chain.request());
					//包装响应体并返回
					return originalResponse.newBuilder()
							.body(new ProgressResponseBody(originalResponse.body(), responseListener))
							.build();
				})
				.build();

		return new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加Rxjava
				.addConverterFactory(GsonConverterFactory.create()) //
				.client(client)
				.build();
	}
}
