package com.cleverua.android.bgtasksframework;

import java.io.Serializable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.cleverua.android.bgtasksframework.BaseApplication.TaskStatus;
import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

public class BgTaskActivity extends BaseActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (isFinishing())
        	return;

        Button b = (Button) findViewById(R.id.the_button);
        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				log("Starting service ");

				Intent i = new Intent(BgTaskActivity.this, BgTasksService.class);
				i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, MyApplication.TaskEnum.SAMPLE_TASK);
				i.putExtra(BgTasksService.TASK_ACTIVITY_CLASS_KEY, BgTaskActivity.class);
				i.putExtra("bg_task_parameter_1", "example of first parameter");

				getApp().startBackgroundTask(i, "Operation in progress");
            }
        });

        b = (Button) findViewById(R.id.yet_another_button);
        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				log("Starting service ");

				Intent i = new Intent(BgTaskActivity.this, BgTasksService.class);
				i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, MyApplication.TaskEnum.YET_ANOTHER_TASK);
				i.putExtra(BgTasksService.TASK_ACTIVITY_CLASS_KEY, BgTaskActivity.class);
				i.putExtra("bg_task_parameter_1", "example of first parameter");

				getApp().startBackgroundTask(i, "Non cancelabe operation in progress");
            }
        });
}
    
    @Override
	protected void onBackgroundTaskStopped(TaskEnum taskId, TaskStatus taskStatus, Serializable taskResult, int taskErrorCode) {
    	super.onBackgroundTaskStopped(taskId, taskStatus, taskResult, taskErrorCode);
    	if (taskId == TaskEnum.SAMPLE_TASK) {
			if (taskStatus == TaskStatus.COMPLETED) {
				Toast.makeText(getApplicationContext(),"Sample Task has been completed successfully! Result: " + taskResult, Toast.LENGTH_LONG).show();
			} else if (taskStatus == TaskStatus.ERROR) {
				Toast.makeText(getApplicationContext(),"Sample Task has failed! Error code: " + taskErrorCode, Toast.LENGTH_LONG).show();
			} else if (taskStatus == TaskStatus.VOID) {
				Toast.makeText(getApplicationContext(),"Sample Task has canceled! Result: " + taskResult, Toast.LENGTH_LONG).show();
			}
    	} else if (taskId == TaskEnum.YET_ANOTHER_TASK) {
			if (taskStatus == TaskStatus.COMPLETED) {
				inform("Yet Another Task has been completed successfully! Result: " + taskResult);
			} else if (taskStatus == TaskStatus.ERROR) {
				inform("Yet Another Task has failed! Error code: " + taskErrorCode);
			} else if (taskStatus == TaskStatus.VOID) {
				inform("Yet Another Task has canceled!");
			}
    	}
    	getApp().invalidateTask(taskId);
    }          
}