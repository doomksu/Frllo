package ru.gov.pfr.FNCIDictionaryes;

import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author kneretin
 */
public class SexDictionary {

    private static HashMap<String, String> map = new HashMap<>();
    private static SexDictionary instance;

    private SexDictionary() {
        map = new HashMap<>();
        map.put("М", "1");
        map.put("Ж", "2");
    }

    public static SexDictionary getInstance() {
        if (instance == null) {
            instance = new SexDictionary();
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
        LoggingService.writeLog("sexDict size: " + getInstance().instance.map.size(), "debug");
        for (String string : getInstance().map.keySet()) {
            LoggingService.writeLog("sexDict: " + string + " val: " + getInstance().map.get(string), "debug");
        }
    }
}
