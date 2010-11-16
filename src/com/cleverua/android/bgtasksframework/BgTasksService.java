package com.cleverua.android.bgtasksframework;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class BgTasksService extends IntentService {

    private static final String TAG = "TasksService";
    
    public static final String TASK_ACTIVITY_CLASS_KEY    = "ACTIVITY_CLASS";
    public static final String TASK_ID_EXTRA_KEY          = "TASK_ID";
    public static final String TASK_STATUS_EXTRA_KEY      = "TASK_STATUS";
    public static final String TASK_RESULT_EXTRA_KEY	  = "TASK_RESULT";
    public static final String TASK_ERROR_CODE_EXTRA_KEY  = "TASK_ERROR_CODE";

    public BgTasksService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: " + intent);
        
        String taskId = intent.getStringExtra(TASK_ID_EXTRA_KEY);
        Class activityClass = (Class) intent.getSerializableExtra(TASK_ACTIVITY_CLASS_KEY);
        
        validateExtras(taskId, activityClass);
        getApp().onTaskStarted(MyApplication.TaskEnum.valueOf(taskId), activityClass);

        SystemClock.sleep(5 * 1000); // this is a JOB ;)

        if (!isCancelled(taskId)) {
            getApp().onTaskCompleted(MyApplication.TaskEnum.valueOf(taskId), "This is result, might be arbitrary object");
            // uncomment to see error response
            //app.onTaskError(MyApplication.TaskEnum.valueOf(taskId), 1);
        } else {
            Log.d(TAG, "Task: " + taskId + " has been cancelled, doing nothing");
        }
    }

    private boolean isCancelled(String taskId) {
        return getApp().getTaskStatus(MyApplication.TaskEnum.valueOf(taskId)) == MyApplication.TaskStatus.VOID;
    }
    
    private void validateExtras(String taskId, Class activityClass) {
        if (taskId == null || taskId.equals("")) {
            throw new RuntimeException("Intent extra " + TASK_ID_EXTRA_KEY + " is required");
        }

        if (null == MyApplication.TaskEnum.valueOf(taskId)) {
            throw new RuntimeException("Know nothing about " + taskId + " task!");
        }

        if (activityClass == null) {
            throw new RuntimeException("Intent extra " + TASK_ACTIVITY_CLASS_KEY + " is required");
        }
    }
    
    private MyApplication getApp() {
    	return (MyApplication) getApplication();
    }

}
