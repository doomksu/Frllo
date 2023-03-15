package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.controller.MainWindowController;
import ru.gov.pfr.fbdpReader.utils.ConverterMessanger;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javafx.scene.control.Alert;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.personEntities.convertedXMLSource.PersonConvertedXMLSourceData;
import ru.gov.pfr.personEntities.fbdpSources.PersonDBFBDPSourceData;
import ru.gov.pfr.personEntities.fbdpSources.PersonFBDPSources;
import ru.gov.pfr.personEntities.fbdpSources.PersonFileSourceData;
import ru.gov.pfr.personEntities.frlloSources.PersonFRLLOSourceData;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.FRLLOSQLFactory;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_DATE_END_TEG;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_DATE_START_TEG;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_NPERS_TEG;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.StatisticsService;
import ru.gov.pfr.utils.ConverterStatistics;
import ru.gov.pfr.utils.LoadDatesTemplater;
import ru.gov.pfr.writer.PersonWriter;

public class FrlloConverter {

    protected PersonWriter writer;
    protected ErrorWriter errorWriter;
    private final MainWindowController controller;
    private final ConverterStatistics globalStatistics;       //статистика по массиву запросов одного запуска 
    private boolean hasErrors = false;
    private ConverterStatistics tempStatistics;             //статистика по последнему запросу

    private long avgRequestTime;
    private long fullCheckerTime = 0;
    private long requestStartTime;
    private long fullRequestTime;
    private ConverterMessanger messanger;
    private static final String LOG_PERIOD_QUERY = "LOG_PERIOD_QUERY";

    public FrlloConverter(MainWindowController controller) throws Exception {
        this.controller = controller;
        writer = new PersonWriter(this);
        errorWriter = new ErrorWriter();
        globalStatistics = new ConverterStatistics(this);
        tempStatistics = new ConverterStatistics(this);
        messanger = new ConverterMessanger(this, null);
    }

    public void parsePersonLine(String string) throws Exception {
        if (!string.contains("FA;IM;OT;RDAT") && !string.isEmpty()) {    //header
            PersonFBDPSources person = new PersonFileSourceData(string);
            try {
                workPerson(person);
            } catch (Exception ex) {
                LoggingService.writeLog("error while work line: " + string, "error");
                LoggingService.writeLog(ex);
                if (ex.getMessage().contains("ERRORCODE=-4470, SQLSTATE=08003")) {
                    throw ex;
                }
            }
            globalStatistics.addReadedCount();
            tempStatistics.addReadedCount();
        }
    }

    public void parsePersonDataMap(LinkedHashMap<String, String> map, boolean isFBDP) throws Exception {
        try {
            PersonDataSource person = null;
            if (isFBDP) {
                person = new PersonDBFBDPSourceData(map);
            } else {
                person = new PersonFRLLOSourceData(map);
            }
            globalStatistics.addReadedCount();
            tempStatistics.addReadedCount();
            workPerson(person);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            if (ex.getMessage().contains("ERRORCODE=-4470, SQLSTATE=08003")) {
                throw ex;
            }
        }
    }

    public void showCountInController() {
        messanger.showCurrentInfo();
    }

    public void packOutFile() throws Exception {
        if (writer != null) {
            if (writer.getCheckedFiles() != null) {
                if (writer.getCheckedFiles().isEmpty()) {
                    LoggingService.writeLog("no checked files", "debug");
                }
            } else {
                LoggingService.writeLog("writer checked files is null", "error");
            }
        } else {
            LoggingService.writeLog("writer is null", "error");
        }
    }

