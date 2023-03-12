package ru.gov.pfr.xmlStructure.blocs;

public class FRLLOIdentify extends CitizenshipIdentify {

    public FRLLOIdentify(String docType, String serial, String number, String dateIssue, String additionalDateIssue, boolean isRussian) {
        super(docType, serial, number, dateIssue, additionalDateIssue, isRussian);
    }

    @Override
    protected void calculateFields(String docType, String serial, String number, String dateIssue, String additionalDateIssue) {
        map.put("doc_type", docType);
        map.put("serial", serial);
        map.put("num", number);
        map.put("date_issue", dateIssue);
    }

    @Override
    public boolean isValid() {
        if (map.get("serial") == null
                || map.get("doc_type") == null
                || map.get("num") == null
                || map.get("date_issue") == null) {
            return false;
        }
        return true;
    }

}
