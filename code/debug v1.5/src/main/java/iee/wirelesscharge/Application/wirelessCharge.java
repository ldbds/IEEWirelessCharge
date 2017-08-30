package iee.wirelesscharge.Application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import iee.wirelesscharge.Service.CarCommSocket;
import iee.wirelesscharge.Service.DataBaseService;
import iee.wirelesscharge.datatype.AppConfigure;
import iee.wirelesscharge.datatype.CarInfo;
import iee.wirelesscharge.datatype.ChargeSysInfo;
import iee.wirelesscharge.datatype.PortInfo;

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
        //startBluetoothConnect();
    }

    public void startBluetoothConnect() {
        mBTCommSocket.connect(mPortInfo.BTMAC);
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
                        //startBluetoothConnect();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    }
}
