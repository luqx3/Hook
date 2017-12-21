//package com.lexing360.hook.ui;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.v4.app.NavUtils;
//import android.support.v7.app.AppCompatActivity;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.widget.ListView;
//
//import com.lexing360.hook.R;
//import com.lexing360.hook.common.Config;
//import com.lexing360.hook.common.Share;
//import com.lexing360.hook.friendsCircle.database.Task;
//import com.lexing360.hook.friendsCircle.adapter.SnsInfoAdapter;
//import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
//
//
//public class MomentListActivity extends AppCompatActivity {
//
//    public static boolean snsListUpdated = false;
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//            case R.id.filter_menu_btn:
//                showFilterDialog();
//                return true;
//            case R.id.export_confirm_btn:
//                exportSelectedSns();
//                SharedPreferences mySharedPreferences=getSharedPreferences("hook_setting", Activity.MODE_PRIVATE);
//                SharedPreferences.Editor editor=mySharedPreferences.edit();
//                Share.snsLastExportTime=Share.snsLastTime;
//                editor.putLong("snsLastExportTime",Share.snsLastExportTime);
//                editor.commit();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.moment_export_menu, menu);
//        return true;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_moment_list);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .build();
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
//                .defaultDisplayImageOptions(defaultOptions)
//                .build();
//        ImageLoader.getInstance().init(config);
//
//        updateSnsList();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (MomentListActivity.snsListUpdated) {
//            MomentListActivity.snsListUpdated = false;
//            updateSnsList();
//        }
//    }
//
//    protected void updateSnsList() {
//        ListView snsListView = (ListView)findViewById(R.id.sns_list_view);
//        SnsInfoAdapter adapter = new SnsInfoAdapter(this, R.layout.sns_item, Share.snsData.snsList);
//        snsListView.setAdapter(adapter);
//    }
//
//    protected void showFilterDialog() {
//        Intent intent = new Intent(this, UserSelectActivity.class);
//        startActivity(intent);
//    }
//
//    protected void exportSelectedSns() {
//        Task.saveToJSONFile(Share.snsData.snsList, Config.EXT_DIR + "exported_sns.json", true);
//        new AlertDialog.Builder(this)
//                .setMessage(String.format(getString(R.string.export_success), Config.EXT_DIR + "exported_sns.json"))
//                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                })
//                .show();
//    }
//}
