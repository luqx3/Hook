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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.lexing360.hook.chatroom.SqlCipher.hook;

/**
 * Created by zzb on 2017/12/20.
 */

public class ChatMsgReader {
    ArrayList<MsgInfo> msgList = new ArrayList<MsgInfo>();
    Context mContext;
    SQLiteDatabase chatDB;
    String WechatTextSingleType="2";

    public ChatMsgReader(Context context){
        this.mContext=context;
    }

    public List<WechatTextSingle> uploadEnMicroMsgDB(final String ... params){
        Cursor cursor;
        List<WechatTextSingle> list=new ArrayList<>();

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
