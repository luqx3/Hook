package com.lexing360.hook.message;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lexing360.hook.common.Config;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.message.model.MsgInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zzb on 2017/12/15.
 */

public class MessageTable {
    static MessageTable instance=null;
    static SQLiteDatabase MsgDb;
    String dbPath = Config.EXT_DIR + "deEnMicroMsg.db";
    String key;

    private MessageTable(){


    }

    public static MessageTable getInstance(){
        if(instance==null){
            instance=new MessageTable();
        }
        return instance;
    }


    public Observable<List<MsgInfo>> getAllMsg() throws Exception {
        MsgDb = SQLiteDatabase.openDatabase(dbPath, null, 0);
        return Observable.create(new ObservableOnSubscribe<List<MsgInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<MsgInfo>> emitter) throws Exception {
                Cursor cursor = MsgDb.query("message",
                        new String[]{"talkerId", "talker", "createTime", "content"},
                        "createTime > " + Share.msgLastExportTime,
                        new String[]{}, "", "", "createTime DESC", "");
                List<MsgInfo> result = new ArrayList<>();
                boolean isFrist = true;
                while (cursor.moveToNext()) {
                    if (isFrist) {
                        Share.msgLastTime = cursor.getLong(cursor.getColumnIndex("createTime"));
                        isFrist = false;
                    }
                    result.add(new MsgInfo(cursor));
                }
                cursor.close();
                MsgDb.close();
                emitter.onNext(result);
                emitter.onComplete();

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void saveToJSONFile(List<MsgInfo> msgList, String fileName, boolean onlySelected) {
        JSONArray msgListJSON = new JSONArray();

        for (int snsIndex=0; snsIndex<msgList.size(); snsIndex++) {
            MsgInfo currentMsg = msgList.get(snsIndex);
            if (onlySelected && !currentMsg.selected) {
                continue;
            }
            JSONObject msgJSON = new JSONObject();
            try {
//                msgJSON.put("isCurrentUser", currentMsg.isCurrentUser);
//                msgJSON.put("talkerId", currentMsg.talkerId);
                msgJSON.put("talker", currentMsg.talker);
                msgJSON.put("content", currentMsg.content);
                msgListJSON.put(msgJSON);
            } catch (Exception exception) {
                Log.e("Error", "exception", exception);
            }
        }

        File jsonFile = new File(fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                Log.e("Error", "exception", e);
            }
        }

        try {
            FileWriter fw = new FileWriter(jsonFile.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msgListJSON.toString());
            bw.close();
        } catch (IOException e) {
            Log.e("Error", "exception", e);
        }
    }

}
