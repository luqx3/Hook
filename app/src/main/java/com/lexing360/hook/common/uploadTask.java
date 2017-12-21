package com.lexing360.hook.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.lexing360.hook.chatroom.ChatMsgReader;
import com.lexing360.hook.friendsCircle.database.SnsMsgReader;
import com.lexing360.hook.http.MsgDTO;
import com.lexing360.hook.http.UploadRetrofit;
import com.lexing360.hook.http.WechatTextSingle;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zzb on 2017/12/21.
 */

public class uploadTask {
    static int count=0;
    public static void  uploadAll(final Context context,final boolean isFrist) {
        count=0;
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Init.copyDB(context, isFrist);
                try {
                    List<WechatTextSingle> list=ChatMsgReader.getInstance(context).uploadEnMicroMsgDB();
                    emitter.onNext("群消息");
                    uploadHttp(context,list,emitter);
                    list=SnsMsgReader.getInstance(context).querySnsMicroMsgDatabase();
                    emitter.onNext("朋友圈");
                    uploadHttp(context,list,emitter);
                }catch (Throwable e){
                    EventBus.getDefault().post(new Message(e.getMessage(),Message.ERROR));
                }
            }
        }) .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        EventBus.getDefault().post(new Message("正在上传"+s));
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new Message(e.getMessage(),Message.ERROR));
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new Message("",Message.FINISH));
                    }
                });
//        Observable.create(new ObservableOnSubscribe<List<WechatTextSingle>>() {
//            @Override
//            public void subscribe(ObservableEmitter<List<WechatTextSingle>> emitter) throws Exception {
//                Init.copyDB(context, isFrist);
//                try {
//                   // emitter.onNext(ChatMsgReader.getInstance(context).uploadEnMicroMsgDB());
//                    emitter.onNext(SnsMsgReader.getInstance(context).querySnsMicroMsgDatabase());
//                    emitter.onComplete();
//                }catch (Throwable e){
//                    EventBus.getDefault().post(new Message(e.getMessage(),Message.ERROR));
//                }
//            }
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<WechatTextSingle>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//                    @Override
//                    public void onNext(List<WechatTextSingle> list) {
//                        Log.i("luqx","size "+list.size());
//                        EventBus.getDefault().post(new Message("正在上传数据"));
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        EventBus.getDefault().post(new Message(e.getMessage(),Message.ERROR));
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        EventBus.getDefault().post(new Message("",Message.FINISH));
//                    }
//                });
    }

    public static void uploadHttp(final Context context,List<WechatTextSingle> list,final ObservableEmitter<String> emitter) throws Exception{
        UploadRetrofit.getInstance().getUploadService()
                .submitVehicleDemand(new MsgDTO(list))
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        count++;
                        if(response.code()==200){
                            EventBus.getDefault().post(new Message("上传成功"));
                            SharedPreferences mySharedPreferences=context.getSharedPreferences("hook_setting", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor=mySharedPreferences.edit();
                            Share.msgLastExportTime=Share.msgLastTime;
                            Share.snsLastExportTime=Share.snsLastTime;
                            editor.putLong("msgLastExportTime",Share.msgLastExportTime);
                            editor.putLong("snsLastExportTime",Share.snsLastExportTime);
                            editor.commit();
                        }else {
                            EventBus.getDefault().post(new Message("上传失败" +"\n" +
                                    "response: "+(response == null ? "" : response.message()),Message.ERROR));
                        }
                        if(count==2){
                            emitter.onComplete();
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        count++;
                        if(count==2){
                            emitter.onComplete();
                        }
                        //throw new Exception("");
                    }
                });
    }
}
