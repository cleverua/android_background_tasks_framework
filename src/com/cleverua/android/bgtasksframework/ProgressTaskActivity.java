package com.cleverua.android.bgtasksframework;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;

import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

public class ProgressTaskActivity extends BaseActivity {

	private static final int PROGRESS_DIALOG_ID = 1;
	private static final int WAIT_DIALOG_ID     = 2;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.progress_task);	
		
		showDialog(PROGRESS_DIALOG_ID);
		
		forwardTaskStatus();
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    registerReceiver(broadcastReceiver, new IntentFilter(getBackgroundTaskKey().name()));
	    forwardTaskStatus();
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
		
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		Dialog dialog = null;
		if (dialogId == PROGRESS_DIALOG_ID) {
            ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(R.string.app_name);
            progress.setMessage(getIntent().getStringExtra(BgTasksService.PROGRESS_MESSAGE_EXTRA_KEY));
            progress.setCancelable(false);
            progress.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						getApp().cancelTask(getBackgroundTaskKey());
						dialog.cancel();
						showDialog(WAIT_DIALOG_ID);
						return true;
					}
					return false;
				}
            });
            dialog = progress;
		} else if (dialogId == WAIT_DIALOG_ID) {
            ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(R.string.app_name);
            progress.setMessage("[en]Please wait...");
            progress.setCancelable(false);
            dialog = progress;
		} else {
			dialog = super.onCreateDialog(dialogId);
		}
		return dialog;
	}
	
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("Received broadcast : " + intent.getExtras());
            forwardTaskStatus();
        }
    };

	private void forwardTaskStatus() {
    	getApp().startTargetActivity(this, getBackgroundTaskKey());
	}
	
	private TaskEnum getBackgroundTaskKey() {
		return (TaskEnum)getIntent().getSerializableExtra(BgTasksService.TASK_ID_EXTRA_KEY);
	}

}
