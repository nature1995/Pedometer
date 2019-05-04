package cn.bluemobi.dylan.step.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.List;

import cn.bluemobi.dylan.step.R;
import cn.bluemobi.dylan.step.step.UpdateUiCallBack;
import cn.bluemobi.dylan.step.step.service.StepService;
import cn.bluemobi.dylan.step.step.utils.SharedPreferencesUtils;
import cn.bluemobi.dylan.step.view.StepArcView;

import static java.lang.Thread.sleep;

/**
 * pedometer: main page
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_data;
    private StepArcView cc;
    private TextView tv_set;
    private TextView tv_isSupport;
    private SharedPreferencesUtils sp;
    private Context context;

    private void assignViews() {
        tv_data = (TextView) findViewById(R.id.tv_data);
        cc = (StepArcView) findViewById(R.id.cc);
        tv_set = (TextView) findViewById(R.id.tv_set);
        tv_isSupport = (TextView) findViewById(R.id.tv_isSupport);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        getvibrate();
        getweather();
        aboutus();
        location();
        assignViews();
        initData();
        addListener();

    }


    private void location(){
        Button button=(Button)findViewById(R.id.location);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mythread mythread = new Mythread();
                mythread.start();
            }
        });
    }

    private void getvibrate(){
        Button button=(Button)findViewById(R.id.vibrateBotton);
        final Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(30);//vibrate is reloaded, 30 milliseconds last
            }
        });
    }

    private void getweather(){
        Button button = (Button)findViewById(R.id.weatherBotton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });
    }

    private void aboutus() {
        Button button = (Button)findViewById(R.id.aboutBotton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, aboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private class Mythread extends  Thread{
        private Mythread(){
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            }
            List<Address> location = null;
            while(location == null){
                location = Getposition.getcity(context);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            String displayLocation = "Location: " + location.get(0).getAddressLine(0);

            
            TextView textView =(TextView)findViewById(R.id.show_location);
            textView.setText(displayLocation);
        }
    }

    private void addListener() {
        tv_set.setOnClickListener(this);
        tv_data.setOnClickListener(this);
    }

    private void initData() {
        sp = new SharedPreferencesUtils(this);
        //get the setting steps, default 7000
        String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
        //start at 0
        cc.setCurrentCount(Integer.parseInt(planWalk_QTY), 0);
        tv_isSupport.setText("Calculating the number of steps...");
        setupService();
    }


    private boolean isBind = false;

    /**
     * Initiate pedometer service
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * Interface for checking application service
     *
     * Functions of ServiceConnection are called in main thread
     */
    ServiceConnection conn = new ServiceConnection() {
        /**
         * It is called when connecting with Service，connecting with service through IBind
         * @param name actual component connected with Service
         * @param service IBind for service communication
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService stepService = ((StepService.StepBinder) service).getService();
            //initialize the setting
            String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
            cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepService.getStepCount());

            //listener callback for steps
            stepService.registerCallback(new UpdateUiCallBack() {
                @Override
                public void updateUi(int stepCount) {
                    String planWalk_QTY = (String) sp.getParam("planWalk_QTY", "7000");
                    cc.setCurrentCount(Integer.parseInt(planWalk_QTY), stepCount);
                }
            });
        }

        /**
         * It is called when connection lost
         * It happens when thread is crashed or killed
         * It will not remove the connection, onConnection()will be called when restart the service
         * @param name lost component name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_set:
                startActivity(new Intent(this, SetPlanActivity.class));
                break;
            case R.id.tv_data:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
        }
        Button button=(Button)findViewById(R.id.vibrateBotton);
        final Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(30);//vibrate is reloaded, 30 milliseconds last
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBind) {
            this.unbindService(conn);
        }
    }

}

