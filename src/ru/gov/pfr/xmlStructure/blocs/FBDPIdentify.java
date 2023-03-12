package ru.gov.pfr.xmlStructure.blocs;

import ru.gov.pfr.FNCIDictionaryes.DocumentNamesDictionary;

public class FBDPIdentify extends CitizenshipIdentify {

    public FBDPIdentify(String docType, String serial, String number, String dateIssue, String additionalDateIssue, boolean isRussian) {
        super(docType, serial, number, dateIssue, additionalDateIssue, isRussian);
        this.additionalDate = additionalDateIssue;
    }

    private String chooseDocumentIssueDate(String docDate, String lgtDate) {
        if (docDate == null || docDate.isEmpty()) {
            return lgtDate;
        }
        return docDate;
    }

    private String clearDocumentIDs(String rawNum) {
        return rawNum.replaceAll("\\s", "").replaceAll(" ", "");
    }

    @Override
    protected void calculateFields(String docType, String serial, String number, String dateIssue, String additionalDateIssue) {
        map.put("KDOK", docType);
        map.put("doc_type", DocumentNamesDictionary.getInstance().getValue(docType));
        map.put("serial", clearDocumentIDs(serial));
        map.put("num", clearDocumentIDs(number));
        map.put("date_issue", chooseDocumentIssueDate(dateIssue, additionalDateIssue));
    }

    public String getDocType() {
        return map.get("doc_type");
    }

    public String getSerial() {
        return map.get("serial");
    }

    public String getNumber() {
        return map.get("num");
    }

    public String getIssue() {
        return map.get("date_issue");
    }
}
