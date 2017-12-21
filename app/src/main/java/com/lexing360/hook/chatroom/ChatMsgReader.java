package com.lexing360.hook.chatroom;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.lexing360.hook.chatroom.model.MsgInfo;
import com.lexing360.hook.common.Config;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.http.MsgDTO;
import com.lexing360.hook.http.UploadRetrofit;
import com.lexing360.hook.http.WechatTextSingle;


import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by zzb on 2017/12/20.
 */

public class ChatMsgReader {


    ArrayList<MsgInfo> msgList = new ArrayList<MsgInfo>();
    Context mContext;
    SQLiteDatabase chatDB;
    String WechatTextSingleType="1";
    static ChatMsgReader instance;

    SQLiteDatabaseHook hook = new SQLiteDatabaseHook(){
        @Override
        public void preKey(SQLiteDatabase database){
        }
        @Override
        public void postKey(SQLiteDatabase database){
            //最关键的一句！！！
            //database.rawExecSQL("PRAGMA cipher_migrate")这句最为关键，原因如下：
            //现在SQLCipher for Android已经是3.X版本了，而微信居然还停留在2.X时代，所以这句话是为了能够用3.X的开源库兼容2.X的加密解密方法，如果不加这句话，是无法对数据库进行解密的。
            database.rawExecSQL("PRAGMA cipher_migrate;");
        }
    };

    private ChatMsgReader(Context context){
        this.mContext=context;
    }
    public static ChatMsgReader getInstance(Context context){
        if(instance==null){
            instance=new ChatMsgReader(context);
        }
        return instance;
    }

    public List<WechatTextSingle> uploadEnMicroMsgDB(final String ... params){
        Cursor cursor;
        List<WechatTextSingle> list=new ArrayList<>();

        File databaseFile = mContext.getDatabasePath( Config.EXT_DIR + "EnMicroMsg.db");
        Log.i("luqx","KEY: "+Share.KEY);
        chatDB = SQLiteDatabase.openOrCreateDatabase(databaseFile, Share.KEY, null , hook);
        cursor = chatDB.query("message",
                new String[]{"talkerId", "talker", "createTime", "content"},
                "createTime > " + (params.length>0 ? params[0]:Share.msgLastExportTime),
                new String[]{}, "", "", "createTime DESC", "");
        boolean isFrist = true;
        while (cursor.moveToNext()) {
            if (isFrist) {
                Share.msgLastTime = cursor.getLong(cursor.getColumnIndex("createTime"));
                isFrist = false;
            }
            list.add(new WechatTextSingle(WechatTextSingleType,cursor));
        }
        cursor.close();
        chatDB.close();
        return list;
    }

    public List<MsgInfo> queryEnMicroMsgDB(final String ... params){
        Cursor cursor;
        msgList = new ArrayList<>();
        File databaseFile = mContext.getDatabasePath( Config.EXT_DIR + "EnMicroMsg.db");
        chatDB = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(databaseFile, Share.KEY, null,hook);
        cursor = chatDB.query("message",
                new String[]{"talkerId", "talker", "createTime", "content"},
                "createTime > " + (params.length>0 ? params[0]:Share.msgLastExportTime),
                new String[]{}, "", "", "createTime DESC", "");
        boolean isFrist = true;
        while (cursor.moveToNext()) {
            if (isFrist) {
                Share.msgLastTime = cursor.getLong(cursor.getColumnIndex("createTime"));
                isFrist = false;
            }
            MsgInfo msgInfo=new MsgInfo(cursor);
            Cursor nicknameCursor=chatDB.query("rcontact",
                    new String[]{"nickname"},
                    "username  = '" +  msgInfo.talker +"'",
                    new String[]{}, "", "", "", "1");
            if(nicknameCursor.moveToNext()){
                msgInfo.nickname=nicknameCursor.getString(0);
            }
            nicknameCursor.close();
            msgList.add(msgInfo);

        }
        cursor.close();
        chatDB.close();
        return msgList;
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
                msgJSON.put("nickname", currentMsg.nickname);
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
