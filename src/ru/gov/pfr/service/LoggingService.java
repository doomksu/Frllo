package ru.gov.pfr.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

public class LoggingService {

    private static volatile LoggingService instance;
    private FileWriter logWriter;
    private File logFile;
    private boolean isAddTimer;
    private boolean isAddType;
    private HashSet<String> allowedLogTypes;
    private ArrayList<String> buffer;
    private boolean useBuffer;

    /**
     * Загружаем сервис логирования. По умолчанию файл дописывается и режимы
     * записи устанавливаются в default
     */
    private LoggingService() {
        System.out.println("reload LoggingService");
        String path = "frllo_log.txt";
        logFile = new File(path);
        isAddTimer = true;
        isAddType = true;
        try {
            logWriter = createLogWriter(logFile);
        } catch (IOException ex) {
            redirectLogFile();
            LoggingService.writeLog("ERROR: LoggingService cant write log");
        }
        //Включаем некоторые опции логов поумолчанию, затем установим их из настроек
        allowedLogTypes = new HashSet<>();
        allowedLogTypes.add("default");
        allowedLogTypes.add("error");
        allowedLogTypes.add("debug");
        allowedLogTypes.add("test");
        buffer = new ArrayList<>();
        useBuffer = true;
    }

    private FileWriter createLogWriter(File logFile) throws IOException {
        return new FileWriter(logFile, false);
    }

    private void redirectLogFile() {
        File log = new File("frllo_log.txt");
        try {
            PrintStream pslog = new PrintStream(log);
            if (!log.exists()) {
                log.createNewFile();
            }
            System.setOut(pslog);
            System.setErr(pslog);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);   //если лог не перенаправлен выводит в консоль
        }
    }

    public static LoggingService getInstance() {
        if (instance == null) {
            instance = new LoggingService();
        }
        return instance;
    }

    public void closeLogs() {
        try {
            logWriter.close();
        } catch (IOException ex) {
            redirectLogFile();
            LoggingService.writeLog("ERROR: Cant close log file");
        }
    }

    public static void writeLog(String message) {
        writeLog(message, "default");
    }

    /**
     * Запись StackTrace Exception
     *
     * @param Exception ex
     */
    public static void writeLog(Throwable ex) {
        Throwable printEx = ex;
        String exception = "";
        if (printEx.toString().isEmpty()) {
            exception += printEx.getMessage();
        } else {
            exception += printEx.toString();
        }
        String stackTrace = "\r\n";
        for (StackTraceElement ste : printEx.getStackTrace()) {
            stackTrace += "\t" + ste.toString() + "\r\n";
        }
        writeLog(exception + stackTrace, "error");
        if (printEx.getCause() != null) {
            writeLog(printEx.getCause());
        }
    }

    public static void writeLogNoTypeNoTime(String message) {
        writeLog(message + "\r\n", "default", false);
    }

    public static void writeLogIfDummy(String dummyFileName, String message, String type) {
        if (SettingsService.getInstance().checkDummyFile(dummyFileName)) {
            writeLog(message, "default", true);
        }
    }

    public static void writeLog(String message, String type) {
        writeLog(message, "default", true);
    }

    public static void writeLog(String message, String type, boolean writeTypeAndTime) {
        try {
            if (writeTypeAndTime) {
                if (getInstance().allowedLogTypes.contains(type)) {
                    String header = getDateTimeString() + (instance.isAddType ? ":" + type + ":\t" : "");
                    String line = header + message + "\r\n";
                    getInstance().logWriter.write(line);
                    if (getInstance().useBuffer) {
                        getInstance().buffer.add(line);
                    }
                    getInstance().logWriter.flush();
                }
            } else {
                getInstance().logWriter.write(message);
                if (getInstance().useBuffer) {
                    getInstance().buffer.add(message);
                }
                getInstance().logWriter.flush();
            }
        } catch (IOException ex) {
            LoggingService.writeLog(getDateTimeString() + " : ERROR while write to log: " + message + "  type: " + type);
            LoggingService.writeLog(ex);
        }
    }

    public static String getDateTimeString() {
        if (instance.isAddTimer) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            return now.format(dtf);
        }
        return "";
    }

    public void recreateLog() throws IOException {
        if (logFile.exists()) {
            if (logWriter != null) {
                logWriter.close();
            }
            logFile.delete();
            instance = new LoggingService();
        }
    }

    public void switchTimer(boolean isOn) {
        isAddTimer = isOn;
    }

    public void switchLogType(boolean b) {
        isAddType = b;
    }

}
