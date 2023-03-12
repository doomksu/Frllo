package ru.gov.pfr.fbdpReader.utils;

import ru.gov.pfr.controller.MainWindowController;
import ru.gov.pfr.fbdpReader.FrlloConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.StatisticsService;
import ru.gov.pfr.utils.LoadDatesTemplater;

public class ConverterMessanger {

    private String currentDate = "";
    private LoadDatesTemplater templater;
    private MainWindowController controller;
    private FrlloConverter converter;
    private Instant fullStartInstant;
    private Instant partStartInstant;

    public ConverterMessanger(FrlloConverter converter, LoadDatesTemplater templater) {
        this.converter = converter;
        this.controller = converter.getController();
        this.templater = templater;
        fullStartInstant = Instant.now();
        partStartInstant = Instant.now();
    }

    public void showQueryForDateStartExplanation(String sqlName, int number, int parts) {
        partStartInstant = Instant.now();
        String mes = LoggingService.getDateTimeString() + "  Запуск запроса: " + sqlName + " с " + templater.getStartDateString()
                + " по " + templater.getEndDateString()
                + "\r\nзапрос на дату: " + currentDate
                + "\r\nчасть " + number + " из " + parts + "\r\n";
        controller.showStatusInfo(mes);
    }

    public void showQueryForDateEndExplanation(int number, int parts) {
        String mes = LoggingService.getDateTimeString() + " выполнение скрипта завершено -  часть " + number + " из " + parts + " на дату: " + currentDate
                + "\r\nвремя выполнения " + StatisticsService.countRequestTime(partStartInstant)
                + "  прочитано / записано : " + converter.getTempStatistics().getReadedPersonsCount()
                + " / "
                + converter.getTempStatistics().getWritedCount()
                + "\r\nневалидных : " + converter.getTempStatistics().getSkipedInvalidCount()
                + "   уже в БД : " + converter.getTempStatistics().getSkipedDBCount() + "\r\n";
        controller.showStatusInfo(mes);
    }

    public void showPeriodEndExplanation() {
        String mes = LoggingService.getDateTimeString() + "  Завершен запрос по датам с " + templater.getStartDateString()
                + " по " + templater.getEndDateString()
                + "\r\nвремя выполнения " + StatisticsService.countRequestTime(fullStartInstant)
                + "  прочитано / записано всего : " + converter.getGlobalStatistics().getReadedPersonsCount()
                + " / "
                + converter.getGlobalStatistics().getWritedCount()
                + "\r\nневалидных всего : " + converter.getGlobalStatistics().getSkipedInvalidCount()
                + "   уже в БД всего : " + converter.getGlobalStatistics().getSkipedDBCount() + "\r\n";
        controller.showStatusInfo(mes);
    }

    public void showDateEndExplanation(int number) {
        String mes = LoggingService.getDateTimeString() + "  Завершены запросы на дату  " + templater.getStartDateString()
                + "\r\nвремя выполнения " + StatisticsService.countRequestTime(fullStartInstant)
                + "\r\nпрочитано / записано: " + converter.getTempStatistics().getReadedPersonsCount()
                + " / "
                + converter.getTempStatistics().getWritedCount()
                + "\r\nневалидных : " + converter.getTempStatistics().getSkipedInvalidCount()
                + "   уже в БД : " + converter.getTempStatistics().getSkipedDBCount()
                + "\r\nдата : " + number + "  из  " + templater.getSize()
                + "\r\n";
        controller.showStatusInfo(mes);
    }

    public void showCurrentInfo() {
        double avgCheckerTime = 0;
        if (converter.getGlobalStatistics().getReadedPersonsCount() != 0) {
            avgCheckerTime = (converter.getFullCheckerTime() / converter.getGlobalStatistics().getReadedPersonsCount());
        } else {
            avgCheckerTime = 0;
        }
        int count = converter.getGlobalStatistics().getReadedPersonsCount();
        String info
                = " Прочитано: \t" + makeCountString(count) + "\r\n"
                + " валидных записей: \t" + converter.getGlobalStatistics().getValidCount() + "\r\n"
                + " Записано: \t" + converter.getGlobalStatistics().getWritedCount() + "\r\n"
                + " Текущая пачка: \t" + converter.getCurrentPacakgeWrited() + "\r\n"
                + " Текущий файл: \t" + converter.getCurrentFileName() + "\r\n"
                + " не валидных записей: \t" + converter.getGlobalStatistics().getSkipedInvalidCount() + "\r\n"
                + " уже в БД: \t" + converter.getGlobalStatistics().getSkipedDBCount() + "\r\n"
                + " среднее время получения одной записи:\t" + converter.getAvgRequestTime() + " мсек \r\n"
                + " среднее время проверки одной записи: \t" + avgCheckerTime + " мсек \r\n"
                + " время выполнения запроса: \t" + StatisticsService.countRequestTime(fullStartInstant);
        controller.showCount(makeCountString(count), info);
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String makeCountString(int count) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(dtf);
        String countText = "";
        String source = String.valueOf(count);
        while (source.length() > 3) {
            countText = source.substring(source.length() - 3, source.length()) + " " + countText;
            source = source.substring(0, source.length() - 3);
        }
        if (source.length() > 0) {
            countText = source + " " + countText;
        }
        countText = countText + "  " + dateTime;
        return countText;
    }

    public void showPreparationMessages(ArrayList<String> queryes) {
        String mes = LoggingService.getDateTimeString() + " Подготовка к запуску скриптов: \r\n";
        for (String query : queryes) {
            mes += query + "\r\n";
        }
        controller.showStatusInfo(mes);
    }

    public void showExplanationMessage(String mes) {
        controller.showStatusInfo(mes);
    }

}
