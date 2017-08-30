package iee.wirelesscharge.datatype;

import java.text.DecimalFormat;

/**
 * Created by lich on 2017/8/25.
 */
public class ChargeRecord {
    public int index;
    public int year;
    public int month;
    public int date;
    public int day;
    public int hour;
    public int minute;
    public float charge;
    public int charge_hour;
    public int charge_min;
    public int car_num;
    public int port_num;


    public String toStringSimple(){
        DecimalFormat fnum2 = new DecimalFormat("##0.00");
        return String.valueOf(index) +" -- " +
                String.valueOf(month) + "/" + String.valueOf(date) + "  " +
                String.valueOf(hour) + ":"+ String.valueOf(minute) +"  " +
                fnum2.format(charge) + "  " +
                String.valueOf(charge_hour) + ":"+ String.valueOf(charge_min);
    }
    @Override
    public String toString() {
        DecimalFormat fnum2 = new DecimalFormat("##0.00");
        return  String.valueOf(index) + ","
                +  String.valueOf(year) +","
                +  String.valueOf(month) + ","
                +  String.valueOf(date) +","
                + String.valueOf(day) +","
                + String.valueOf(hour) +","
                + String.valueOf(minute) +","
                + fnum2.format(charge) +","
                + String.valueOf(charge_hour) +","
                + String.valueOf(charge_min) +","
                + String.valueOf(car_num) +","
                + String.valueOf(port_num);
    }

    public boolean loadString(String ss){
        try {
            String spilts[] = ss.split(",", 12);
            index = Integer.parseInt(spilts[0]);
            year = Integer.parseInt(spilts[1]);
            month = Integer.parseInt(spilts[2]);
            date = Integer.parseInt(spilts[3]);
            day = Integer.parseInt(spilts[4]);
            hour = Integer.parseInt(spilts[5]);
            minute = Integer.parseInt(spilts[6]);
            charge = Float.parseFloat(spilts[7]);
            charge_hour = Integer.parseInt(spilts[8]);
            charge_min = Integer.parseInt(spilts[9]);
            car_num = Integer.parseInt(spilts[10]);
            port_num = Integer.parseInt(spilts[11]);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
