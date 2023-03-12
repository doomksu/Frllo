package ru.gov.pfr.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoadDatesTemplater {

    private int currentIndex = 0;
    private int size;
    private String fPart;
    private String sPart;
    private String tPart;
    private LocalDate startDate;
    private String startDateString;
    private LocalDate endDate;
    private String endDateString;
    private LocalDate currentTemplateDate;

    public LoadDatesTemplater(String dateFrom, String dateTo) throws Exception {
        startDateString = dateFrom;
        endDateString = dateTo;
        startDate = createDateFromString(dateFrom);
        endDate = createDateFromString(dateTo);
        size = 0;
        if (startDate.isEqual(endDate)) {
            size = 1;
        } else {
            LocalDate tempStart = startDate;
            while (tempStart.isBefore(endDate)) {
                size++;
                tempStart = tempStart.plusDays(1);
            }
        }
        currentTemplateDate = startDate;
    }

    public String getTemplate() {
        if (currentIndex == 0) {
            currentIndex++;
            return currentTemplateDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (currentIndex < size) {
            currentIndex++;
            currentTemplateDate = currentTemplateDate.plusDays(1);
            return currentTemplateDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }

    private LocalDate createDateFromString(String dateString) {
        String[] sParts = dateString.split("-");
        LocalDate ld = LocalDate.of(
                Integer.parseInt(sParts[0]),
                Integer.parseInt(sParts[1]),
                Integer.parseInt(sParts[2]));
        return ld;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getSize() {
        return size;
    }

    public String formatTemplate(String val) {
        if (val.length() <= 3) {
            return val;
        } else {
            fPart = val.substring(0, 3);
            if (val.length() <= 6) {
                return fPart + "-" + val.substring(3, val.length());
            } else {
                sPart = val.substring(3, 6);
                if (val.length() <= 9) {
                    return fPart + "-" + sPart + "-" + val.substring(6, val.length());
                } else {
                    tPart = val.substring(6, 9);
                    return fPart + "-" + sPart + "-" + tPart + " " + val.substring(9, val.length());
                }
            }
        }
    }

    public String getStartDateString() {
        return startDateString;
    }

    public String getEndDateString() {
        return endDateString;
    }

}
