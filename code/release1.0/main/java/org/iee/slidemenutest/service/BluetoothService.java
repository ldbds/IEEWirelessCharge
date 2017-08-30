package org.iee.slidemenutest.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 这个类完成安装和管理蓝牙和其他设备连接的所有工作。有一个线程负责监听进来的连接
 * 一个线程负责连接设备，还有一个线程完成连接时的数据传输
 */
public abstract class BluetoothService {
    // 调试
    private static final String TAG = "Bluetooth";

    // 创建服务器套接字的SDP记录的名字
    private static final String NAME = "wirelessChargeBT";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private String mBTNAME = "";
    private String mBTMAC = "00:00:00:00:00:00";
    private Context mContext;

    public String getName() {
        return mBTNAME;
    }
    public String getBTMAC() {
        return mBTMAC;
    }
    public Context getContext() {
        return mContext;
    }

    public BluetoothService(Context context) {
        mContext = context;
        lock = new Object();

        if (BluetoothAdapter.getDefaultAdapter() == null){
            Toast.makeText(mContext, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(enableIntent);
        }
    }

    public enum BLUETOOTH_SERVICE_STATE {
        IDLE, CONNECTING, CONNECTED, CANCELLING
    }

    private BLUETOOTH_SERVICE_STATE mState = BLUETOOTH_SERVICE_STATE.IDLE;

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    protected void setState(BLUETOOTH_SERVICE_STATE state) {
        Log.i(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        if (state == BLUETOOTH_SERVICE_STATE.CONNECTED){
            n_timeout = 0;
        }
    }

    /**
     * Return the current connection state.
     */
    public BLUETOOTH_SERVICE_STATE getState() {
        return mState;
    }

    private int n_count = 0;        //  connect thread in queue
    private boolean f_stop = true;  //  stopping connect thread
    private int n_timeout = 0;      //  restart BluetoothAdapter if frequently timeout
    private Object lock;            //  lock connect thread process, keep connect thread wait in queue

    public void resetBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            return;
        }
        mBluetoothAdapter.disable();//直接关闭蓝牙
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ee) {
            Log.e(TAG, "Sleep Interrupted");
        }
        mBluetoothAdapter.enable();//直接打开蓝牙
    }
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param BTMAC The BluetoothDevice MAC address to connect
     * @note be sure to have BluetoothService at CONNECTED state, otherwise the quest will be rejected.
     */
    public synchronized void connect(String BTMAC) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            return;
        }
        if ((mState == BLUETOOTH_SERVICE_STATE.CONNECTED)
            && (BTMAC.compareTo(mBTMAC) == 0)){
            return;
        }

        f_stop = false;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(BTMAC);
        if (device == null) {
            Log.e(TAG, "Invalid BTMAC");
            return;
        }
        n_count++;

        if (mConnectThread != null) {
            mConnectThread.cancel();
        }
        mConnectThread = new ConnectThread(device);
        mBTNAME = device.getName();
        mConnectThread.start();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        f_stop = true;
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private InputStream mmInStream = null;
        private OutputStream mmOutStream = null;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            synchronized (lock) {
                mBTMAC = mmDevice.getAddress();
                Log.i(TAG, "BEGIN mConnectThread");
                try {
                    /**
                     * connecting process
                     */
                    setState(BLUETOOTH_SERVICE_STATE.CONNECTING);

                    // Always cancel discovery because it will slow down a connection
                    mBluetoothAdapter.cancelDiscovery();
                    //连接建立前先配对
                    if (mmDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        Method creMethod = BluetoothDevice.class
                                .getMethod("createBond");
                        Log.i(TAG, "bonding BluetoothDevice");
                        creMethod.invoke(mmDevice);
                    } else {
                        Log.i(TAG, "BluetoothDevice is bonded");
                    }

                    // Make a connection to the BluetoothSocket
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();

                    /**
                     * connected process
                     */
                    setState(BLUETOOTH_SERVICE_STATE.CONNECTED);
                    mmInStream = mmSocket.getInputStream();
                    mmOutStream = mmSocket.getOutputStream();

                    /**
                     * user handler
                     */
                    BluetoothService.this.BTConnectHandler(mmInStream, mmOutStream);

                } catch (Exception e) {
                    Log.i(TAG, "Interrupted");
                    if (!f_stop && n_count <= 1) {
                        n_timeout = n_timeout+1;
                        if (n_timeout >5){
                            n_timeout = 0;
                            resetBluetooth();
                        }
                        //	pause 3s before reconnecting
                        try {
                            sleep(1000);
                        } catch (InterruptedException ee) {
                            Log.e(TAG, "Sleep Interrupted");
                        }
                    }
                    //	quit
                }
                /**
                 * free socket
                 */
                try {
                    mmSocket.close();
                }catch (IOException e){
                    Log.e(TAG, "close() of connect socket failed", e);
                }

                /**
                 * if not stopping or replaced , reconnect
                 */
                if (!f_stop && n_count <= 1) {
                    connect(mBTMAC);
                }

                /**
                 * free as idle process
                 */
                setState(BLUETOOTH_SERVICE_STATE.IDLE);
                n_count--;
            }
        }

        public void cancel() {
            try {
                if (null != mmSocket) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                Log.i(TAG, new String(buffer));
            } catch (Exception e) {
                Log.e(TAG, "Exception during write" + buffer);
            }
        }
    }

    /**
     * Handler of the communication protocol
     *
     * @param in,out I/O stream of the current Bluetooth Device
     * @throws Exception When Connection lost or reset, Exception occurs ,can be leave to the caller;
     *                   If reconnection wanted, throw an exception.
     *                   If endding connection wanted, return.
     */
    protected abstract void BTConnectHandler(InputStream in, OutputStream out) throws Exception;

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param bytes byte array to write
     * @see ConnectThread#write(byte[])
     */
    public synchronized void write(byte[] bytes) {
        try {
            // Create temporary object
            ConnectThread r;
            if (mState != BLUETOOTH_SERVICE_STATE.CONNECTED) {
                Log.e(TAG, "write while not connected");
                return;
            }
            r = mConnectThread;
            // Perform the write
            r.write(bytes);
        } catch (Exception e) {
            Log.e(TAG, "write fail");
            return;
        }
    }

}