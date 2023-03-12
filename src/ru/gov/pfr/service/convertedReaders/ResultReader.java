package ru.gov.pfr.service.convertedReaders;

import ru.gov.pfr.controller.MainWindowController;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.utils.ResultBlock;
import ru.gov.pfr.utils.ResultMap;

public class ResultReader implements Runnable {

    protected File folder;
    protected ResultMap totalResultMap = new ResultMap();
    protected HashMap<String, FileWriter> writers;
    protected File listOfIdsFolder;
    protected boolean isMonitization = false;
    private static ResultReader instance;
    private boolean isWorkInProgress;
    private MainWindowController controller;
    private final int READERS_THREADS_MAX_COUNT = 100;
    private ExecutorService readersExecutorService;

    private ResultReader() {
        isWorkInProgress = false;
    }

    public static ResultReader getInstance() {
        if (instance == null) {
            instance = new ResultReader();
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
                writers = new HashMap<>();
                listOfIdsFolder = new File("resultErrors");
                listOfIdsFolder.mkdirs();
                readResultFilesLoop();
            } else {
                controller.showStatusInfo("! Другой процесс уже занят чтением протоколов");
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        } finally {
            isWorkInProgress = false;
        }
    }

    private void readResultFilesLoop() throws InterruptedException {
        folder = new File(SettingsService.getInstance().getValue("resultsPath"));
        if (!folder.isDirectory()) {
            LoggingService.writeLog("Ошибка пути к папке: " + folder.getAbsolutePath(), "error");
            controller.showStatusInfo("Ошибка пути к папке: " + folder.getAbsolutePath());
            return;
        }
        if (folder.listFiles().length == 0) {
            LoggingService.writeLog("Папка протоколов загрузки пуста:" + folder.getAbsolutePath(), "error");
            controller.showStatusInfo(LoggingService.getDateTimeString() + " Папка протоколов загрузки пуста: \r\n" + folder.getAbsolutePath());
            return;
        }
        String mes = "\r\n" + LoggingService.getDateTimeString() + " Чтение протоколов в: " + folder.getAbsolutePath() + " файлов: " + folder.listFiles().length;
        LoggingService.writeLog(mes, "debug");
        controller.showStatusInfo(mes);

        int filesPackCount = 0;
        folder = new File(SettingsService.getInstance().getValue("resultsPath"));
        ArrayList<File> filesGroup = new ArrayList<>();
        for (File resultFile : folder.listFiles()) {
            filesGroup.add(resultFile);
//            if (filesGroup.size() == READERS_THREADS_MAX_COUNT) {
            filesPackCount++;
            readResultFiles(filesGroup, filesPackCount);
            filesGroup.clear();
//            }
        }
        if (filesGroup.size() > 0) {
            filesPackCount++;
            readResultFiles(filesGroup, filesPackCount);
        }
        if (folder.listFiles().length > 0) {
            controller.showStatusInfo("В папке протоколов после загрузки остались файлы: " + folder.listFiles().length);
        }
    }

    private synchronized void readResultFiles(ArrayList<File> filesGroup, int groupIndex) throws InterruptedException {
        readersExecutorService = Executors.newFixedThreadPool(5);
        ArrayList<ResultFileReader> readers = new ArrayList<>();
        ArrayList<Future<Boolean>> futureReaders = new ArrayList<>();
        for (File resultFile : filesGroup) {
            String clearFileName = this.getFileNameFromFile(resultFile);
            if (clearFileName != null) {
                ResultFileReader rfr = new ResultFileReader(resultFile, clearFileName, controller);
                readers.add(rfr);
            }
        }
        if (!readers.isEmpty()) {
            String mes = "\r\n" + LoggingService.getDateTimeString() + " Запуск чтения пачки протоколов: " + filesGroup.size() + " шт., индекс  " + groupIndex;
            LoggingService.writeLog(mes, "debug");
            controller.showStatusInfo(mes);
        }
        for (ResultFileReader reader : readers) {
            futureReaders.add(readersExecutorService.submit(reader));
        }
        readersExecutorService.shutdown();
        boolean allDone = false;
        while (!allDone) {
            boolean maybe = true;
            for (Future<Boolean> reader : futureReaders) {
                if (!reader.isDone() && !reader.isCancelled()) {
                    if (reader.isCancelled()) {
                        LoggingService.writeLog(">> reader" + reader.hashCode() + " is cancelled", "debug");
                    }
                    Thread.sleep(10);
                    if (!reader.isDone() && !reader.isCancelled()) {
                        maybe = false;
                    }
                }
            }
            allDone = maybe;
        }
        String mes = "\r\n" + LoggingService.getDateTimeString() + " Завершено чтение пачки протоколов индекс: " + groupIndex;
        LoggingService.writeLog(mes, "debug");
        controller.showStatusInfo(mes);
    }

    private String getFileNameFromFile(File file) {
        if (file.getName().contains("result_") && file.getName().contains(".xml")) {
            String outFileName = file.getName().replace("result_", "");   //check result file prefix

            int yearIndex = outFileName.indexOf("__");
            int xmlIndex = outFileName.indexOf(".xml");
            int index = Integer.min(yearIndex, xmlIndex);
            outFileName = outFileName.substring(0, index) + ".xml";
            outFileName = outFileName.replace("_", "-");
            return outFileName;
        } else {
            LoggingService.writeLog("unknown file in result folder: " + file.getName(), "debug");
        }
        return null;
    }

    public void writePersonErrors(ResultBlock rb) throws Exception {
        String byCode = rb.getDescription();
        if (writers.containsKey(byCode) == false) {
            File list = new File(listOfIdsFolder.getAbsolutePath() + "\\" + byCode + ".csv");
            FileWriter writer = new FileWriter(list, true);
            if (list.exists()) {
                list.delete();
            }
            writers.put(byCode, writer);
        }
        writers.get(byCode).write(rb.getId() + "\r\n");
        writers.get(byCode).flush();
    }

    public synchronized boolean isWorkInProgress() {
        return isWorkInProgress;
    }

}
