package org.iee.slidemenutest.datatype;

import android.util.Log;

/**
 * Created by lich on 2016/12/6.
 * format : ParkedCarID|RF433Channel|LocateVolt
 */
public class CarInfo{
    private static final String TAG = "CarInfo";
    public static final String defaultCarID = "默认车辆";
    private static final int defaultRF433Channel = 0;
    private static final int defaultLocateVolt = 230;
    public String CarID;
    public int RF433Channel;
    public int LocateVolt;

    @Override
    public String toString() {
        return CarID + "|" + String.valueOf(RF433Channel) + "|" + String.valueOf(LocateVolt);
    }

    public CarInfo() {
        CarID = defaultCarID;
        RF433Channel = defaultRF433Channel;
        LocateVolt = defaultLocateVolt;
    }

    public CarInfo(String str) {
        this();
        try {
            String[] strSplit = str.split("\\|", 3);
            CarID = strSplit[0];
            RF433Channel = Integer.valueOf(strSplit[1]);
            LocateVolt = Integer.valueOf(strSplit[2]);
        } catch (Exception e) {
            Log.e(TAG, "Incorrect data format");
        }
    }
}
