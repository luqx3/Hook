package com.lexing360.hook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.lexing360.hook.common.Init;
import com.lexing360.hook.common.Permission;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.database.Task;
import com.lexing360.hook.friendsCircle.SnsStat;
import com.lexing360.hook.message.MessageTable;
import com.lexing360.hook.message.MsgStat;
import com.lexing360.hook.message.model.MsgInfo;
import com.lexing360.hook.ui.MomentListActivity;
import com.lexing360.hook.ui.MsgListActivity;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    Task task = null;
    SnsStat snsStat = null;
    Button circleBtn, msgBtn,reInitBtn;
    boolean isReady = false;
    public List<MsgInfo> msgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleBtn = (Button) findViewById(R.id.launch_circle);
        msgBtn = (Button) findViewById(R.id.launch_message);
        reInitBtn = (Button) findViewById(R.id.reInit);
        SQLiteDatabase.loadLibs(this);
        Permission.getPermission(this);
        copyDB(true);
        task = new Task(this.getApplicationContext());
        reInitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyDB(false);
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
                    }
                } catch (Throwable e) {
                    ((TextView) findViewById(R.id.description_textview_2)).setText("Error: " + e.getMessage());
                }
            }
        });
    }

    void copyDB(boolean isFrist) {
        circleBtn.setEnabled(false);
        msgBtn.setEnabled(false);
        Init.InitDB(getApplicationContext(), isFrist).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable e) {
                ((TextView) findViewById(R.id.description_textview_2)).setText("Error: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                isReady = true;
                circleBtn.setEnabled(true);
                msgBtn.setEnabled(true);
                ((TextView) findViewById(R.id.description_textview_2)).setText("更新数据完成");
            }
        });

    }

    void loadMsgTask() throws Exception {
        MessageTable.getInstance()
                .getAllMsg()
                .subscribe(new Observer<List<MsgInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<MsgInfo> msgInfos) {
                        Log.i("Msg Size:", msgInfos.size() + "");
                        MsgStat msgStat = new MsgStat(msgInfos);
                        Share.msgData = msgStat;
                    }

                    @Override
                    public void onError(Throwable e) {
                        ((TextView) findViewById(R.id.description_textview_2)).setText("Error: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        msgBtn.setText(R.string.launch_message);
                        msgBtn.setEnabled(true);
                        Intent intent = new Intent(MainActivity.this, MsgListActivity.class);
                        startActivity(intent);

                    }
                });
    }

    class RunningTask extends AsyncTask<Void, Void, Void> {

        Throwable error = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                task.initSnsReader();
                task.snsReader.runSnsMicroMsg("0");
                snsStat = new SnsStat(task.snsReader.getSnsList());
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
                    ((TextView) findViewById(R.id.description_textview_2)).setText("Error: " + this.error.getMessage());
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
}
