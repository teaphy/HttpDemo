package com.teaphy.okhttptest.retrofit.bean;

/**
 * Created by Administrator
 * on 2016/6/22.
 */
public class ResultInfo<T> {
    String retCode;
    String retInfo;
    T result;

    public ResultInfo() {
    }

    public ResultInfo(String retCode, String retInfo, T result) {
        this.retCode = retCode;
        this.retInfo = retInfo;
        this.result = result;
    }

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public String getRetInfo() {
        return retInfo;
    }

    public void setRetInfo(String retInfo) {
        this.retInfo = retInfo;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "retCode='" + retCode + '\'' +
                ", retInfo='" + retInfo + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
