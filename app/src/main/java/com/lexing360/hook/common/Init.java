package com.lexing360.hook.common;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static java.lang.Thread.sleep;

/**
 * Created by zzb on 2017/12/18.
 */

public class Init {

    public static String settingFile="hook_setting";

    public static  void testRoot() throws Exception {
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();
    }
    public static void makeExtDir() {
        File extDir = new File(Config.EXT_DIR);
        if (!extDir.exists()) {
            extDir.mkdir();
        }
    }
    public static void copySP() throws Exception{
        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        String destDir = Config.EXT_DIR;
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes("mount -o remount,rw " + dataDir + "\n");
        outputStream.writeBytes("rm " + destDir + "system_config_prefs.xml\n");
        outputStream.writeBytes(" cp "+dataDir + "/data/" + Config.WECHAT_PACKAGE +"/shared_prefs/system_config_prefs.xml " + destDir + "\n");
        outputStream.writeBytes("chmod 777 " + destDir + "system_config_prefs.xml\n");
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();
        sleep(500);
    }
    public static void deleteSnsAndMsgDB() throws  Exception{
        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        String destDir = Config.EXT_DIR;
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes("mount -o remount,rw " + dataDir + "\n");

        outputStream.writeBytes("rm " + destDir + "SnsMicroMsg.db\n");
        outputStream.writeBytes("rm " + destDir + "EnMicroMsg.db\n");
        outputStream.writeBytes("sleep 1000\n");

        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();
        sleep(500);
    }
    //复制数据库信息
    //数据库路径：MD5(mm+UIN）;
    public static void copySnsAndMsgDB(String folder) throws Exception {
        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        String destDir = Config.EXT_DIR;
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes("mount -o remount,rw " + dataDir + "\n");

        //outputStream.writeBytes("rm " + destDir + "SnsMicroMsg.db\n");
        //outputStream.writeBytes("rm " + destDir + "EnMicroMsg.db\n");

        outputStream.writeBytes(" cp " + dataDir + "/data/" + Config.WECHAT_PACKAGE +"/MicroMsg/"+folder +"/SnsMicroMsg.db  " + destDir + "\n");
        outputStream.writeBytes(" cp " + dataDir + "/data/" + Config.WECHAT_PACKAGE +"/MicroMsg/"+folder +"/EnMicroMsg.db  "+  destDir+"\n");

        outputStream.writeBytes("sleep 1000\n");
        outputStream.writeBytes("chmod 777 " + destDir + "SnsMicroMsg.db\n");
        outputStream.writeBytes("chmod 777 " + destDir + "EnMicroMsg.db\n");

        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();
    }

    //获取微信UIN
    public static String getUIN() throws Exception {
        String result = "";
        InputStream inputStream = new FileInputStream(Config.EXT_DIR + "system_config_prefs.xml");
        boolean isParse = true;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parse = factory.newPullParser();
        parse.setInput(inputStream, "UTF-8");
        int eventType = parse.getEventType();
        while (XmlPullParser.END_DOCUMENT != eventType && isParse) {
            String nodeName = parse.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("int".equals(nodeName)) {
                        if (parse.getAttributeValue(0).equals("default_uin")) {
                            Share.UIN=parse.getAttributeValue(1);
                            return parse.getAttributeValue(1);
                        }
                    }
                    break;
                default:
                    break;
            }
            if (isParse) {
                eventType = parse.next();
            }
        }
        return result;
    }


    public static Observable<String> InitDB(final Context context,final boolean isFirst){
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                copyDB(context,isFirst);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void copyDB(final Context context,final boolean isFirst) throws Exception{
        boolean exists=false;
        if(isFirst){
            makeExtDir();
            testRoot();
            copySP();
            while (!exists){
                File file=new File(Config.EXT_DIR + "system_config_prefs.xml");
                if(file.exists()){
                    exists=true;
                    getUIN();
                }
                sleep(100);
            }
            Share.FOLDER=WeixinMD5.n(("mm"+ Share.UIN).getBytes());
            //计算数据库秘钥
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED){
                Share.IMEI = ((TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                Share.KEY= WeixinMD5.n((Share.IMEI + Share.UIN).getBytes()).substring(0,7);
                Log.i("luqx","秘钥：   "+Share.IMEI+"  "+Share.UIN+"   "+Share.KEY);
            }else {
                throw new Exception("No permission");
            }
        }
        //deleteSnsAndMsgDB();
        copySnsAndMsgDB(Share.FOLDER);
        //获取上次获取的数据的时间。
        SharedPreferences settings = context.getSharedPreferences(settingFile, Activity.MODE_PRIVATE);
        Share.snsLastExportTime = Share.snsLastTime=settings.getLong("snsLastExportTime",0);
        Share.msgLastExportTime = Share.msgLastTime=settings.getLong("msgLastExportTime",0);
        exists=false;
        while (!exists){
            File enFile=new File(Config.EXT_DIR + "EnMicroMsg.db");
            File snsFile=new File(Config.EXT_DIR + "SnsMicroMsg.db");
            if(enFile.exists() && snsFile.exists()){
                exists=true;
            }
            sleep(100);
        }
        //sleep(1000);
    }
}
