package iee.wirelesscharge.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import iee.wirelesscharge.Application.wirelessCharge;
import iee.wirelesscharge.Dialogue.DeviceListActivity;
import iee.wirelesscharge.Service.BluetoothService;
import iee.wirelesscharge.Service.CarCommSocket;
import iee.wirelesscharge.R;
import iee.wirelesscharge.datatype.AppConfigure;
import iee.wirelesscharge.datatype.CarInfo;
import iee.wirelesscharge.datatype.ChargeRecord;
import iee.wirelesscharge.datatype.ChargeSysInfo;
import iee.wirelesscharge.datatype.PortInfo;

import java.text.DecimalFormat;

public class DebugActivity extends Activity {
    private static final String TAG = "DebugActivity";

    // request code
    static final private int REQUEST_CONNECT_DEVICE = 2;

    static private AppConfigure defaultConf = new AppConfigure();

    // private
    private wirelessCharge mApplication;

    //region components
    EditText et_433_Channel;

    TextView tv_Sys_Bluetooth;
    TextView tv_BT_Name;
    TextView tv_BT_MAC;
    TextView tv_Sys_State;
    TextView tv_433_Timeout;
    TextView tv_433_Channel;
    TextView tv_Rcv_Uout;
    TextView tv_Rcv_Iout;
    TextView tv_Tmt_Udc;
    TextView tv_Tmt_Idc;

    TextView tv_Rcv_T;
    TextView tv_Tmt_T;
    TextView tv_Tmt_PhaseShift;
    TextView tv_Rly_locate;
    TextView tv_Rly_output;

    TextView tv_trade_time;

    TextView tv_preserved1;
    TextView tv_preserved2;
    TextView tv_preserved3;
    TextView tv_preserved4;

    TextView tv_rcd_index;

    Button btn_bluetooth;
    Button btn_433set;
    Button btn_fetch;
    Button btn_stop;
    Button btn_locate;
    Button btn_show;
//    Button btn_scope;
    //endregion

    private BTRECReceiver btReceiver = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "--- ON CREATE---");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        mApplication = wirelessCharge.getInstance();
        //  use "XXXXXXX|xxxxxxx" as debug config
