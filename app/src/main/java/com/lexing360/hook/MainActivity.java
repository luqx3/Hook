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
import com.lexing360.hook.common.Init;
import com.lexing360.hook.common.Message;
import com.lexing360.hook.common.Permission;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.common.uploadTask;
import com.lexing360.hook.friendsCircle.SnsStat;

import net.sqlcipher.database.SQLiteDatabase;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {
    Button uploadBtn;
    boolean isReady = false;

    TextView descText;
    boolean isFrist = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
//        circleBtn = (Button) findViewById(R.id.launch_circle);
//        msgBtn = (Button) findViewById(R.id.launch_message);
//        reInitBtn = (Button) findViewById(R.id.reInit);
        uploadBtn = (Button) findViewById(R.id.exportAll);
        SQLiteDatabase.loadLibs(this);
        Permission.getPermission(this);
        descText = (TextView) findViewById(R.id.description_textview_2);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadBtn.setEnabled(false);
                descText.setText("正在查询数据");
                uploadTask.uploadAll(getApplicationContext(),isFrist);
                isFrist=false;
                //new uploadAllTask().execute();

            }
        });
    }
    @Subscribe ( threadMode = ThreadMode.MAIN )
    public void printMsg(Message msg){
        descText.setText(msg.TYPE+"  "+ msg.MSG);
        Log.i("luqx",msg.MSG);
        if(Message.FINISH.equals(msg.TYPE)){
            uploadBtn.setEnabled(true);
        }
        if(Message.ERROR.equals(msg.TYPE)){
            descText.setText("重新上传");
            uploadTask.uploadAll(getApplicationContext(),isFrist);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }
}
