package com.cleverua.android.bgtasksframework;

import com.cleverua.android.bgtasksframework.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends Activity {

    protected static final int DIALOG_INFORM_ID = -1000000;
    protected static final int DIALOG_ALERT_ID  = -1000001;
    
    protected static final String STATE_DIALOG_INFORM_MSG = "BgTaskActivity.dialogInformMsg";
    protected static final String STATE_DIALOG_ALERT_MSG  = "BgTaskActivity.dialogAlertMsg";
    
    private String dialogInformMsg;
    private String dialogAlertMsg;
    
    private String tag;
    protected boolean isCleanStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCleanStart = (savedInstanceState == null);
        logInfo("onCreate: isCleanStart = " + isCleanStart);
    }

    @Override
    protected void onStart() {
        super.onStart();
        logInfo("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logInfo("onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        logInfo("onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        logInfo("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        logInfo("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logInfo("onDestroy");
    }

    @Override
    public void onBackPressed() {
        logInfo("onBackPressed");
        super.onBackPressed();
    }
    
    @Override
    public void finish() {
        logInfo("finish");
        super.finish();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dialogInformMsg = savedInstanceState.getString(STATE_DIALOG_INFORM_MSG);
        dialogAlertMsg  = savedInstanceState.getString(STATE_DIALOG_ALERT_MSG);
        logInfo("onRestoreInstanceState");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (dialogInformMsg != null) {
            outState.putString(STATE_DIALOG_INFORM_MSG, dialogInformMsg);
        }
        if (dialogAlertMsg != null) {
            outState.putString(STATE_DIALOG_ALERT_MSG, dialogAlertMsg);
        }
        logInfo("onSaveInstanceState");
    }

    protected final void inform(String message) {
        dialogInformMsg = message;
        showDialog(DIALOG_INFORM_ID);
    }
    
    protected final void alert(String message) {
        dialogAlertMsg = message;
        showDialog(DIALOG_ALERT_ID);
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
        
        if (id == DIALOG_INFORM_ID) {
            return new AlertDialog.Builder(this)
                .setMessage(dialogInformMsg == null ? "" : dialogInformMsg)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.app_name)
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dismissDialog(id);
                        dialogInformMsg = null;
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialogInformMsg = null;
                    }
                })
                .create();
            
        } else if (id == DIALOG_ALERT_ID) {
            return new AlertDialog.Builder(this)
                .setMessage(dialogAlertMsg == null ? "" : dialogAlertMsg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.app_name)
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dismissDialog(id);
                        dialogAlertMsg = null;
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialogAlertMsg = null;
                    }
                })
                .create();
        }
        
        return super.onCreateDialog(id);
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        
        if (id == DIALOG_INFORM_ID) {
            ((AlertDialog) dialog).setMessage(dialogInformMsg);
        } else if (id == DIALOG_ALERT_ID) {
            ((AlertDialog) dialog).setMessage(dialogAlertMsg);
        }
        
        super.onPrepareDialog(id, dialog);
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

    protected MyApplication getApp() {
        MyApplication app = (MyApplication) getApplication();
        return app;
    }
}
