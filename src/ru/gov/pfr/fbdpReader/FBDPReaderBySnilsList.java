package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.StatisticsService;

public class FBDPReaderBySnilsList extends FRLLOSourceReader implements Runnable {

    protected File listFile;
    protected int CHUNK_SIZE = 500;
    protected ArrayList<String> lists;

    private static final String LOG_QUERY = "LOG_QUERY";

    public FBDPReaderBySnilsList(EnteryPoint ep) throws Exception {
        super(ep);
        lists = new ArrayList<>();
    }

    public FBDPReaderBySnilsList(EnteryPoint ep, File file, boolean byId, boolean onlyMonetization) throws Exception {
        super(ep);
        listFile = file;
        lists = new ArrayList<>();
    }

    @Override
    protected void read() throws Exception {
        LoggingService.writeLog("read() by FBDPBaseReader: " + listFile.getAbsolutePath(), "debug");
        enteryPoint.getMainController().showStatusInfo("Запуск запроса по списку СНИЛС");
        readSnilsList();
        try {
            runPersonsListThroghQueryes();
            converter.closeWriter();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            enteryPoint.getMainController().showErrorMessage(ex.getMessage());
        }
        isDone = true;
    }

    protected synchronized void runPersonsListThroghQueryes() {
        Instant fullStartTime = Instant.now();
        int executedQueryes = 0;
        int partNumber = 0;
        String homePath = Paths.get("").toAbsolutePath().toString();
        if (lists == null || lists.isEmpty()) {
            LoggingService.writeLog(">> runPersonsListThroghQueryes lists is"
                    + (lists == null ? " null " : (lists.isEmpty() ? " empty " : " error ")), "debug");
            return;
        }
        for (String list : lists) {
            partNumber++;
            for (String sqlName : getListOfSnilsQueryes()) {
                try {
                    String pathsql = homePath + "\\sql\\" + sqlName;
                    File queryFile = new File(pathsql);
                    if (queryFile.exists()) {
                        String query = ConnectionService.getInstance().queryFromFile(queryFile);
                        query = query.replace("<MAN_NPERS>", list);
                        String mes = "Запуск запроса: " + sqlName + " список " + partNumber + " из " + lists.size() + " запрос " + executedQueryes
                                + "\r\n время: " + LoggingService.getDateTimeString() + "\r\n";
                        converter.getController().showStatusInfo(mes);
                        converter.setWriterKey(sqlName, null);
                        Instant startTime = Instant.now();

                        LoggingService.writeLogIfDummy(LOG_QUERY, "runPersonsListThroghQueryes query: +\r\n" + query, "debug");
                        ConnectionService.getInstance().makeRequestPart(converter, query);
                        executedQueryes++;
                        mes = "Запрос завершен - список " + partNumber + " из " + lists.size() + " запрос " + executedQueryes
                                + "\r\n время: " + LoggingService.getDateTimeString()
                                + "\r\n завершено за " + StatisticsService.countRequestTime(startTime)
                                + "\r\n прочитано по части: " + converter.getTempStatistics().getReadedPersonsCount()
                                + "\r\n записано по части: " + converter.getTempStatistics().getWritedCount()
                                + "\r\n невалидных по части: " + converter.getTempStatistics().getSkipedInvalidCount()
                                + "\r\n уже в БД по части: " + converter.getTempStatistics().getSkipedDBCount() + "\r\n";
                        converter.getController().showStatusInfo(mes);
                    } else {
                        throw new Exception("Не найден скрипт: " + pathsql);
                    }
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                }
            }
        }
        String mes = "Завершен запрос по списку СНИЛС из: " + lists.size() + " списков "
                + "\r\n выполнено запросов: " + executedQueryes
                + "\r\n завершено за " + StatisticsService.countRequestTime(fullStartTime)
                + "\r\n прочитано всего: " + converter.getGlobalStatistics().getReadedPersonsCount()
                + "\r\n записано всего: " + converter.getGlobalStatistics().getWritedCount()
                + "\r\n невалидных всего: " + converter.getGlobalStatistics().getSkipedInvalidCount()
                + "\r\n уже в БД всего: " + converter.getGlobalStatistics().getSkipedDBCount() + "\r\n";
        LoggingService.writeLog(mes, "debug");
        converter.getController().showStatusInfo(mes);
        lists.clear();
    }

    private ArrayList<String> getListOfSnilsQueryes() {
        ArrayList<String> sqlNames = new ArrayList<>();
        if (SettingsService.getInstance().checkDummyFile("CLOSE_MONETIZATION")) {
            sqlNames.add("fetch_last.sql");
            LoggingService.writeLog("only query for close monetization: fetch_last.sql", "error");
            return sqlNames;
        }
        switch (SettingsService.getInstance().getValue("writeFilter")) {
            case "writeAll":
                sqlNames.add("npers_open.sql");
                sqlNames.add("npers_closed.sql");
                break;
            case "writeOpen":
                sqlNames.add("npers_open.sql");
                break;
            case "writeClosed":
                sqlNames.add("npers_closed.sql");
                break;
        }
        if (sqlNames.isEmpty()) {
            LoggingService.writeLog("no queryes selected for filter: " + SettingsService.getInstance().getValue("writeFilter"), "error");
        }
        return sqlNames;
    }

    protected void readSnilsList() throws Exception {
        StringBuilder comb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(listFile), "cp1251"));
        String string;
        boolean first = true;
        lists.clear();
        int inCurrent = 0;
        int inTotal = 0;
        while ((string = reader.readLine()) != null) {
            String val = string.trim();
            val = getSnilsFromString(val);
            if (val != null) {
                if (!enteryPoint.isSnilsWriten(val)) {
                    if (!first) {
                        comb.append(",'" + val.trim() + "'");
                    } else {
                        comb.append("'" + val.trim() + "'");
                    }
                    first = false;
                    inCurrent++;
                    inTotal++;
                    if (inCurrent == CHUNK_SIZE) {
                        lists.add(comb.toString());
                        first = true;
                        comb = new StringBuilder();
                        inCurrent = 0;
                    }
                }
            }
        }
        if (comb.length() > 0) {
            lists.add(comb.toString());
        }
        LoggingService.writeLog("will request by: " + lists.size() + " parts, total: " + inTotal, "debug");
    }

    private String getSnilsFromString(String string) {
        for (String part : string.split(";")) {
            if (part.trim().matches("\\d{3}-\\d{3}-\\d{3} \\d{2}")) {
                return part;
            }
        }
        return null;
    }
}