//        mApplication.setmAppConfigure(defaultConf);

        initView();

        //动态注册蓝牙接收广播接收器
        btReceiver = new BTRECReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CarCommSocket.BTREC_BROADCAST);
        this.registerReceiver(btReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "--- ON DESTORY---");
        this.unregisterReceiver(btReceiver);
        super.onDestroy();
    }


    private void initView() {
        TableLayout mOutputTable = (TableLayout) findViewById(R.id.output_table);

        tv_Sys_Bluetooth = addNewText(mOutputTable, "蓝牙状态");
        tv_BT_Name = addNewText(mOutputTable, "蓝牙名称");
        tv_BT_MAC = addNewText(mOutputTable, "蓝牙地址");
        tv_433_Timeout = addNewText(mOutputTable, "433超时");
        tv_433_Channel = addNewText(mOutputTable, "433频道");
        tv_Sys_State = addNewText(mOutputTable, "系统状态号");
        tv_Tmt_PhaseShift = addNewText(mOutputTable, "地面端移相");
        tv_rcd_index = addNewText(mOutputTable, "读取记录条数");
        tv_Rly_locate = addNewText(mOutputTable, "定位继电器");
        tv_Rly_output = addNewText(mOutputTable, "输出继电器");
        tv_Rcv_Uout = addNewText(mOutputTable, "车载端电压");
        tv_Rcv_Iout = addNewText(mOutputTable, "车载端电流");
        tv_Tmt_Udc = addNewText(mOutputTable, "地面端直流电压");
        tv_Tmt_Idc = addNewText(mOutputTable, "地面端直流电流");
        tv_Rcv_T = addNewText(mOutputTable, "车载端温度");
        tv_Tmt_T = addNewText(mOutputTable, "地面端温度");
        tv_trade_time = addNewText(mOutputTable, "充电时间");


        tv_preserved1 = addNewText(mOutputTable, "充电电量");
        tv_preserved2 = addNewText(mOutputTable, "保留位2");
        tv_preserved3 = addNewText(mOutputTable, "保留位3");
        tv_preserved4 = addNewText(mOutputTable, "保留位4");

        TableLayout mInputTable = (TableLayout) findViewById(R.id.input_table);
        et_433_Channel = addNewEdit(mInputTable, "433频道");
        et_433_Channel.setText(String.valueOf(mApplication.getmCarInfo().RF433Channel));

        btn_bluetooth = (Button) findViewById(R.id.btn_bluetooth);
        btn_433set = (Button) findViewById(R.id.btn_433set);
        btn_locate = (Button) findViewById(R.id.btn_locate);
        btn_fetch = (Button) findViewById(R.id.btn_fetch);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_show = (Button) findViewById(R.id.btn_show);
//        btn_scope = (Button) findViewById(R.id.btn_scope);

        btn_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DebugActivity.this, DeviceListActivity.class);
                intent.putExtra("bluetooth_devices", "车位选择");
                intent.putExtra("no_devices_found", "无");
                intent.putExtra("scanning", "搜索中...");
                intent.putExtra("scan_for_devices", "搜索车位");
                intent.putExtra("select_device", "选择");
                intent.putExtra("doDiscover",true);
                startActivityForResult(intent, DebugActivity.REQUEST_CONNECT_DEVICE);
            }
        });

        btn_433set.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String channel = et_433_Channel.getText().toString().trim().toUpperCase();
                if (channel.length() != 12) {
                    Toast.makeText(wirelessCharge.getInstance(), "should be 12-bytes-long-String", Toast.LENGTH_SHORT).show();
                }else {
                    mApplication.getmBTCommSocket().sendCommand(
                            CarCommSocket.CarCommand.FETCH_RECORD, 0x00);

                    CarInfo conf = mApplication.getmCarInfo();
                    conf.RF433Channel = channel;
                    mApplication.getmDataBaseService().saveCarInfo(conf);

                    mApplication.setmAppConfigure(defaultConf);

                    mApplication.getmBTCommSocket().sendCommand(
                            CarCommSocket.CarCommand.SELECT_CHANNEL,
                            0, channel);
                    Toast.makeText(DebugActivity.this,"开始通道设置", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_locate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mApplication.getmBTCommSocket().sendCommand(
                        CarCommSocket.CarCommand.START_LOCATE);
                Toast.makeText(DebugActivity.this,"开始定位", Toast.LENGTH_SHORT).show();
            }
        });

        btn_fetch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mApplication.getmBTCommSocket().sendCommand(
                        CarCommSocket.CarCommand.FETCH_RECORD, 0x01);
                Toast.makeText(DebugActivity.this,"请求回传", Toast.LENGTH_SHORT).show();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mApplication.getmBTCommSocket().sendCommand(
                        CarCommSocket.CarCommand.STOP_CHARGE);
                Toast.makeText(DebugActivity.this,"请求停止", Toast.LENGTH_SHORT).show();
            }
        });

