package com.lexing360.hook.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by zzb on 2017/12/12.
 */

public class Permission {
    static int REQUEST_CODE=100;
    public static boolean getPhoneStatePer(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_CODE);
            return false;
        }
        return true;
    }
    public static boolean getReadAndWritePer(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
            return false;
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
            return false;
        }
        return true;
    }

    public static void getPermission(Context context){
        getPhoneStatePer(context);
        getReadAndWritePer(context);
    }
}
