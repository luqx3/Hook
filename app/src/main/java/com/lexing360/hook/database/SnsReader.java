package com.lexing360.hook.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lexing360.hook.common.Config;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.friendsCircle.model.SnsInfo;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by chiontang on 2/12/16.
 */
public class SnsReader {
    static String KEY="0f31d31";

    Class SnsDetail = null;
    Class SnsDetailParser = null;
    Class SnsObject = null;
    Parser parser = null;
    ArrayList<SnsInfo> snsList = new ArrayList<SnsInfo>();
    String currentUserId = "";
    Context mContext;

    public SnsReader(Class SnsDetail, Class SnsDetailParser, Class SnsObject, Context context){
        this.SnsDetail = SnsDetail;
        this.SnsDetailParser = SnsDetailParser;
        this.SnsObject = SnsObject;
        this.parser = new Parser(SnsDetail, SnsDetailParser, SnsObject);
        this.mContext=context;

    }

    public void runSnsMicroMsg() throws Throwable {
        Log.d("luqx", "Querying Sns database.");
        querySnsMicroMsgDatabase();
        Task.saveToJSONFile(this.snsList, Config.EXT_DIR + "all_sns.json", false);
    }
    public void runEnMicroMsg() throws Throwable {
        Log.d("luqx", "EnMicroMsg Sns database.");
        queryEnMicroMsgdbDatabase();
        Task.saveToJSONFile(this.snsList, Config.EXT_DIR + "all_sns.json", false);
    }

    public ArrayList<SnsInfo> getSnsList() {
        return this.snsList;
    }
    protected void queryEnMicroMsgdbDatabase() throws Throwable {
        SqlCipher.decrypt(mContext,"EnMicroMsg.db","deEnMicroMsg.db", KEY);
        String dbPath = Config.EXT_DIR + "deEnMicroMsg.db";
        if (!new File(dbPath).exists()) {
            Log.e("luqx", "EnMicroMsg DB file not found");
            throw new Exception("DB file not found");
        }
        SQLiteDatabase database = SQLiteDatabase.openDatabase(dbPath, null, 0);
        Cursor cursor = database.query("message", new String[]{"content",} ,"", new String[]{},"","","createTime DESC","");
        while (cursor.moveToNext()) {
            Log.i("luqx",cursor.toString());
            //addSnsInfoFromCursor(cursor);
        }
    }
    protected void querySnsMicroMsgDatabase() throws Throwable {
        String dbPath = Config.EXT_DIR + "SnsMicroMsg.db";
        if (!new File(dbPath).exists()) {
            Log.e("Error", "DB file not found");
            throw new Exception("DB file not found");
        }
        snsList.clear();
        SQLiteDatabase database = SQLiteDatabase.openDatabase(dbPath, null, 0);
        getCurrentUserIdFromDatabase(database);
        Cursor cursor = database.query("SnsInfo", new String[]{"SnsId", "userName", "createTime", "content", "attrBuf"} ,"createTime > "+ Share.snsLastExportTime, new String[]{},"","","createTime DESC","");
        boolean isFrist=true;
        while (cursor.moveToNext()) {
            if(isFrist){
                Share.snsLastTime=cursor.getLong(cursor.getColumnIndex("createTime"));
                isFrist=false;
            }
            addSnsInfoFromCursor(cursor);
        }
        cursor.close();
        database.close();
    }

    protected void getCurrentUserIdFromDatabase(SQLiteDatabase database) throws Throwable {
        Cursor cursor = database.query("snsExtInfo3", new String[]{"userName"}, "ROWID=?", new String[]{"1"}, "", "", "", "1");
        if (cursor.moveToNext()) {
            this.currentUserId = cursor.getString(cursor.getColumnIndex("userName"));
        }
        cursor.close();
        Log.d("Error", "Current userID=" + this.currentUserId);
    }

    protected void addSnsInfoFromCursor(Cursor cursor) throws Throwable {
        byte[] snsDetailBin = cursor.getBlob(cursor.getColumnIndex("content"));
        byte[] snsObjectBin = cursor.getBlob(cursor.getColumnIndex("attrBuf"));
        SnsInfo newSns = parser.parseSnsAllFromBin(snsDetailBin, snsObjectBin);

        for (int i=0;i<snsList.size();i++) {
            if (snsList.get(i).id.equals(newSns.id)) {
                return;
            }
        }

        if (newSns.authorId.equals(this.currentUserId)) {
            newSns.isCurrentUser = true;
        }

        for (int i=0;i<newSns.comments.size();i++) {
            if (newSns.comments.get(i).authorId.equals(this.currentUserId)) {
                newSns.comments.get(i).isCurrentUser = true;
            }
        }

        for (int i=0;i<newSns.likes.size();i++) {
            if (newSns.likes.get(i).userId.equals(this.currentUserId)) {
                newSns.likes.get(i).isCurrentUser = true;
            }
        }

        snsList.add(newSns);
        //newSns.print();
    }



}
