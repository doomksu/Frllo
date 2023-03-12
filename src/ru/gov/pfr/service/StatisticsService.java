package ru.gov.pfr.service;

import ru.gov.pfr.controller.MainWindowController;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import ru.gov.pfr.service.queryRecivers.StatisticsWriter;
import ru.gov.pfr.utils.ConverterStatistics;

public class StatisticsService {

    private static StatisticsService instance;
    private static String todayDateString;

    private StatisticsService() {

    }

    public static StatisticsService getInstance() {
        if (instance == null) {
            instance = new StatisticsService();
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDateTime now = LocalDateTime.now();
        todayDateString = now.format(dtf);
        return instance;
    }

    public String getTodayDateString() {
        return todayDateString;
    }

    public void updateStatistics(int statisticsIndex, ConverterStatistics statistics) throws Exception {
        ConnectionService.getInstance().updateStatistics(statisticsIndex,
                statistics.getWritedCount(),
                statistics.getSkipedDBCount(),
                statistics.getSkipedInvalidCount()
        );
    }

    public void writeStatistics(MainWindowController controller) {
        try {
            StatisticsWriter sw = new StatisticsWriter(controller);
            sw.fetchStatistics();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            controller.showErrorMessage(ex.getMessage());
        }
    }

    public static String countAvgTime(Instant fromInstant, long count) {
        return timeInDuration(Duration.between(fromInstant, Instant.now()).dividedBy(count));
    }

    public static String countRequestTime(Instant fromInstant) {
        return timeInDuration(Duration.between(fromInstant, Instant.now()));

    }

    private static String timeInDuration(Duration duration) {
        String time = "";
        if (duration.toHours() >= 1) {
            time = duration.toHours() + " ч. ";
            duration = duration.minusHours(duration.toHours());
        }
        if (duration.toMinutes() >= 1) {
            time += duration.toMinutes() + " мин. ";
            duration = duration.minusMinutes(duration.toMinutes());
        }
        if (duration.getSeconds() > 0) {
            time += duration.getSeconds() + " сек. ";
            duration = duration.minusSeconds(duration.getSeconds());
        }
        time += duration.toMillis() + " мсек. ";
        return time;
    }

}
