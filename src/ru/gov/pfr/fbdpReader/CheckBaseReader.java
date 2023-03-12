package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import java.io.File;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;

public class CheckBaseReader extends FRLLOSourceReader implements Runnable {

    public CheckBaseReader(EnteryPoint ePoint) throws Exception {
        super(ePoint);
    }

    @Override
    protected void read() throws Exception {
        LoggingService.writeLog("read() by CheckBaseReader", "debug");
        enteryPoint.getMainController().showStatusInfo("Запуск запроса контроля таблицы льготников");
        File queryFile = new File(SettingsService.getInstance().getSQLPath() + "\\" + "check_changes_open.sql");
        ConnectionService.getInstance().makeRequestPart(converter, ConnectionService.getInstance().queryFromFile(queryFile));
        converter.clearTempResults();
        ConnectionService.getInstance().reconnectFBDP();
    }

    public void closeWriter() throws Exception {
        converter.closeWriter();
    }

}
