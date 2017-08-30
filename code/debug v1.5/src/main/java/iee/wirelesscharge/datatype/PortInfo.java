package iee.wirelesscharge.datatype;

import android.util.Log;

/**
 * Created by lich on 2016/11/23.
 *  format : PortID|BTNAME|BTCODE|ParkedCarID
 */
public class PortInfo {
    private static final String TAG = "PortInfo";
    public static final String defaultPortID = "默认车位";
    private static final String defaultBTNAME = "UNNAMED";
    private static final String defaultBTMAC = "00:00:00:00:00:00";
    public String BTNAME;
    public String PortID;
    public String BTMAC;

    @Override
    public String toString() {
        return PortID + "|" + BTNAME + "|"+BTMAC;
    }

    public PortInfo() {
        PortID = defaultPortID;
        BTNAME = defaultBTNAME;
        BTMAC = defaultBTMAC;
    }

    public PortInfo(String str) {
        this();
        try {
            String[] strSplit = str.split("\\|", 3);
            PortID = strSplit[0];
            BTNAME = strSplit[1];
            BTMAC = strSplit[2];
        } catch (Exception e) {
            Log.e(TAG, "Incorrect data format");
        }
    }
}


