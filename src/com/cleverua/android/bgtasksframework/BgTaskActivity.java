package com.cleverua.android.bgtasksframework;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

public class BgTaskActivity extends BaseActivity {

    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button b = (Button) findViewById(R.id.the_button);
        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                log("Starting service");
                Intent i = new Intent(BgTaskActivity.this, BgTasksService.class);
                i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, MyApplication.TaskEnum.DOWNLOAD_IMAGES_TASK.name());
                i.putExtra(BgTasksService.TASK_ACTIVITY_CLASS_KEY, BgTaskActivity.class);
                startService(i);
                progress = getProgress();
                progress.show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        dismissProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaskEnum taskId = MyApplication.TaskEnum.DOWNLOAD_IMAGES_TASK;
        registerReceiver(broadcastReceiver, new IntentFilter(taskId.name()));
        MyApplication.TaskStatus status = getApp().getTaskStatus(taskId);
        log("onResume: " + taskId + " status is: " + status);
        if (status == MyApplication.TaskStatus.STARTED) {
            if (progress == null) {
                progress = getProgress();
            }
            progress.show();
        }
        if (status == MyApplication.TaskStatus.COMPLETED) {
            inform("Task has been completed successfully!");
            getApp().invalidateTask(taskId);
            getApp().cancelBgTaskNotification();
        }
        if (status == MyApplication.TaskStatus.ERROR) {
            alert("Task has failed! Error code: " + getApp().getTaskErrorCode(taskId));
            getApp().invalidateTask(taskId);
            getApp().cancelBgTaskNotification();
        }
    }

    private ProgressDialog getProgress() {
        ProgressDialog p = new ProgressDialog(this);
        p.setTitle(R.string.app_name);
        p.setMessage("Operation in progress...");
        p.setCancelable(true);
        p.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                log("Progress dialog cancelled");
                getApp().invalidateTask(MyApplication.TaskEnum.DOWNLOAD_IMAGES_TASK);
                dismissProgress();
            }
        });
        return p;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("Received broadcast: " + intent.getExtras());
            getApp().invalidateTask(MyApplication.TaskEnum.DOWNLOAD_IMAGES_TASK);

            if (intent.getStringExtra(MyApplication.TASK_OUTCOME_EXTRA_KEY).equals(MyApplication.ERROR_OUTCOME)) {
                final int errorCode = intent.getIntExtra(MyApplication.ERROR_CODE_EXTRA_KEY, MyApplication.UNKNOWN_ERROR);
                alert("Task has failed! Error code: " + errorCode);
            } else {
                inform("Task has been completed successfully!");
            }

            getApp().cancelBgTaskNotification();
            dismissProgress();
        }
    };

    private void dismissProgress() {
        if (progress != null) {
            progress.dismiss();
        }
        progress = null;
    }

}