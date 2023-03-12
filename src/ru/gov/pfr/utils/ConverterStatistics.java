package ru.gov.pfr.utils;

import ru.gov.pfr.fbdpReader.FrlloConverter;

public class ConverterStatistics {

    private int validCount;
    private int readedPersonsCount;
    private int skipedInvalidCount;
    private int skipedDBCount;
    private int writedCount;
    private int countToShowStatistics;
    private FrlloConverter converter;

    public ConverterStatistics(FrlloConverter converter) {
        this.converter = converter;
        skipedInvalidCount = 0;
        skipedDBCount = 0;
        validCount = 0;
        readedPersonsCount = 0;
        writedCount = 0;
        countToShowStatistics = 0;
    }

    public void addReadedCount() {
        readedPersonsCount++;
        countToShowStatistics++;
        if (countToShowStatistics == 100) {
            countToShowStatistics = 0;
            converter.showCountInController();
        }
    }

    public void addValidCount() {
        validCount++;
    }

    public void addSkipByDB() {
        skipedDBCount++;
    }

    public void addSkipByError() {
        skipedInvalidCount++;
    }

    public void addWritedCount() {
        writedCount++;
    }

    public int getValidCount() {
        return validCount;
    }

    public int getReadedPersonsCount() {
        return readedPersonsCount;
    }

    public int getSkipedInvalidCount() {
        return skipedInvalidCount;
    }

    public int getSkipedDBCount() {
        return skipedDBCount;
    }

    public int getWritedCount() {
        return writedCount;
    }

}
