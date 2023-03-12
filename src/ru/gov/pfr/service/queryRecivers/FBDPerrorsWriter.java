package ru.gov.pfr.service.queryRecivers;

import ru.gov.pfr.FNCIDictionaryes.RegionsDictionary;
import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.FRLLOSQLFactory;
import ru.gov.pfr.service.LoggingService;

public class FBDPerrorsWriter implements QueryReciver {

    private MainWindowController controller;
    private HashMap<String, BufferedWriter> writers;
    private final File folder;

    public FBDPerrorsWriter(MainWindowController controller) {
        this.controller = controller;
        folder = new File("Ошибки ФБДП по регионам");
        folder.mkdir();
        writers = new HashMap<>();
        LoggingService.writeLog(">>FBDPerrorsWriter", "debug");
    }

    @Override
    public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
        String region = RegionsDictionary.getRegionNameByCode(resultMap.get("REGION"));
        if (!writers.containsKey(region)) {
            File regionErr = new File(folder.getAbsolutePath() + "\\" + region);
            try {
                BufferedWriter regionWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(regionErr), "Cp1251"));
                writers.put(region, regionWriter);
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }
        BufferedWriter writer = writers.get(region);

        writer.write(makePersonErrorString(resultMap));
        writer.flush();
    }

    public void writeErrors() {
        makeMessage("Запуск сбора ошибок ФБДП по регионам");
        FBDPerrorsWriter eWriter = this;
        new Thread(
                new Runnable() {
            @Override
            public void run() {
                String query = "";
                File queryFile = new File("sql\\fbdp_errors.sql");
                try {
                    makeMessage("запуск скрипта: " + queryFile + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(queryFile);
                    query = query.replaceAll(FRLLOSQLFactory.DB_SCHEMA_TEG, FRLLOSQLFactory.DB_SCHEMA_NAME);
                    ConnectionService.getInstance().fetchData(query, eWriter, new String[1]);
                    makeMessage("Запрос выполнен - закрытие файлов");
                    for (BufferedWriter writer : writers.values()) {
                        writer.flush();
                        writer.close();
                    }
                } catch (Exception ex) {
                    LoggingService.writeLog("query error \r\n" + query, "error");
                    makeMessage("ошибка запроса " + queryFile.getAbsolutePath());
                    LoggingService.writeLog(ex);
                }
            }
        }).start();
    }

    @Override
    public void close() throws Exception {

    }

    private String makePersonErrorString(LinkedHashMap<String, String> resultMap) {
        String line = resultMap.get("NPERS") + ";" + resultMap.get("REASON") + "\r\n";
        return line;
    }

    private void makeMessage(String string) {
        if (controller != null) {
            controller.showStatusInfo(string);
        }
        LoggingService.writeLog(string, "debug");
    }

}
