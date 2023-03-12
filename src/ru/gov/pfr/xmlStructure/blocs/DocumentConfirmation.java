package ru.gov.pfr.xmlStructure.blocs;

import java.util.HashMap;
import ru.gov.pfr.service.LoggingService;

public class DocumentConfirmation extends FBDPIdentify {

    public DocumentConfirmation(String docType, String serial, String number, String dateIssue, String additionalDateIssue, boolean isRussian) {
        super(docType, serial, number, dateIssue, additionalDateIssue, isRussian);
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void calculateFields(String docType, String serial, String number, String dateIssue, String additionalDateIssue) {
        map.put("doc_type", docType);
        map.put("serial", serial);
        map.put("num", number);
        map.put("date_issue", dateIssue);
//        map.put("doc_type_name",DocumentNamesDictionary.getValue(docType));   //инные документы - имя
    }

    public void setDocument(CitizenshipIdentify document) {
        map.put("doc_type_name", document.getDoctypeOriginal());   //инные документы - имя
        keysQueue = new String[]{"doc_type", "doc_type_name", "serial", "num", "date_issue", "authority"};
    }

    @Override
    protected void init() {
        wrapperTeg = "doc";
        keysQueue = new String[]{"doc_type", "doc_type_name", "serial", "num", "date_issue", "authority"};
        map = new HashMap<>();
    }

    public void printDocType() {
        LoggingService.writeLog("doctype: " + map.get("doc_type_name"), "debug");
    }

}
