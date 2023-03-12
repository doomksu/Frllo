package ru.gov.pfr.FNCIDictionaryes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

/**
 * NVP - KL.KSIVA FNSI - 1.2.643.5.1.13.13.99.2.48
 *
 * @author kneretin
 */
public class DocumentNamesDictionary {

    private static File dictFile;
    private static DocumentNamesDictionary instance;
    private static HashMap<String, String> map = new HashMap<>();

    private DocumentNamesDictionary() {
        String here = new File(".").getAbsolutePath();
        File folder = new File(here.substring(0, here.indexOf(".")));
        dictFile = new File(folder.getAbsolutePath() + "\\dict\\doc\\document.csv");
    }

    private static void readDict(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1251"));
        String string;
        while ((string = reader.readLine()) != null) {
            if (string.contains(";")) {
                String[] vals = string.split(";");
                map.put(vals[0].trim(), vals[1].trim());
            }
        }
        reader.close();
    }

    public static DocumentNamesDictionary getInstance() {
        if (instance == null) {
            DocumentNamesDictionary dnd = new DocumentNamesDictionary();
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
        if (getInstance().map.get(key) == null) {

            return null;
//            LoggingService.writeLog(">>no value for " + key, "debug");
//            print();
        }
        return getInstance().map.get(key);
    }

    public static void print() {
        LoggingService.writeLog("--DocumentNamesDictionary--", "debug");
        for (String key : getInstance().map.keySet()) {
            LoggingService.writeLog(getInstance().map.get(key) + "\t by key: " + key  , "debug");
        }
    }
}
