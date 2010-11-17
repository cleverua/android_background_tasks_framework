package com.cleverua.android.bgtasksframework;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.cleverua.android.bgtasksframework.MyApplication.TaskEnum;

public class BaseApplication extends Application {

    public static final int UNKNOWN_ERROR = -1;
    private static final int TASK_NOTIFICATION_ID = -1000000;
    
    private static Map<TaskEnum, Task> tasks;

    public enum TaskStatus {
        STARTED, ERROR, COMPLETED, CANCELING, VOID
    }

    private String tag;
    
    @Override
    public void onCreate() {
        super.onCreate();
        logInfo("onCreate");
        tasks = new HashMap<TaskEnum, Task>();
    }

    public TaskStatus getTaskStatus(TaskEnum taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            return task.status;
        }
        return TaskStatus.VOID;
    }
    
    public void cancelTask(TaskEnum taskId) {
    	Task task = tasks.get(taskId);
    	task.status = TaskStatus.CANCELING;
    }
    
    public void invalidateTask(TaskEnum taskId) {
        tasks.remove(taskId);
        cancelNotification();
    }

    public void startBackgroundTask(Intent serviceIntent, String message) {
        Intent intent = new Intent(this, ProgressTaskActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BgTasksService.TASK_ID_EXTRA_KEY,
				serviceIntent.getSerializableExtra(BgTasksService.TASK_ID_EXTRA_KEY));
		intent.putExtra(BgTasksService.PROGRESS_MESSAGE_EXTRA_KEY, message);
		
		startActivity(intent);
		startService(serviceIntent);
    }

    protected void onTaskStarted(TaskEnum taskId, Class activityClass) {
        logInfo("onTaskStarted: " + taskId);
        tasks.put(taskId, new Task(activityClass));
    }

    protected void onTaskError(TaskEnum taskId, int errorCode) {
        log("onTaskError: " + taskId + ": errorCode: " + errorCode);
        Task task = tasks.get(taskId);
        if (task != null) {
            task.status = TaskStatus.ERROR;
            task.errorCode = errorCode;
            postNotification(task, taskId);
            postBroadcast(task, taskId);
        }
    }

    protected void onTaskCompleted(TaskEnum taskId, Serializable result) {
        logInfo("onTaskCompleted: " + taskId);
        Task task = tasks.get(taskId);
        if (task != null) {
            task.status = TaskStatus.COMPLETED;
            task.result = result;
            postNotification(task, taskId);
            postBroadcast(task, taskId);
        }
    }

    protected void onTaskCancelled(TaskEnum taskId, Serializable result) {
        logInfo("onTaskCancelled: " + taskId);
        Task task = tasks.get(taskId);
        if (task != null) {
            task.status = TaskStatus.VOID;
            task.result = result;
            postNotification(task, taskId);
            postBroadcast(task, taskId);
        }
    }

    private void cancelNotification() {
        getNotificationManager().cancel(TASK_NOTIFICATION_ID);
    }

    private void postNotification(Task task, TaskEnum taskId) {
        Notification notification = new Notification(R.drawable.icon,
                getString(R.string.app_name), System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        Intent i = new Intent(this, task.activityClass);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, taskId);
        i.putExtra(BgTasksService.TASK_STATUS_EXTRA_KEY, task.status);
        i.putExtra(BgTasksService.TASK_RESULT_EXTRA_KEY, task.result);
        i.putExtra(BgTasksService.TASK_ERROR_CODE_EXTRA_KEY, task.errorCode);
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(this, "Task completed", "Touch here to open the activity", contentIntent);
        getNotificationManager().notify(TASK_NOTIFICATION_ID, notification);
    }
    
    private void postBroadcast(Task task, TaskEnum taskId) {
    	Intent i = new Intent(taskId.name());
    	
    	i.putExtra(BgTasksService.TASK_ACTIVITY_CLASS_KEY, task.activityClass);
        i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, taskId);
        i.putExtra(BgTasksService.TASK_STATUS_EXTRA_KEY, task.status);
        i.putExtra(BgTasksService.TASK_RESULT_EXTRA_KEY, task.result);
        i.putExtra(BgTasksService.TASK_ERROR_CODE_EXTRA_KEY, task.errorCode);
    	
        sendBroadcast(i);
    }
    
    public void startTargetActivity(Activity context, TaskEnum taskId) {
        Task task = tasks.get(taskId);
        if (task != null && task.status != TaskStatus.STARTED && task.status != TaskStatus.CANCELING) {
	    	Intent i = new Intent(this, task.activityClass);
	        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        i.putExtra(BgTasksService.TASK_ID_EXTRA_KEY, taskId);
	        i.putExtra(BgTasksService.TASK_STATUS_EXTRA_KEY, task.status);
	        i.putExtra(BgTasksService.TASK_RESULT_EXTRA_KEY, task.result);
	        i.putExtra(BgTasksService.TASK_ERROR_CODE_EXTRA_KEY, task.errorCode);
	        
	        context.startActivity(i);
	     }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    protected String getTag() {
        if (tag == null) {
            tag = this.getString(R.string.app_name) + ' ' + this.getClass().getSimpleName();
        }
        return tag;
    }

    protected void log(String msg) {
        Log.d(getTag(), msg);
    }

    protected void logInfo(String msg) {
        Log.i(getTag(), msg);
    }

    protected void log(String msg, Throwable tr) {
        Log.e(getTag(), msg, tr);
    }
    
    

    private class Task {
        
		private TaskStatus status;
        private int errorCode;
        private Class activityClass;
        private Serializable result;

        private Task(Class activityClass) {
            this.status = TaskStatus.STARTED;
            this.errorCode = UNKNOWN_ERROR;
            this.activityClass = activityClass;
            this.result = null;
        }
    }
}
