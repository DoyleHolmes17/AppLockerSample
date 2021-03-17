package com.farid.applockersample;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppLockService extends android.app.Service {

    //true when a new app has been checked/unchecked in the app lock activity
    protected static boolean triggerLockedAppUpdate = false;
    protected static boolean serviceRunning = false;
    private static final String TAG = "AppLockService";
    private static final String NONE_PKG = "NONE_PKG";
    private static AppLockService instance;
    private Map<String, Boolean> mLockedPackages;
    private String mLastPackageName = "";
    SharedPreferences sharedPreferences;
    private PatternOverlayView overlayView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayParams;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayParams = getLayoutParam();
        overlayView = new PatternOverlayView(getApplicationContext());

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "far_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Farid Notif",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Farid Locker")
                    .setContentText("Application Locker running").build();

            startForeground(1, notification);
        }
    }

    private WindowManager.LayoutParams getLayoutParam() {
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public AppLockService getInstance() {
            return AppLockService.this;
        }
    }

    private boolean init() {
        mLockedPackages = new HashMap<>();
        updateLockedAppStates();
        return true;
    }

    //updates the list of locked apps, and resets all apps to lock state
    private void updateLockedAppStates() {
//        final Set<String> apps = getLockedApps();
        mLockedPackages.clear();
//        for (String s : apps) {
//            Log.i("updating locked apps", s);
        mLockedPackages.put("com.instagram.android", true);
        mLockedPackages.put("com.whatsapp", true);
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand: starting service...");
        sharedPreferences = getApplicationContext().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);

        if (!init()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        //check running apps
        checkTimer();
        instance = this;
        serviceRunning = true;

        return START_STICKY;
    }

    private void checkTimer() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (serviceRunning) {
                //check which version of Android is running, and call appropriate methods
                int currentApiVersion = Build.VERSION.SDK_INT;
                if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP) {
                    checkRunningApps();
                } else {
                    checkRunningAppsLegacy();
                }
            }

        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        Log.i("onTaskRemoved:", "restart service after getting killed");

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @SuppressWarnings("deprecation")
    //This method is only called for API versions before LOLLIPOP (21)
    private String checkRunningAppsLegacy() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo activityManagerInfo = runningTask.get(0);
        String currentPackageName = activityManagerInfo.topActivity.getPackageName();
        Log.i("activity on top:", "" + currentPackageName);

        //if apps have been updated, update mLockedPackages
        if (triggerLockedAppUpdate) {
            updateLockedAppStates();
            triggerLockedAppUpdate = false;
        }

        //if the last package is not the same as current package, re-lock the last package if it is a locked app
        if (!mLastPackageName.equals(currentPackageName)) {
            Log.i(TAG, "appchanged " + " (" + mLastPackageName + ">" + currentPackageName + ")");
            onAppClose(mLastPackageName);
            mLastPackageName = currentPackageName;
        }

        onAppOpen(mLastPackageName);

        return mLastPackageName;

    }

    @TargetApi(21)
    //for API level 21 and up
    private String checkRunningApps() {
//        Log.e("MASUKKKK RUNNING", "OKOKO");
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000, ts);
        if (usageStats == null || usageStats.size() == 0) {
//            Log.e("MASUKKKK RING", "00000");
            return NONE_PKG;
        }
        RecentUseComparator mRecentComp = new RecentUseComparator();
        Collections.sort(usageStats, mRecentComp);

        String currentPackageName = usageStats.get(0).getPackageName();
//        Log.e("MASUKKKK RUNNING ON TOP", "" + currentPackageName);

        //if apps have been updated, update mLockedPackages
        if (triggerLockedAppUpdate) {
            updateLockedAppStates();
            triggerLockedAppUpdate = false;
        }

        //if the last package is not the same as current package, re-lock the last package if it is a locked app
        if (!mLastPackageName.equals(currentPackageName)) {
            Log.e(TAG, "appchanged " + " (" + mLastPackageName + ">" + currentPackageName + ")");
            onAppClose(mLastPackageName);
            mLastPackageName = currentPackageName;
        }

        onAppOpen(mLastPackageName);

        return mLastPackageName;
    }

    @TargetApi(21)
    private static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    //NOTE:
    //check how to make this work with API 18
    @TargetApi(19)
    public static boolean requirePermissions(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode != AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    public static void stop() {
        if (instance != null) {
            Log.i("stopService:", "stopping service");
            serviceRunning = false;
            instance.stopSelf();
        }
    }

    //gets a list of applications that are currently enabled for locking on the App Lock activity screen
    private Set<String> getLockedApps() {
        Set<String> lockedApps = new HashSet<>();
        Map<String, ?> appKeyValues = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : appKeyValues.entrySet()) {
            if (entry.getValue().toString().equals("true")) {
                lockedApps.add(entry.getKey());
            }
        }
        return lockedApps;
    }

    //check if the app being opened is in the list of locked applications
    private void onAppOpen(final String open) {
        Date currentTime = Calendar.getInstance().getTime();
//        Log.e("CURRTIME", String.valueOf(currentTime.getHours()));

        if (mLockedPackages.containsKey(open) && 8 <= currentTime.getHours() && currentTime.getHours() <= 16) {
            Log.e("onAppOpen:", "it is a locked app");
            onLockedAppOpen(open);
        }
    }

    //if the app is in the lock state, show the lockscreen
    private void onLockedAppOpen(final String open) {
        final boolean locked = mLockedPackages.get(open);
        if (locked) {
            Log.e("onLockedAppOpen:", "the app is locked, showLocker()");
            showLocker(open);
//            showOverlay();
        }
    }

    //when the app loses focus, set the app to the lock state again
    private void onAppClose(String close) {
        if (mLockedPackages.containsKey(close)) {
            Log.e("onAppClose", "locking the app again");
            mLockedPackages.put(close, true);
        }
    }

    private void showLocker(String packageName) {
        Intent intent = new Intent(getApplicationContext(), MainOverlayActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.putExtra("PACKAGE_NAME", packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void showOverlay() {
        windowManager.addView(overlayView, overlayParams);
    }

    //sets the app state to unlocked
    public void unlockApp(String packageName) {
        Log.e(TAG, "unlocking app (packageName=" + packageName + ")");
        if (mLockedPackages.containsKey(packageName)) {
            mLockedPackages.put(packageName, false);
        }
    }

    private boolean checkPermission() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = null;
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            } else {
                return true;
            }
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}