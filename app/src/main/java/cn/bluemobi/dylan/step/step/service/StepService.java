package cn.bluemobi.dylan.step.step.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bluemobi.dylan.step.R;
import cn.bluemobi.dylan.step.activity.MainActivity;
import cn.bluemobi.dylan.step.step.UpdateUiCallBack;
import cn.bluemobi.dylan.step.step.accelerometer.StepCount;
import cn.bluemobi.dylan.step.step.accelerometer.StepValuePassListener;
import cn.bluemobi.dylan.step.step.bean.StepData;
import cn.bluemobi.dylan.step.step.utils.DbUtils;

public class StepService extends Service implements SensorEventListener {
    private String TAG = "StepService";
    /**
     * storage every 30 seconds
     */
    private static int duration = 30 * 1000;
    /**
     * Current date
     */
    private static String CURRENT_DATE = "";
    private SensorManager sensorManager;
    private BroadcastReceiver mBatInfoReceiver;
    private TimeCount time;
    /**
     * Current steps
     */
    private int CURRENT_STEP;
    private static int stepSensorType = -1;
    /**
     * Detect if have history record
     */
    private boolean hasRecord = false;
    private int hasStepCount = 0;
    private int previousStepCount = 0;
    private NotificationManager mNotificationManager;
    /**
     * Accelerator sensor steps
     */
    private StepCount mStepCount;
    private StepBinder stepBinder = new StepBinder();
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        initNotification();
        initTodayData();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();
        startTimeCount();

    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Initial Notification bar
     */
    private void initNotification() {
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Today's steps" + CURRENT_STEP + " steps")
                .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.logo);
        Notification notification = mBuilder.build();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(notifyId_Step, notification);
        Log.d(TAG, "initNotification()");
    }

    /**
     * Initial today's steps
     */
    private void initTodayData() {
        CURRENT_DATE = getTodayDate();
        DbUtils.createDb(this, "DylanStepCount");
        DbUtils.getLiteOrm().setDebugged(false);
        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENT_DATE});
        if (list.size() == 0 || list.isEmpty()) {
            CURRENT_STEP = 0;
        } else if (list.size() == 1) {
            Log.v(TAG, "StepData=" + list.get(0).toString());
            CURRENT_STEP = Integer.parseInt(list.get(0).getStep());
        } else {
            Log.v(TAG, "Some Wrong！");
        }
        if (mStepCount != null) {
            mStepCount.setSteps(CURRENT_STEP);
        }
        updateNotification();
    }

    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.d(TAG, "screen off");
                    duration = 60000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.d(TAG, "screen unlock");
//                    save();
                    duration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                    save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.i(TAG, " receive ACTION_SHUTDOWN");
                    save();
                } else if (Intent.ACTION_DATE_CHANGED.equals(action)) {
                    save();
                    isNewDay();
                } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                    isCall();
                    save();
                    isNewDay();
                } else if (Intent.ACTION_TIME_TICK.equals(action)) {
                    isCall();
                    save();
                    isNewDay();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }

    private void isNewDay() {
        String time = "00:00";
        if (time.equals(new SimpleDateFormat("HH:mm").format(new Date())) || !CURRENT_DATE.equals(getTodayDate())) {
            initTodayData();
        }
    }


    private void isCall() {
        String time = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("achieveTime", "21:00");
        String plan = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("planWalk_QTY", "7000");
        String remind = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("remind", "1");
        Logger.d("time=" + time + "\n" +
                "new SimpleDateFormat(\"HH: mm\").format(new Date()))=" + new SimpleDateFormat("HH:mm").format(new Date()));
        if (("1".equals(remind)) &&
                (CURRENT_STEP < Integer.parseInt(plan)) &&
                (time.equals(new SimpleDateFormat("HH:mm").format(new Date())))
                ) {
            remindNotify();
        }

    }

    private void startTimeCount() {
        if (time == null) {
            time = new TimeCount(duration, 1000);
        }
        time.start();
    }

    private void updateNotification() {
        Intent hangIntent = new Intent(this, MainActivity.class);
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = mBuilder.setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Today's steps" + CURRENT_STEP + " steps")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(hangPendingIntent)
                .build();
        mNotificationManager.notify(notifyId_Step, notification);
        if (mCallback != null) {
            mCallback.updateUi(CURRENT_STEP);
        }
        Log.d(TAG, "updateNotification()");
    }

    private UpdateUiCallBack mCallback;

    public void registerCallback(UpdateUiCallBack paramICallback) {
        this.mCallback = paramICallback;
    }

    int notifyId_Step = 100;
    int notify_remind_id = 200;
    private void getvibrate(){

        final Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(30);

    }

    private void remindNotify() {

        Intent hangIntent = new Intent(this, MainActivity.class);
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String plan = this.getSharedPreferences("share_date", Context.MODE_MULTI_PROCESS).getString("planWalk_QTY", "7000");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("Today's steps" + CURRENT_STEP + " steps")
                .setContentText((Integer.valueOf(plan) - CURRENT_STEP) + " steps from the target，work hard！")
                .setContentIntent(hangPendingIntent)
                .setTicker(getResources().getString(R.string.app_name) + "Remind you to start exercising")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setSmallIcon(R.mipmap.logo);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(notify_remind_id, mBuilder.build());
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stepBinder;
    }

    public class StepBinder extends Binder {

        public StepService getService() {
            return StepService.this;
        }
    }

    public int getStepCount() {
        return CURRENT_STEP;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        } else {
            addBasePedometerListener();
        }
    }

    private void addCountStepListener() {
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_COUNTER;
            Log.v(TAG, "Sensor.TYPE_STEP_COUNTER");
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_DETECTOR;
            Log.v(TAG, "Sensor.TYPE_STEP_DETECTOR");
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.v(TAG, "Count sensor not available!");
            addBasePedometerListener();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            int tempStep = (int) event.values[0];
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                int thisStepCount = tempStep - hasStepCount;
                int thisStep = thisStepCount - previousStepCount;
                CURRENT_STEP += (thisStep);
                previousStepCount = thisStepCount;
            }
            Logger.d("tempStep" + tempStep);
        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                CURRENT_STEP++;
            }
        }
        updateNotification();
    }

    /**
     * Step by accelerometer
     */
    private void addBasePedometerListener() {
        mStepCount = new StepCount();
        mStepCount.setSteps(CURRENT_STEP);
        Sensor sensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean isAvailable = sensorManager.registerListener(mStepCount.getStepDetector(), sensor,
                SensorManager.SENSOR_DELAY_UI);
        mStepCount.initListener(new StepValuePassListener() {
            @Override
            public void stepChanged(int steps) {
                CURRENT_STEP = steps;
                updateNotification();
            }
        });
        if (isAvailable) {
            Log.v(TAG, "Acceleration sensor can be used");
        } else {
            Log.v(TAG, "Acceleration sensor is not available");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            time.cancel();
            save();
            startTimeCount();
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

    }

    private void save() {
        int tempStep = CURRENT_STEP;

        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENT_DATE});
        if (list.size() == 0 || list.isEmpty()) {
            StepData data = new StepData();
            data.setToday(CURRENT_DATE);
            data.setStep(tempStep + "");
            DbUtils.insert(data);
        } else if (list.size() == 1) {
            StepData data = list.get(0);
            data.setStep(tempStep + "");
            DbUtils.update(data);
        } else {
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        DbUtils.closeDb();
        unregisterReceiver(mBatInfoReceiver);
        Logger.d("stepService close");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
