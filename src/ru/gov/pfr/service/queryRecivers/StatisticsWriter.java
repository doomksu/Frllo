package ru.gov.pfr.service.queryRecivers;

import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;

public class StatisticsWriter implements QueryReciver {

    private MainWindowController controller;
    private BufferedWriter writer;
    private final File statisticsFile;

    private String[] lgotaStatisticsFieldsMap = {"TOTAL", "TOTAL_RESULT", "NOT_LOADED", "GOOD", "ERROR"};
    private String HEADER = "Всего записей; Всего результатов получено; не загружено; Загружено; Ошибка\r\n";

    private String[] lgotaFileStatisticsFieldsMap = {"FILEID", "STATID", "FILENAME", "TOTAL", "TOTAL_RESULT", "GOOD", "NOT_LOADED", "TWINS", "ALL_POSITIVE", "ERROR"};
    private String FILE_HEADER = "IDF; ID; Файл; всего записей; всего результатов; загружено; не загружено; двойники; всего положительных реультатов; ошибки\r\n";

    private String[] monetizationFileStatisticsFieldsMap = {"FILEID", "STATID", "FILENAME", "TOTAL", "TOTAL_RESULT", "GOOD", "NOT_LOADED", "ERROR"};
    private String MONETIZATION_FILE_HEADER = "IDF; ID; Файл; всего записей; всего результатов; загружено; не загружено; ошибки\r\n";

    private String[] fileStatisticsFieldsMap = {"ID", "FILENAME", "CREATION_DATE"};
    private String FILES_HEADER = "ID; Имя файла; дата\r\n";

    public StatisticsWriter(MainWindowController controller) throws Exception {
        this.controller = controller;
        makeMessage("Запуск статистики: " + LoggingService.getDateTimeString());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(dtf);
        statisticsFile = new File("статистика загрузки " + dateTime + ".csv");
        if (statisticsFile.isFile()) {
            statisticsFile.delete();
        }
        statisticsFile.createNewFile();
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(statisticsFile), "Cp1251"));
    }

    @Override
    public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
        writer.write(parseString(resultMap, fieldsMap));
        writer.flush();
    }

    private String parseString(LinkedHashMap<String, String> resultMap, String[] fieldsMap) {
        String statString = "";
        for (String key : fieldsMap) {
            if (resultMap.containsKey(key)) {
                statString += resultMap.get(key) + ";";
            } else {
                statString += ";";
            }
        }
        return statString + "\r\n";
    }

    @Override
    public void close() throws Exception {
        writer.flush();
        writer.close();
    }

    public void fetchStatistics() throws Exception {
        StatisticsWriter sw = this;
        new Thread(
                new Runnable() {
            @Override
            public void run() {
                try {
                    File statisticsSQLfile = new File("sql\\load_statistics.sql");
                    String query = ConnectionService.getInstance().queryFromFile(statisticsSQLfile);
                    writer.write(HEADER);
                    writer.flush();
                    makeMessage("запуск скрипта: " + statisticsSQLfile.getAbsolutePath() + "\t" + LoggingService.getDateTimeString());
                    ConnectionService.getInstance().fetchData(query, sw, lgotaStatisticsFieldsMap);
                    writer.flush();
                    //
                    writer.write("\r\n");
                    writer.write("по файлам\r\n");
                    writer.write(FILE_HEADER);
                    statisticsSQLfile = new File("sql\\file_statistics.sql");
                    makeMessage("запуск скрипта: " + statisticsSQLfile.getAbsolutePath() + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(statisticsSQLfile);
                    ConnectionService.getInstance().fetchData(query, sw, lgotaFileStatisticsFieldsMap);
                    writer.flush();
                    //
                    writer.write("\r\n");
                    writer.write("по файлам монетизации\r\n");
                    writer.write(MONETIZATION_FILE_HEADER);
                    statisticsSQLfile = new File("sql\\file_monetization_statistics.sql");
                    makeMessage("запуск скрипта: " + statisticsSQLfile.getAbsolutePath() + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(statisticsSQLfile);
                    ConnectionService.getInstance().fetchData(query, sw, monetizationFileStatisticsFieldsMap);
                    writer.flush();
                    //
                    writer.write("\r\nФайлы льготников");
                    statisticsSQLfile = new File("sql\\files_data.sql");
                    writer.write(FILES_HEADER);
                    makeMessage("запуск скрипта: " + statisticsSQLfile.getAbsolutePath() + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(statisticsSQLfile);
                    ConnectionService.getInstance().fetchData(query, sw, fileStatisticsFieldsMap);
                    writer.flush();
                    //
                    writer.write("\r\nФайлы монетизации");
                    statisticsSQLfile = new File("sql\\monetization_files_data.sql");
                    writer.write(FILES_HEADER);
                    makeMessage("запуск скрипта: " + statisticsSQLfile.getAbsolutePath() + "\t" + LoggingService.getDateTimeString());
                    query = ConnectionService.getInstance().queryFromFile(statisticsSQLfile);
                    ConnectionService.getInstance().fetchData(query, sw, fileStatisticsFieldsMap);
                    writer.flush();
                    //
                    close();
                    makeMessage("сбор статистики завершен: " + sw.getStatisticsFile().getAbsolutePath() + "\t" + LoggingService.getDateTimeString());
                } catch (Exception ex) {
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

    public MainWindowController getController() {
        return controller;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public File getStatisticsFile() {
        return statisticsFile;
    }
}
