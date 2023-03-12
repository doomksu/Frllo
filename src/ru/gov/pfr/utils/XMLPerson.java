package ru.gov.pfr.utils;

import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

public class XMLPerson {

    private String controllString;
    private String snils;
    private String nvpId;
    private String convertGUID;

    private int loadResult;
    private HashMap<String, String> map = new HashMap<>();
    private boolean isMonetization = false;

    public XMLPerson() {

    }

    public XMLPerson(boolean monetization) {
        isMonetization = monetization;
    }

    public void parseString(String string) {
        if (string.contains("<document_id>")) {
            convertGUID = XMLValues.getValue(string);
        }
        if (string.contains("<ext_citizen_id>")) {
            nvpId = XMLValues.getValue(string);
        }
        if (string.contains("<snils>")) {
            snils = formatSnils(XMLValues.getValue(string));
        }
        if (!XMLValues.isSingleTag(string)) {
            map.put(XMLValues.getTagName(string), XMLValues.getValue(string));
        }
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
            LoggingService.writeLog("wrongSnils: " + value, "debug");
        }
        return value;
    }

    public void setResults(int isPersonLoaded) {
        loadResult = isPersonLoaded;
    }

    public String getControlParamsString(String fileID) {
        String queryParams = getQueryParamsString(controllString, snils, nvpId, convertGUID, fileID, loadResult);
        return queryParams;
    }

    public String isLoadedQueryParams() {
        String query = " npers = '" + snils + "' "
                + " and id = '" + controllString + "' ";
        return query;
    }

    public static String getQueryParamsString(String controlString, String snils, String nvpID, String guid, String fileID, int isLoaded) {
        String vals = "("
                + "'" + valueOrEmptyString(controlString) + "',"
                + "'" + valueOrEmptyString(snils) + "',"
                + "'" + valueOrEmptyString(nvpID) + "',"
                + "'" + valueOrEmptyString(guid) + "',"
                + fileID + ","
                + isLoaded
                + ")";
        return vals;
    }

    public String getFullValues(String fileID) {
        String vals = "("
                + "'" + valueOrEmptyString(this.controllString) + "',"
                + "'" + valueOrEmptyString(snils) + "',"
                + "'" + valueOrEmptyString(nvpId) + "',"
                + "'" + valueOrEmptyString(convertGUID) + "',"
                + fileID + ","
                + "0" + ","
                + "'" + valueOrEmptyString(map.get("surname")) + "',"
                + "'" + valueOrEmptyString(map.get("name")) + "',"
                + "'" + valueOrEmptyString(map.get("patronymic")) + "',"
                + "'" + valueOrEmptyString(map.get("birthdate")) + "',"
                + "'" + valueOrEmptyString(map.get("sex")) + "',"
                + "'" + valueOrEmptyString(map.get("citizenship")) + "',"
                + "'" + valueOrEmptyString(map.get("doc_type")) + "',"
                + "'" + valueOrEmptyString(map.get("serial")) + "',"
                + "'" + valueOrEmptyString(map.get("num")) + "',"
                + "'" + valueOrEmptyString(map.get("date_issue")) + "',"
                + "'" + valueOrEmptyString(map.get("region")) + "',"
                + "'" + valueOrEmptyString(map.get("ext_benefit_code")) + "',";
        if (isMonetization) {
            vals += "'" + valueOrEmptyString(map.get("start_date")) + "',"
                    + "'" + valueOrEmptyString(map.get("end_date")) + "',"
                    + "0," //nsu
                    + fileID + ","//mfileid
                    + "0" //ismloaded
                    ;
        } else {
            vals += "'" + valueOrEmptyString(map.get("receive_date")) + "',"
                    + "'" + valueOrEmptyString(map.get("cancel_date")) + "',"
                    + "1," //nsu
                    + fileID + ","//mfileid
                    + "0" //ismloaded
                    ;
        }

        vals += ")";
        return vals;
    }

    private static String valueOrEmptyString(String val) {
        if (val != null) {
            return val;
        }
        return "";
    }

    public String getUpdateString(String fileID) {
        String vals = " set NPERS = '" + valueOrEmptyString(snils) + "', "
                + " ID_NVP = '" + valueOrEmptyString(nvpId) + "', "
                + " WHERE FILE_GUID = '" + this.convertGUID + "'"
                + " AND LOAD_FILE_GUID_ID = " + fileID;
        return vals;
    }

}
