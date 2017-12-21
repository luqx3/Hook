package com.lexing360.hook.http;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by zzb on 2017/12/20.
 */

public interface IApp {
    //POST /v1/vehicles/wechat
    @Headers("Content-Type: application/json")
    @POST("vehicles/wechat")
    Call<String> submitVehicleDemand(@Body MsgDTO list);

    @Headers("Content-Type: application/json")
    @POST("vehicles/wechat")
    Observable<String> uploadMsg(@Body MsgDTO list);


}
