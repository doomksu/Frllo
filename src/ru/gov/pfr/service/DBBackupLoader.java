package ru.gov.pfr.service;

import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javafx.scene.control.TextArea;

public class DBBackupLoader implements Runnable {

    private File file;
    private final TextArea textArea;
    private MainWindowController controller;
    private int fileType = -1;
    public static final int ERROR_CODE = 0;
    public static final int RESULT_ERROR = 1;
    public static final int PERSONS = 2;
    public static final int LOAD_FILES = 3;
    private ArrayList<String> batch;
    private int BUFFER_SIZE = 5;

    public DBBackupLoader(File file, TextArea tA, int fileType) {
        this.file = file;
        this.textArea = tA;
        this.fileType = fileType;
        batch = new ArrayList<>();
        try {
            int buffer = Integer.parseInt(SettingsService.getInstance().getValue("BUFFER_SIZE"));
            BUFFER_SIZE = buffer;
        } catch (Exception ex) {
            LoggingService.writeLog("cant parse number fo buffer size: "
                    + SettingsService.getInstance().getValue("BUFFER_SIZE"), "error");
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1251"));
            String string;
            int num = 0;
            double avgTime = 0;
            long startTime = 0;
            long endTime = 0;
            while ((string = reader.readLine()) != null) {
                string = string.replace("\"", "");
                string = string.replace(";", ",");
                if (!string.isEmpty()) {
                    batch.add(insertString(string));
                }
                if (batch.size() == BUFFER_SIZE) {
                    startTime = System.currentTimeMillis();
                    ConnectionService.getInstance().executeInsertUpdateQuery(batch);
                    LoggingService.writeLog("string: " + string, "debug");
                    endTime = System.currentTimeMillis();
                    if (batch.size() != 0) {
                        avgTime = (endTime - startTime) / batch.size();
                        String mes = LoggingService.getDateTimeString() + " Прочитано из: " + file.getName() + "  "
                                + num + " cреднее время: "
                                + avgTime + " msec время пакета: " + (endTime - startTime) + "msec пакеты по: " + BUFFER_SIZE;
                        LoggingService.writeLog(mes, "debug");
                        controller.showStatusInfo(mes);
                    }
                    batch.clear();
                }
                num++;
            }
            if (batch.size() > 0) {
                ConnectionService.getInstance().executeInsertUpdateQuery(batch);
                batch.clear();
            }
            reader.close();
            controller.showStatusInfo("Файл завершен: " + file.getName());
            file.delete();
            LoggingService.writeLog("file done: " + file.getAbsolutePath(), "debug");
            if (!file.delete()) {
                controller.showStatusInfo("Файл завершен: " + file.getName());
                LoggingService.writeLog("cant delete file: " + file.getAbsolutePath(), "error");
            } else {
                LoggingService.writeLog("delete: " + file.getAbsolutePath(), "debug");
                controller.showStatusInfo("Файл удален: " + file.getName());
            }

            String mes = LoggingService.getDateTimeString() + " Прочитано из файла: " + num;
            controller.showStatusInfo(mes);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

    protected void showRunInfo(String mess) {
        if (textArea != null) {
            textArea.setText(LoggingService.getDateTimeString() + " " + mess);
        }
    }

    public void setController(MainWindowController controller) {
        this.controller = controller;
    }

    private String insertString(String string) {
        String query = "";
        try {
            switch (this.fileType) {
//                case ERROR_CODE:
//                    query = ConnectionService.getInstance().insertErrorCodeFromLine(string);
//                    break;
//                case LOAD_FILES:
//                    query = ConnectionService.getInstance().insertLoadedFileFromLine(string);
//                    break;
                case PERSONS:
                    query = ConnectionService.getInstance().getInsertPersonFromLineQuery(string);
                    break;
//                case RESULT_ERROR:
//                    query = ConnectionService.getInstance().insertResultErrorFromLine(string);
//                    break;
                default:
                    LoggingService.writeLog(">> no file type seted", "debug");
                    break;
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
        return query;
    }

}
