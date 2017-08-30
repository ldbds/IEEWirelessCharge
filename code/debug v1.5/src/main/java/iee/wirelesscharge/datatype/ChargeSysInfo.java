package iee.wirelesscharge.datatype;

import java.io.Serializable;

/**
 * Created by lich on 2016/11/9.
 */
public class ChargeSysInfo implements Serializable {
    public long time_Tag = 0L;
    public String BT_Name = "";
    public String BT_MAC = "";

    public int system_state = 0;        // 充电状态 0-7

    public  int RF433_timeout = 0; // 433通信超时0~255 ,offset 0, unit 1
    public  int RF433_channel = 0; // 433通信超时0~255 ,offset 0, unit 1

    public  float receiver_Uout = 0;    //  车载端电压0~500V, offset 0, unit 1V
    public  float receiver_Iout = 0;    //  车载端电流0~100A, offset 0, unit 0.1A
    public  int receiver_T = 0;         //  车载端温度 0~200 Cdeg, offset -30, unit 1Cdeg

    public  float transmitter_Udc = 0;  //  地面端电压0~500V, offset 0, unit 1V
    public  float transmitter_Idc = 0;  //  地面端电流0~100A, offset 0, unit 0.1A
    public  int transmitter_T = 0;      //  地面端温度 0~200 Cdeg, offset -30, unit 1Cdeg

    public int transmitter_phaseshift = 0;  // 地面端电压移相角
    public boolean relay_locate = false;      //  定位继电器 1为闭合 0为断开s
    public boolean relay_output = false;      //  输出继电器 1为闭合 0为断开

    public int trade_charge_time_hour = 0;  // 充电小时数 0~24
    public int trade_charge_time_min = 0;   // 充电分钟数 0~60
    public int trade_charge_time_sec = 0;   // 充电秒数 0~60

    public float trade_total_charge = 0;
    //public int preserved_data1 = 0; //  preserved data in frame 2
    public int preserved_data2 = 0;
    public int preserved_data3 = 0;
    public int preserved_data4 = 0;

    @Override
    public String toString() {
        return time_Tag + ","
                + BT_Name +","
                + BT_MAC + ","
                + String.valueOf(system_state) +","
                + String.valueOf(RF433_timeout) +","
                + String.valueOf(RF433_channel) +","
                + String.valueOf(receiver_Uout) +","
                + String.valueOf(receiver_Iout) +","
                + String.valueOf(receiver_T) +","
                + String.valueOf(transmitter_Udc) +","
                + String.valueOf(transmitter_Idc) +","
                + String.valueOf(transmitter_T) +","
                + String.valueOf(transmitter_phaseshift) +","
                + String.valueOf(relay_locate) +","
                + String.valueOf(relay_output) +","
                + String.valueOf(trade_charge_time_hour) +","
                + String.valueOf(trade_charge_time_min) +","
                + String.valueOf(trade_charge_time_sec) +","
                + String.valueOf(trade_total_charge) +","
                + String.valueOf(preserved_data2) +","
                + String.valueOf(preserved_data3) +","
                + String.valueOf(preserved_data4);
    }

    public boolean loadString(String ss){
        try {
            String spilts[] = ss.split(",", 22);
            time_Tag = Long.parseLong(spilts[0]);
            BT_Name = spilts[1];
            BT_MAC = spilts[2];
            system_state = Integer.parseInt(spilts[3]);
            RF433_timeout = Integer.parseInt(spilts[4]);
            RF433_channel = Integer.parseInt(spilts[5]);
            receiver_Uout = Float.parseFloat(spilts[6]);
            receiver_Iout = Float.parseFloat(spilts[7]);
            receiver_T = Integer.parseInt(spilts[8]);
            transmitter_Udc = Float.parseFloat(spilts[9]);
            transmitter_Idc = Float.parseFloat(spilts[10]);
            transmitter_T = Integer.parseInt(spilts[11]);
            transmitter_phaseshift = Integer.parseInt(spilts[12]);
            relay_locate = Boolean.parseBoolean(spilts[13]);
            relay_output = Boolean.parseBoolean(spilts[14]);
            trade_charge_time_hour = Integer.parseInt(spilts[15]);
            trade_charge_time_min = Integer.parseInt(spilts[16]);
            trade_charge_time_sec = Integer.parseInt(spilts[17]);
            trade_total_charge = Float.parseFloat(spilts[18]);
            preserved_data2 = Integer.parseInt(spilts[19]);
            preserved_data3 = Integer.parseInt(spilts[20]);
            preserved_data4 = Integer.parseInt(spilts[21]);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
