package ru.gov.pfr.service.convertedReaders;

import ru.gov.pfr.controller.MainWindowController;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;

public class ConvertedReader implements Runnable {

    private boolean isWorkInProgress;
    protected MainWindowController controller;
    private static ConvertedReader instance;
    protected File folder;

    private ConvertedReader() {
        isWorkInProgress = false;
    }

    public static ConvertedReader getInstance() {
        if (instance == null) {
            instance = new ConvertedReader();
        }
        return instance;
    }

    public void setController(MainWindowController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            if (!isWorkInProgress) {
                isWorkInProgress = true;
                readConvertedFiles();
            } else {
                controller.showStatusInfo("! Другой процесс уже занят чтением конвертации");
            }
        } catch (InterruptedException ex) {
            LoggingService.writeLog(ex);
        } finally {
            isWorkInProgress = false;
        }
    }

    private synchronized void readConvertedFiles() throws InterruptedException {
        String path = SettingsService.getInstance().getValue("loadedPath");
        folder = new File(path);
        if (!folder.isDirectory()) {
            LoggingService.writeLog("Ошибка пути к папке: " + folder.getAbsolutePath(), "error");
            controller.showStatusInfo("Ошибка пути к папке: " + folder.getAbsolutePath());
            return;
        }
        if (folder.listFiles().length == 0) {
            LoggingService.writeLog("Папка сконвертированных файлов пуста:" + folder.getAbsolutePath(), "error");
            controller.showStatusInfo(LoggingService.getDateTimeString() + " Папка сконвертированных файлов пуста: \r\n" + folder.getAbsolutePath());
            return;
        }
        int size = folder.listFiles().length;
        ExecutorService es = Executors.newFixedThreadPool(5);
        ArrayList<ConvertedFileReader> readers = new ArrayList<>();
        ArrayList<Future<Boolean>> futureReaders = new ArrayList<>();

        for (File convertedFile : folder.listFiles()) {
            ConvertedFileReader cfr = new ConvertedFileReader(convertedFile, controller);
            readers.add(cfr);
        }
        if (!readers.isEmpty()) {
            String mes = "\r\n" + LoggingService.getDateTimeString() + " Чтение файлов в : " + folder.getAbsolutePath() + "  файлов: " + readers.size();
            LoggingService.writeLog(mes, "debug");
            controller.showStatusInfo(mes);

            for (ConvertedFileReader reader : readers) {
                try {
                    futureReaders.add(es.submit(reader));
                } catch (Exception ex) {
                    LoggingService.writeLog("Ошибка при чтении файла: " + reader.getFile().getName(), "error");
                    LoggingService.writeLog(ex);
                }
            }

            for (ConvertedFileReader reader : readers) {
                try {
                    reader.checkFile();
                } catch (Exception ex) {
                    LoggingService.writeLog("Cant check file id: " + reader.getFile().getName(), "error");
                }
            }

            es.shutdown();
            boolean allDone = false;
            while (!allDone) {
                boolean maybe = true;
                for (Future<Boolean> reader : futureReaders) {
                    if (!reader.isDone() && !reader.isCancelled()) {
                        if (reader.isCancelled()) {
                            LoggingService.writeLog(">> reader" + reader.hashCode() + " is cancelled", "debug");
                        }
                        Thread.sleep(100);
                        if (!reader.isDone() && !reader.isCancelled()) {
                            maybe = false;
                        }
                    }
                }
                allDone = maybe;
            }
        }
        String mes = "\r\n" + LoggingService.getDateTimeString() + " Завершено чтение переданных файлов в : " + folder.getAbsolutePath();
        LoggingService.writeLog(mes, "debug");
        controller.showStatusInfo(mes);
        String countMessage = "";
        int totalyReadedCount = 0;
        for (ConvertedFileReader futureReader : readers) {
            totalyReadedCount += futureReader.getReadedCount();
            countMessage += futureReader.getStatusMessage() + "\r\n";
        }
        controller.showCount("Прочтено записей " + totalyReadedCount, countMessage);
    }

    public synchronized boolean isWorkInProgress() {
        return isWorkInProgress;
    }

}
