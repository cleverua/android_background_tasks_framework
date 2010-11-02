package com.cleverua.android.bgtasksframework;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

public class BgTaskActivity extends BaseActivity {

    private static final int PROGRESS_DIALOG_ID = 1;
    private static final int CANCEL_CONFIRMATION_DIALOG_ID = 2;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button b = (Button) findViewById(R.id.the_button);
        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                log("Starting service");
                Intent i = new Intent(BgTaskActivity.this, BgTasksService.class);
                i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, MyApplication.TaskEnum.SAMPLE_TASK.name());
                i.putExtra(BgTasksService.TASK_ACTIVITY_CLASS_KEY, BgTaskActivity.class);
                startService(i);
                showDialog(PROGRESS_DIALOG_ID);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	switch (id) {
    		case PROGRESS_DIALOG_ID:
    			ProgressDialog p = new ProgressDialog(this);
    	        p.setTitle(R.string.app_name);
    	        p.setMessage("Operation in progress...");
    	        p.setCancelable(false);
                p.setOnKeyListener(new OnKeyListener() {
    				@Override
    				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
    					if (keyCode == KeyEvent.KEYCODE_BACK) {
    						showDialog(CANCEL_CONFIRMATION_DIALOG_ID);
    						return true;
    					}
    					return false;
    				}
                });
                dialog = p;
    	        break;
    		case CANCEL_CONFIRMATION_DIALOG_ID:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage("Are you sure you want to exit?")
    			       .setCancelable(false)
    			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    	    	                log("Progress dialog cancelled");
    	    	                getApp().invalidateTask(MyApplication.TaskEnum.SAMPLE_TASK);
    	    	                hideDialogs();
    			           }
    			       })
    			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			dialog = builder.create();
    			break;
    		default:
    			dialog = super.onCreateDialog(id);
    			break;
    	}
    	return dialog;
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaskEnum taskId = MyApplication.TaskEnum.SAMPLE_TASK;
        registerReceiver(broadcastReceiver, new IntentFilter(taskId.name()));
        
        MyApplication.TaskStatus status = getApp().getTaskStatus(taskId);
        log("onResume: " + taskId + " status is: " + status);
        if (status == MyApplication.TaskStatus.COMPLETED) {
        	hideDialogs();
        	taskSuccessMessage();
            getApp().invalidateTask(taskId);
        }
        if (status == MyApplication.TaskStatus.ERROR) {
        	hideDialogs();
        	taskFailedMessage();
            getApp().invalidateTask(taskId);
        }
    }

    private void hideDialogs() {
    	dismissDialog(PROGRESS_DIALOG_ID);
    	dismissDialog(CANCEL_CONFIRMATION_DIALOG_ID);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("Received broadcast: " + intent.getExtras());
            
            TaskEnum taskId = MyApplication.TaskEnum.SAMPLE_TASK;
            MyApplication.TaskStatus status = getApp().getTaskStatus(taskId);
            
            if (status == MyApplication.TaskStatus.COMPLETED) {
            	taskSuccessMessage();
            }
            if (status == MyApplication.TaskStatus.ERROR) {
            	taskFailedMessage();
            }

            getApp().invalidateTask(MyApplication.TaskEnum.SAMPLE_TASK);
            hideDialogs();
        }
    };
    
    private void taskSuccessMessage() {
    	inform("Task has been completed successfully! Result: " + 
        		getApp().getTaskResult(MyApplication.TaskEnum.SAMPLE_TASK));
    }
    
    private void taskFailedMessage() {
    	alert("Task has failed! Error code: " + getApp().getTaskErrorCode(MyApplication.TaskEnum.SAMPLE_TASK));
    }

}