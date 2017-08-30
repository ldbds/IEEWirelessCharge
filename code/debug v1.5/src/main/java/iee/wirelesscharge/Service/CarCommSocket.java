package iee.wirelesscharge.Service;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import iee.wirelesscharge.Application.wirelessCharge;
import iee.wirelesscharge.datatype.ChargeRecord;
import iee.wirelesscharge.datatype.ChargeSysInfo;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * Created by lich on 2016/11/9.
 */
public class CarCommSocket extends BluetoothService {
    private int record_index = 0;
    private String currentRcdFileName = null;
    public CarCommSocket(Context context) {
        super(context);
        mChargeSysInfo = new ChargeSysInfo();
        record_index = 0;
    }

    //蓝牙接收数据广播的actions
    public static final String BTREC_BROADCAST = "com.wirelesscharge.action.BTRECEIVE";

    @Override
    protected void BTConnectHandler(InputStream in, OutputStream out) throws Exception {

        while (true) {
            // 检测连接超时及起始码,得到19位数据报文
            byte[] buffer = new byte[19];
            int count = 0;
            while (count < 19) {
                if (in.available() == 0) {
                    Thread.sleep(3000);
                    if (in.available() == 0) {
                        Thread.sleep(3000);
                        if (in.available() == 0) {
                            Thread.sleep(3000);
                            if (in.available() == 0) {
                                throw new TimeoutException("Timeout");
                            }
                        }
                    }
                }
                count += in.read(buffer, count, 1);
                if (buffer[0] != 0x5A) {
                    count = 0;
                    continue;
                }
                if ((count >= 2) && ((buffer[1]) != (byte) 0xA5)) {
                    count = 0;
                    continue;
                }
            }
//            in.skip(in.available());

            if ((buffer[0] == 0x5A) && (buffer[1] == (byte) 0xA5)) {
                //更新数据
                refreshCarInfo(buffer);
                //发送广播
                Intent intent = new Intent(BTREC_BROADCAST);
                intent.putExtra("state", BLUETOOTH_SERVICE_STATE.CONNECTED);
                intent.putExtra("data", mChargeSysInfo);
                getContext().sendBroadcast(intent);


//                wirelessCharge.getInstance().getmDataBaseService().saveLog(mChargeSysInfo);
            }
        }
    }

    @Override
    protected void setState(BLUETOOTH_SERVICE_STATE state) {
        super.setState(state);
        Intent intent = new Intent(BTREC_BROADCAST);
        intent.putExtra("state", state);
        ChargeSysInfo EmptyInfo = new ChargeSysInfo();
        EmptyInfo.BT_MAC = getBTMAC();
        EmptyInfo.BT_Name = getName();
        intent.putExtra("data", EmptyInfo);
        intent.putExtra("rcdIndex", record_index);
        getContext().sendBroadcast(intent);
    }

    public enum CarCommand {
        START_LOCATE,
        STOP_CHARGE,
        SELECT_CHANNEL,
        START_CHARGE,
        FETCH_RECORD
    }

    private ChargeSysInfo mChargeSysInfo;

    public ChargeSysInfo getChargeSysInfo() {
        return mChargeSysInfo;
    }


