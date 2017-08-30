package iee.wirelesscharge.Service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import iee.wirelesscharge.datatype.AppConfigure;
import iee.wirelesscharge.datatype.CarInfo;
import iee.wirelesscharge.datatype.ChargeRecord;
import iee.wirelesscharge.datatype.ChargeSysInfo;
import iee.wirelesscharge.datatype.PortInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lich on 2016/11/23.
 */

/**
 * App Configure files saved in SDcard/CarData/configure.txt
 * Car Configure files saved in SDcard/CarData/CarX.txt, X is ID number
 */
public class DataBaseService {

    private static final String TAG = "DataBaseService";

    static final private String AppConfFileName = "configure";
    static final private String CarInfoFilePrefix = "Car";
    static final private String PortInfoFilePrefix = "Port";
    static final private String FileSurfix = ".conf";

    private String filePath;
    private Context mContext;

    public DataBaseService(Context context) {
        mContext = context;
        initFileWrite();
    }

    private void initFileWrite() {
//      filePath = mContext.getExternalFilesDir();
        //创建目录CarData
        filePath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "carData";
        Log.i(TAG, "file path is:" + filePath);

        File dirFile = new File(filePath);
        dirFile.mkdirs();
    }


    //region ================AppConfigure ==========================
    public AppConfigure loadAppConfigure() {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath, AppConfFileName + FileSurfix));
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(fileInputStream));

            AppConfigure conf = new AppConfigure(bufferedReader.readLine());

            bufferedReader.close();
            fileInputStream.close();
            Log.i(TAG, "AppConfigure loaded");
            return conf;
        } catch (IOException e) {
            Log.i(TAG, "load AppConfigure err");
            return new AppConfigure();
        }
    }

    public void saveAppConfigure(AppConfigure conf) {
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath, AppConfFileName + FileSurfix));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write(conf.toString());

            outputStreamWriter.close();
            fileOutputStream.close();
            Log.i(TAG, "AppConfigure saved");
        } catch (IOException e) {
            Log.i(TAG, "save AppConfigure err");
        }
    }
    //endregion

    //region ==============CarInfo=========================
    public CarInfo loadCarInfo(String carID) {
        return loadCarInfo(new File(filePath, CarInfoFilePrefix + carID + FileSurfix));
    }

    public CarInfo loadCarInfo(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(fileInputStream));

            CarInfo info = new CarInfo(bufferedReader.readLine());

            bufferedReader.close();
            fileInputStream.close();
            Log.i(TAG, "CarInfo loaded");
            return info;
        } catch (IOException e) {
            Log.i(TAG, "load CarInfo err");
            return new CarInfo();
        }
    }

    public CarInfo[] getCarInfoList() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith(CarInfoFilePrefix);
            }
        };
        File[] files = new File(filePath).listFiles(filter);
        CarInfo[] carInfos = new CarInfo[files.length];
        for (int i = 0; i < files.length; i++) {
            carInfos[i] = loadCarInfo(files[i]);
        }
        return carInfos;
    }

    public void saveCarInfo(CarInfo info) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath, CarInfoFilePrefix + info.CarID + FileSurfix));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write(info.toString());

            outputStreamWriter.close();
            fileOutputStream.close();
            Log.i(TAG, "CarInfo saved");
        } catch (IOException e) {
            Log.i(TAG, "save CarInfo err");
        }
    }
    //endregion

    //region ==============PortInfo=========================
    public PortInfo loadPortInfo(String portID) {
        return loadPortInfo(new File(filePath, PortInfoFilePrefix + portID + FileSurfix));
    }

    public PortInfo loadPortInfo(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(fileInputStream));

            PortInfo info = new PortInfo(bufferedReader.readLine());

            bufferedReader.close();
            fileInputStream.close();
            Log.i(TAG, "PortInfo loaded");
            return info;
        } catch (IOException e) {
            Log.i(TAG, "load PortInfo err");
            return new PortInfo();
        }
    }

    public PortInfo[] getPortInfoList() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith(PortInfoFilePrefix);
            }
        };
        File[] files = new File(filePath).listFiles(filter);
        PortInfo[] PortInfos = new PortInfo[files.length];
        for (int i = 0; i < files.length; i++) {
            PortInfos[i] = loadPortInfo(files[i]);
        }
        return PortInfos;
    }

    public void savePortInfo(PortInfo info) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath, PortInfoFilePrefix + info.PortID + FileSurfix));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write(info.toString());

            outputStreamWriter.close();
            fileOutputStream.close();
            Log.i(TAG, "PortInfo saved");
        } catch (IOException e) {
            Log.i(TAG, "save PortInfo err");
        }
    }
    //endregion

    // region ==========================Charge Data===================
    public ChargeRecord[] getRecords(String BTMAC){
        int size = 150;
        ChargeRecord[] records = new ChargeRecord[size];
        for (int i=0;i<size;i++){
            records[i] = new ChargeRecord();
        }
        int i = 0;
        try {
            File[] files = new File(filePath + File.separator + BTMAC.replace(":","")).listFiles();
            String recentName = null;
            long lastModify = 0L;
            for (File file: files){
                if  (file.lastModified() > lastModify){
                    recentName = file.getName();
                    lastModify = file.lastModified();
                }
            }
            File rcdFile = new File(filePath + File.separator + BTMAC.replace(":",""),
                    recentName);
            FileInputStream fileInputStream = new FileInputStream(rcdFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            bufferedReader.readLine(); // empty line
            while(i < size) {
                String line = bufferedReader.readLine();
                if (records[i].loadString(line)){
                    i++;
                }else{
                    throw new IOException();
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
            return records;
        }catch (IOException e){
            if(i == 0) {
                Log.i(TAG, "read Rcd err");
                return null;
            }else if(i<size) {
                ChargeRecord[] trim_records = new ChargeRecord[i];
                for (int cnt = 0; cnt < i; cnt++) {
                    trim_records[cnt] = records[cnt];
                }
                return trim_records;
            }else{
                return records;
            }
        }catch (Exception e){
            Log.i(TAG, "no Rcd");
            return null;
        }
    }

    public String getCurrentRcdFileName(String BTMAC){
        File dirFile = new File(filePath + File.separator + BTMAC.replace(":",""));
        dirFile.mkdirs();
        return filePath + File.separator + BTMAC.replace(":","") + File.separator +
                new SimpleDateFormat("MM-dd-HH-mm-ss").format(new Date(System.currentTimeMillis())) + ".txt";
    }
    public void saveRecord(String rcdfileName, ChargeRecord rcd){
        try {
            File rcdFile = new File(rcdfileName);
            FileOutputStream fileOutputStream = new FileOutputStream(rcdFile,true);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write("\r\n" + rcd.toString());

            outputStreamWriter.close();
            fileOutputStream.close();
        }catch(Exception e){
            Log.i(TAG, "save Rcd err");
        }

    }
    //endregion
    //region  ===========================LOG=============================

    public void resetLog(){
        try {
            File logFile = new File(filePath,
                    "LOG" +  ".txt");
            if (logFile.exists()) {
                logFile.delete();
            }
        }catch(Exception e){
            Log.i(TAG, "save Log err");
         }
        return;
    }
    public void saveLog(ChargeSysInfo info) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath,
                    "LOG"
//                            +new SimpleDateFormat(" yyyy-MM-dd").format(new Date(System.currentTimeMillis()))
                    +  ".txt"),
                    true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write("\r\n" + info.toString());

            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            Log.i(TAG, "save Log err");
        }
    }

    public ChargeSysInfo[] readLog(int size, int timeStep, long base_time){
        ChargeSysInfo[] infodata = new ChargeSysInfo[size];
        for (int i=0;i<size;i++){
            infodata[i] = new ChargeSysInfo();
        }
        int i = 0;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath,
                    "LOG" +  ".txt"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            bufferedReader.readLine(); // empty line
            while(i < size) {
                String line = bufferedReader.readLine();
                if (infodata[i].loadString(line)){
                    if ((infodata[i].time_Tag) > (base_time - (size+1-i)*timeStep)){
                        i++;
                    }
                }else{
                    throw new IOException();
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
;
            return infodata;
        }catch (IOException e){
            if(i == 0) {
                Log.i(TAG, "read Log err");
                return null;
            }else if(i<size) {
                ChargeSysInfo[] trim_infodata = new ChargeSysInfo[i];
                for (int cnt = 0; cnt < i; cnt++) {
                    trim_infodata[cnt] = infodata[cnt];
                }
                return trim_infodata;
            }else{
                return infodata;
            }
        }
    }

    //endregion
}
