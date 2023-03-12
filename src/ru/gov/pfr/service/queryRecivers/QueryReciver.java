package ru.gov.pfr.service.queryRecivers;

import java.util.LinkedHashMap;

public interface QueryReciver {

    public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception;

    public void close() throws Exception;
}
