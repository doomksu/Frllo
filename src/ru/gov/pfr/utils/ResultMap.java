package ru.gov.pfr.utils;

import java.util.HashMap;
import java.util.HashSet;
import ru.gov.pfr.service.LoggingService;

public class ResultMap {

    //description/HashSet<GUID's>
    private HashMap<String, HashSet<String>> notLoadedPersonsMap;
    private HashSet<String> loadedPersonsMap;
    private HashSet<String> doublePersonsMap;

    public ResultMap() {
        notLoadedPersonsMap = new HashMap<>();
        loadedPersonsMap = new HashSet<>();
        doublePersonsMap = new HashSet<>();
    }

    public void addLoadedPerson(String id) {
        loadedPersonsMap.add(id);
    }

    public void addDoubleLoadedPerson(String id) {
        doublePersonsMap.add(id);
    }

    public void addNotLoadedPerson(String descr, String id) {
        if (notLoadedPersonsMap.containsKey(descr) == false) {
            notLoadedPersonsMap.put(descr, new HashSet<>());
        }
        notLoadedPersonsMap.get(descr).add(id);
    }

    public void printResults() {
        for (String key : notLoadedPersonsMap.keySet()) {
            LoggingService.writeLog(">>\t" + key + "; человек: " + notLoadedPersonsMap.get(key).size(), "debug");
        }
        LoggingService.writeLog(">>\tЗагружено; человек: " + loadedPersonsMap.size(), "debug");
    }

    public void combine(ResultMap rm) {
        for (String outKey : rm.notLoadedPersonsMap.keySet()) {
            for (String idVal : rm.notLoadedPersonsMap.get(outKey)) {
                addNotLoadedPerson(outKey, idVal);
            }
        }
        this.loadedPersonsMap.addAll(rm.loadedPersonsMap);
    }

    public String containPersonInCode(String value) {
        for (String key : notLoadedPersonsMap.keySet()) {
            if (notLoadedPersonsMap.get(key).contains(value)) {
                return key;
            }
        }
        return null;
    }

    public int size() {
        int count = 0;
        for (HashSet<String> subMap : this.notLoadedPersonsMap.values()) {
            count += subMap.size();
        }
        return count;
    }

    /**
     * Получить данные о загрузке льшотника по его GUID
     *
     * @param convertGUID
     * @return 0 - not found \-1 loadWithError \ 1 loadOK \ 2 load double
     */
    public int isPersonLoaded(String convertGUID) {
        if (loadedPersonsMap.contains(convertGUID)) {
            return 1;
        }
        if (doublePersonsMap.contains(convertGUID)) {
            return 2;
        }
        for (String key : notLoadedPersonsMap.keySet()) {
            if (notLoadedPersonsMap.get(key).contains(convertGUID)) {
                return -1;
            }
        }
        return 0;
    }

    public void removePerson(String convertGUID) {
        loadedPersonsMap.remove(convertGUID);
        doublePersonsMap.remove(convertGUID);
        notLoadedPersonsMap.remove(convertGUID);
    }

}
