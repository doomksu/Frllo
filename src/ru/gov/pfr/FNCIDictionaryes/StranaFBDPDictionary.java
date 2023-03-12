package ru.gov.pfr.FNCIDictionaryes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

/**
 * Ассоциация аббревиатуры имени страны к коду страны. Ключи - аббревиатуры в
 * латинице, значения коды для xml формата
 *
 * @author kneretin
 */
public class StranaFBDPDictionary {

    private static File dictFile;
    private static StranaFBDPDictionary instance;
    private static HashMap<String, String> map = new HashMap<>();

    private StranaFBDPDictionary() {
        String here = new File(".").getAbsolutePath();
        File folder = new File(here.substring(0, here.indexOf(".")));
        dictFile = new File(folder.getAbsolutePath() + "\\dict\\doc\\source\\strana_kod_kodm.csv");
    }

    private static void readDict(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String string;
        while ((string = reader.readLine()) != null) {
            if (string.contains(";")) {
                String[] vals = string.split(";");
                map.put(vals[0].trim(), vals[1].trim());
            }
        }
        reader.close();
    }

    public static StranaFBDPDictionary getInstance() {
        if (instance == null) {
            StranaFBDPDictionary dnd = new StranaFBDPDictionary();
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
        LoggingService.writeLog("--StranaFBDPDictionary--", "debug");
        for (String key : getInstance().map.keySet()) {
            LoggingService.writeLog("key: " + key + " - " + getInstance().map.get(key), "debug");
        }
    }

    public HashMap<String, String> getMap() {
        return map;
    }

}
