package com.teaphy.okhttptest.retrofit.api;

import com.teaphy.okhttptest.urlConstant.HttpConstant;
import com.teaphy.okhttptest.request.ProgressRequestBody;
import com.teaphy.okhttptest.retrofit.bean.Person;
import com.teaphy.okhttptest.retrofit.bean.ResultInfo;
import com.teaphy.okhttptest.retrofit.bean.Score;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Created by Administrator
 * on 2016/6/22.
 */
public interface PersonService {

    @GET(HttpConstant.ACTION_QUERY_PERSON)
    Call<ResultInfo<List<Person>>> queryPersons();

    @GET(HttpConstant.ACTION_QUERY_SCORE)
    Call<ResultInfo<List<Score>>> queryScore(@Path("name") String name);

    @FormUrlEncoded
    @POST(HttpConstant.ACTION_REGISTER)
    Call<ResultInfo<Person>> register(@Field("id") String id, @Field("name") String name, @Field("pwd") String pwd);

    @POST(HttpConstant.ACTION_REGISTER)
    Call<ResultInfo<Person>> register(@Body RequestBody body);

    @GET(HttpConstant.ACTION_QUERY_PERSON)
    Observable<ResultInfo<List<Person>>> queryPersonsForRxJava();

    @Multipart
    @POST(HttpConstant.ACTION_UPLOAD_FILE)
    Observable<ResultInfo<String>> uploadFile(@Part MultipartBody.Part part, @Part("name") String name);

    @Multipart
    @POST(HttpConstant.ACTION_UPLOAD_FILE)
    Observable<ResultInfo<String>> uploadAnyFiles(@Part List<MultipartBody.Part> parts, @Part("name") String name);

    //下载文件
    @GET
    Flowable<ResponseBody> downloadPicFromNet(@Url String fileUrl);
}
