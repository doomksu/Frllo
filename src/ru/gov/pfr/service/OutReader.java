package ru.gov.pfr.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import ru.gov.pfr.utils.ResultMap;
import ru.gov.pfr.utils.XMLValues;

public class OutReader {

    private File outFolder;
    private final File listOfIdsFolder;
    private HashMap<String, BufferedWriter> writers;
    private HashMap<String, ResultMap> resultMap;

    public OutReader(File folder, HashMap<String, ResultMap> resultMap) {
        outFolder = folder;
        this.resultMap = resultMap;
        listOfIdsFolder = new File("idsList");
        listOfIdsFolder.mkdir();
        writers = new HashMap<>();
    }

    public void readAndSaveSNILS(File outFile, ResultMap map) throws Exception {
        String string;
        boolean write = false;
        String byCode = null;
        String foundedPerson = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile), "utf8"));
        int writedByFile = 0;
        int documentsTotal = 0;
        while ((string = reader.readLine()) != null) {
            if (write) {
                if (string.contains("<snils>")) {
                    foundedPerson = formatSnils(XMLValues.getValue(string));
                }
            }
            if (string.contains("</document>")) {
                if (write == true) {
                    if (writers.containsKey(byCode) == false) {
                        File list = new File(listOfIdsFolder.getAbsolutePath() + "\\" + byCode + ".csv");
                        if (list.exists()) {
                            list.delete();
                        }
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list), "Cp1251"));
                        writers.put(byCode, writer);
                    }
                    writers.get(byCode).write(foundedPerson + "\r\n");
                    writers.get(byCode).flush();
                    writedByFile++;
                }
                write = false;
                documentsTotal++;
            }
            if (string.contains("<document_id>")) {
                byCode = map.containPersonInCode(XMLValues.getValue(string));
                if (byCode != null) {
                    write = true;
                }
            }
        }
        LoggingService.writeLog("out files error: " + outFile.getName() + "; " + writedByFile + "; totalPers: " + documentsTotal, "debug");
        reader.close();
    }

    private String formatSnils(String value) {
        if (value.length() == 11) {
            String res = value.substring(0, 3)
                    + "-"
                    + value.substring(3, 6)
                    + "-"
                    + value.substring(6, 9)
                    + " "
                    + value.substring(9);
            return res;
        } else {
            LoggingService.writeLog("wrongSnils: " + value, "debug");
        }
        return value;
    }
}
