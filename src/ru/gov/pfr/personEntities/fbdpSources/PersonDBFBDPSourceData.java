package ru.gov.pfr.personEntities.fbdpSources;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;

public class PersonDBFBDPSourceData extends PersonFBDPSources {

    public PersonDBFBDPSourceData(LinkedHashMap<String, String> outMap) {
        String replaceTemplate = "&QUOT;";
        for (String string : outMap.values()) {
            string = string.replaceAll(replaceTemplate, "");
        }
        for (String key : keysQueue) {
            if (outMap.containsKey(key)) {
                map.put(key, outMap.get(key).replaceAll("\\p{Cntrl}", " "));
            }
        }
        if (SettingsService.getInstance().checkDummyFile("LOG_QUERY_MAP")) {
            String queryMapString = "\r\n>> queryMap: ";
            for (Map.Entry<String, String> entry : outMap.entrySet()) {
                queryMapString += "key: " + entry.getKey() + "\t\t: " + entry.getValue() + "\r\n";
            }
            LoggingService.writeLogNoTypeNoTime(queryMapString);
        }
        parseDictionary();
        validate();
    }
}
