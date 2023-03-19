package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import ru.gov.pfr.fbdpReader.exceptions.ShowMessageAndWaitException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;

public class FBDPBaseReader extends FRLLOSourceReader implements Runnable {

    protected boolean isRequestByDate = false;
    protected int statisticsIndex = 1;
    protected boolean reRunForDate = false;
    private String dayFromValue;
    private String dayToValue;

    public FBDPBaseReader(EnteryPoint ep, LocalDate start, LocalDate end) throws Exception {
        super(ep);
        this.start = start;
        this.end = end;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyy-MM-dd");
        dayFromValue = start.format(dtf);
        dayToValue = end.format(dtf);
        isRequestByDate = true;
    }

    public FBDPBaseReader(EnteryPoint ep) throws Exception {    //yesterday - today  Scheduler 
        super(ep);
        this.start = LocalDate.now().minusDays(1);
        this.end = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyy-MM-dd");
        dayFromValue = start.format(dtf);
        dayToValue = end.format(dtf);
    }

    @Override
    protected void read() throws Exception {
        try {
            boolean isStatisticsExistsForDate = ConnectionService.getInstance().hasStatisticsMaxIDForDate(dayToValue);
            boolean isAllowedRereadForDate = Boolean.valueOf(SettingsService.getInstance().getValue("allowRecheckDateWithStatistics"));
            if (!isStatisticsExistsForDate || (isStatisticsExistsForDate && isAllowedRereadForDate)) {
                if (isStatisticsExistsForDate && isAllowedRereadForDate) {
                    String mes = "Статистика на даты: " + dayFromValue + " - " + dayToValue + " уже записана, но выгружаем принудительно";
                    LoggingService.writeLog(mes, "debug");
                    enteryPoint.getMainController().showStatusInfo(mes);
                }
                proceedWithRead();
            } else {
                enteryPoint.getMainController().showStatiticsExistsDialog(dayToValue, this);
            }
        } catch (ShowMessageAndWaitException ex) {
            LoggingService.writeLog("Cant check statistics: " + ex.getMessage(), "error");
            String mes = "Нет соединения с БД  - переподключаем и пробуем повторить запрос: " + LoggingService.getDateTimeString();
            enteryPoint.getMainController().showStatusInfo(mes);
            throw ex;
        }
    }

    public void proceedWithRead() throws Exception {
        enteryPoint.getMainController().showStatusInfo("Запуск запроса по датам: "
                + dayFromValue + " - " + dayToValue
                + (isRequestByDate ? "  запрос на даты" : " запрос по планировщику"));
        if (converter == null) {
            converter = new FrlloConverter(controller);
        }
        if (converter != null) {
            converter.startRequest(dayFromValue, dayToValue);
            isDone = true;
            LoggingService.writeLog("close converter - flush buffers", "debug");
            converter.close();
            if (isDone) {
                if (converter.isAllValid()) {
                    converter.packOutFile();
                }
            } else {
                LoggingService.writeLog("reader is not done after read process complete", "error");
            }
            enteryPoint.setIsWorkInProgress(false);
        } else {
            LoggingService.writeLog("converter is null;", "error");
        }
    }

}