    public String getCountInfo() {
        double avgCheckerTime = 0;
        if (globalStatistics.getReadedPersonsCount() != 0) {
            avgCheckerTime = (fullCheckerTime / globalStatistics.getReadedPersonsCount());
        } else {
            avgCheckerTime = 0;
        }
        long endTime = System.currentTimeMillis();
        fullRequestTime = (endTime - requestStartTime) / 1000;
        String info
                = " Прочитано: \t" + globalStatistics.getReadedPersonsCount() + "\r\n"
                + " валидных записей: \t" + globalStatistics.getValidCount() + "\r\n"
                + " Записано: \t" + globalStatistics.getWritedCount() + "\r\n"
                + " Текущая пачка: \t" + writer.getCurrentPacakgeWrited() + "\r\n"
                + " Текущий файл: \t" + writer.getCurrentFileName() + "\r\n"
                + " не валидных записей: \t" + globalStatistics.getSkipedInvalidCount() + "\r\n"
                + " уже в БД: \t" + globalStatistics.getSkipedDBCount() + "\r\n"
                + " среднее время получения одной записи:\t" + avgRequestTime + " мсек \r\n"
                + " среднее время проверки одной записи: \t" + avgCheckerTime + " мсек \r\n"
                + " время выполнения запроса: \t" + fullRequestTime + " сек";
        return info;
    }

    public boolean isAllValid() {
        if (writer != null) {
            for (Boolean isValid : writer.getCheckedFiles().values()) {
                if (!isValid) {
                    return false;
                }
            }
        }
        return true;
    }

