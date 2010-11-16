package com.cleverua.android.bgtasksframework;

import java.util.HashMap;
import java.util.Map;

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

    public int getTaskErrorCode(TaskEnum taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            return task.errorCode;
        }
        return UNKNOWN_ERROR;
    }
    
	public Object getTaskResult(TaskEnum taskId) {
		Task task = tasks.get(taskId);
        if (task != null) {
            return task.result;
        }
        return null;
	}

    public void invalidateTask(TaskEnum taskId) {
        tasks.remove(taskId);
        cancelNotification();
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
            postNotification(task);
            postBroadcast(taskId);
        }
    }

    protected void onTaskCompleted(TaskEnum taskId, Object result) {
        logInfo("onTaskCompleted: " + taskId);
        Task task = tasks.get(taskId);
        if (task != null) {
            task.status = TaskStatus.COMPLETED;
            task.result = result;
            postNotification(task);
            postBroadcast(taskId);
        }
    }

    private void cancelNotification() {
        getNotificationManager().cancel(TASK_NOTIFICATION_ID);
    }

    private void postNotification(Task task) {
        Notification notification = new Notification(R.drawable.icon,
                getString(R.string.app_name), System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        Intent i = new Intent(this, task.activityClass);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(this, "Task completed", "Touch here to open the activity", contentIntent);
        getNotificationManager().notify(TASK_NOTIFICATION_ID, notification);
    }
    
    private void postBroadcast(TaskEnum taskId) {
        sendBroadcast(new Intent(taskId.name()));
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
        private Object result;

        private Task(Class activityClass) {
            this.status = TaskStatus.STARTED;
            this.errorCode = UNKNOWN_ERROR;
            this.activityClass = activityClass;
            this.result = null;
        }
    }
}
