package ru.gov.pfr.service;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.gov.pfr.service.queryRecivers.QueryReciver;

public class PrintQueryRequest implements QueryReciver {

    @Override
    public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
        if (SettingsService.getInstance().checkDummyFile("LOG_QUERY_MAP")) {
            String queryMapString = "\r\n queryMap: ";
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                queryMapString += "\r\n" + entry.getKey() + "\t\t: " + entry.getValue();
            }
            LoggingService.writeLog(queryMapString, "debug");
        }
    }

    @Override
    public void close() throws Exception {
    }

    public void makeRequest(String request) {
        try {
            ConnectionService.getInstance().fetchData(request, this, null);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

}
