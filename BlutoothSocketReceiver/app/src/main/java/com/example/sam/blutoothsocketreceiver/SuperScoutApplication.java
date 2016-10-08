package com.example.sam.blutoothsocketreceiver;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.sam.blutoothsocketreceiver.firebase_classes.Match;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 1/31/16.
 */

public class SuperScoutApplication extends Application implements Application.ActivityLifecycleCallbacks {
    String url = Constants.dataBaseUrl;
    Map<DatabaseReference, String> dataBaseList;
    public Activity currentActivity = null;
    final Thread.UncaughtExceptionHandler originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    public static boolean isRed = false;


    @Override
    public void onCreate() {
        registerActivityLifecycleCallbacks(this);
        super.onCreate();
            FirebaseLists.matchesList = new FirebaseList<>(url + "Matches/", new FirebaseList.FirebaseUpdatedCallback() {
                @Override
                public void execute() {
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("matches_updated"));
                }
            }, Match.class);
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    handleUncaughtException(thread, e);
                }
            });
        new Instabug.Builder(this, "8eab8e2db114c14290df21e21527f1a1")
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .build();
        dataBaseList = Constants.dataBases;
        }
    public void onActivityCreated(Activity activity, Bundle savedInstanceState){currentActivity = activity;}

    public void onActivityDestroyed(Activity activity){currentActivity = null;}

    public void onActivityPaused(Activity activity){}

    public void onActivityResumed(Activity activity){}

    public void onActivitySaveInstanceState(Activity activity, Bundle outState){}

    public void onActivityStarted(Activity activity){}

    public void onActivityStopped(Activity activity){}

    private void handleUncaughtException (Thread thread, Throwable e)
    {
        // The following shows what I'd like, though it won't work like this.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String stacktrace = result.toString();
            saveErrorLog("/sdcard/Super_UIError_log", stacktrace);
            printWriter.close();
            Log.e("UI thread", "CRASHED!");
            takeScreenshot();
            originalUncaughtExceptionHandler.uncaughtException(thread, e);
        }else{
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String stacktrace = result.toString();
            saveErrorLog("/sdcard/Super_threadError_log", stacktrace);
            Log.e("Background thread", "CRASHED");
            Toast.makeText(this, "Background Thread ERROR", Toast.LENGTH_LONG).show();
        }

    }
    private void takeScreenshot() {

        File dir = new File("/sdcard/Super_scout_backup");
        Log.e("dir", dir.getAbsolutePath());
        dir.mkdir();
        File photoFile = new File(dir, (new SimpleDateFormat("MM-dd-yyyy-H:mm:ss").format(new Date())) + ".jpg");
        try {
            // image naming and path  to include sd card  appending name you choose for file
            FileOutputStream file = new FileOutputStream(photoFile);
            View v1 = currentActivity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, file);
            file.flush();
            file.close();

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
            Log.e("photoSave", "CRASHED");
        }

    }
    public void saveErrorLog(String path, String errorMessage){
        try{
        PrintWriter file;
        File dir = new File(path);
        dir.mkdir();
        file = new PrintWriter(new FileOutputStream(new File(dir, (new SimpleDateFormat("MM-dd-yyyy-H:mm:ss").format(new Date())))));
        file.println(errorMessage);
        file.close();
        }catch (IOException IOE){
            Log.e("error log save", "failed");
        }

    }

}