    /**
     * 蓝牙协议结构
     *
     * @param data byte[19]
     */
    private void refreshCarInfo(byte[] data) {
        int[] dataBuf;

        mChargeSysInfo.time_Tag = System.currentTimeMillis();
        mChargeSysInfo.BT_MAC = getBTMAC();
        mChargeSysInfo.BT_Name = getName();

        dataBuf = new int[19];
        for (int i = 0; i < 19; i++)
            dataBuf[i] = data[i] & 0xff;

        if ((dataBuf[0] == 0x5A) && (dataBuf[1] == 0xA5)) {
            switch (dataBuf[2]) {
                case 1:
                    mChargeSysInfo.system_state = dataBuf[3];
                    mChargeSysInfo.RF433_timeout = dataBuf[4];
                    mChargeSysInfo.receiver_Uout = (dataBuf[5] * 256 + dataBuf[6]) * 1.0f;
                    mChargeSysInfo.receiver_Iout = (dataBuf[7] * 256 + dataBuf[8]) * 0.1f;
                    mChargeSysInfo.transmitter_Udc = (dataBuf[9] * 256 + dataBuf[10]) * 1.0f;
                    mChargeSysInfo.transmitter_Idc = (dataBuf[11] * 256 + dataBuf[12]) * 0.1f;
                    mChargeSysInfo.relay_locate = (dataBuf[13] != 0x00);
                    mChargeSysInfo.relay_output = (dataBuf[14] != 0x00);
                    mChargeSysInfo.transmitter_T = dataBuf[15] - 30;
                    mChargeSysInfo.receiver_T = dataBuf[16] - 30;
                    break;
                case 2:
                    mChargeSysInfo.trade_charge_time_hour = dataBuf[3];
                    mChargeSysInfo.trade_charge_time_min = dataBuf[4];
                    mChargeSysInfo.transmitter_phaseshift = (dataBuf[5] * 256 + dataBuf[6]);
                    mChargeSysInfo.RF433_channel = dataBuf[7];
                    mChargeSysInfo.trade_charge_time_sec = dataBuf[8];
                    mChargeSysInfo.trade_total_charge = (dataBuf[9] * 256 + dataBuf[10])*0.01f;
                    mChargeSysInfo.preserved_data2 = (dataBuf[11] * 256 + dataBuf[12]);
                    mChargeSysInfo.preserved_data3 = (dataBuf[13] * 256 + dataBuf[14]);
                    mChargeSysInfo.preserved_data4 = (dataBuf[15] * 256 + dataBuf[16]);
                    break;
                case 3:
                    // 车载端信息回传
                    ChargeRecord rcd = new ChargeRecord();

                    rcd.index = dataBuf[3];
                    rcd.year = dataBuf[4];
                    rcd.month  = dataBuf[5];
                    rcd.date = dataBuf[6];
                    rcd.day = dataBuf[7];
                    rcd.hour = dataBuf[8];
                    rcd.minute = dataBuf[9];
                    rcd.charge = (dataBuf[10] *256 + dataBuf[11]) * 0.01f;;
                    rcd.charge_hour = dataBuf[12];
                    rcd.charge_min = dataBuf[13];
                    rcd.car_num = dataBuf[14];
                    rcd.port_num = dataBuf[15];


                    record_index = rcd.index +1;
                    if (record_index == 1){
                        currentRcdFileName = wirelessCharge.getInstance().getmDataBaseService().getCurrentRcdFileName(
                                wirelessCharge.getInstance().getmPortInfo().BTMAC);
                    }
                    if (record_index %10 == 0){
                        setState(BLUETOOTH_SERVICE_STATE.CONNECTED);
                    }
                    if (record_index >= 150) { // notify finished
                        setState(BLUETOOTH_SERVICE_STATE.CONNECTED);
                    }
                    if (rcd.year == 255) return;  // null record


                    wirelessCharge.getInstance().getmDataBaseService().saveRecord(
                            currentRcdFileName,
                            rcd);
                    return;
            }
        }
        wirelessCharge.getInstance().setmChargeSysInfo(mChargeSysInfo);
    }

    private byte[] calcCRC(byte[] BTSendData) {
        int crc = 0xffff;
        byte bit_flag;
        for (int k = 0; k < 17; k++) {
            crc ^= BTSendData[k];
            for (int j = 0; j < 8; j++) {
                bit_flag = (byte) (crc & 0x0001);
                crc >>= 1;
                if (bit_flag == 1)
                    crc ^= 0xa001;
            }
        }
        BTSendData[17] = (byte) (crc / 256);
        BTSendData[18] = (byte) (crc % 256);
        return BTSendData;
    }


    private byte[] getCmdBytes(CarCommand cmd, int data, String dataString) {

        switch (cmd) {
            case SELECT_CHANNEL:{
                byte[] code = {(byte) 0xFA, 0x55, 0x01, 0x01, (byte)data,
                        (byte)dataString.charAt(0),
                        (byte)dataString.charAt(1),
                        (byte)dataString.charAt(2),
                        (byte)dataString.charAt(3),
                        (byte)dataString.charAt(4),
                        (byte)dataString.charAt(5),
                        (byte)dataString.charAt(6),
                        (byte)dataString.charAt(7),
                        (byte)dataString.charAt(8),
                        (byte)dataString.charAt(9),
                        (byte)dataString.charAt(10),
                        (byte)dataString.charAt(11), 0, 0};
                return calcCRC(code);
            }
            case START_LOCATE: {
                byte[] code = {(byte) 0xFA, 0x55, 0x02, 0x01, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                return calcCRC(code);
            }
            case STOP_CHARGE: {
                byte[] code = {(byte) 0xFA, 0x55, 0x02, 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                return calcCRC(code);
            }
            case FETCH_RECORD:{
                // FA 55 03 01 YY MM DD WW HH MM 00 00 CRC CRC
                byte[] code = {(byte) 0xFA, 0x55, 0x03, 0x00, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                code[3] = (byte)data;
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                code[4] = (byte)(date.getYear() %100);
                code[5] = (byte)(date.getMonth()+1);
                code[6] = (byte)date.getDate();
                code[7] = (byte)date.getDay();
                code[8] = (byte)date.getHours();
                code[9] = (byte)date.getMinutes();
                code[10] = (byte)0x00;
                code[11] = (byte)0x00;

                return calcCRC(code);
            }
            default:
                return null;
        }
    }

    /**
     * Send Command in an unsynchronized manner
     *
     * @param cmd The cmd to write
     * @see ConnectThread#write(byte[])
     */
    public void sendCommand(CarCommand cmd) {
        this.write(getCmdBytes(cmd, 0, null));
    }

    public void sendCommand(CarCommand cmd, int data) {
        this.write(getCmdBytes(cmd, data, null));
    }

    public void sendCommand(CarCommand cmd, int data, String dataString) {
        this.write(getCmdBytes(cmd, data, dataString));
    }
}
