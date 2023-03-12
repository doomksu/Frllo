package ru.gov.pfr.service.convertedReaders;

import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.StatisticsService;
import ru.gov.pfr.service.queryRecivers.FileSelector;
import ru.gov.pfr.utils.ResultBlock;
import ru.gov.pfr.utils.ResultMap;
import ru.gov.pfr.utils.XMLValues;

public class ResultFileReader implements Callable<Boolean> {

    private File resultFile;
    private String outFilesName;
    private ResultMap resultMap;
    private HashMap<String, Integer> outFilesErrorCount;
    private HashMap<String, Integer> outFilesDoubleCount;
    private HashMap<String, Integer> outFilesOKCount;
    private int loadedFileID = -1;
    private int okCount;
    private int errorsCount;
    private int doubleCount;
    private int currentNum = 0;
    private int BUFFER_SIZE = 100;
    private boolean isMonetization = false;
    private MainWindowController controller;
    private Instant readTime;
    private FileSelector fileSelector;

    public ResultFileReader(File resultFile, String clearFileName, MainWindowController controller) {
        this.resultFile = resultFile;
        outFilesName = clearFileName;
        this.controller = controller;
        LoggingService.writeLog("create ResultFileReader for: " + clearFileName, "debug");
        if (clearFileName.contains("monitization-") || clearFileName.contains("monitization_")) {
            isMonetization = true;
        }
        try {
            int buffer = Integer.parseInt(SettingsService.getInstance().getValue("BUFFER_SIZE"));
            BUFFER_SIZE = buffer;
        } catch (Exception ex) {
            LoggingService.writeLog("cant parse number fo buffer size: "
                    + SettingsService.getInstance().getValue("BUFFER_SIZE"), "error");
        }
        resultMap = new ResultMap();
    }

    @Override
    public Boolean call() throws Exception {
        try {
            outFilesErrorCount = new HashMap<>();
            outFilesDoubleCount = new HashMap<>();
            outFilesOKCount = new HashMap<>();
            fileSelector = new FileSelector(outFilesName, isMonetization);
            try {
                ConnectionService.getInstance().fetchData(fileSelector.selectIDByNameQuery(), fileSelector, null);
                loadedFileID = fileSelector.getFileID();
                if (loadedFileID > 0) {
                    readTime = Instant.now();
                    readResultFile();
                } else {
                    String status = LoggingService.getDateTimeString() + " Не установлен ID для файла: \r\n" + resultFile.getName();
                    controller.showStatusInfo(status);
                }
            } catch (Exception ex) {
                LoggingService.writeLog("error while execute: " + fileSelector.selectIDByNameQuery(), "error ");
                LoggingService.writeLog(ex);
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            return false;
        }
        return true;
    }

    private void readResultFile() throws Exception {
        ResultBlock rb = null;
        String string;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), "utf8"));
        int resultErrorsCount = 0;
        doubleCount = 0;
        okCount = 0;
        if (!outFilesErrorCount.containsKey(outFilesName)) {
            outFilesErrorCount.put(outFilesName, 0);
        }
        if (!outFilesDoubleCount.containsKey(outFilesName)) {
            outFilesDoubleCount.put(outFilesName, 0);
        }
        if (!outFilesOKCount.containsKey(outFilesName)) {
            outFilesOKCount.put(outFilesName, 0);
        }
        ArrayList<String> queryes = new ArrayList<>();
        controller.showStatusInfo(LoggingService.getDateTimeString() + " Протокол ID: " + loadedFileID + " - " + resultFile.getName());
        while ((string = reader.readLine()) != null) {
            if (string.contains("<document_id>")) {
                savePersonResult(rb, queryes);
                rb = new ResultBlock(); //create new one
                rb.setDocumentID(XMLValues.getValue(string));
                currentNum++;
                if (queryes.size() == BUFFER_SIZE) {
                    ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
                    controller.showStatusInfo(LoggingService.getDateTimeString() + getStatusMessage());
                    queryes.clear();
                }
            }

            if (rb != null) {
                if (string.contains("<status>")) {
                    rb.setStatus(XMLValues.getValue(string));
                    if (rb.isDoubleStatus()) {
                        doubleCount++;
                    }
                }
                if (string.contains("<code>")) {
                    rb.setCode(XMLValues.getValue(string));
                }
                if (string.contains("<description>")) {
                    rb.setDescription(XMLValues.getValue(string));
                    if (rb.isErrorStatus()) {
                        savePersonErrors(rb, resultFile, queryes);
                    }
                }
            }
        }
        if (rb != null) {
            savePersonResult(rb, queryes);
        }
        if (queryes.isEmpty() == false) {
            ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
            queryes.clear();
        }
        controller.showStatusInfo(LoggingService.getDateTimeString() + getStatusMessage());
        outFilesErrorCount.put(outFilesName, outFilesErrorCount.get(outFilesName) + resultErrorsCount);
        outFilesDoubleCount.put(outFilesName, outFilesDoubleCount.get(outFilesName) + doubleCount);
        outFilesOKCount.put(outFilesName, outFilesOKCount.get(outFilesName) + okCount);
        reader.close();
        this.resultFile.delete();
    }

    private String getStatusMessage() {
        return "   Чтение файла ID: " + loadedFileID + " прочтено: " + currentNum
                + "\r\n " + resultFile.getName()
                + "\r\n OK: " + okCount + "; Дублей: " + doubleCount + "; Ошибок: " + errorsCount
                + "\r\n на одну запись: " + StatisticsService.countAvgTime(readTime, currentNum) + " ; чтение: " + StatisticsService.countRequestTime(readTime)
                + "\r\n";
    }

    protected void savePersonResult(ResultBlock rb, ArrayList<String> queryes) {
        if (rb != null) {
            if (rb.isOkStatus()) {
                resultMap.addLoadedPerson(rb.getId());
                okCount++;
            } else {
                if (rb.isErrorStatus()) {
                    resultMap.addNotLoadedPerson(rb.getDescription(), rb.getId());
                }
                if (rb.isDoubleStatus()) {
                    resultMap.addDoubleLoadedPerson(rb.getId());
                }
            }
            try {
                if (ConnectionService.getInstance().getConnectionFRLLO() != null) {
                    queryes.add(ConnectionService.getInstance().getUpdatePersonResultsStatment(
                            rb.getId(),
                            resultMap.isPersonLoaded(rb.getId()),
                            String.valueOf(loadedFileID),
                            isMonetization)
                    );
                }
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }
    }

    protected void savePersonErrors(ResultBlock rb, File resultFile, ArrayList<String> queryes) throws SQLException {
        if (rb != null) {
            if (rb.isErrorStatus()) {
                errorsCount++;
                queryes.add(ConnectionService.getInstance().getInsertErrorLineStatment(
                        rb,
                        String.valueOf(loadedFileID),
                        resultFile.getName()));
            }
        }
    }

}
