package com.lexing360.hook.http;

import android.database.Cursor;
import android.util.Log;

import com.lexing360.hook.friendsCircle.model.SnsInfo;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zzb on 2017/12/20.
 */

public class WechatTextSingle {
    public WechatTextSingle(String type,SnsInfo snsInfo){
        importCreateTime=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new Date(snsInfo.timestamp * 1000));
        this.text=getText(type,snsInfo.content);
    }
    public WechatTextSingle(String type,Cursor cursor){
        this.importCreateTime=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new Date(cursor.getLong(cursor.getColumnIndex("createTime"))));
        this.text=getText(type,cursor.getString(cursor.getColumnIndex("content")));
    }
    public String importCreateTime;
    public String text;

    String getText(String type,String content){
        JSONObject msgJSON = new JSONObject();
        try {
            msgJSON.put("type", type);
            msgJSON.put("content", content);
        } catch (Exception exception) {
            Log.e("Error", "exception", exception);
            return "ERROR";
        }
        return msgJSON.toString();
    }
}