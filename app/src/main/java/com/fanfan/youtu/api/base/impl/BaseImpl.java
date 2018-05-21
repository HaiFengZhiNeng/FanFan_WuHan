package com.fanfan.youtu.api.base.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.fanfan.novel.common.Constants;
import com.fanfan.youtu.Youtucode;
import com.fanfan.youtu.api.base.Constant;
import com.fanfan.youtu.api.base.OkhttpManager;
import com.fanfan.youtu.token.YoutuSign;
import com.seabreeze.log.Print;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by android on 2017/12/21.
 */

public class BaseImpl<Service> {

    private static Retrofit mRetrofit;
    protected Service mService;

    public BaseImpl(@NonNull Context context) {

        initRetrofit(context);

        mService = mRetrofit.create(getServiceClass());
    }


    private void initRetrofit(Context context) {
        if (null != mRetrofit)
            return;

        YoutuSign.init();

        // 配置 Retrofit
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Constant.API_YOUTU_BASE)                         // 设置 base url
                .client(OkhttpManager.getInstance().getOkhttpClient())                                     // 设置 client
                .addConverterFactory(GsonConverterFactory.create()) // 设置 Json 转换工具
                .build();

    }

    private Class<Service> getServiceClass() {
        return (Class<Service>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private <T> T getApiService(String baseUrl, OkHttpClient client, Class<T> clz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(clz);
    }

}
