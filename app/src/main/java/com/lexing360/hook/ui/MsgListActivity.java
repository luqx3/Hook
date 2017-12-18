package com.lexing360.hook.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.lexing360.hook.R;
import com.lexing360.hook.common.Config;
import com.lexing360.hook.common.Share;
import com.lexing360.hook.database.Task;
import com.lexing360.hook.message.MessageTable;
import com.lexing360.hook.message.adapter.MsgAdapter;


/**
 * Created by zzb on 2017/12/15.
 */

public class MsgListActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView snsListView = (ListView)findViewById(R.id.sns_list_view);
        MsgAdapter adapter = new MsgAdapter(this, R.layout.sns_item, Share.msgData.msgList);
        snsListView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.moment_export_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.filter_menu_btn:
                //showFilterDialog();
                return true;
            case R.id.export_confirm_btn:
                exportSelectedMsg();
                SharedPreferences mySharedPreferences=getSharedPreferences("hook_setting", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=mySharedPreferences.edit();
                Share.msgLastExportTime=Share.msgLastTime;
                editor.putLong("msgLastExportTime",Share.msgLastExportTime);
                editor.commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void exportSelectedMsg() {
        MessageTable.saveToJSONFile(Share.msgData.msgList, Config.EXT_DIR + "exported_msg.json", true);
        new AlertDialog.Builder(this)
                .setMessage(String.format(getString(R.string.export_success), Config.EXT_DIR + "exported_msg.json"))
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
