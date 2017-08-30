package org.iee.slidemenutest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.iee.slidemenutest.datatype.AppConfigure;
import org.iee.slidemenutest.datatype.ChargeSysInfo;
import org.iee.slidemenutest.datatype.PortInfo;
import org.iee.slidemenutest.service.BluetoothService;
import org.iee.slidemenutest.service.CarCommSocket;

import java.text.DecimalFormat;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int[] CAR_BT_CODE = {4,2,8,7};
    private static final String[] PORT_BT_CODE = {"98:D3:31:90:0E:F5","98:D3:31:80:14:76"};
    //region ----------------View Components ------------
    private Button btn_start;
    private Button btn_stop;

    private TextView tv_port;
    private TextView tv_car;

    private TextView tv_info_A;
    private TextView tv_info_V;
    private TextView tv_info_T;

    private TextView tv_progress;
    private ProgressBar pb_progress;
    //endregion


    // Private
    private wirelessCharge mApplication;
    private BTRECReceiver btReceiver;

    static private AppConfigure defaultConf = new AppConfigure();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    // init App
        mApplication = wirelessCharge.getInstance();
        mApplication.setmAppConfigure(defaultConf);


        mState = new AppState();
        initView();

        UpdateState();


        //动态注册蓝牙接收广播接收器
        btReceiver = new BTRECReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CarCommSocket.BTREC_BROADCAST);
        this.registerReceiver(btReceiver, intentFilter);


        // connect default

        PortInfo conf = mApplication.getmPortInfo();
        conf.BTMAC = PORT_BT_CODE[mState.port_index].trim();
        mApplication.getmDataBaseService().savePortInfo(conf);
        mApplication.startBluetoothConnect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    protected void onDestroy() {
        this.unregisterReceiver(btReceiver);
        super.onDestroy();
    }

    private void initView(){

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCharge();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCharge();
            }
        });


        tv_port = (TextView)findViewById(R.id.tx_portNO);
        tv_car = (TextView)findViewById(R.id.tx_carNO);

        tv_info_T = (TextView)findViewById(R.id.tv_info_T);
        tv_info_A = (TextView)findViewById(R.id.tv_info_A);
        tv_info_V = (TextView)findViewById(R.id.tv_info_V);

        pb_progress = (ProgressBar)findViewById(R.id.progressBar);
        tv_progress = (TextView)findViewById(R.id.tv_progress);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_car1) {
            changeCarIndex(0);
        } else if (id == R.id.menu_car2) {
            changeCarIndex(1);
        } else if (id == R.id.menu_car3) {
            changeCarIndex(2);
        } else if (id == R.id.menu_car4) {
            changeCarIndex(3);
        } else if (id == R.id.menu_port1) {
            changePortIndex(0);
            tv_port.setText(getResources().getString(R.string.portNO1));
        } else if (id == R.id.menu_port2) {
            changePortIndex(1);
            tv_port.setText(getResources().getString(R.string.portNO2));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class BTRECReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            BluetoothService.BLUETOOTH_SERVICE_STATE bluetoothState =
                    (BluetoothService.BLUETOOTH_SERVICE_STATE)intent.getSerializableExtra("state");
            //region ---- BT state----------
            if (bluetoothState != null){
                switch ( bluetoothState ) {
                    case CANCELLING:
                        break;
                    case CONNECTED:
                        changeSystemState(0);
                        break;
                    case CONNECTING:
                        changeSystemState(-1);
                        break;
                    case IDLE:
                        changeSystemState(-1);
                        break;
                }
            }
            //endregion

            ChargeSysInfo chargeSysInfo = (ChargeSysInfo) intent.getSerializableExtra("data");
            //region ----- data display -----
            if (chargeSysInfo != null) {
                DecimalFormat fnum = new DecimalFormat("##0.0");

                if (chargeSysInfo.system_state > 0)
                    changeSystemState(chargeSysInfo.system_state);

                tv_info_V.setText(String.valueOf(chargeSysInfo.receiver_Uout) + " V");
                tv_info_A.setText(fnum.format(chargeSysInfo.receiver_Iout) + " A");
                tv_info_T.setText(
                        String.valueOf(chargeSysInfo.trade_charge_time_hour)
                                + ":" +
                                String.valueOf(chargeSysInfo.trade_charge_time_min)
                                + ":" +
                                String.valueOf(chargeSysInfo.trade_charge_time_sec));
            }
            //endregion

        }
    }


    private class AppState{
        int car_index = 0;
        int port_index = 0;
        int system_state = -1; //  -1 : unconnected
    }
    private AppState mState;


    private void changeCarIndex(int index){
        if (index == mState.car_index) return;  // no change
        if (mState.system_state>0) return;   // can't change when charging

        mState.car_index = index;
        UpdateState();
    }

    private synchronized void changePortIndex(int index){
        if (index == mState.port_index) return;  // no change

        if (mState.system_state != -1) mApplication.stopBluetoothConnect();

        try{
            Thread.sleep(200);
        }catch (Exception e){
            ;
        }

        PortInfo conf = mApplication.getmPortInfo();
        conf.BTMAC = PORT_BT_CODE[index].trim();
        mApplication.getmDataBaseService().savePortInfo(conf);
        mApplication.startBluetoothConnect();
        mState.port_index = index;
        mState.system_state = -1;

        UpdateState();
    }


    private void changeSystemState(int state){
        if (state == mState.system_state) return;
        mState.system_state = state;

        UpdateState();
    }

    private void startCharge(){
        mApplication.getmBTCommSocket().sendCommand(
                CarCommSocket.CarCommand.SELECT_CHANNEL,
                CAR_BT_CODE[mState.car_index]);
    }
    private void stopCharge(){
        mApplication.getmBTCommSocket().sendCommand(
                CarCommSocket.CarCommand.STOP_CHARGE);
    }

    private void UpdateState(){
        switch(mState.system_state){
            case -1:
                tv_progress.setVisibility(View.VISIBLE);
                pb_progress.setVisibility(View.VISIBLE);

                tv_info_V.setText("--V");
                tv_info_A.setText("--A");
                tv_info_T.setText("--:--:--");

                btn_start.setEnabled(false);
                btn_stop.setVisibility(View.INVISIBLE);
                break;
            case 0:
                tv_progress.setVisibility(View.INVISIBLE);
                pb_progress.setVisibility(View.INVISIBLE);

                btn_start.setEnabled(true);
                btn_stop.setVisibility(View.INVISIBLE);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                tv_progress.setVisibility(View.INVISIBLE);
                pb_progress.setVisibility(View.INVISIBLE);

                btn_start.setEnabled(false);
                btn_stop.setVisibility(View.VISIBLE);
                break;
            case 7:
                tv_progress.setVisibility(View.INVISIBLE);
                pb_progress.setVisibility(View.INVISIBLE);

                btn_start.setEnabled(false);
                btn_stop.setVisibility(View.INVISIBLE);
                break;

        }
        switch(mState.car_index){
            case 0:
                tv_car.setText(getResources().getString(R.string.carNO1));
                break;
            case 1:
                tv_car.setText(getResources().getString(R.string.carNO2));
                break;
            case 2:
                tv_car.setText(getResources().getString(R.string.carNO3));
                break;
            case 3:
                tv_car.setText(getResources().getString(R.string.carNO4));
                break;
        }

        switch(mState.port_index){
            case 0:
                tv_port.setText(getResources().getString(R.string.portNO1));
                break;
            case 1:
                tv_port.setText(getResources().getString(R.string.portNO2));
                break;
        }
    }
}
