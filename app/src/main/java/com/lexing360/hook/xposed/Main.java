//package com.lexing360.hook.xposed;
//
//import android.util.Log;
//
//
//import com.lexing360.hook.common.Share;
//
//import de.robv.android.xposed.IXposedHookLoadPackage;
//import de.robv.android.xposed.XC_MethodHook;
//import de.robv.android.xposed.XposedHelpers;
//import de.robv.android.xposed.callbacks.XC_LoadPackage;
//
//
//public class Main implements IXposedHookLoadPackage {
//
//    @Override
//    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
//
//        if (loadPackageParam.packageName.equals("com.tencent.mm")) {
//            Log.d("Xposed", "进入微信");
//
//            Class mSQLiteDatabase = XposedHelpers.findClass(
//                    "com.tencent.wcdb.database.SQLiteDatabase", loadPackageParam.classLoader);
//            Log.d("Xposed", "Class mSQLiteDatabase == " + mSQLiteDatabase);
//            Class mSQLiteDatabaseConfiguration = XposedHelpers.findClass(
//                    "com.tencent.wcdb.database.SQLiteDatabaseConfiguration",
//                    loadPackageParam.classLoader
//            );
//            Log.d("Xposed", "Class mSQLiteDatabaseConfiguration == " + mSQLiteDatabaseConfiguration);
//            Class mSQLiteCipherSpec = XposedHelpers.findClass(
//                    "com.tencent.wcdb.database.SQLiteCipherSpec",
//                    loadPackageParam.classLoader
//            );
//            Log.d("Xposed", "Class mSQLiteCipherSpec == " + mSQLiteCipherSpec);
//            Log.d("Xposed", "byte[].class == " + byte[].class);
//
//            XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteConnectionPool",
//                    loadPackageParam.classLoader,
//                    "open",
//                    mSQLiteDatabase,
//                    mSQLiteDatabaseConfiguration,
//                    byte[].class,
//                    mSQLiteCipherSpec,
//                    int.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            super.beforeHookedMethod(param);
//                            byte[] array = (byte[]) param.args[2];
//                            Log.d("Xposed", "array == " + (new String(array)));
//                            Share.KEY=new String(array);
//                        }
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//
//                        }
//                    });
//        }
//
//    }
//
//
//}
//
