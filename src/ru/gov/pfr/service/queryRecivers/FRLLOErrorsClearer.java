package ru.gov.pfr.service.queryRecivers;

import ru.gov.pfr.controller.MainWindowController;
import java.io.File;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.FRLLOSQLFactory;
import ru.gov.pfr.service.LoggingService;

public class FRLLOErrorsClearer {

    private MainWindowController controller;

    public FRLLOErrorsClearer(MainWindowController controller) {
        this.controller = controller;
    }

    public void clear() {
        makeMessage("Запуск очистки таблиц ошибок");
        new Thread(
                new Runnable() {
            @Override
            public void run() {
                String query = "";
                File queryFile = new File("sql\\clear_fbdp_errors.sql");
                try {
                    makeMessage("запуск скрипта: " + queryFile + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(queryFile);
                    query = query.replaceAll(FRLLOSQLFactory.DB_SCHEMA_TEG, FRLLOSQLFactory.DB_SCHEMA_NAME);
                    ConnectionService.getInstance().executeUpdate(query);
                    makeMessage("Запрос выполнен");
                } catch (Exception ex) {
                    LoggingService.writeLog("query error \r\n" + query, "error");
                    makeMessage("ошибка запроса " + queryFile.getAbsolutePath());
                    LoggingService.writeLog(ex);
                }
                queryFile = new File("sql\\clear_frllo_errors.sql");
                try {
                    makeMessage("запуск скрипта: " + queryFile + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(queryFile);
                    query = query.replaceAll(FRLLOSQLFactory.DB_SCHEMA_TEG, FRLLOSQLFactory.DB_SCHEMA_NAME);
                    ConnectionService.getInstance().executeUpdate(query);
                    makeMessage("Запрос выполнен");
                } catch (Exception ex) {
                    LoggingService.writeLog("query error \r\n" + query, "error");
                    makeMessage("ошибка запроса " + queryFile.getAbsolutePath());
                    LoggingService.writeLog(ex);
                }
                queryFile = new File("sql\\clear_double_records.sql");
                try {
                    makeMessage("запуск скрипта: " + queryFile + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(queryFile);
                    query = query.replaceAll(FRLLOSQLFactory.DB_SCHEMA_TEG, FRLLOSQLFactory.DB_SCHEMA_NAME);
                    ConnectionService.getInstance().executeUpdate(query);
                    makeMessage("Запрос выполнен");
                } catch (Exception ex) {
                    LoggingService.writeLog("query error \r\n" + query, "error");
                    makeMessage("ошибка запроса " + queryFile.getAbsolutePath());
                    LoggingService.writeLog(ex);
                }
            }
        }).start();
    }

    
     private void makeMessage(String string) {
        if (controller != null) {
            controller.showStatusInfo(string);
        }
        LoggingService.writeLog(string, "debug");
    }
}
