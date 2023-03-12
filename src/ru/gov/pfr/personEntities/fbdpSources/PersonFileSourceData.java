package ru.gov.pfr.personEntities.fbdpSources;

import ru.gov.pfr.service.LoggingService;

public class PersonFileSourceData extends PersonFBDPSources {

    private final static String SEPARATOR = ";";
    private final static int stringParts = 37;
    private static String[] keysQueueHeadered = new String[]{"NPERS", "ID", "FA", "IM", "OT", "RDAT", "POL",
        "REG_IND", "REG_RE", "REG_RA", "REG_GOROD", "REG_PUNKT", "REG_UL", "REG_DOM", "REG_KOR", "REG_KVA",
        "FAKT_RE", "FAKT_RA", "FAKT_GOROD", "FAKT_PUNKT", "FAKT_UL", "FAKT_DOM", "FAKT_KOR", "FAKT_KVA",
        "KDOK", "PASS", "PASN", "PAS_DAT", "PAS_KEM", "GRAJDAN", "VREG", "L1", "L2", "NSU1", "SROKS",
        "SROKPO", "RE", "RA", "CHANGEDATE"};

    public PersonFileSourceData(String string) {
        modifyWithDictionaryes = true;
        String replaceTemplate = "&QUOT;";
        string = string.replaceAll(replaceTemplate, "");
        String[] splited = string.split(SEPARATOR);
        while (splited.length < stringParts - 1) {
            string += "\"\";";
            splited = string.split(SEPARATOR);
        }
        if (splited.length < stringParts - 2) {
            LoggingService.writeLog("wrong length error: " + string, "error");
        } else {
            fillMap(splited);
        }
        parseDictionary();
        validate();
    }

    private void fillMap(String[] splited) {
        if (splited.length == keysQueueHeadered.length) {
            for (int i = 0; i < splited.length; i++) {
                String string = splited[i];
                if (string.startsWith("\"")) {
                    string = string.substring(1);
                }
                if (string.endsWith("\"")) {
                    string = string.substring(0, string.length() - 1);
                }
                if (i < keysQueueHeadered.length) {
                    map.put(keysQueueHeadered[i], string.trim());
                } else {
                    LoggingService.writeLog("cant put value: " + string.trim() + " in field # " + i, "error");
                }
            }
        } else {
            for (int i = 0; i < splited.length; i++) {
                String string = splited[i];
                if (string.startsWith("\"")) {
                    string = string.substring(1);
                }
                if (string.endsWith("\"")) {
                    string = string.substring(0, string.length() - 1);
                }
                if (i < keysQueue.length) {
                    map.put(keysQueue[i], string.trim());

                } else {
                    LoggingService.writeLog("cant put value: " + string.trim() + " in field # " + i, "error");
                }
            }
        }
    }

    @Override
    public boolean isDBWriteble() {
        return true;
    }
    
    
}
