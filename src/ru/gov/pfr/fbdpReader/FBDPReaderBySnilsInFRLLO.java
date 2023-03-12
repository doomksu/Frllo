package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import ru.gov.pfr.service.ConnectionService;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_SCHEMA_NAME;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_SCHEMA_TEG;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.queryRecivers.QueryReciver;

public class FBDPReaderBySnilsInFRLLO extends FBDPReaderBySnilsList implements QueryReciver {
    
    private int totalPersonCount = 0;
    
    private int countInCurrentString = 0;
    private int countInCurrentLists = 0;
    private StringBuilder comb = new StringBuilder();
    boolean firstNPERSinLine = true;
    private static final int MAX_LISTS_STRING = 5;
    private int workQueryCount;
    private boolean once = true;
    private int lastReaded = 0;
    private int newReaded = 0;
    
    public FBDPReaderBySnilsInFRLLO(EnteryPoint ep) throws Exception {
        super(ep);
        CHUNK_SIZE = 500;
        workQueryCount = 0;
    }
    
    public void runRead() throws Exception {
        read();
    }
    
    @Override
    protected void read() throws Exception {
        totalPersonCount = 0;
        enteryPoint.getMainController().showStatusInfo("Запуск проверки по БД ФРЛЛО");
        String sqlName = "select_distinct_frllo_npers.sql";
        File queryFile = new File(SettingsService.getInstance().getSQLPath() + "\\" + sqlName);
        String query = ConnectionService.getInstance().queryFromFile(queryFile);
        query = query.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        
        String mes = LoggingService.getDateTimeString() + " Запуск запроса: " + sqlName;
        converter.getController().showStatusInfo(mes);
        ConnectionService.getInstance().fetchData(query, this, null);
        controller.showStatusInfo(LoggingService.getDateTimeString() + " Завершена проверка БД по спискам СНИЛС");
        isDone = true;
    }
    
    @Override
    public synchronized void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
        if (resultMap.containsKey("NPERS")) {
            if (!resultMap.get("NPERS").isEmpty() && resultMap.get("NPERS") != null) {
                if (!resultMap.get("NPERS").equalsIgnoreCase("null")) {
                    comb.append((!firstNPERSinLine ? ", " : "") + "'" + resultMap.get("NPERS").trim() + "'");
                    firstNPERSinLine = false;
                    totalPersonCount++;
                    countInCurrentString++;
                    countInCurrentLists++;
                }
            }
            if (countInCurrentString == CHUNK_SIZE && comb != null) {
                lists.add(comb.toString());
                countInCurrentString = 0;
                comb = new StringBuilder();
                firstNPERSinLine = true;
            }
            if (lists.size() == MAX_LISTS_STRING) {
                try {
                    callToFetchPersonData();
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                    enteryPoint.getMainController().showErrorMessage(ex.getMessage());
                }
            }
        }
    }
    
    private void callToFetchPersonData() throws Exception {
        converter.getController().showStatusInfo(LoggingService.getDateTimeString() + " Прочитано: " + totalPersonCount);
        workQueryCount++;
        if (lists != null && !lists.isEmpty()) {
            runPersonsListThroghQueryes();
        }
        converter.getController().showStatusInfo(LoggingService.getDateTimeString() + " Выполнен запрос: " + workQueryCount);
        
        countInCurrentLists = 0;
        comb = new StringBuilder();
        countInCurrentString = 0;
    }
    
    @Override
    public void close() throws Exception {  //дорабатываем остаток СНИЛС
        if (!lists.isEmpty() || comb.length() != 0) {
            lists.add(comb.toString());
            callToFetchPersonData();
        }
        
        converter.getController().showStatusInfo(LoggingService.getDateTimeString() + " Выполнено запросов всего: " + workQueryCount);
        converter.closeWriter();
        ConnectionService.getInstance().reconnectFBDP();
    }
    
}
