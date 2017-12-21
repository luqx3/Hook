package com.lexing360.hook.http;


import java.util.concurrent.TimeUnit;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by zzb on 2017/12/20.
 */

public class UploadRetrofit {
    private static OkHttpClient httpClient;
    private static Retrofit retrofit;
    String BASE_URL="http://gateway-dev.lexing360.com/v1/";
    IApp uploadService ;
    private static UploadRetrofit instance=null ;

    private UploadRetrofit(){
        httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(httpClient)
                .build();
        uploadService = retrofit.create(IApp.class);
    }

    public static UploadRetrofit getInstance(){
        if(instance==null){
            instance=new UploadRetrofit();
        }
        return  instance;
    }

    public IApp getUploadService(){
        return uploadService;
    }

    public Observable<String> upLoad(MsgDTO dto){
        return getInstance().getUploadService()
                .uploadMsg(dto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
