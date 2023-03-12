package ru.gov.pfr.utils;

import ru.gov.pfr.service.LoggingService;

public class XMLResult {

    private String guid;
    private String reg_num;
    private String status;
    private String code;
    private String description;


    public void parseString(String string) {
        if (string.contains("<document_id>")) {
            guid = XMLValues.getValue(string);
        }
        if (string.contains("<reg_num>")) {
            reg_num = XMLValues.getValue(string);
        }
        if (string.contains("<status>")) {
            status = XMLValues.getValue(string);
        }
        if (string.contains("<code>")) {
            code = XMLValues.getValue(string);
        }
        if (string.contains("<description>")) {
            description = XMLValues.getValue(string);
        }
    }

    public void print() {
        LoggingService.writeLog(">>print result: " + guid + " reg_num: " + reg_num, "debug");
    }

    public String getGuid() {
        return guid;
    }

    public String getReg_num() {
        return reg_num;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
