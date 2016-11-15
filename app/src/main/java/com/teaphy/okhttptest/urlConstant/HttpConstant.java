package com.teaphy.okhttptest.urlConstant;

/**
 * Created by Administrator
 * on 2016/6/14.
 */
public class HttpConstant {
    //public static final String BASE_URL = "http://172.27.35.1:8080/teaphy/";
    //public static final String BASE_URL = "http://192.168.1.103:8080/teaphy/";
    public static final String BASE_URL = "http://192.168.1.104:8080/teaphy/";

    public static final String ACTION_REGISTER = "operatePerson/register";

    public static final String ACTION_QUERY_PERSON = "operatePerson/queryPersons";

    public static final String ACTION_QUERY_SCORE = "operatePerson/queryScore/{name}";

    public static final String ACTION_UPLOAD_FILE = "operatePerson/uploadFile";

    public static final String ACTION_UPLOAD_ANY_FILES = "uploadAnyFiles";
}
