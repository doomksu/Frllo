package ru.gov.pfr.utils;

import ru.gov.pfr.service.LoggingService;

//<element>
//    <document_id>1ab821a9-9b32-4092-b8f1-b40667afeb58</document_id>
//    <status>400</status>
//    <errors>
//      <element>
//        <code>BenefitDateError</code>
//        <description>В передаваемых льготах дата назначения льготы больше даты отмены</description>
//      </element>
//    </errors>
//  </element>
public class ResultBlock {

    private String id;
    private String status;
    private String code;
    private String description;
    private boolean isErrorCode = false;

    public ResultBlock() {

    }

    public ResultBlock(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public void setDocumentID(String value) {
        id = value;
    }

    public void setStatus(String status) {
        this.status = status;
        if (status.equals("400")) {
            isErrorCode = true;
        }
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isErrorStatus() {
        return isErrorCode;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void print() {
        LoggingService.writeLog(">>ResulrBlock: id: " + id
                + " code:" + code
                + " description: " + description, "debug");
    }

    public boolean isDoubleStatus() {
        if (status.equals("208")) {
            return true;
        }
        return false;
    }

    public boolean isOkStatus() {
        if (status.equals("200")) {
            return true;
        }
        return false;
    }

}
