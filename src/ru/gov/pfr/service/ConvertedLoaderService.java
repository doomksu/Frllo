package ru.gov.pfr.service;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import static java.lang.Thread.sleep;
import ru.gov.pfr.service.convertedReaders.ConvertedReader;
import ru.gov.pfr.service.convertedReaders.ResultReader;

/**
 * Сервис загрузки сконвертированных данных. Запускает процессы чтения файлов в
 * папке загрузки, контролирует занятость программы
 *
 * @author kneretin
 */
public class ConvertedLoaderService {

    private static ConvertedLoaderService instance;
    public volatile byte status = 0;
    public volatile boolean doStop = false;
    private EnteryPoint enteryPoint;

    private ConvertedLoaderService() {
        status = 0;
    }

    public static ConvertedLoaderService getInstance() {
        if (instance == null) {
            instance = new ConvertedLoaderService();
        }
        return instance;
    }

    void setEnteryPoint(EnteryPoint ep) {
        this.enteryPoint = ep;
    }

    public void initiateBothLoading() throws InterruptedException {
        while (enteryPoint.isWorkInProgress()) {
            sleep(100);
        }
        ConvertedReader.getInstance().setController(enteryPoint.getMainController());
        Thread convertedReaderThread = new Thread(ConvertedReader.getInstance());
        convertedReaderThread.start();
        convertedReaderThread.join();

        ResultReader.getInstance().setController(enteryPoint.getMainController());
        Thread resultReaderThread = new Thread(ResultReader.getInstance());
        resultReaderThread.start();
        resultReaderThread.join();
    }

    public void initiateConvertedReader() throws InterruptedException {
        while (enteryPoint.isWorkInProgress() || ResultReader.getInstance().isWorkInProgress()) {
            LoggingService.writeLog(">> WorkInProgress on initiateConvertedReader ", "debug");
            sleep(100);
        }
        LoggingService.writeLog(">> start loading converted files", "debug");
        ConvertedReader.getInstance().setController(enteryPoint.getMainController());
        Thread convertedReaderThread = new Thread(ConvertedReader.getInstance());
        convertedReaderThread.start();

    }

    public void initiateResultsReader() throws InterruptedException {
        while (enteryPoint.isWorkInProgress() || ConvertedReader.getInstance().isWorkInProgress()) {
            LoggingService.writeLog(">> WorkInProgress on initiateResultsReader ", "debug");
            sleep(100);
        }
        LoggingService.writeLog(">> start loading result files", "debug");
        ResultReader.getInstance().setController(enteryPoint.getMainController());
        Thread resultReaderThread = new Thread(ResultReader.getInstance());
        resultReaderThread.start();
    }

    public synchronized void setStatus(byte newStatus) {
        status = newStatus;
    }

    public synchronized byte getStatus() {
        return status;
    }

}
