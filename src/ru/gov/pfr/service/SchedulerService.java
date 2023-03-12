package ru.gov.pfr.service;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import ru.gov.pfr.fbdpReader.exceptions.ShowMessageAndWaitException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SchedulerService implements Runnable {

    private boolean doBreak = false;
    private EnteryPoint enteryPoint;
    private String lastStartDateTime;

    private static final String START_TIME_PATTERN = "\\d{1,2}:\\d{1,2}";
    private static final String SLEEP_INTERVAL_PATTERN = "^\\d{1,2}$";
    private final int DEFAULT_START_HOUR = 23;
    private final int DEFAULT_START_MINUTES = 00;
    private final int DEFAULT_SLEEP_MINUTES = 20;

    public SchedulerService(EnteryPoint ep) {
        enteryPoint = ep;
    }

    public void initiateYesterdayConvertation() throws ShowMessageAndWaitException, Exception {

        lastStartDateTime = getCurrentDateTimeString();
        String yesterday = getYesterdayDateString();
        String message = "";
        if (isAtWorkTime()) {
                ConvertedLoaderService.getInstance().initiateBothLoading();
        }
        if (!ConnectionService.getInstance().hasStatisticsMaxIDForDate(yesterday)) {
            LoggingService.writeLog("no statistics for today: " + yesterday, "debug");
            if (isAtWorkTime()) {
                enteryPoint.startYesterdayConversation();
            } else {
                message = "ожидание времени запроса - "
                        + getStartHour()
                        + " часов "
                        + getStartMinutes()
                        + " минут "
                        + lastStartDateTime;
                enteryPoint.getMainController().showLastRunLabel(lastStartDateTime);
                enteryPoint.getMainController().showStatusInfo(message);
            }
        } else {
            message = "проверка: данные за " + yesterday + " уже получены: " + getCurrentDateTimeString();
            enteryPoint.getMainController().showLastRunLabel(lastStartDateTime);
            enteryPoint.getMainController().showStatusInfo(message);
        }
    }

    @Override
    public void run() {
        enteryPoint.getMainController().showStatusInfo("планировщик запущен: " + getCurrentDateTimeString());
        while (true) {
            if (doBreak) {
                LoggingService.writeLog("stop schedule loop", "debug");
                break;
            }
            lastStartDateTime = getCurrentDateTimeString();
//            String yesterday = getYesterdayDateString();
            String message = "";
            try {
                if (!enteryPoint.isWorkInProgress()) {
                    initiateYesterdayConvertation();
                } else {
                    message = "проверка: программа занята: " + lastStartDateTime;
                }
            } catch (ShowMessageAndWaitException ex) {
                try {
                    LoggingService.writeLog("Cant check statistics: " + ex.getMessage(), "error");
                    String mes = "Нет соединения с БД  - переподключаем и пробуем повторить запрос: " + LoggingService.getDateTimeString();
                    enteryPoint.getMainController().showStatusInfo(mes);
                    ConnectionService.getInstance().reconnectFRLLO();
                    ConnectionService.getInstance().reconnectFBDP();
                    Thread.sleep(getSleepPeriod() * 1000 * 60);
                } catch (InterruptedException ex1) {
                    LoggingService.writeLog(ex);
                } catch (SQLException ex1) {
                    LoggingService.writeLog("Ошибка при переподключении к БД", "error");
                    LoggingService.writeLog(ex);
                }
            } catch (Exception ex) {
                LoggingService.writeLog("Cant check statistics", "error");
                LoggingService.writeLog(ex);
            }
            enteryPoint.getMainController().showLastRunLabel(lastStartDateTime);
            enteryPoint.getMainController().showStatusInfo(message);
            try {
                Thread.sleep(getSleepPeriod() * 1000 * 60);
            } catch (InterruptedException ex) {
                LoggingService.writeLog(ex);
            }
        }
        LoggingService.writeLog("out of scheduled loop", "debug");
    }

    private int getSleepPeriod() {
        int intervalInt = DEFAULT_SLEEP_MINUTES;
        String interval = SettingsService.getInstance().getValue("schedulerSleepInterval");
        try {
            intervalInt = Integer.parseInt(interval);
        } catch (NumberFormatException ex) {
            LoggingService.writeLog(ex);
            intervalInt = DEFAULT_SLEEP_MINUTES;
        }
        return intervalInt;
    }

    /**
     * Проверить время для выгрузки - либо с 9 вечера \ либо с 12 ночи текущей
     * даты
     *
     * @return
     */
    private boolean isAtWorkTime() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() >= getStartHour() && now.getMinute() >= getStartMinutes()) {
            return true;
        }
        return false;
    }

    private String getCurrentDateTimeString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return now.format(dtf);
    }

    private String getYesterdayDateString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        return yesterday.format(dtf);
    }

    public void stopLooping() {
        doBreak = true;
        enteryPoint.setIsWorkInProgress(false);
        enteryPoint.getMainController().showStatusInfo("планировщик остановлен: " + lastStartDateTime);
        LoggingService.writeLog("планировщик остановлен: " + lastStartDateTime, "debug");
    }

    public static boolean isStartTimePatternValid(String value) {
        if (value.matches(START_TIME_PATTERN)) {
            String[] parts = value.split(":");
            if (parts.length != 2) {
                return false;
            }
            try {
                int valH = Integer.parseInt(parts[0]);
                int valM = Integer.parseInt(parts[1]);

                if (valH < 0 || valH > 23) {
                    return false;
                }
                if (valM < 0 || valM > 59) {
                    return false;
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    public static boolean isSleepIntervalPatternValid(String value) {
        if (value.matches(SLEEP_INTERVAL_PATTERN)) {
            try {
                int val = Integer.parseInt(value);
                if (val <= 0 || val > 60) {
                    return false;
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    private int getStartHour() {
        String val = SettingsService.getInstance().getValue("schedulerStartTime");
        String[] parts = val.split(":");
        try {
            int hVal = Integer.parseInt(parts[0]);
            return hVal;
        } catch (Exception ex) {

        }
        return DEFAULT_START_HOUR;
    }

    private int getStartMinutes() {
        String val = SettingsService.getInstance().getValue("schedulerStartTime");
        String[] parts = val.split(":");
        try {
            int mVal = Integer.parseInt(parts[1]);
            return mVal;
        } catch (Exception ex) {

        }
        return DEFAULT_START_MINUTES;
    }

}
