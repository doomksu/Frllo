package service;

import ru.gov.pfr.service.LoggingService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import ru.gov.pfr.utils.XMLValues;

public class ResultFileCounter implements Callable<Boolean> {

    private HashMap<String, HashSet<String>> uniqueIdsMap;
    private HashMap<String, Integer> byFile;
    private HashSet<File> files;
    private String packName;
    private int count = 0;
    private int uniqueCount = 0;

    public ResultFileCounter(String fileName, HashSet<File> files) {
        uniqueIdsMap = new HashMap<>();
        byFile = new HashMap<>();
        this.files = files;
        packName = fileName;
    }

    @Override
    public Boolean call() {
        LoggingService.writeLog("read pack of files: " + packName + " files: " + files.size(), "debug");
        int filesCount = 0;
        for (File resultFile : files) {
            uniqueIdsMap.put(resultFile.getName(), new HashSet<>());
            try {
//                LoggingService.writeLog("read result: " + resultFile.getName(), "debug");
                String string;
                byFile.put(resultFile.getName(), 0);
                int byFileCount = 0;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), "utf8"));
                while ((string = reader.readLine()) != null) {
                    if (string.contains("<document_id>")) {
                        count++;
                        byFileCount++;
                        if (XMLValues.getValue(string).isEmpty() == false) {
                            uniqueIdsMap.get(resultFile.getName()).add(XMLValues.getValue(string));
                        }
                    }
                }
                byFile.put(resultFile.getName(), byFileCount);
                reader.close();
                filesCount++;
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }

        getTotalUnique();
        LoggingService.writeLog("read total by pack: " + packName + " files: " + filesCount + " persons: " + count, "debug");
        for (String string : uniqueIdsMap.keySet()) {
            LoggingService.writeLog("       byFile: " + string + " : " + byFile.get(string)
                    + " uniqueByFile: " + uniqueIdsMap.get(string).size(), "debug");
        }
        uniqueIdsMap = null;
        return true;
    }

    public int getCount() {
        return count;
    }

    public HashMap<String, HashSet<String>> getUniqueIdsMap() {
        return uniqueIdsMap;
    }

    public HashMap<String, Integer> getByFile() {
        return byFile;
    }

    public String getPackName() {
        return packName;
    }

    private void getTotalUnique() {
        int total = 0;
        for (HashSet<String> value : uniqueIdsMap.values()) {
            total += value.size();
        }
        uniqueCount = total;
    }

    public int getUniqueCount() {
        return uniqueCount;
    }
    

    public int getTotalResults() {
        int total = 0;
        for (Integer value : byFile.values()) {
            total += value;
        }
        return total;
    }

}
