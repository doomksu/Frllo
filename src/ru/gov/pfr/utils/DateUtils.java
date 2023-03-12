package ru.gov.pfr.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public final static String datePattern = "yyyy-MM-dd";
    public final static String dateTimePattern = "yyyy-MM-dd'T'hh:mm:ssXXX";

    public static String thresholdMinDateOrIncomingDate(LocalDate threshold, LocalDate incomingDate) {
        if (incomingDate.isBefore(threshold)) {
            return threshold.format(DateTimeFormatter.ofPattern(datePattern));
        }
        return incomingDate.format(DateTimeFormatter.ofPattern(datePattern));
    }

    public static String thresholdMinDateOrIncomingDate(String threshold, String incoming) {
        if (incoming.isEmpty()) {
            return incoming;
        }
        LocalDate thresholdDate = LocalDate.parse(threshold, DateTimeFormatter.ofPattern(datePattern));
        LocalDate incomingDate = LocalDate.parse(incoming, DateTimeFormatter.ofPattern(datePattern));
        if (incomingDate.isBefore(thresholdDate)) {
            return threshold;
        }
        return incoming;
    }

    public static String getEndOfYearDateString() {
        LocalDateTime now = LocalDateTime.now();
        now = now.withMonth(12);
        now = now.withDayOfMonth(31);
        return now.format(DateTimeFormatter.ofPattern(DateUtils.datePattern));
    }

    public static String textDateFormatToConvertedFormat(String textDate) {
        return null;
    }
}
