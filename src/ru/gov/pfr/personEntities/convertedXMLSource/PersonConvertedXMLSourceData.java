package ru.gov.pfr.personEntities.convertedXMLSource;

import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.utils.XMLValues;

public class PersonConvertedXMLSourceData extends PersonDataSource {

    private int loadResult;
    private String controllString;
    private String snils;
    private String nvpId;
    private String convertGUID;

    public PersonConvertedXMLSourceData(PersonDataSource fbdpPerson) {
        String[] parts = fbdpPerson.makeXMLStructure().makeLgotaXML().split("\r\n");
        for (String string : parts) {
            if (string.contains("<document_id>")) {
                convertGUID = XMLValues.getValue(string);
            }
            if (string.contains("<ext_citizen_id>")) {
                nvpId = XMLValues.getValue(string);
            }
            if (string.contains("<snils>")) {
                snils = formatSnils(XMLValues.getValue(string));
                map.put("NPERS", snils);
            }
            if (string != null && !string.isEmpty()) {
                if (!XMLValues.isSingleTag(string)) {
                    map.put(XMLValues.getTagName(string), XMLValues.getValue(string));
                }
            }
        }
    }

    private String formatSnils(String value) {
        if (value.length() == 11) {
            String res = value.substring(0, 3)
                    + "-"
                    + value.substring(3, 6)
                    + "-"
                    + value.substring(6, 9)
                    + " "
                    + value.substring(9);
            return res;
        } else {
//            LoggingService.writeLog("wrongSnils: " + value, "debug");
        }
        return value;
    }

    public void constructControllString() {
        String hash = map.get("surname") + map.get("name") + map.get("patronymic") + map.get("sex")
                + map.get("citizenship")
                + map.get("snils")
                + map.get("doc_type") + map.get("serial") + map.get("num") + map.get("date_issue")
                + map.get("region")
                + map.get("ext_benefit_code") + map.get("receive_date") + map.get("cancel_date");
        controllString = snils + hash.hashCode();
    }

    public void setResults(int isPersonLoaded) {
        loadResult = isPersonLoaded;
    }

    public String getControllString() {
        return controllString;
    }

    public String getSnils() {
        return snils;
    }

    public String getNvpId() {
        return nvpId;
    }

    public String getConvertGUID() {
        return convertGUID;
    }

    public int getLoadResult() {
        return loadResult;
    }

    @Override
    public boolean isNeedCitizenshipConfirmation() {
        return false;
    }

    @Override
    public boolean isDBWriteble() {
        return false;
    }

}
