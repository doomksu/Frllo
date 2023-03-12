package ru.gov.pfr.FNCIDictionaryes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author kneretin
 */
public class StranaDictionary {

    private static File dictFile;
    private static StranaDictionary instance;
    private static HashMap<String, String> map = new HashMap<>();

    public StranaDictionary() {
        String here = new File(".").getAbsolutePath();
        File folder = new File(here.substring(0, here.indexOf(".")));
        dictFile = new File(folder.getAbsolutePath() + "\\dict\\doc\\strana.csv");
    }

    private static void readDict(File file) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "cp1251"));
        String string;
        while ((string = reader.readLine()) != null) {
            if (string.contains(";")) {
                String[] vals = string.split(";");
                map.put(vals[0].trim(), vals[1].trim());
            }
        }
        reader.close();
    }

    public static StranaDictionary getInstance() {
        if (instance == null) {
            StranaDictionary dnd = new StranaDictionary();
            try {
                readDict(dictFile);
                instance = dnd;
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }
        return instance;
    }

    public static void addValue(String key, String value) {
        getInstance().map.put(key, value);
    }

    public static String getValue(String key) {
        return getInstance().map.get(key);
    }

    public static void print() {
        LoggingService.writeLog("--StranaDictionary--", "debug");
        for (String key : getInstance().map.keySet()) {
            LoggingService.writeLog("key: " + key + " - " + getInstance().map.get(key), "debug");
        }
    }
}
