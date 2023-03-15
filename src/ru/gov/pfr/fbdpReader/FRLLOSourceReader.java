package ru.gov.pfr.fbdpReader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ru.gov.pfr.controller.MainWindowController;
import ru.gov.pfr.enteryPoint.EnteryPoint;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;

public abstract class FRLLOSourceReader implements Runnable {

    protected EnteryPoint enteryPoint;
    protected MainWindowController controller;
    protected FrlloConverter converter;
    protected boolean isCanceled;
    protected boolean isDone;

    protected boolean isByDates = false;
    protected LocalDate start;
    protected LocalDate end;
    private final String FRLLO_CONNECTION_IS_CLOSED = "Соединение с контрольной БД ФРЛЛО закрыто!";
    private final String FRLLO_CONNECTION_ERROR = "Ошибка соединения с контрольной БД ФРЛЛО: ";
    private final String FBDP_CONNECTION_IS_CLOSED = "Соединение с БД ФБДП закрыто!";
    private final String FBDP_CONNECTION_ERROR = "Ошибка соединения с БД ФБДП: ";
    private final String COMMON_ERRORS_LIST = "Возникли ошибки при запуске чтения: ";

    public FRLLOSourceReader(EnteryPoint ep) throws Exception {
        enteryPoint = ep;
        controller = enteryPoint.getMainController();
        converter = new FrlloConverter(controller);
        isCanceled = false;
        isDone = false;
    }

    protected void closeSource() {
        ConnectionService.getInstance().setCanceled(true);
    }

    /**
     * Определить закрытые соединения и вернуть текст ошибок
     *
     * @return
     * @throws Exception
     */
    protected List<String> getSourcesError() throws Exception {
        ArrayList<String> errors = new ArrayList<>();
        try {
            if (ConnectionService.getInstance().getConnectionFRLLO() != null) {
                if (ConnectionService.getInstance().getConnectionFRLLO().isClosed()) {
                    errors.add(FRLLO_CONNECTION_IS_CLOSED);
                }
            }
        } catch (Exception ex) {
            errors.add(FRLLO_CONNECTION_ERROR + ex.getMessage());
        }
        try {
            if (ConnectionService.getInstance().getConnectionFBDP() != null) {
                if (ConnectionService.getInstance().getConnectionFBDP().isClosed()) {
                    enteryPoint.getMainController().showStatusInfo(FBDP_CONNECTION_IS_CLOSED);
                    errors.add(FBDP_CONNECTION_IS_CLOSED);
                }
            }
        } catch (Exception ex) {
            errors.add(FBDP_CONNECTION_ERROR + ex.getMessage());
        }
        return errors;
    }

    /**
     * Запуск процесса чтения из источника FRLLO
     */
    protected abstract void read() throws Exception;

    /**
     * Старт потока обработки
     */
    @Override
    public void run() {
        try {
            if (ConnectionService.getInstance().getConnectionFRLLO() != null) {
                List<String> errors = getSourcesError();
                if (errors.isEmpty()) {
                    enteryPoint.setIsWorkInProgress(true);
                    read();
                } else {
                    String wholeErrorMessage = "";
                    LoggingService.writeLog(COMMON_ERRORS_LIST, "error");
                    for (String string : errors) {
                        LoggingService.writeLog("\t" + string, "error");
                        enteryPoint.getMainController().showStatusInfo(string);
                    }
                    enteryPoint.makeErrorAlarm(wholeErrorMessage);
                }
            } else {
                LoggingService.writeLog(FRLLO_CONNECTION_ERROR, "error");
                enteryPoint.makeErrorAlarm(FRLLO_CONNECTION_ERROR);
            }
            converter.close();
            converter = null;
            enteryPoint.setIsWorkInProgress(false);
        } catch (Exception ex) {
            enteryPoint.setIsWorkInProgress(false);
            enteryPoint.makeErrorAlarm("Ошибка при чтении");
            LoggingService.writeLog(ex);
        }
    }

    public void stop() throws Exception {
        this.isCanceled = isCanceled;
        LoggingService.writeLog("reader - stop", "debug");
        if (converter != null) {
            LoggingService.writeLog("close converter - flush buffers", "debug");
            converter.close();
        }
        closeSource();
    }

    public void setDates(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
        isByDates = true;
    }
}
