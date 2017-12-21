package com.lexing360.hook.chatroom;

import android.content.Context;

import com.lexing360.hook.common.Config;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;

/**
 * Created by zzb on 2017/12/15.
 */

public class SqlCipher {

    public  static SQLiteDatabaseHook hook = new SQLiteDatabaseHook(){
        @Override
        public void preKey(SQLiteDatabase database){
        }
        @Override
        public void postKey(SQLiteDatabase database){
            //最关键的一句！！！
            //database.rawExecSQL("PRAGMA cipher_migrate")这句最为关键，原因如下：
            //现在SQLCipher for Android已经是3.X版本了，而微信居然还停留在2.X时代，所以这句话是为了能够用3.X的开源库兼容2.X的加密解密方法，如果不加这句话，是无法对数据库进行解密的。
            database.rawExecSQL("PRAGMA cipher_migrate;");
            database.rawExecSQL("PRAGMA cipher_use_hmac = off;");
            database.rawExecSQL("PRAGMA kdf_iter = 4000;");
            //PRAGMA cipher_use_hmac = off;
            //PRAGMA kdf_iter = 4000;
        }
    };

    /**
     * 加密数据库
     * @param encryptedName 加密后的数据库名称
     * @param decryptedName 要加密的数据库名称
     * @param key 密码
     */
    public static void encrypt(Context context, String encryptedName, String decryptedName, String key) {
        try {
            File databaseFile = context.getDatabasePath( Config.EXT_DIR + decryptedName);
            //打开要加密的数据库
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, "", null);

            //新建加密后的数据库文件
            File encrypteddatabaseFile = context.getDatabasePath( Config.EXT_DIR + encryptedName);

            //连接到加密后的数据库，并设置密码
            database.rawExecSQL(String.format("ATTACH DATABASE '%s' as "+ encryptedName.split("\\.")[0] +" KEY '"+ key +"';", encrypteddatabaseFile.getAbsolutePath()));
            //输出要加密的数据库表和数据到加密后的数据库文件中
            database.rawExecSQL("SELECT sqlcipher_export('"+ encryptedName.split("\\.")[0] +"');");
            //断开同加密后的数据库的连接
            database.rawExecSQL("DETACH DATABASE "+ encryptedName.split("\\.")[0] +";");

            //打开加密后的数据库，测试数据库是否加密成功
            SQLiteDatabase encrypteddatabase = SQLiteDatabase.openOrCreateDatabase(encrypteddatabaseFile, key, null);
            //关闭数据库
            encrypteddatabase.close();
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解密数据库
     * @param encryptedName 要解密的数据库名称
     * @param decryptedName 解密后的数据库名称
     * @param key 密码
     */
    public static void decrypt(Context context,String encryptedName, String decryptedName, String key) {

        try {
            File databaseFile = context.getDatabasePath( Config.EXT_DIR + encryptedName);

            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, key, null,hook);

            File decrypteddatabaseFile = context.getDatabasePath( Config.EXT_DIR + decryptedName);

            //连接到解密后的数据库，并设置密码为空
            database.rawExecSQL(String.format("ATTACH DATABASE '%s' as "+ decryptedName.split("\\.")[0] +" KEY '';", decrypteddatabaseFile.getAbsolutePath()));
            database.rawExecSQL("SELECT sqlcipher_export('"+ decryptedName.split("\\.")[0] +"');");
            database.rawExecSQL("DETACH DATABASE "+ decryptedName.split("\\.")[0] +";");

            SQLiteDatabase decrypteddatabase = SQLiteDatabase.openOrCreateDatabase(decrypteddatabaseFile, "", null);
            decrypteddatabase.close();
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