//        btn_scope.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(DebugActivity.this, ScopeActivity.class));
//            }
//        });

        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChargeRecord[]  rcds = mApplication.getmDataBaseService().getRecords(mApplication.getmPortInfo().BTMAC);
                if (rcds == null) return;
                String[] items = new String[rcds.length];
                for (int i=0; i <rcds.length;i++ ) {
                    items[i] = rcds[i].toStringSimple();
                }
                new AlertDialog.Builder(DebugActivity.this)
                        .setTitle("充电记录")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ;
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DebugActivity.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                String newBT = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                PortInfo conf = mApplication.getmPortInfo();
                conf.BTMAC = newBT;
                mApplication.getmDataBaseService().savePortInfo(conf);

                mApplication.setmAppConfigure(defaultConf);
                mApplication.startBluetoothConnect();
            }
        }
    }


    private TextView addNewText(TableLayout mOutputTable, CharSequence label) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();

        TextView lb_view = new TextView(this);
        lb_view.setText(label);
        layoutParams.column = 0;
        layoutParams.weight = 1;
        lb_view.setLayoutParams(layoutParams);
        lb_view.setGravity(Gravity.LEFT);
        lb_view.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

        TextView txt_view = new TextView(this);
        txt_view.setText("--");
        layoutParams.column = 1;
        layoutParams.weight = 1;
        txt_view.setLayoutParams(layoutParams);
        txt_view.setGravity(Gravity.RIGHT);
        txt_view.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

        TableRow new_row = new TableRow(this);
        TableLayout.LayoutParams layoutParams2 = new TableLayout.LayoutParams();
        layoutParams2.width = TableLayout.LayoutParams.MATCH_PARENT;
        layoutParams2.height = TableLayout.LayoutParams.MATCH_PARENT;
        layoutParams2.setMargins(0, 0, 30, 20);
        new_row.setLayoutParams(layoutParams2);
        new_row.addView(lb_view);
        new_row.addView(txt_view);
        mOutputTable.addView(new_row);
        return txt_view;
    }


    private EditText addNewEdit(TableLayout mOutputTable, CharSequence label) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();

        TextView lb_view = new TextView(this);
        lb_view.setText(label);
        layoutParams.column = 0;
        layoutParams.weight = 1;
        lb_view.setLayoutParams(layoutParams);
        lb_view.setGravity(Gravity.LEFT);
        lb_view.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);

        EditText edit_view = new EditText(this);
        edit_view.setText("0");
        layoutParams.column = 1;
        layoutParams.weight = 1;
        edit_view.setLayoutParams(layoutParams);
        edit_view.setGravity(Gravity.RIGHT);
        edit_view.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Large);
        edit_view.setSingleLine(true);
        edit_view.setSelectAllOnFocus(true);

        TableRow new_row = new TableRow(this);
        TableLayout.LayoutParams layoutParams2 = new TableLayout.LayoutParams();
        layoutParams2.width = TableLayout.LayoutParams.MATCH_PARENT;
        layoutParams2.height = TableLayout.LayoutParams.MATCH_PARENT;
        layoutParams2.setMargins(0, 0, 30, 20);
        new_row.setLayoutParams(layoutParams2);
        new_row.addView(lb_view);
        new_row.addView(edit_view);
        mOutputTable.addView(new_row);
        return edit_view;
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
                        tv_Sys_Bluetooth.setText("断开中");
                        break;
                    case CONNECTED:
                        tv_Sys_Bluetooth.setText("已连接");
                        break;
                    case CONNECTING:
                        tv_Sys_Bluetooth.setText("正在连接");
                        break;
                    case IDLE:
                        tv_Sys_Bluetooth.setText("空闲");
                        break;
                }
            }
            //endregion


            int rcd_index = intent.getIntExtra("rcdIndex",-1);
            //region -----rcd index --------
            if (rcd_index != -1){
                tv_rcd_index.setText(String.valueOf(rcd_index));
            }
            //endregion

            ChargeSysInfo chargeSysInfo = (ChargeSysInfo) intent.getSerializableExtra("data");
            //region ----- data display -----
            if (chargeSysInfo != null) {
                DecimalFormat fnum = new DecimalFormat("##0.0");
                DecimalFormat fnum2 = new DecimalFormat("##0.00");

                tv_Sys_State.setText(String.valueOf(chargeSysInfo.system_state));
                if (chargeSysInfo.relay_locate) {
                    tv_Rly_locate.setText("ON");
                } else {
                    tv_Rly_locate.setText("OFF");
                }
                if (chargeSysInfo.relay_output) {
                    tv_Rly_output.setText("ON");
                } else {
                    tv_Rly_output.setText("OFF");
                }
                tv_trade_time.setText(
                        String.valueOf(chargeSysInfo.trade_charge_time_hour)
                                + ":" +
                        String.valueOf(chargeSysInfo.trade_charge_time_min)
                                + ":" +
                        String.valueOf(chargeSysInfo.trade_charge_time_sec));

                tv_BT_MAC.setText(chargeSysInfo.BT_MAC);
                tv_BT_Name.setText(chargeSysInfo.BT_Name);

                tv_433_Channel.setText(String.valueOf(chargeSysInfo.RF433_channel));
                tv_433_Timeout.setText(String.valueOf(chargeSysInfo.RF433_timeout));

                tv_Tmt_Udc.setText(String.valueOf(chargeSysInfo.transmitter_Udc) + " V");
                tv_Tmt_Idc.setText(fnum.format(chargeSysInfo.transmitter_Idc) + " A");
                tv_Tmt_T.setText(String.valueOf(chargeSysInfo.transmitter_T) + " ℃");
                tv_Tmt_PhaseShift.setText(String.valueOf(chargeSysInfo.transmitter_phaseshift));
                tv_Rcv_Uout.setText(String.valueOf(chargeSysInfo.receiver_Uout) + " V");
                tv_Rcv_Iout.setText(fnum.format(chargeSysInfo.receiver_Iout) + " A");
                tv_Rcv_T.setText(String.valueOf(chargeSysInfo.receiver_T) + " ℃");

                tv_preserved1.setText(fnum2.format(chargeSysInfo.trade_total_charge));
                tv_preserved2.setText(String.valueOf(chargeSysInfo.preserved_data2));
                tv_preserved3.setText(String.valueOf(chargeSysInfo.preserved_data3));
                tv_preserved4.setText(String.valueOf(chargeSysInfo.preserved_data4));
            }
            //endregion

        }
    }

}
