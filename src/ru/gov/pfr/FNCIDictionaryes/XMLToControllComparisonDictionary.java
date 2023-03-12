package ru.gov.pfr.FNCIDictionaryes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

/**
 * Сопоставление формата xml с данными записанными в таблице контроля(PERSONS2)
 * ключи по xml формату, значения по контрольной таблице
 *
 * @author kneretin
 */
public class XMLToControllComparisonDictionary {

    private static File dictFile;
    private static XMLToControllComparisonDictionary instance;
    private static HashMap<String, String> map = new HashMap<>();

    private XMLToControllComparisonDictionary() {
        String here = new File(".").getAbsolutePath();
        File folder = new File(here.substring(0, here.indexOf(".")));
        dictFile = new File(folder.getAbsolutePath() + "\\dict\\doc\\xmlToControllComparison.csv");
    }

    private static void readDict(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
        String string;
        while ((string = reader.readLine()) != null) {
            if (string.contains(";")) {
                String[] vals = string.split(";");
                if (vals.length == 2) {
                    map.put(vals[0].trim(), vals[1].trim());
                } else {
                    map.put(vals[0].trim(), "");
                }
            }
        }
        reader.close();
    }

    public static XMLToControllComparisonDictionary getInstance() {
        if (instance == null) {
            XMLToControllComparisonDictionary dnd = new XMLToControllComparisonDictionary();
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
        LoggingService.writeLog("--XMLToControllComparisonDictionary--", "debug");
        for (String key : getInstance().map.keySet()) {
            LoggingService.writeLog("key: " + key + " - " + getInstance().map.get(key), "debug");
        }
    }

    public HashMap<String, String> getMap() {
        return map;
    }

}
