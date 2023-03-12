package ru.gov.pfr.fbdpReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.utils.DateUtils;

class ErrorWriter {

    private ArrayList<String> sqlBuffer;
    protected int BUFFER_SIZE = 100;
    private String convertionDate;

    public ErrorWriter() {
        sqlBuffer = new ArrayList<>();
        try {
            SettingsService.getInstance().readSettings();
            BUFFER_SIZE = Integer.parseInt(SettingsService.getInstance().getValue("BUFFER_SIZE"));
        } catch (Exception ex) {
            BUFFER_SIZE = 100;
        }
        SimpleDateFormat dtf = new SimpleDateFormat(DateUtils.datePattern);
        Date now = new Date();
        convertionDate = dtf.format(now);
    }

    public void addPerson(PersonDataSource person) {
        if (!person.isValid()) {
            sqlBuffer.add(createPersonInsertErrorQuery(person));
        }
        if (sqlBuffer.size() == BUFFER_SIZE) {
            flushBuffer();
        }
    }

    /**
     * Проверить остались ли записи в буффере и если остались дописать их и
     * очистить буффер
     */
    public void flushBuffer() {
        if (sqlBuffer.isEmpty() == false) {
            try {
                writeBuffer();
                sqlBuffer.clear();
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }
    }

    private void writeBuffer() throws Exception {
        ConnectionService.getInstance().executeInsertUpdateQuery(sqlBuffer);
    }

    private String createPersonInsertErrorQuery(PersonDataSource person) {
        String npers = person.getNPERS();
        String nvpID = person.getNVPID();
        String region = person.getRegion();
        String why = person.getValidationMeassage();
        if (why != null && why.length() > 400) {
            why = why.substring(0, 398);
        }
        return ConnectionService.getInstance().getInsertFBDPErrorQuery(npers, nvpID, region, convertionDate, why);
    }

}
