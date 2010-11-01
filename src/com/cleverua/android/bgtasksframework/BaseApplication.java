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
    
    public static final String ERROR_CODE_EXTRA_KEY   = "error_code";
    public static final String TASK_OUTCOME_EXTRA_KEY = "outcome";

    public static final String ERROR_OUTCOME     = "error";
    public static final String COMPLETED_OUTCOME = "completed";

    private static Map<TaskEnum, Task> tasks;

    private static final int TASK_NOTIFICATION_ID = 55;

    public enum TaskStatus {
        STARTED, ERROR, COMPLETED, VOID
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
        return UNKNOWN_ERROR /* Some generic error */;
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
    }

    /* ? */void onTaskStarted(TaskEnum taskId, Class activityClass) {
        logInfo("onTaskStarted: " + taskId);
        tasks.put(taskId, new Task(activityClass));
    }

    /* ? */void onTaskError(TaskEnum taskId, int errorCode) {
        log("onTaskError: " + taskId + ": errorCode: " + errorCode);
        Task task = tasks.get(taskId);
        if (task != null) {
            task.status = TaskStatus.ERROR;
            task.errorCode = errorCode;
            postNotification(task);

            Intent intent = new Intent(taskId.name());
            intent.putExtra(TASK_OUTCOME_EXTRA_KEY, ERROR_OUTCOME);
            intent.putExtra(ERROR_CODE_EXTRA_KEY, task.errorCode);
            log("Sending broadcast that task: " + taskId + " had error: " + task.errorCode);
            sendBroadcast(intent);
        }
    }

    /* ? */void onTaskCompleted(TaskEnum taskId, Object result) {
        logInfo("onTaskCompleted: " + taskId);
        Task task = tasks.get(taskId);
        if (task != null) {
            task.status = TaskStatus.COMPLETED;
            task.result = result;
            postNotification(task);

            Intent intent = new Intent(taskId.name());
            intent.putExtra(TASK_OUTCOME_EXTRA_KEY, COMPLETED_OUTCOME);
            log("Sending broadcast that task: " + taskId + " has completed");
            sendBroadcast(intent);
        }
    }

    public final void cancelBgTaskNotification() {
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
