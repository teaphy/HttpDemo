package com.teaphy.okhttptest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;
import com.teaphy.okhttptest.retrofit.AtyRetrofit;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

	@BindView(R.id.btnOkHttp)
	Button btnOkHttp;
	@BindView(R.id.btnRetrofit)
	Button btnRetrofit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setListener();
	}

	private void setListener() {
		RxView.clicks(btnOkHttp)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(view -> {
					Intent intent = new Intent(this, AtyOkHttp.class);
					startActivity(intent);
				});

		RxView.clicks(btnRetrofit)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(view -> {
					Intent intent = new Intent(this, AtyRetrofit.class);
					startActivity(intent);
				});
	}

}
