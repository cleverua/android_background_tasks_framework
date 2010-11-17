package com.cleverua.android.bgtasksframework;

import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

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
	public static final String PROGRESS_MESSAGE_EXTRA_KEY = "PROGRESS_MESSAGE_KEY";

    public BgTasksService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: " + intent);
        
        TaskEnum taskId = (TaskEnum)intent.getSerializableExtra(TASK_ID_EXTRA_KEY);
        Class activityClass = (Class) intent.getSerializableExtra(TASK_ACTIVITY_CLASS_KEY);
        
        validateExtras(taskId, activityClass);
        getApp().onTaskStarted(taskId, activityClass);

        // SAMPLE TASK
        if (taskId.equals(MyApplication.TaskEnum.SAMPLE_TASK)) {
        	boolean isCancelled = false;
        	int step = 0;
        	for (int i = 0; i<20; i++) {
                if (!isCancelled(taskId)) {
                	SystemClock.sleep(500);
                	step = i;
                } else {
                    Log.d(TAG, "Task: " + taskId + " has been cancelled, doing nothing");
                	isCancelled = true;
                	break;
                }
        	}

        	if (isCancelled) {
        		getApp().onTaskCancelled(taskId, step);
        	} else {
        		getApp().onTaskCompleted(taskId, step);
        	}
        }
        
        
        
        // YET ANOTHER TASK
        if (taskId.equals(MyApplication.TaskEnum.YET_ANOTHER_TASK)) {
        	boolean isCancelled = false;
        	
        	for (int i = 0; i<10; i++) {
                if (!isCancelled(taskId)) {
                	SystemClock.sleep(100);
                } else {
                    Log.d(TAG, "Task: " + taskId + " has been cancelled, doing nothing");
                	isCancelled = true;
                	break;
                }
        	}

        	if (isCancelled) {
        		getApp().onTaskCancelled(taskId, null);
        	} else {
        		getApp().onTaskCompleted(taskId, null);
        	}
        }
    }

    private boolean isCancelled(TaskEnum taskId) {
        return getApp().getTaskStatus(taskId) == MyApplication.TaskStatus.CANCELING;
    }
    
    private void validateExtras(TaskEnum taskId, Class activityClass) {
        if (taskId == null) {
            throw new RuntimeException("Valid Intent extra " + TASK_ID_EXTRA_KEY + " is required");
        }

        if (activityClass == null) {
            throw new RuntimeException("Intent extra " + TASK_ACTIVITY_CLASS_KEY + " is required");
        }
    }
    
    private MyApplication getApp() {
    	return (MyApplication) getApplication();
    }

}