    public void startRequest(String dayFrom, String dayTo) {
        try {
            LoadDatesTemplater templater = new LoadDatesTemplater(dayFrom, dayTo);
            messanger = new ConverterMessanger(this, templater);
            String dateTemplate = "";
            ArrayList<File> queryFiles = getSelectedPeriodQueryes();
            int dayPart = 1;
            while ((dateTemplate = templater.getTemplate()) != null) {
                int part = 1;
                for (File sqlFile : queryFiles) {
                    String fileName = sqlFile.getName();
                    int statisticsIndex = ConnectionService.getInstance().createStatisticsLine(dateTemplate, dateTemplate);
                    String queryTemplate = ConnectionService.getInstance().queryFromFile(sqlFile);
                    setWriterKey(fileName, dateTemplate);
                    String query = queryTemplate.replace(DB_DATE_START_TEG, dateTemplate).replace(DB_DATE_END_TEG, dateTemplate);

                    LoggingService.writeLogIfDummy(LOG_PERIOD_QUERY, "sqlName: " + sqlFile.getName() + "\r\n" + query, "debug");
                    messanger.setCurrentDate(dateTemplate);
                    messanger.showQueryForDateStartExplanation(sqlFile.getName(), part, queryFiles.size());

                    ConnectionService.getInstance().makeRequestPart(this, query);
                    clearTempResults();
                    StatisticsService.getInstance().updateStatistics(statisticsIndex, getTempStatistics());
                    closeWriter();
                    ConnectionService.getInstance().reconnectFBDP();
                    messanger.showQueryForDateEndExplanation(part, queryFiles.size());
                    part++;
                }
                messanger.showDateEndExplanation(dayPart);
                dayPart++;
            }
            messanger.showPeriodEndExplanation();

        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            String errMess = "При запросе произошла ошибка ";
            controller.showMessage(errMess + " " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setWriterKey(String forFileName, String addition) {
        writer.setCurrentWriterKey(makeCurrentWriterKey(forFileName, addition));
    }

    private void workPerson(PersonDataSource person) throws Exception {
        if (person.isValid()) {
            globalStatistics.addValidCount();
            tempStatistics.addValidCount();
            PersonConvertedXMLSourceData pXML = new PersonConvertedXMLSourceData(person);
            boolean isNotAllreadyWrited = true;
            if (!SettingsService.getInstance().checkDummyFile("CLOSE_MONETIZATION")) {
                long startTime = 0;
                long endTime = 0;
                startTime = System.currentTimeMillis();
                FRLLOChangesChecker checker = makeChangesChecker(pXML, person);
                isNotAllreadyWrited = checker.isPersonChangedOrNew();
                endTime = System.currentTimeMillis();
                avgRequestTime = (endTime - startTime);
                fullCheckerTime += avgRequestTime / 1000;
            }
            if (isNotAllreadyWrited) {
                try {
                    makeDBUpdatesOnWritePerson(person);
                    writer.write(person);
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                }
                person = null;
            } else {
                globalStatistics.addSkipByDB();
                tempStatistics.addSkipByDB();
            }
        } else {
            globalStatistics.addSkipByError();
            tempStatistics.addSkipByError();
            hasErrors = true;
            errorWriter.addPerson(person);
        }
    }

    protected FRLLOChangesChecker makeChangesChecker(PersonConvertedXMLSourceData pXML, PersonDataSource person) throws Exception {
        return new FRLLOChangesChecker(pXML,
                ConnectionService.getInstance().selectConvertedPersonMap(person.getNPERS()));
    }

    protected void makeDBUpdatesOnWritePerson(PersonDataSource person) throws SQLException {
        HashMap<String, String> vals = new HashMap<>();
        vals.put(DB_NPERS_TEG, person.getNPERS());
        ConnectionService.getInstance().executeUpdateStandart(FRLLOSQLFactory.deleteFBDPerrorsStatment, vals);
        ConnectionService.getInstance().executeUpdateStandart(FRLLOSQLFactory.deleteFRLLOerrorsStatment, vals);
    }

    public String makeCurrentWriterKey(String forFileName, String addition) {
        String key = null;
        forFileName = forFileName.replace(".sql", "").replace("changes_", "").replace("npers_", "");
        key = forFileName + (addition == null ? "-" : "-" + addition + "-");
        return key;
    }

    public MainWindowController getController() {
        return controller;
    }

    public void close() throws Exception {
        writer.close();
        errorWriter.flushBuffer();
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public ConverterStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public ConverterStatistics getTempStatistics() {
        return tempStatistics;
    }

    public int getWritedCount() {
        return globalStatistics.getWritedCount();
    }

    public long getFullCheckerTime() {
        return fullCheckerTime;
    }

    public long getFullRequestTime() {
        return fullRequestTime;
    }

    public void setFullRequestTime(long fullRequestTime) {
        this.fullRequestTime = fullRequestTime;
    }

    public long getAvgRequestTime() {
        return avgRequestTime;
    }

    public void clearTempResults() {
        tempStatistics = new ConverterStatistics(this);
        writer.setTempStatistics(tempStatistics);
        writer.setGlobalStatistics(globalStatistics);
        writer.setTempStatistics(tempStatistics);
    }

    public void closeWriter() throws Exception {
        if (writer != null) {
            writer.closeWriters();
        }
        if (errorWriter != null) {
            errorWriter.flushBuffer();
        }
    }

    private ArrayList<File> getSelectedPeriodQueryes() {
        ArrayList<File> sqlFiles = new ArrayList<>();
        switch (SettingsService.getInstance().getValue("writeFilter")) {
            case "writeAll":
                sqlFiles.add(new File(SettingsService.getInstance().getSQLPath() + "\\" + "changes_open.sql"));
                sqlFiles.add(new File(SettingsService.getInstance().getSQLPath() + "\\" + "changes_closed.sql"));
                break;
            case "writeOpen":
                sqlFiles.add(new File(SettingsService.getInstance().getSQLPath() + "\\" + "changes_open.sql"));
                break;
            case "writeClosed":
                sqlFiles.add(new File(SettingsService.getInstance().getSQLPath() + "\\" + "changes_closed.sql"));
                break;
        }
        if (sqlFiles.isEmpty()) {
            LoggingService.writeLog("no queryes selected for filter: " + SettingsService.getInstance().getValue("writeFilter"), "error");
        }
        return sqlFiles;
    }

    public String getCurrentPacakgeWrited() {
        return String.valueOf(writer.getCurrentPacakgeWrited());
    }

    public String getCurrentFileName() {
        return writer.getCurrentFileName();
    }

}
