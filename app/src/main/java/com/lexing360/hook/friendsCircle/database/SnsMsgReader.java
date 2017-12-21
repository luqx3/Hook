package com.lexing360.hook.friendsCircle.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lexing360.hook.common.Config;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.http.WechatTextSingle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

/**
 * Created by zzb on 2017/12/21.
 */

public class SnsMsgReader {
    protected Context context = null;
    Class SnsDetailParser = null;
    Class SnsDetail = null;
    Class SnsObject = null;
    Parser parser = null;
    List<WechatTextSingle> wechatTextSingleList=new ArrayList<>();
    static SnsMsgReader instance;

    private SnsMsgReader(Context context) {
        this.context = context;
        initSnsReader();
        //this.makeExtDir();
    }

    public static SnsMsgReader getInstance(Context mContext){
        if(instance==null){
            instance=new SnsMsgReader(mContext);
        }
        return instance;
    }
    private void initSnsReader() {
        File outputAPKFile = new File(Config.EXT_DIR + "wechat.apk");
        if (!outputAPKFile.exists())
            copyAPKFromAssets();
        try {
            Config.initWeChatVersion("6.3.13.64_r4488992");
            DexClassLoader cl = new DexClassLoader(
                    outputAPKFile.getAbsolutePath(),
                    context.getDir("outdex", 0).getAbsolutePath(),
                    null,
                    ClassLoader.getSystemClassLoader());
            SnsDetailParser = cl.loadClass(Config.SNS_XML_GENERATOR_CLASS);
            SnsDetail = cl.loadClass(Config.PROTOCAL_SNS_DETAIL_CLASS);
            SnsObject = cl.loadClass(Config.PROTOCAL_SNS_OBJECT_CLASS);
            parser = new Parser(SnsDetail, SnsDetailParser, SnsObject);
        } catch (Throwable e) {
            Log.e("Error" , "exception", e);
        }
    }
    private void copyAPKFromAssets() {
        InputStream assetInputStream = null;
        File outputAPKFile = new File(Config.EXT_DIR + "wechat.apk");
        if (!outputAPKFile.exists()){
            byte[] buf = new byte[1024];
            try {
                outputAPKFile.createNewFile();
                assetInputStream = context.getAssets().open("wechat.apk");
                FileOutputStream outAPKStream = new FileOutputStream(outputAPKFile);
                int read;
                while((read = assetInputStream.read(buf)) != -1) {
                    outAPKStream.write(buf, 0, read);
                }
                assetInputStream.close();
                outAPKStream.close();
            } catch (Exception e) {
                Log.e("Error", "exception", e);
            }
        }
        // outputAPKFile.delete();

    }
    public List<WechatTextSingle>  querySnsMicroMsgDatabase(String ... params) throws Throwable {
        String dbPath = Config.EXT_DIR + "SnsMicroMsg.db";
        if (!new File(dbPath).exists()) {
            Log.e("Error", "DB file not found");
            throw new Exception("DB file not found");
        }
        wechatTextSingleList.clear();
        SQLiteDatabase database = SQLiteDatabase.openDatabase(dbPath, null, 0);
        Cursor cursor ;
        cursor=database.query("SnsInfo", new String[]{"SnsId", "userName", "createTime", "content", "attrBuf"},
                "createTime > " +  (params.length>0 ? params[0] : Share.snsLastExportTime ),
                new String[]{}, "", "", "createTime DESC", "");
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
        return wechatTextSingleList;
    }
    protected void addSnsInfoFromCursor(Cursor cursor) throws Throwable {
        byte[] snsDetailBin = cursor.getBlob(cursor.getColumnIndex("content"));
        WechatTextSingle newSns = parser.parseWechatTextSingleFromBin(snsDetailBin);
        wechatTextSingleList.add(newSns);
    }
}
