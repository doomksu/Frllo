package ru.gov.pfr.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author User
 */
public class SettingsService {

    private TreeMap<String, String> map;
    private static SettingsService instance;

    private String BOOT_PATH;
    private String SQL_PATH;
    private final String DUMMY_FOLDERPATH;
    private HashMap<String, Boolean> dummyFiles;

    private SettingsService() {
        File folder = new File(Paths.get("").toAbsolutePath().toString());
        BOOT_PATH = folder.getAbsolutePath();
        SQL_PATH = BOOT_PATH + "\\sql";
        DUMMY_FOLDERPATH = BOOT_PATH + "\\" + "DUMMY";
        System.out.println(">> start SettingsService");
        map = new TreeMap<>();
        readSettings();
        HashMap<String, String> tempmap = new HashMap<>();
        for (String key : map.keySet()) {
            tempmap.put(key, map.get(key));
        }
        setDefaultSettings();
        for (String key : tempmap.keySet()) {
            map.put(key, tempmap.get(key));
        }
        dummyFiles = new HashMap<>();
        saveSettings();
    }

    public static SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public void saveSettings() {
        try {
            File settingsFile = new File("settings.set");
            FileWriter fw = new FileWriter(settingsFile, false);
            //frllo tables
            for (String key : map.keySet()) {
                fw.append(key + ":" + map.get(key) + "\r\n");
            }
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

    public void setValueFromString(String string) {
        String key = string.substring(0, string.indexOf(":"));
        String val = string.replace(key + ":", "");
        map.put(key, val);
    }

    public void readSettings() {
        try {
            File settingsFile = new File("settings.set");
            if (settingsFile.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(settingsFile), "cp1251"));
                String string;
                while ((string = reader.readLine()) != null) {
                    if (string.contains(":")) {
                        String key = string.substring(0, string.indexOf(":"));
                        String val = string.replace(key + ":", "");
                        map.put(key, val);
                    }
                }
                reader.close();
            } else {
                settingsFile.createNewFile();
                FileWriter fw = new FileWriter(settingsFile, false);
                setDefaultSettings();
                for (String key : map.keySet()) {
                    fw.append(key + ":" + map.get(key) + "\r\n");
                }
                fw.flush();
                fw.close();
                readSettings();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean checkDummyFile(String mockFileName) {
        if (!dummyFiles.containsKey(mockFileName)) {
            String path = DUMMY_FOLDERPATH + "\\" + mockFileName + ".dummy";
            File booleanMockFile = new File(path);
            if (booleanMockFile.exists()) {
                dummyFiles.put(mockFileName, true);
                return true;
            } else {
                dummyFiles.put(mockFileName, false);
            }
        }
        return dummyFiles.get(mockFileName);
    }

    private void setDefaultSettings() {
        //frllo_tables
        map.put("frllo_login", "db2admin");
        map.put("frllo_password", "db2admin");
        map.put("frllo_ip", "10.100.15.40");
        map.put("frllo_port", "50000");
        map.put("frllo_dbSchemaName", "NATA");
        map.put("frllo_dbname", "ROS");
        //fbdp_tables
        map.put("fbdp_login", "db2admin");
        map.put("fbdp_password", "db2admin");
        map.put("fbdp_ip", "10.100.15.47");
        map.put("fbdp_port", "50000");
        map.put("fbdp_dbSchemaName", "CHANGES");
        map.put("fbdp_dbname", "REGIONAL");
        map.put("fatherNameTeg", "patronymic");
        map.put("schedulerSleepInterval", "30");
        map.put("schedulerWorkHour", "21");
        map.put("schedulerStartTime", "00:20");
        map.put("foreignBirthDocument", "true");
        map.put("ussrPasportAsDifferent", "true");
        map.put("autoStartScheduler", "true");
        map.put("resultsPath", "");
        map.put("loadedPath", "");
        map.put("outConvertedFolderPath", "out\\");
        map.put("writeFilter", "writeAll");
        map.put("maxPackagesSize", "100000");
        map.put("BUFFER_SIZE", "10");
        map.put("minimum_lgota_cancel_date", "2020-12-01");
        map.put("db_type", "db2");
        map.put("doWriteAlreadySendedData", "false");
        map.put("allowRecheckDateWithStatistics", "false");
    }

    public String getValue(String key) {
        return map.get(key);
    }

    public String getRootFolder() {
        return BOOT_PATH;
    }

    public String getSQLPath() {
        return SQL_PATH;
    }
}
