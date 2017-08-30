package org.iee.slidemenutest;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.iee.slidemenutest.service.CarCommSocket;
import org.iee.slidemenutest.service.ClsUtils;
import org.iee.slidemenutest.service.DataBaseService;
import org.iee.slidemenutest.datatype.AppConfigure;
import org.iee.slidemenutest.datatype.CarInfo;
import org.iee.slidemenutest.datatype.ChargeSysInfo;
import org.iee.slidemenutest.datatype.PortInfo;

/**
 * Created by lich on 2016/12/6.
 */
public class wirelessCharge extends Application {

    //region =================singleton=============
    static private wirelessCharge instance;

    static public wirelessCharge getInstance() {
        return instance;
    }
    //endregion

    BTRECReceiver btReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


        // start service
        mBTCommSocket = new CarCommSocket(getApplicationContext());
        mDataBaseService = new DataBaseService(getApplicationContext());
        // load settings
        setmAppConfigure(mDataBaseService.loadAppConfigure());

        //动态注册蓝牙设置变更广播接收器
        btReceiver = new BTRECReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(btReceiver, intentFilter);
    }

    @Override
    public void onTerminate() {
        mBTCommSocket.stop();
        this.unregisterReceiver(btReceiver);
        super.onTerminate();
    }

    //region =================Configure====================
    private PortInfo mPortInfo;
    private CarInfo mCarInfo;
    private AppConfigure mAppConfigure;

    public AppConfigure getmAppConfigure() {
        return mAppConfigure;
    }

    public void setmAppConfigure(AppConfigure mAppConfigure) {
        this.mAppConfigure = mAppConfigure;
        mCarInfo = mDataBaseService.loadCarInfo(mAppConfigure.CarID);
        mPortInfo = mDataBaseService.loadPortInfo(mAppConfigure.PortID);
    }

    public void startBluetoothConnect() {
        mBTCommSocket.connect(mPortInfo.BTMAC);
    }

    public void stopBluetoothConnect(){
        mBTCommSocket.stop();
    }

    public PortInfo getmPortInfo() {
        return mPortInfo;
    }

    public CarInfo getmCarInfo() {
        return mCarInfo;
    }

    //endregion

    //region ================System State==================
    private ChargeSysInfo mChargeSysInfo;

    public ChargeSysInfo getmChargeSysInfo() {
        return mChargeSysInfo;
    }

    public void setmChargeSysInfo(ChargeSysInfo mChargeSysInfo) {
        this.mChargeSysInfo = mChargeSysInfo;
    }
    //endregion

    //region ==================Services===========================
    private DataBaseService mDataBaseService;
    private CarCommSocket mBTCommSocket;

    public DataBaseService getmDataBaseService() {
        return mDataBaseService;
    }

    public CarCommSocket getmBTCommSocket() {
        return mBTCommSocket;
    }

    //endregion

    private class BTRECReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    case BluetoothAdapter.STATE_ON:
                        startBluetoothConnect();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
//            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
//                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if(dev.getBondState() != BluetoothDevice.BOND_BONDED){
//                    try{
//                        ClsUtils.setPin(dev.getClass(), dev, "1234");
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
    }
}
