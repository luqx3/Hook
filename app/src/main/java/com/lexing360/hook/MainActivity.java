package com.lexing360.hook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.lexing360.hook.chatroom.ChatMsgReader;
import com.lexing360.hook.common.Config;
import com.lexing360.hook.common.Init;
import com.lexing360.hook.common.Permission;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.friendsCircle.database.SnsMsgReader;
import com.lexing360.hook.friendsCircle.database.Task;
import com.lexing360.hook.friendsCircle.SnsStat;
import com.lexing360.hook.chatroom.ChatStat;
import com.lexing360.hook.chatroom.model.MsgInfo;
import com.lexing360.hook.http.MsgDTO;
import com.lexing360.hook.http.UploadRetrofit;
import com.lexing360.hook.http.WechatTextSingle;
import com.lexing360.hook.ui.MomentListActivity;
import com.lexing360.hook.ui.ChatListActivity;

import net.sqlcipher.database.SQLiteDatabase;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
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

public class MainActivity extends AppCompatActivity {
    Task task = null;
    SnsStat snsStat = null;
    Button circleBtn, msgBtn, reInitBtn, exportbtn;
    boolean isReady = false;
    public List<MsgInfo> msgList = new ArrayList<>();

    TextView descText;
    boolean isFrist = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleBtn = (Button) findViewById(R.id.launch_circle);
        msgBtn = (Button) findViewById(R.id.launch_message);
        reInitBtn = (Button) findViewById(R.id.reInit);
        exportbtn = (Button) findViewById(R.id.exportAll);
        SQLiteDatabase.loadLibs(this);
        Permission.getPermission(this);
        //copyDB(true);
        task = new Task(this.getApplicationContext());
        descText = (TextView) findViewById(R.id.description_textview_2);
        exportbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportbtn.setEnabled(false);
                descText.setText("正在查询数据");
                uploadAll();
                //new uploadAllTask().execute();

            }
        });
        reInitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openService();
                copyDB(isFrist);
            }
        });
        circleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReady) {
                    circleBtn.setText(R.string.exporting_sns);
                    circleBtn.setEnabled(false);
                    new RunningTask().execute();
                }
            }
        });
        msgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msgBtn.setText(R.string.exporting_msg);
                msgBtn.setEnabled(false);
                try {
                    if (isReady) {
                        loadMsgTask();
                        //upload();
                    }
                } catch (Throwable e) {
                    descText.setText("Error: " + e.getMessage());
                }
            }
        });
    }

    void copyDB(boolean isfrist) {
        this.isFrist=false;
        circleBtn.setEnabled(false);
        msgBtn.setEnabled(false);
        Init.InitDB(getApplicationContext(), isfrist).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable e) {
                descText.setText("Error: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                isReady = true;
                circleBtn.setEnabled(true);
                msgBtn.setEnabled(true);
                descText.setText("更新数据完成");
            }
        });

    }


    void uploadAll() {
        Observable.create(new ObservableOnSubscribe<List<WechatTextSingle>>() {
            @Override
            public void subscribe(ObservableEmitter<List<WechatTextSingle>> emitter) throws Exception {
                Init.copyDB(getApplicationContext(), true);
                task.initSnsReader();
                task.snsReader.runSnsMicroMsg();
                emitter.onNext(new ChatMsgReader(getApplicationContext()).uploadEnMicroMsgDB());
                emitter.onNext(task.snsReader.getWechatTextSingleList());
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<WechatTextSingle>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(List<WechatTextSingle> list) {
                        UploadRetrofit.getInstance().getUploadService()
                                .submitVehicleDemand(Share.msgDTO)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        if(response.code()==200){
                                            descText.setText("上传成功" +"\n"
                                                    + "response: "+(response == null ? "" : response.message()));
                                            SharedPreferences mySharedPreferences=getSharedPreferences("hook_setting", Activity.MODE_PRIVATE);
                                            SharedPreferences.Editor editor=mySharedPreferences.edit();
                                            Share.msgLastExportTime=Share.msgLastTime;
                                            Share.snsLastExportTime=Share.snsLastTime;
                                            editor.putLong("msgLastExportTime",Share.msgLastExportTime);
                                            editor.putLong("snsLastExportTime",Share.snsLastExportTime);
                                            editor.commit();
                                        }else {
                                            descText.setText("上传失败" +"\n"
                                                    + "response: "+(response == null ? "" : response.message()));
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        descText.setText("ERROR: "+t.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    class uploadAllTask extends AsyncTask<Void, Void, Void> {
        Throwable error = null;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Init.copyDB(getApplicationContext(), true);
                task.initSnsReader();
                MsgDTO dto=new MsgDTO();
                task.snsReader.runSnsMicroMsg();
                dto.list.addAll(new ChatMsgReader(getApplicationContext()).uploadEnMicroMsgDB("0"));
                dto.list.addAll(task.snsReader.getWechatTextSingleList());
                Share.msgDTO=dto;
            } catch (Throwable e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            exportbtn.setEnabled(true);
            descText.setText("正在上传" +"\n");
            UploadRetrofit.getInstance().getUploadService()
                    .submitVehicleDemand(Share.msgDTO)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.code()==200){
                                descText.setText("上传成功" +"\n"
                                        + "response: "+(response == null ? "" : response.message()));
                                SharedPreferences mySharedPreferences=getSharedPreferences("hook_setting", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor=mySharedPreferences.edit();
                                Share.msgLastExportTime=Share.msgLastTime;
                                Share.snsLastExportTime=Share.snsLastTime;
                                editor.putLong("msgLastExportTime",Share.msgLastExportTime);
                                editor.putLong("snsLastExportTime",Share.snsLastExportTime);
                                editor.commit();
                            }else {
                                descText.setText("上传失败" +"\n"
                                        + "response: "+(response == null ? "" : response.message()));
                            }
                        }
                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            descText.setText("ERROR: "+t.getMessage());
                        }
                    });
            if(error!=null){
                descText.setText(error.getMessage());
            }
        }
    }
    List<WechatTextSingle> list;
    class RunningTask extends AsyncTask<Void, Void, Void> {

        Throwable error = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                list= SnsMsgReader.getInstance(getApplicationContext()).querySnsMicroMsgDatabase();
                //task.initSnsReader();
                //task.snsReader.runSnsMicroMsg("0");
                //snsStat = new SnsStat(task.snsReader.getSnsList());
            } catch (Throwable e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            super.onPostExecute(voidParam);
            circleBtn.setText(R.string.launch_circle);
            circleBtn.setEnabled(true);
            if (this.error != null) {
                Toast.makeText(MainActivity.this, R.string.not_rooted, Toast.LENGTH_LONG).show();
                Log.e("Error", "exception", this.error);

                try {
                    descText.setText("Error: " + this.error.getMessage());
                } catch (Throwable e) {
                    Log.e("Error", "exception", e);
                }
                return;
            }
            Share.snsData = snsStat;
            Intent intent = new Intent(MainActivity.this, MomentListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void openService() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void upload() throws Exception {
        io.reactivex.Observable.create(new ObservableOnSubscribe<List<WechatTextSingle>>() {
            @Override
            public void subscribe(ObservableEmitter<List<WechatTextSingle>> emitter) throws Exception {
                emitter.onNext(new ChatMsgReader(getApplicationContext()).uploadEnMicroMsgDB("0"));
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<WechatTextSingle>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<WechatTextSingle> list) {
                        Share.msgDTO = new MsgDTO(list);
                        UploadRetrofit.getInstance().getUploadService()
                                .submitVehicleDemand(Share.msgDTO)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        descText.setText("上传成功" + (response == null ? "" : response));
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        descText.setText("Error: 上传失败 " + t.getMessage());
                                    }
                                });
                    }
                    @Override
                    public void onError(Throwable e) {
                        descText.setText("Error: " + e.getMessage());
                        msgBtn.setText(R.string.launch_message);
                        msgBtn.setEnabled(true);

                    }

                    @Override
                    public void onComplete() {
                        msgBtn.setText(R.string.launch_message);
                        msgBtn.setEnabled(true);

                    }
                });
    }

    void loadMsgTask() throws Exception {
        io.reactivex.Observable.create(new ObservableOnSubscribe<List<MsgInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<MsgInfo>> emitter) throws Exception {
                emitter.onNext(new ChatMsgReader(getApplicationContext()).queryEnMicroMsgDB());
                emitter.onComplete();

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MsgInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<MsgInfo> msgInfos) {
                        Log.i("Msg Size:", msgInfos.size() + "");
                        ChatStat chatStat = new ChatStat(msgInfos);
                        Share.msgData = chatStat;
                    }

                    @Override
                    public void onError(Throwable e) {
                        descText.setText("Error: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        msgBtn.setText(R.string.launch_message);
                        msgBtn.setEnabled(true);
                        Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                        startActivity(intent);

                    }
                });
    }
}
