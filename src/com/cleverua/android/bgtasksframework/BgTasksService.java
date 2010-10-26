package com.cleverua.android.bgtasksframework;

import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class BgTasksService extends IntentService {

    private static final String TAG = "TasksService";
    
    public static final String TASK_ACTIVITY_CLASS_KEY = "ACTIVITY_CLASS";
    public static final String TASK_ID_EXTRA_KEY       = "TASK_ID";

    public BgTasksService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: " + intent);
        String taskId = intent.getStringExtra(TASK_ID_EXTRA_KEY);
        Class activityClass = (Class) intent.getSerializableExtra(TASK_ACTIVITY_CLASS_KEY);
        Log.d(TAG, "Got task id: " + taskId);

        if (taskId == null || taskId.equals("")) {
            throw new RuntimeException("Intent extra " + TASK_ID_EXTRA_KEY + " is required");
        }

        if (null == MyApplication.TaskEnum.valueOf(taskId)) {
            throw new RuntimeException("Know nothing about " + taskId + " task!");
        }

        if (activityClass == null) {
            throw new RuntimeException("Intent extra " + TASK_ACTIVITY_CLASS_KEY + " is required");
        }

        MyApplication app = (MyApplication) getApplication();
        app.onTaskStarted(MyApplication.TaskEnum.valueOf(taskId), activityClass);

        SystemClock.sleep(5 * 1000); // this is a JOB ;)

        if (!isCancelled(MyApplication.TaskEnum.valueOf(taskId))) {
            app.onTaskCompleted(MyApplication.TaskEnum.valueOf(taskId));
            // uncomment to see error response
            // app.onTaskError(MyApplication.TaskEnum.valueOf(taskId), 1);
        } else {
            Log.d(TAG, "Task: " + taskId + " has been cancelled, doing nothing");
        }
    }

    private boolean isCancelled(TaskEnum taskId) {
        MyApplication app = (MyApplication) getApplication();
        return app.getTaskStatus(taskId) == MyApplication.TaskStatus.VOID;
    }

}
