package iee.wirelesscharge.datatype;

import android.util.Log;


/**
 * Created by lich on 2016/12/6.
 */
public class AppConfigure{
    private static final String TAG = "AppConfigure";
    public String PortID;
    public String CarID;

    @Override
    public String toString() {
        return PortID + "|" + CarID;
    }

    public AppConfigure() {
        PortID = PortInfo.defaultPortID;
        CarID = CarInfo.defaultCarID;
    }

    public AppConfigure(String str) {
        this();
        try {
            String[] strSplit = str.split("\\|", 2);
            PortID = strSplit[0];
            CarID = strSplit[1];
        } catch (Exception e) {
            Log.e(TAG, "Incorrect data format");
        }
    }
}


